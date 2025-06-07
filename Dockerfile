FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# 시스템 기본 패키지 및 종속성 설치
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    gnupg \
    unzip \
    openjdk-21-jre \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    libopencv-dev \
    libopencv4.5-java \
    libopencv4.5-jni \
    fonts-liberation \
    libnss3 \
    libxss1 \
    libasound2 \
    libgbm1 \
    libvulkan1 \
    xdg-utils \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

# Google Chrome 설치
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb \
    && rm google-chrome-stable_current_amd64.deb

# ChromeDriver 버전 다운로드
RUN CHROME_VERSION=$(google-chrome --version | grep -oP '\d+\.\d+\.\d+\.\d+') \
    && wget -O /tmp/chromedriver.zip "https://storage.googleapis.com/chrome-for-testing-public/$CHROME_VERSION/linux64/chromedriver-linux64.zip" \
    && unzip /tmp/chromedriver.zip -d /usr/local/bin/ \
    && mv /usr/local/bin/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver \
    && chmod +x /usr/local/bin/chromedriver \
    && rm -rf /tmp/chromedriver.zip /usr/local/bin/chromedriver-linux64

# 작업 디렉토리 설정
WORKDIR /app
COPY build/libs/*.jar app.jar

ENV LD_LIBRARY_PATH="/usr/lib/x86_64-linux-gnu:/usr/lib/jni"

ENTRYPOINT ["java",
  "-Djava.library.path=/usr/lib/x86_64-linux-gnu:/usr/lib/jni",
  "-jar","app.jar"]