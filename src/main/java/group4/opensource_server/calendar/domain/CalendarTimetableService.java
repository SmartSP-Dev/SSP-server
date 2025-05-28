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


@Service
public class CalendarTimetableService {

    private static WebDriver driver;
    private static final Lock lock = new ReentrantLock();

    private final EveryTimeTimetableRepository timetableRepository;
    private final ObjectMapper objectMapper;

    public CalendarTimetableService(EveryTimeTimetableRepository timetableRepository, ObjectMapper objectMapper) {
        this.timetableRepository = timetableRepository;
        this.objectMapper = objectMapper;
    }

    public static WebDriver getWebDriver() {
        if (driver == null) {
            lock.lock();
            try {
                if (driver == null) {
                    WebDriverManager.chromedriver().setup();

                    ChromeOptions options = new ChromeOptions();
                    options.addArguments(
                            "--headless=new",
                            "--disable-gpu",
                            "--window-size=1920x1080",
                            "--no-sandbox",
                            "--disable-dev-shm-usage",
                            "user-agent=Mozilla/5.0"
                    );

                    // 고유한 임시 프로필 디렉터리 생성
                    try {
                        Path tmpProfile = Files.createTempDirectory("chrome-user-data-");
                        options.addArguments("--user-data-dir=" + tmpProfile.toAbsolutePath());
                    } catch (IOException e) {
                        throw new RuntimeException("크롬 프로필 임시 폴더 생성 실패", e);
                    }

                    // 크로스 오리진 이슈 방지
                    options.addArguments("--remote-allow-origins=* ");

                    driver = new ChromeDriver(options);
                }
            } finally {
                lock.unlock();
            }
        }
        return driver;
    }

    public Map<String, Object> crawlSchedule(String url) {
        WebDriver driver = getWebDriver();
        driver.get(url);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("tablebody")));
        } catch (Exception e) {
            System.out.println("tablebody 로딩 실패");
            return Map.of("error", "시간표를 불러오지 못했습니다.");
        }

        Map<String, List<CalendarTimetableDto>> scheduleMap = new HashMap<>();

        try {
            WebElement tableHead = driver.findElement(By.className("tablehead"));
            List<WebElement> tdElements = tableHead.findElements(By.tagName("td"));
            List<String> days = tdElements.stream()
                    .map(WebElement::getText)
                    .filter(s -> !s.isBlank())
                    .toList();

            WebElement tableBody = driver.findElement(By.className("tablebody"));
            List<WebElement> subjects = tableBody.findElements(By.className("subject"));

            Pattern topPattern = Pattern.compile("top:\\s*(\\d+)px");
            Pattern heightPattern = Pattern.compile("height:\\s*(\\d+)px");

            for (WebElement subject : subjects) {
                String style = subject.getAttribute("style");
                Matcher topMatcher = topPattern.matcher(style);
                Matcher heightMatcher = heightPattern.matcher(style);
                if (!topMatcher.find() || !heightMatcher.find()) continue;

                int top = Integer.parseInt(topMatcher.group(1));
                int height = Integer.parseInt(heightMatcher.group(1));

                int startMinutes = ((top - 450) / 25) * 30;
                int endMinutes = startMinutes + (int) Math.ceil((double)(height - 1) / 25) * 30;

                List<String> timeSlots = new ArrayList<>();
                for (int m = startMinutes; m < endMinutes; m += 15) {
                    int hour = 9 + m / 60;
                    int minute = m % 60;
                    timeSlots.add(String.format("%02d:%02d", hour, minute));
                }

                WebElement parentTd = subject.findElement(By.xpath("./ancestor::td"));
                WebElement parentTr = parentTd.findElement(By.xpath("./ancestor::tr"));
                List<WebElement> allTds = parentTr.findElements(By.tagName("td"));
                int tdIndex = allTds.indexOf(parentTd);

                if (tdIndex >= 0 && tdIndex < days.size()) {
                    String day = days.get(tdIndex);
                    String subjectName = subject.getText();

                    scheduleMap.computeIfAbsent(day, k -> new ArrayList<>())
                            .add(new CalendarTimetableDto(timeSlots, subjectName));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "시간표 구조 파싱 실패");
        }

        List<Map<String, Object>> finalSchedules = new ArrayList<>();
        for (var entry : scheduleMap.entrySet()) {
            List<Map<String, Object>> subjectBlocks = new ArrayList<>();
            for (var item : entry.getValue()) {
                subjectBlocks.add(Map.of(
                        "subject", item.getSubject(),
                        "times", item.getTimes()
                ));
            }
            finalSchedules.add(Map.of("time_point", entry.getKey(), "subjects", subjectBlocks));
        }

        return Map.of("payload", Map.of("schedules", finalSchedules));
    }

    public Map<String, Object> crawlScheduleAndSave(User user, String url) {
        Map<String, Object> result = crawlSchedule(url);
        Object payload = result.get("payload");

        try {
            String json = objectMapper.writeValueAsString(payload);

            EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                    .orElse(EveryTimeTimetable.builder().user(user).build());

            timetable.setTimetableJson(json);
            timetableRepository.save(timetable);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("시간표 저장 실패", e);
        }

        return result;
    }

    public Map<String, Object> getSavedTimetable(User user) {
        EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("저장된 시간표가 없습니다"));

        try {
            Map<String, Object> payload = objectMapper.readValue(timetable.getTimetableJson(), Map.class);
            return Map.of("payload", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("시간표 불러오기 실패", e);
        }
    }
}
