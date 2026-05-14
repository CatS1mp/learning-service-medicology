# Learning Service — Medicology

Dịch vụ quản lý nội dung học tập, quiz và theo dõi tiến độ cho nền tảng Medicology. Tương tác với Auth để uỷ quyền và Dictionary để chuẩn hoá thuật ngữ.

## Công nghệ
- Spring Boot 3.3, Java 17
- Spring Data JPA (RDBMS)
- Spring Security (uỷ quyền theo roles)

## Tính năng chính
- Quản lý khoá học, bài học, bài kiểm tra/quiz.
- Ghi nhận tiến độ người học, thống kê tổng quan (không chứa PHI).
- Tích hợp từ điển y khoa để gợi ý/chuẩn hoá thuật ngữ (tuỳ use-case).

## Yêu cầu
- Java 17, Maven 3.9+
- CSDL quan hệ (cấu hình qua `spring.datasource.*`)

## Cấu hình môi trường (ví dụ)
- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medicology_learning`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`
- Tuỳ chọn: `SERVER_PORT=8083`

## Chạy local
```bash
mvn spring-boot:run
```
Hoặc build JAR:
```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

## Kiểm thử
```bash
mvn verify -q
```

## Bảo mật & tuân thủ
- Không ghi log câu trả lời/điểm số mang tính nhạy cảm; ẩn danh hoá khi thống kê.
- Không đưa PHI vào URL, thông báo lỗi hoặc logging.
- Theo dõi tương quan qua Correlation-Id từ API Gateway.

## Tài liệu API
- Khi bật OpenAPI (springdoc), có thể truy cập Swagger UI và spec OpenAPI. Đường dẫn tuỳ cấu hình.
