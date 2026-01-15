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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Selenium WebDriver 관리 클래스
 *
 * <주요 기능>
 * 1. Singleton 패턴으로 WebDriver 인스턴스 관리 (메모리 최적화)
 * 2. Double-Checked Locking으로 스레드 안전성 확보
 * 3. Docker/Linux 환경에서의 Headless Chrome 실행 최적화
 *
 * <트러블슈팅>
 * - 문제1: 동시 요청 시 서버 자원 고갈 → Singleton 패턴으로 해결
 * - 문제2: Docker 환경 샌드박스 오류 → --no-sandbox 옵션
 * - 문제3: Docker 공유 메모리 부족 → --disable-dev-shm-usage 옵션
 */
@Slf4j
@Component
public class TimetableWebDriverManager {

    /**
     * Singleton 패턴을 위한 WebDriver 인스턴스
     * - static으로 선언하여 전체 애플리케이션에서 1개만 생성
     * - 메모리 최적화: 여러 사용자가 동시에 요청해도 브라우저는 1개만 실행
     */
    private static WebDriver driver;

    /**
     * ReentrantLock: 스레드 안전성 확보를 위한 락
     * - Double-Checked Locking 패턴 구현에 사용
     * - synchronized 키워드보다 세밀한 제어 가능
     */
    private static final Lock lock = new ReentrantLock();

    /**
     * WebDriver 인스턴스를 반환 (Singleton 패턴 + Double-Checked Locking)
     *
     * <Double-Checked Locking 동작 원리>
     * 1. 첫 번째 체크 (lock 없이): 이미 생성되었으면 바로 반환 (빠른 경로)
     * 2. lock 획득: 여러 스레드가 동시 진입 방지
     * 3. 두 번째 체크 (lock 내부): 락 대기 중 다른 스레드가 생성했을 가능성 체크
     * 4. 최초 1회만 생성
     *
     * @return 공유되는 WebDriver 인스턴스
     * @throws WebDriverInitializationException WebDriver 초기화 실패 시
     */
    public WebDriver getDriver() {
        // 첫 번째 체크: 락 없이 빠르게 확인
        if (driver == null) {
            lock.lock();
            try {
                // 두 번째 체크: Double-Checked Locking
                if (driver == null) {
                    driver = initializeDriver();
                    log.info("WebDriver 초기화 완료 (Singleton)");
                }
            } finally {
                lock.unlock();
            }
        }
        return driver;
    }

    /**
     * WebDriver를 초기화합니다.
     *
     * @return 초기화된 ChromeDriver 인스턴스
     * @throws WebDriverInitializationException 초기화 실패 시
     */
    private WebDriver initializeDriver() {
        try {
            // Chrome Driver 자동 설정 (운영체제에 맞는 버전 자동 다운로드)
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

        // 임시 사용자 프로필 디렉토리 생성
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
     * WebDriver를 종료합니다.
     * 애플리케이션 종료 시 또는 테스트 후 호출
     */
    public void quitDriver() {
        if (driver != null) {
            lock.lock();
            try {
                if (driver != null) {
                    driver.quit();
                    driver = null;
                    log.info("WebDriver 종료 완료");
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
