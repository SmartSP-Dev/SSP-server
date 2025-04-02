
# 베이스 이미지 (예: OpenJDK 21)
FROM eclipse-temurin:21-jre

# JAR 파일을 컨테이너 내에 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 포트 설정 (예: 8080)
EXPOSE 443

# JAR 파일 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
