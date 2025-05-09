# 1. 베이스 이미지
FROM eclipse-temurin:21-jre

# 2. OCR 관련 패키지 설치 (tesseract, kor/eng 언어 지원)
USER root
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev curl unzip && \
    apt-get install -y tesseract-ocr-kor tesseract-ocr-eng && \
    apt-get clean

# 3. 환경변수 설정
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV LANGS=eng+kor
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu

# 4. JAR 복사
COPY build/libs/*.jar app.jar

# 5. 포트 노출
EXPOSE 8080

# 6. 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
