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

    // 동시성 제어 설정
    /**
     * 동시에 실행 가능한 최대 크롤링 작업 수
     *
     * <설정 근거>
     * - 각 WebDriver는 약 300MB 메모리 사용
     * - 3개 동시 실행 시: 300MB × 3 = 900MB (안정적)
     * - 서버 사양에 따라 조정 가능 (메모리 여유 있으면 5로 증가)
     *
     * <효과>
     * - 메모리 사용량 예측 가능 (최대 900MB)
     * - Race Condition 방지 (각 스레드가 독립적인 WebDriver 사용)
     * - 적절한 동시 처리로 성능 향상 (1명씩 처리보다 3배 빠름)
     */
    public static final int MAX_CONCURRENT_CRAWLING = 3;

    /**
     * WebDriver 획득 대기 시간 (초)
     *
     * <설정 근거>
     * - 크롤링 작업이 평균 5~10초 소요
     * - 대기 줄이 길어도 30초 안에는 차례가 옴
     * - 30초 이상 대기는 비정상 상황으로 판단
     */
    public static final int ACQUIRE_TIMEOUT_SECONDS = 30;

    private TimetableConstants() {
        throw new IllegalStateException("Utility class");
    }
}
