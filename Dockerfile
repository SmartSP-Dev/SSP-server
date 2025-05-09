# 베이스 이미지
FROM ubuntu:22.04

# 시스템 패키지 설치 및 Tesseract + 한국어 데이터 설치
RUN apt-get update && apt-get install -y \
    openjdk-21-jre \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    libpng-dev \
    libjpeg-dev \
    libtiff-dev \
    libleptonica-dev \
    && apt-get clean

# 작업 디렉토리 생성
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 환경변수 설정 (LD_LIBRARY_PATH를 시스템 전체로 설정)
ENV LD_LIBRARY_PATH="/usr/lib/x86_64-linux-gnu"

# 포트 오픈
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
