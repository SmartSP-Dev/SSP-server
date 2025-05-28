FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    openjdk-21-jre \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    chromium-browser \
    chromium-chromedriver \
    fonts-liberation \
    libnss3 \
    libxss1 \
    libasound2 \
    libgbm1 \
    && apt-get clean

WORKDIR /app
COPY build/libs/*.jar app.jar

ENV LD_LIBRARY_PATH="/usr/lib/x86_64-linux-gnu"

# 핵심 실행 명령어
ENTRYPOINT ["java", "-Djava.library.path=/usr/lib/x86_64-linux-gnu", "-jar", "app.jar"]