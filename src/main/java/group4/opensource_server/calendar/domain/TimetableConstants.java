package group4.opensource_server.calendar.domain;

public class TimetableConstants {
    // 시간표 CSS 파싱 상수
    public static final int BASE_TOP_PX = 450;          // 09:00 기준 top 픽셀
    public static final int PX_PER_SLOT = 25;           // 30분당 픽셀
    public static final int MINUTES_PER_SLOT = 30;      // 한 슬롯당 분
    public static final int BASE_HOUR = 9;              // 시작 시간 (09:00)
    public static final int TIME_SLOT_INTERVAL = 15;    // 시간 슬롯 간격 (15분)

    // WebDriver 설정
    public static final int PAGE_LOAD_TIMEOUT_SECONDS = 10;
    public static final String WINDOW_SIZE = "1920x1080";
    public static final String USER_AGENT = "Mozilla/5.0";

    private TimetableConstants() {
        throw new IllegalStateException("Utility class");
    }
}
