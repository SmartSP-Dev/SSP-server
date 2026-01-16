package group4.opensource_server.calendar.service;

import group4.opensource_server.calendar.domain.TimetableConstants;
import group4.opensource_server.calendar.exception.WebDriverInitializationException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Selenium WebDriver 관리 클래스 (Semaphore + ThreadLocal 패턴)
 *
 * <주요 개선사항>
 * 1. Semaphore를 통한 동시 실행 개수 제한 (최대 3개)
 * 2. ThreadLocal을 사용하여 각 스레드가 독립적인 WebDriver 인스턴스 보유
 * 3. Race Condition 완전 해결 (각 스레드가 자기만의 브라우저 사용)
 *
 * <동작 원리>
 * 1. acquire(): Semaphore에서 permit 획득 + 새 WebDriver 생성
 * 2. 크롤링 작업 수행 (독립적인 WebDriver 사용)
 * 3. release(): WebDriver 종료 + Semaphore permit 반환
 *
 * <메모리 관리>
 * - 최대 3개의 WebDriver만 동시 실행 (300MB × 3 = 900MB)
 * - 4번째 요청부터는 대기 (최대 30초)
 * - 작업 완료 시 즉시 정리하여 메모리 반환
 *
 * <트러블슈팅 해결>
 * ✅ 문제1: 동시 요청 시 서버 자원 고갈 → Semaphore로 개수 제한
 * ✅ 문제2: Race Condition (URL 충돌) → ThreadLocal로 격리
 * ✅ 문제3: Docker 환경 오류 → --no-sandbox, --disable-dev-shm-usage
 */
@Slf4j
@Component
public class TimetableWebDriverManager {

    /**
     * 동시 실행 개수를 제어하는 Semaphore
     * - permits: 3개 (최대 3명이 동시에 크롤링 가능)
     * - 4번째 사람은 대기 줄에 섬
     */
    private final Semaphore semaphore = new Semaphore(TimetableConstants.MAX_CONCURRENT_CRAWLING);

    /**
     * 각 스레드가 독립적인 WebDriver 인스턴스를 보유
     * - Thread-1: 자기만의 브라우저 인스턴스
     * - Thread-2: 자기만의 브라우저 인스턴스
     * - Thread-3: 자기만의 브라우저 인스턴스
     * → 서로 간섭 없이 독립적으로 크롤링
     */
    private final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * WebDriver를 획득합니다 (Semaphore permit 획득 + WebDriver 생성).
     *
     * <사용 예시>
     * ```java
     * webDriverManager.acquire();
     * try {
     *     WebDriver driver = webDriverManager.getDriver();
     *     driver.get("https://example.com");
     *     // 크롤링 작업
     * } finally {
     *     webDriverManager.release(); // 반드시 호출!
     * }
     * ```
     *
     * @throws InterruptedException Semaphore 대기 중 인터럽트 발생 시
     * @throws WebDriverInitializationException 30초 내에 permit 획득 실패 시
     */
    public void acquire() throws InterruptedException {
        log.debug("WebDriver 획득 시도 (대기 중인 permits: {})", semaphore.availablePermits());

        // Semaphore에서 permit 획득 시도 (최대 30초 대기)
        boolean acquired = semaphore.tryAcquire(
                TimetableConstants.ACQUIRE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        if (!acquired) {
            log.error("WebDriver 획득 실패: {}초 타임아웃", TimetableConstants.ACQUIRE_TIMEOUT_SECONDS);
            throw new WebDriverInitializationException(
                    "크롤링 작업이 너무 많아 대기 시간 초과되었습니다. 잠시 후 다시 시도해주세요."
            );
        }

        try {
            // ThreadLocal에 새로운 WebDriver 인스턴스 생성 및 저장
            WebDriver driver = initializeDriver();
            driverThreadLocal.set(driver);
            log.info("WebDriver 획득 완료 (현재 활성: {}개)",
                    TimetableConstants.MAX_CONCURRENT_CRAWLING - semaphore.availablePermits());

        } catch (Exception e) {
            // WebDriver 생성 실패 시 permit 반환
            semaphore.release();
            log.error("WebDriver 초기화 실패, permit 반환", e);
            throw e;
        }
    }

    /**
     * 현재 스레드의 WebDriver를 반환합니다.
     *
     * @return 현재 스레드에 할당된 WebDriver 인스턴스
     * @throws IllegalStateException acquire()를 먼저 호출하지 않은 경우
     */
    public WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("acquire()를 먼저 호출해야 합니다");
        }
        return driver;
    }

    /**
     * WebDriver를 해제합니다 (WebDriver 종료 + Semaphore permit 반환).
     *
     * <주의사항>
     * - 반드시 finally 블록에서 호출하여 자원 누수 방지
     * - release() 누락 시 다른 사용자들이 영원히 대기하게 됨
     */
    public void release() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.debug("WebDriver 종료 완료");
            } catch (Exception e) {
                log.warn("WebDriver 종료 중 예외 발생 (무시)", e);
            } finally {
                driverThreadLocal.remove();
                semaphore.release();
                log.info("WebDriver 반환 완료 (사용 가능: {}개)", semaphore.availablePermits());
            }
        }
    }

    /**
     * WebDriver를 초기화합니다.
     *
     * @return 초기화된 ChromeDriver 인스턴스
     * @throws WebDriverInitializationException 초기화 실패 시
     */
    private WebDriver initializeDriver() {
        try {
            // Chrome Driver 자동 설정
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();

            ChromeOptions options = createChromeOptions();
            return new ChromeDriver(options);

        } catch (Exception e) {
            log.error("WebDriver 초기화 실패", e);
            throw new WebDriverInitializationException("WebDriver 초기화 실패", e);
        }
    }

    /**
     * Chrome 옵션을 생성합니다 (Docker 환경 최적화).
     *
     * @return 설정된 ChromeOptions
     * @throws WebDriverInitializationException 임시 프로필 생성 실패 시
     */
    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Docker/Linux 환경 최적화 옵션
        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--window-size=" + TimetableConstants.WINDOW_SIZE,
                "--no-sandbox",                   // Docker 샌드박스 오류 해결
                "--disable-dev-shm-usage",        // 공유 메모리 부족 해결
                "user-agent=" + TimetableConstants.USER_AGENT,
                "--remote-allow-origins=*"
        );

        // 임시 사용자 프로필 디렉토리 생성 (테스트 격리)
        try {
            Path tmpProfile = Files.createTempDirectory("chrome-user-data-");
            options.addArguments("--user-data-dir=" + tmpProfile.toAbsolutePath());
            log.debug("Chrome 임시 프로필 생성: {}", tmpProfile);
        } catch (IOException e) {
            log.error("Chrome 프로필 임시 폴더 생성 실패", e);
            throw new WebDriverInitializationException("Chrome 프로필 임시 폴더 생성 실패", e);
        }

        return options;
    }

    /**
     * 현재 활성화된 WebDriver 개수를 반환합니다 (모니터링용).
     *
     * @return 사용 중인 WebDriver 개수
     */
    public int getActiveDriverCount() {
        return TimetableConstants.MAX_CONCURRENT_CRAWLING - semaphore.availablePermits();
    }
}
