# SSP-Server

> **AI 기반 학습 루틴 관리 및 퀴즈 생성 시스템의 백엔드 서버**

SSP-server는 사용자 맞춤형 서비스를 제공하기 위해 데이터의 효율적인 처리와 안정적인 API 환경을 구축하는 것을 목표로 합니다.

---

## 📚 주요 기능

### 1. AI 기반 퀴즈 생성
- **GPT-4o API 연동**: 사용자가 업로드한 학습 자료(텍스트, PDF, 이미지)를 분석하여 맞춤형 퀴즈 자동 생성
- **다양한 문제 유형**: 객관식, OX, 빈칸 채우기 문제 지원
- **키워드 기반 출제**: 특정 키워드를 중심으로 한 문제 생성으로 학습 효율성 극대화

### 2. OCR 기반 이미지 텍스트 추출
- **Tesseract OCR + OpenCV**: 이미지에서 텍스트 자동 추출 및 전처리
- **PDF 파싱**: Apache PDFBox를 활용한 PDF 문서 텍스트 추출
- **대용량 처리**: 최대 30,000자까지 효율적으로 처리

### 3. 에브리타임 시간표 크롤링
- **Selenium WebDriver**: 에브리타임 시간표 자동 크롤링
- **동시성 제어**: Semaphore + ThreadLocal 패턴으로 안정적인 멀티스레드 크롤링 (최대 3명 동시 처리)
- **메모리 최적화**: 최대 900MB로 메모리 사용량 제한

### 4. 학습 루틴 관리
- **복습 알림**: 주간 퀴즈 요약 제공 및 복습 필요 항목 자동 갱신
- **학습 통계**: 사용자별 퀴즈 응시 횟수, 정답률 추적

### 5. 소셜 로그인
- **Kakao & Apple OAuth 2.0**: 간편한 소셜 로그인 지원
- **JWT 기반 인증**: 무상태(Stateless) 인증으로 확장성 확보

---

## 🛠 기술 스택

### Core Framework
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **Spring Boot** | 3.4.4 | 생산성 높은 엔터프라이즈 애플리케이션 개발 프레임워크 |
| **Java** | 21 | Virtual Threads 등 최신 성능 개선 기능 활용 |
| **Gradle** | 8.x | 빠르고 유연한 빌드 자동화 도구 |

### Database & Persistence
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **MySQL** | 8.x | 안정적이고 검증된 관계형 데이터베이스 |
| **Spring Data JPA** | 3.4.4 | ORM을 통한 생산적인 데이터베이스 접근 및 관리 |
| **Hibernate** | 6.x | JPA 구현체로 N+1 쿼리 방지 및 최적화 |

### Security & Authentication
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **Spring Security** | 6.x | 포괄적인 보안 프레임워크로 인증/인가 처리 |
| **JWT (jjwt)** | 0.11.5 | 무상태 토큰 기반 인증으로 확장성 확보 |
| **OAuth 2.0** | - | Kakao/Apple 소셜 로그인 구현 |

### AI & External API
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **OpenAI API** | GPT-4o | 학습 자료 분석 및 맞춤형 퀴즈 자동 생성 |
| **RestTemplate** | - | 외부 API 연동 및 Connection Pooling |

### OCR & Document Processing
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **Tesseract OCR** | 5.13.0 | 이미지에서 텍스트 추출 |
| **OpenCV** | 4.5.4 | 이미지 전처리 및 품질 개선 |
| **Apache PDFBox** | 3.0.4 | PDF 문서 텍스트 추출 |

### Web Crawling
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **Selenium WebDriver** | 4.20.0 | 동적 웹 페이지 크롤링 (에브리타임 시간표) |
| **WebDriverManager** | 5.8.0 | ChromeDriver 자동 설정 및 버전 관리 |

### DevOps & Deployment
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **Docker** | - | 컨테이너 기반 배포로 환경 일관성 확보 |
| **GitHub Actions** | - | 지속적 통합(CI)으로 코드 품질 자동 검증 |

### Documentation & Development
| 기술 | 버전 | 사용 이유 |
|------|------|-----------|
| **SpringDoc OpenAPI** | 2.2.0 | Swagger UI를 통한 API 문서 자동 생성 |
| **Lombok** | - | 보일러플레이트 코드 제거로 생산성 향상 |

---

## 🏗 시스템 아키텍처

### ERD (Entity Relationship Diagram)
> 데이터베이스 설계 및 테이블 간 관계를 나타냅니다.

![ERD](docs/images/erd.png)

*이미지를 추가하려면 `docs/images/erd.png` 경로에 ERD 이미지를 저장하세요.*

---

### 시스템 구조도
> 전체 시스템의 구성 요소 및 데이터 흐름을 나타냅니다.

![System Architecture](docs/images/architecture.png)

*이미지를 추가하려면 `docs/images/architecture.png` 경로에 구조도 이미지를 저장하세요.*

---

## 🚀 시작하기

### 사전 요구사항
- Java 21
- MySQL 8.x
- Docker (선택사항)
- Tesseract OCR (로컬 개발 시)

### 환경 변수 설정
```bash
# application.yml 또는 환경 변수로 설정
KAKAO_API_KEY=your_kakao_api_key
KAKAO_REDIRECT_URI=your_redirect_uri
OPENAI_API_KEY=your_openai_api_key
JWT_SECRET_KEY=your_jwt_secret
DATASOURCE_URL=jdbc:mysql://localhost:3306/ssp_db
DATASOURCE_USERNAME=your_db_username
DATASOURCE_PASSWORD=your_db_password
```

### 로컬 실행
```bash
# 1. 저장소 클론
git clone https://github.com/SmartSP-Dev/SSP-server.git
cd SSP-server

# 2. 빌드
./gradlew clean build

# 3. 실행
./gradlew bootRun
```

### Docker 실행
```bash
# Docker 이미지 빌드
docker build -t ssp-server .

# 컨테이너 실행
docker run -d -p 8080:8080 \
  -e KAKAO_API_KEY=your_key \
  -e OPENAI_API_KEY=your_key \
  ssp-server
```

---

## 📖 API 문서

서버 실행 후 다음 URL에서 Swagger UI를 통해 API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui/index.html
```

### 주요 API 엔드포인트

#### 퀴즈 관리
- `POST /quizzes` - 퀴즈 생성
- `GET /quizzes` - 사용자 퀴즈 목록 조회
- `POST /quizzes/{quizId}/attempts` - 퀴즈 응시
- `DELETE /quizzes/{quizId}` - 퀴즈 삭제

#### 시간표 관리
- `POST /calendar/timetable` - 에브리타임 시간표 크롤링 및 저장
- `GET /calendar/timetable/me` - 저장된 시간표 조회

#### 인증
- `POST /auth/kakao` - Kakao 로그인
- `POST /auth/apple` - Apple 로그인

---

## 🔧 주요 기술적 개선 사항

### 1. Quiz 서비스 리팩토링
- **SRP 준수**: QuizService를 OpenAiClient, QuizPromptBuilder, QuizGrader로 분리
- **매직 넘버 제거**: QuizConstants, QuizStatus Enum 도입
- **N+1 쿼리 방지**: @EntityGraph 및 Bulk 연산 적용
- **타입 안정성**: Map → DTO 변환

### 2. Calendar 서비스 동시성 개선
- **Semaphore + ThreadLocal 패턴**: 최대 3개 WebDriver 동시 실행으로 Race Condition 해결
- **메모리 보호**: 최대 900MB로 사용량 제한
- **성능 향상**: 3명 동시 처리로 3배 성능 개선

### 3. CI/CD 최적화
- **GitHub Actions**: 자동 빌드로 코드 품질 검증
- **Docker 배포**: 컨테이너 기반 일관된 배포 환경

---

## 📂 프로젝트 구조

```
SSP-server/
├── src/main/java/group4/opensource_server/
│   ├── auth/              # 인증 관련 (JWT, OAuth)
│   ├── calendar/          # 시간표 크롤링
│   │   ├── controller/
│   │   ├── service/       # EverytimeCrawler, TimetableParser, WebDriverManager
│   │   ├── domain/        # Entity, Repository, Constants
│   │   ├── dto/
│   │   └── exception/
│   ├── quiz/              # 퀴즈 생성 및 관리
│   │   ├── controller/
│   │   ├── service/       # QuizService, OpenAiClient, QuizPromptBuilder, QuizGrader
│   │   ├── domain/        # Entity, Repository, Constants, Enum
│   │   ├── dto/
│   │   └── exception/
│   ├── ocr/               # OCR 및 PDF 처리
│   └── user/              # 사용자 관리
├── src/main/resources/
│   └── application.yml    # 설정 파일
├── docs/
│   └── images/            # ERD, 구조도 이미지
├── Dockerfile
└── build.gradle
```

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 라이선스

This project is licensed under the MIT License.

---

## 👥 팀원

- **Backend Developer**: [팀원 이름]
- **Frontend Developer**: [팀원 이름]

---

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 Issues를 통해 연락주세요.

- GitHub: [SmartSP-Dev/SSP-server](https://github.com/SmartSP-Dev/SSP-server)
