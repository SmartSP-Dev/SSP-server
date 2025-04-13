package group4.opensource_server.calendar.domain;

import group4.opensource_server.calendar.dto.CalendarTimetableDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CalendarTimetableService {

    private static WebDriver driver;
    private static final Lock lock = new ReentrantLock();

    public static WebDriver getWebDriver() {
        if (driver == null) {
            lock.lock();
            try {
                if (driver == null) {
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--disable-gpu");
                    options.addArguments("--window-size=1920x1080");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("user-agent=Mozilla/5.0");

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

                    scheduleMap.putIfAbsent(day, new ArrayList<>());
                    scheduleMap.get(day).add(new CalendarTimetableDto(timeSlots, subjectName));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "시간표 구조 파싱 실패");
        }

        List<Map<String, Object>> finalSchedules = new ArrayList<>();
        for (Map.Entry<String, List<CalendarTimetableDto>> entry : scheduleMap.entrySet()) {
            List<Map<String, Object>> subjectBlocks = new ArrayList<>();
            for (CalendarTimetableDto item : entry.getValue()) {
                subjectBlocks.add(Map.of(
                        "subject", item.getSubject(),
                        "times", item.getTimes()
                ));
            }
            finalSchedules.add(Map.of("time_point", entry.getKey(), "subjects", subjectBlocks));
        }

        return Map.of("payload", Map.of("schedules", finalSchedules));
    }
}
