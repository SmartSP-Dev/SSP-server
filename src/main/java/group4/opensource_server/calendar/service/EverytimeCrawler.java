package group4.opensource_server.calendar.service;

import group4.opensource_server.calendar.domain.TimetableConstants;
import group4.opensource_server.calendar.exception.CrawlingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 에브리타임 시간표 크롤링 클래스
 *
 * <주요 기능>
 * 1. Selenium WebDriver로 에브리타임 페이지 접근
 * 2. 동적 페이지 로딩 대기
 * 3. DOM에서 시간표 관련 요소 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EverytimeCrawler {

    private final TimetableWebDriverManager webDriverManager;

    /**
     * 에브리타임 크롤링 결과를 담는 데이터 클래스
     */
    public record CrawlResult(
            List<String> days,
            List<WebElement> subjects
    ) {}

    /**
     * 에브리타임 시간표 URL을 크롤링합니다.
     *
     * @param url 에브리타임 시간표 공유 URL
     * @return 크롤링 결과 (요일 목록, 강의 요소 목록)
     * @throws CrawlingException 크롤링 실패 시
     */
    public CrawlResult crawl(String url) {
        try {
            WebDriver driver = webDriverManager.getDriver();
            log.info("에브리타임 크롤링 시작: {}", url);

            // URL 접속
            driver.get(url);

            // 페이지 로딩 대기
            waitForPageLoad(driver);

            // 요일 정보 추출
            List<String> days = extractDays(driver);
            log.debug("추출된 요일: {}", days);

            // 강의 블록 추출
            List<WebElement> subjects = extractSubjects(driver);
            log.debug("추출된 강의 블록 수: {}", subjects.size());

            return new CrawlResult(days, subjects);

        } catch (Exception e) {
            log.error("에브리타임 크롤링 실패: {}", url, e);
            throw new CrawlingException("시간표 크롤링 실패", e);
        }
    }

    /**
     * 페이지 로딩을 대기합니다.
     * 에브리타임은 JavaScript로 시간표를 렌더링하므로 대기 필요
     *
     * @param driver WebDriver 인스턴스
     * @throws CrawlingException 페이지 로딩 실패 시
     */
    private void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(TimetableConstants.PAGE_LOAD_TIMEOUT_SECONDS)
            );
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("tablebody")));
            log.debug("페이지 로딩 완료");
        } catch (Exception e) {
            log.error("페이지 로딩 실패", e);
            throw new CrawlingException("시간표 페이지 로딩 실패", e);
        }
    }

    /**
     * 요일 정보를 추출합니다.
     *
     * @param driver WebDriver 인스턴스
     * @return 요일 목록 (예: ["월", "화", "수", "목", "금"])
     * @throws CrawlingException 요일 추출 실패 시
     */
    private List<String> extractDays(WebDriver driver) {
        try {
            WebElement tableHead = driver.findElement(By.className("tablehead"));
            List<WebElement> tdElements = tableHead.findElements(By.tagName("td"));

            return tdElements.stream()
                    .map(WebElement::getText)
                    .filter(s -> !s.isBlank())
                    .toList();
        } catch (Exception e) {
            log.error("요일 정보 추출 실패", e);
            throw new CrawlingException("요일 정보 추출 실패", e);
        }
    }

    /**
     * 강의 블록 요소를 추출합니다.
     *
     * @param driver WebDriver 인스턴스
     * @return 강의 블록 WebElement 목록
     * @throws CrawlingException 강의 블록 추출 실패 시
     */
    private List<WebElement> extractSubjects(WebDriver driver) {
        try {
            WebElement tableBody = driver.findElement(By.className("tablebody"));
            return tableBody.findElements(By.className("subject"));
        } catch (Exception e) {
            log.error("강의 블록 추출 실패", e);
            throw new CrawlingException("강의 블록 추출 실패", e);
        }
    }
}
