package group4.opensource_server.calendar.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group4.opensource_server.calendar.dto.CalendarTimetableDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.calendar.domain.EveryTimeTimetableRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.regex.*;

/**
 * 에브리타임 시간표 크롤링 서비스
 *
 * <주요 기능>
 * 1. Selenium WebDriver를 이용한 에브리타임 시간표 크롤링
 * 2. Singleton 패턴으로 WebDriver 인스턴스 관리 (메모리 최적화)
 * 3. Docker/Linux 환경에서의 Headless Chrome 실행
 * 4. CSS 스타일 파싱을 통한 정확한 시간 정보 추출
 *
 * <트러블슈팅 핵심>
 * - 문제1: 동시 요청 시 서버 자원 고갈 → Singleton 패턴으로 해결
 * - 문제2: Docker 환경 샌드박스 오류 → --no-sandbox 옵션으로 해결
 * - 문제3: Docker 공유 메모리 부족 → --disable-dev-shm-usage 옵션으로 해결
 */
@Service
public class CalendarTimetableService {

    /**
     * Singleton 패턴을 위한 WebDriver 인스턴스
     * - static으로 선언하여 전체 애플리케이션에서 1개만 생성
     * - 메모리 최적화: 여러 사용자가 동시에 요청해도 브라우저는 1개만 실행
     * - 예: 10명 동시 요청 시 기존(10개 브라우저, 3GB) → 개선(1개 브라우저, 300MB)
     */
    private static WebDriver driver;

    /**
     * ReentrantLock: 스레드 안전성 확보를 위한 락
     * - Double-Checked Locking 패턴 구현에 사용
     * - synchronized 키워드 대신 명시적인 락 사용으로 더 세밀한 제어 가능
     */
    private static final Lock lock = new ReentrantLock();

    /**
     * 시간표 데이터 저장/조회를 위한 JPA Repository
     */
    private final EveryTimeTimetableRepository timetableRepository;

    /**
     * JSON 직렬화/역직렬화를 위한 Jackson ObjectMapper
     */
    private final ObjectMapper objectMapper;

    /**
     * 생성자: Spring DI를 통한 의존성 주입
     *
     * @param timetableRepository 시간표 데이터베이스 접근 객체
     * @param objectMapper JSON 변환 라이브러리
     */
    public CalendarTimetableService(EveryTimeTimetableRepository timetableRepository, ObjectMapper objectMapper) {
        this.timetableRepository = timetableRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * WebDriver 인스턴스를 반환 (Singleton 패턴 + Double-Checked Locking)
     *
     * <트러블슈팅 핵심 메소드>
     *
     * [문제 상황 1] 동시 요청 시 서버 자원 고갈
     * - 문제: 매 요청마다 새로운 브라우저 생성 → 메모리 부족으로 서버 다운
     * - 해결: Singleton 패턴으로 1개만 생성하여 재사용
     *
     * [문제 상황 2] 멀티스레드 환경에서 중복 생성
     * - 문제: 여러 스레드가 동시에 생성 시도 시 driver가 여러 개 생성될 수 있음
     * - 해결: Double-Checked Locking으로 스레드 안전성 확보
     *
     * [문제 상황 3] Docker/Linux 환경에서 브라우저 실행 실패
     * - 문제1: "Failed to move to new namespace" (샌드박스 권한 오류)
     * - 해결1: --no-sandbox 옵션 적용
     * - 문제2: "session deleted because of page crash" (공유 메모리 부족)
     * - 해결2: --disable-dev-shm-usage 옵션 적용
     *
     * <Double-Checked Locking 동작 원리>
     * 1. 첫 번째 체크 (lock 없이): 이미 생성되었으면 바로 반환 (빠른 경로)
     * 2. lock 획득: 여러 스레드가 동시 진입 방지
     * 3. 두 번째 체크 (lock 내부): 락 대기 중 다른 스레드가 생성했을 가능성 체크
     * 4. 최초 1회만 생성
     *
     * @return 공유되는 WebDriver 인스턴스
     */
    public static WebDriver getWebDriver() {
        // ===== 첫 번째 체크: 락 없이 빠르게 확인 =====
        // 이미 생성되었으면 바로 반환 (성능 최적화)
        if (driver == null) {

            // ===== 락 획득: 임계 영역(Critical Section) 진입 =====
            // 여러 스레드가 동시에 진입하는 것을 방지
            lock.lock();
            try {
                // ===== 두 번째 체크: Double-Checked Locking =====
                // 첫 번째 체크와 락 획득 사이에 다른 스레드가 이미 생성했을 수 있음
                if (driver == null) {

                    // Chrome Driver 자동 설정 (운영체제에 맞는 버전 자동 다운로드)
                    WebDriverManager.chromedriver().setup();

                    // ===== Chrome 옵션 설정 (Docker 환경 최적화) =====
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments(
                            "--headless=new",
                            "--disable-gpu",
                            "--window-size=1920x1080",
                            "--no-sandbox",
                            "--disable-dev-shm-usage",
                            "user-agent=Mozilla/5.0"
                    );

                    try {
                        Path tmpProfile = Files.createTempDirectory("chrome-user-data-");
                        options.addArguments("--user-data-dir=" + tmpProfile.toAbsolutePath());
                    } catch (IOException e) {
                        throw new RuntimeException("크롬 프로필 임시 폴더 생성 실패", e);
                    }
                    options.addArguments("--remote-allow-origins=* ");

                    // ===== WebDriver 생성 (최초 1회만 실행) =====
                    driver = new ChromeDriver(options);
                }
            } finally {
                // ===== 반드시 락 해제 (finally 블록으로 보장) =====
                // try 블록에서 예외 발생해도 반드시 실행됨
                lock.unlock();
            }
        }
        // 이미 생성된 driver 인스턴스 반환 (싱글톤)
        return driver;
    }

    /**
     * 에브리타임 시간표 크롤링 메인 로직
     *
     * <크롤링 프로세스>
     * 1단계: WebDriver로 URL 접속
     * 2단계: DOM 로딩 대기 (동적 페이지 대응)
     * 3단계: 요일 정보 추출 (tablehead에서)
     * 4단계: 강의 블록 파싱 (CSS style에서 시간 계산)
     * 5단계: JSON 형식으로 변환
     *
     * <시간 변환 알고리즘>
     * - 에브리타임은 강의 시간을 CSS의 top, height 속성으로 표현
     * - 변환 공식: startMinutes = ((top - 450) / 25) * 30
     * - 기준: top=450px가 09:00, 25px = 30분 단위
     * - 예시: top=550px → ((550-450)/25)*30 = 120분 → 11:00
     *
     * @param url 에브리타임 시간표 공유 URL
     * @return 파싱된 시간표 데이터 (JSON 형태)
     */
    public Map<String, Object> crawlSchedule(String url) {
        // ===== 1단계: Singleton WebDriver로 URL 접속 =====
        WebDriver driver = getWebDriver();
        driver.get(url);

        // ===== 2단계: 동적 페이지 로딩 대기 =====
        // 에브리타임은 JavaScript로 시간표를 렌더링하므로 대기 필요
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // "tablebody" 클래스가 DOM에 나타날 때까지 최대 10초 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("tablebody")));
        } catch (Exception e) {
            System.out.println("tablebody 로딩 실패");
            return Map.of("error", "시간표를 불러오지 못했습니다.");
        }

        // 요일별 강의 리스트를 저장할 Map
        // key: "월", "화", "수", ... / value: 해당 요일의 강의 목록
        Map<String, List<CalendarTimetableDto>> scheduleMap = new HashMap<>();

        try {
            // ===== 3단계: 요일 정보 추출 =====
            // HTML 구조: <div class="tablehead"><td>월</td><td>화</td>...</div>
            WebElement tableHead = driver.findElement(By.className("tablehead"));
            List<WebElement> tdElements = tableHead.findElements(By.tagName("td"));

            // Stream API로 공백 제거 후 요일 목록 생성
            List<String> days = tdElements.stream()
                    .map(WebElement::getText)      // 텍스트 추출 ("월", "화", ...)
                    .filter(s -> !s.isBlank())      // 빈 문자열 제외
                    .toList();

            // ===== 4단계: 강의 블록 파싱 =====
            // HTML 구조: <div class="tablebody"><div class="subject" style="top:550px; height:50px;">강의명</div></div>
            WebElement tableBody = driver.findElement(By.className("tablebody"));
            List<WebElement> subjects = tableBody.findElements(By.className("subject"));

            // ===== 정규식 패턴: CSS style에서 top, height 추출 =====
            // 예시 style: "top: 550px; height: 50px; left: 100px;"
            Pattern topPattern = Pattern.compile("top:\\s*(\\d+)px");        // top 값 추출
            Pattern heightPattern = Pattern.compile("height:\\s*(\\d+)px");  // height 값 추출

            // 각 강의 블록 순회
            for (WebElement subject : subjects) {
                // style 속성 전체 가져오기
                String style = subject.getAttribute("style");

                // 정규식 매칭
                Matcher topMatcher = topPattern.matcher(style);
                Matcher heightMatcher = heightPattern.matcher(style);

                // top이나 height가 없으면 스킵
                if (!topMatcher.find() || !heightMatcher.find()) continue;

                // 픽셀 값을 숫자로 변환
                int top = Integer.parseInt(topMatcher.group(1));      // 예: 550
                int height = Integer.parseInt(heightMatcher.group(1));  // 예: 50

                // ===== 시간 변환 알고리즘 (핵심) =====
                // 기준: top=450px가 09:00 시작
                // 공식: ((top - 450) / 25) * 30분
                //
                // 예시 1: top=450 → ((450-450)/25)*30 = 0분 → 09:00
                // 예시 2: top=550 → ((550-450)/25)*30 = 120분 → 11:00
                // 예시 3: top=475 → ((475-450)/25)*30 = 30분 → 09:30
                int startMinutes = ((top - 450) / 25) * 30;

                // 종료 시간 계산 (높이를 30분 단위로 변환)
                // Math.ceil: 올림 처리 (25px 미만도 한 칸으로 계산)
                int endMinutes = startMinutes + (int) Math.ceil((double)(height - 1) / 25) * 30;

                // ===== 15분 단위 시간 슬롯 생성 =====
                // 예: 09:00~10:00 강의 → ["09:00", "09:15", "09:30", "09:45"]
                List<String> timeSlots = new ArrayList<>();
                for (int m = startMinutes; m < endMinutes; m += 15) {
                    int hour = 9 + m / 60;     // 09:00 기준으로 시간 계산
                    int minute = m % 60;       // 분 계산
                    timeSlots.add(String.format("%02d:%02d", hour, minute));
                }

                // ===== 요일 인덱스 찾기 (XPath로 부모 요소 탐색) =====
                // HTML 구조: <tr><td><div class="subject">...</div></td></tr>
                WebElement parentTd = subject.findElement(By.xpath("./ancestor::td"));  // 부모 <td> 찾기
                WebElement parentTr = parentTd.findElement(By.xpath("./ancestor::tr")); // 부모 <tr> 찾기
                List<WebElement> allTds = parentTr.findElements(By.tagName("td"));      // 같은 행의 모든 <td> 찾기

                // 현재 강의가 몇 번째 열(요일)인지 인덱스 구하기
                int tdIndex = allTds.indexOf(parentTd);

                // 요일 배열 범위 내에 있는 경우만 처리
                if (tdIndex >= 0 && tdIndex < days.size()) {
                    String day = days.get(tdIndex);        // 요일 이름 (예: "월")
                    String subjectName = subject.getText(); // 강의명 (예: "컴퓨터구조")

                    // Map에 요일별로 강의 추가
                    // computeIfAbsent: 키가 없으면 새 ArrayList 생성
                    scheduleMap.computeIfAbsent(day, k -> new ArrayList<>())
                            .add(new CalendarTimetableDto(timeSlots, subjectName));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "시간표 구조 파싱 실패");
        }

        // ===== 5단계: JSON 응답 형식으로 변환 =====
        // 최종 형태: { "payload": { "schedules": [ { "time_point": "월", "subjects": [...] }, ... ] } }
        List<Map<String, Object>> finalSchedules = new ArrayList<>();
        for (var entry : scheduleMap.entrySet()) {
            // 각 요일의 강의 리스트를 Map으로 변환
            List<Map<String, Object>> subjectBlocks = new ArrayList<>();
            for (var item : entry.getValue()) {
                subjectBlocks.add(Map.of(
                        "subject", item.getSubject(),  // 강의명
                        "times", item.getTimes()       // 시간 슬롯 배열
                ));
            }
            // 요일 단위로 묶어서 추가
            finalSchedules.add(Map.of("time_point", entry.getKey(), "subjects", subjectBlocks));
        }

        // API 응답 형식으로 반환
        return Map.of("payload", Map.of("schedules", finalSchedules));
    }

    /**
     * 에브리타임 시간표를 크롤링하고 데이터베이스에 저장
     *
     * <프로세스>
     * 1. crawlSchedule()로 시간표 크롤링
     * 2. JSON 문자열로 직렬화
     * 3. DB에 저장 (기존 데이터가 있으면 업데이트, 없으면 신규 생성)
     *
     * <사용 시나리오>
     * - 사용자가 에브리타임 시간표 URL을 앱에 등록할 때
     * - 가중치 기반 시간 매칭에 활용하기 위해 크롤링 결과를 영구 저장
     *
     * @param user 시간표를 저장할 사용자
     * @param url 에브리타임 시간표 공유 URL
     * @return 크롤링 결과 (payload 포함)
     * @throws RuntimeException JSON 직렬화 실패 시
     */
    public Map<String, Object> crawlScheduleAndSave(User user, String url) {
        // ===== 1단계: 시간표 크롤링 =====
        Map<String, Object> result = crawlSchedule(url);
        Object payload = result.get("payload");

        try {
            // ===== 2단계: JSON 문자열로 변환 (직렬화) =====
            // ObjectMapper가 Java 객체를 JSON 문자열로 변환
            // 예: { "schedules": [...] } → "{\"schedules\":[...]}"
            String json = objectMapper.writeValueAsString(payload);

            // ===== 3단계: DB에 저장 또는 업데이트 =====
            // findByUser(): 사용자의 기존 시간표 조회
            // orElse(): 없으면 새로운 엔티티 생성 (Builder 패턴)
            EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                    .orElse(EveryTimeTimetable.builder().user(user).build());

            // JSON 문자열을 엔티티에 설정
            timetable.setTimetableJson(json);

            // JPA save(): 기존 데이터면 UPDATE, 신규면 INSERT
            timetableRepository.save(timetable);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("시간표 저장 실패", e);
        }

        // 크롤링 결과 반환 (클라이언트에게 즉시 보여주기 위함)
        return result;
    }

    /**
     * 데이터베이스에 저장된 시간표 조회
     *
     * <사용 시나리오>
     * - 사용자가 이전에 등록한 시간표를 다시 조회할 때
     * - 매번 크롤링하지 않고 저장된 데이터 재사용 (성능 최적화)
     *
     * @param user 시간표를 조회할 사용자
     * @return 저장된 시간표 데이터 (JSON 형태)
     * @throws RuntimeException 저장된 시간표가 없거나 역직렬화 실패 시
     */
    public Map<String, Object> getSavedTimetable(User user) {
        // ===== 1단계: DB에서 시간표 조회 =====
        // findByUser(): 사용자의 시간표 엔티티 조회
        // orElseThrow(): 없으면 예외 발생
        EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("저장된 시간표가 없습니다"));

        try {
            // ===== 2단계: JSON 문자열을 Map으로 변환 (역직렬화) =====
            // ObjectMapper가 JSON 문자열을 Java Map 객체로 변환
            // 예: "{\"schedules\":[...]}" → { "schedules": [...] }
            Map<String, Object> payload = objectMapper.readValue(timetable.getTimetableJson(), Map.class);

            // API 응답 형식으로 반환
            return Map.of("payload", payload);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("시간표 불러오기 실패", e);
        }
    }
}
