# Learning Service API Guideline

## 1. Mục đích tài liệu

Tài liệu này mô tả endpoint trong `learning-service-medicology`, gồm:

- Công dụng của endpoint
- Input request
- Output response
- Màn hình hoặc flow nên sử dụng
- Lưu ý triển khai FE/BE

## 2. Base URL và tài liệu kỹ thuật

- Local: `http://localhost:8081`
- Staging: `Chưa cấu hình riêng trong repo`
- Production: `https://learning-service-medicology-eae21d20151f.herokuapp.com`

Swagger / OpenAPI:

- `GET /swagger-ui.html`
- `GET /api-docs`
- `GET /` redirect sang `/swagger-ui/index.html`

## 3. Authentication và quy ước chung

### 3.1 Authentication

- Public: `/`, Swagger
- Internal route không yêu cầu JWT ở security layer: `/api/v1/learning/internal/**`
- Toàn bộ endpoint business còn lại dưới `/api/v1/learning/**` yêu cầu JWT Bearer
- Lưu ý: các endpoint create/update/delete course, section, lesson và AI feedback hiện chưa gắn `ROLE_ADMIN` ở controller/security; về thực tế chỉ cần một JWT hợp lệ để gọi

Header chuẩn:

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Header cho contract nội bộ:

```http
X-Internal-Token: <shared-secret>
```

### 3.2 Kiểu lỗi

Format lỗi hiện tại:

```json
{
  "status": 400,
  "message": "Mô tả lỗi",
  "timestamp": "2026-04-13T10:30:00"
}
```

Mapping chính:

- `IllegalArgumentException`, validation, `InvalidRequestException`, `InvalidFileException` trả `400`
- `DataIntegrityViolationException` có thể trả `409 Conflict` cho lỗi trùng dữ liệu như slug
- `StorageUploadException` trả `502 Bad Gateway`
- Lỗi chưa bắt riêng trả `500 Internal Server Error`

### 3.3 Quy ước response

- Service này dùng `ApiResponse<T>` theo format:

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

- Các endpoint đọc đơn giản thường trả `code=200`, `message=Success`
- Các endpoint create/update có thể trả custom message như `"Course created successfully"`
- Một số endpoint admin-like chưa tách riêng DTO role-based, nên FE cần phân quyền ở tầng khác nếu cần

## 4. Tóm tắt mapping theo màn hình

| Màn hình / flow | Endpoint chính |
| --- | --- |
| Course catalog | `GET /api/v1/learning/courses` |
| My enrolled courses | `GET /api/v1/learning/courses/enrolled` |
| Course available cho learner | `GET /api/v1/learning/courses/student/available` |
| Chi tiết course / roadmap | `GET /api/v1/learning/courses/{courseId}`, `GET /api/v1/learning/courses/{courseId}/roadmap` |
| Learning path | `GET /api/v1/learning/courses/path` |
| Enroll course | `POST /api/v1/learning/courses/{courseId}/enroll` |
| Danh sách section | `GET /api/v1/learning/courses/{courseId}/sections` |
| Danh sách lesson / lesson detail | `GET /api/v1/learning/sections/{sectionId}/lessons`, `GET /api/v1/learning/lessons/{lessonId}` |
| Hoàn thành lesson | `POST /api/v1/learning/lessons/{lessonId}/complete` |
| Theo dõi progress | `GET /api/v1/learning/progress`, `GET /api/v1/learning/progress/activity`, `POST /api/v1/learning/progress/streak/ping` |
| Theo dõi block progress | `PATCH /api/v1/learning/lessons/{lessonId}/blocks/{blockId}/progress`, `GET /api/v1/learning/lessons/{lessonId}/blocks/progress` |
| AI feedback | `POST /api/v1/learning/ai-feedback`, `GET /api/v1/learning/ai-feedback` |
| Contract assessment nội bộ | `GET /api/v1/learning/internal/assessment-access`, `POST /api/v1/learning/internal/assessment-result` |

## 5. Nhóm API — Course và Section

### 5.1 Catalog, enrollment, roadmap

- `GET /api/v1/learning/courses`
  - **Mục đích:** Lấy toàn bộ course
  - **Response:** `200 OK`, `ApiResponse<List<CourseResponse>>`
- `GET /api/v1/learning/courses/enrolled`
  - **Mục đích:** Lấy danh sách course learner đã enroll
  - **Response:** `200 OK`, `ApiResponse<List<CourseResponse>>`
- `GET /api/v1/learning/courses/student/available`
  - **Mục đích:** Lấy course learner còn có thể học
  - **Response:** `200 OK`, `ApiResponse<List<CourseResponse>>`
- `GET /api/v1/learning/courses/{courseId}`
  - **Mục đích:** Lấy chi tiết course
  - **Response:** `200 OK`, `ApiResponse<CourseResponse>`
- `GET /api/v1/learning/courses/{courseId}/roadmap`
  - **Mục đích:** Lấy roadmap/outline của course
  - **Response:** `200 OK`, `ApiResponse<CourseResponse>`
- `GET /api/v1/learning/courses/path`
  - **Mục đích:** Lấy learning path tổng hợp
  - **Response:** `200 OK`, `ApiResponse<Map<String, Object>>`
- `POST /api/v1/learning/courses/{courseId}/enroll`
  - **Mục đích:** Enroll learner vào course
  - **Response:** `201 Created`, `ApiResponse<Void>`
  - **Ghi chú:** Endpoint này khác với `POST /lessons/{lessonId}/enroll`
- `POST /api/v1/learning/courses`
  - **Mục đích:** Tạo course mới
  - **Body:** `multipart/form-data` gồm phần `request` (JSON parse thành `CourseRequest`) và `iconFile`
  - **Response:** `201 Created`, `ApiResponse<CourseResponse>`
  - **Ghi chú:** Nếu JSON trong `request` không hợp lệ sẽ trả `400`
- `PUT /api/v1/learning/courses/{courseId}`
  - **Mục đích:** Cập nhật course
  - **Body:** `CourseRequest`
  - **Response:** `200 OK`, `ApiResponse<CourseResponse>`
- `DELETE /api/v1/learning/courses/{courseId}`
  - **Mục đích:** Xóa course
  - **Response:** `200 OK`, `ApiResponse<Void>`

### 5.2 Section trong course

- `GET /api/v1/learning/courses/{courseId}/sections`
  - **Mục đích:** Lấy section của course
  - **Response:** `200 OK`, `ApiResponse<List<SectionSummaryResponse>>`
- `POST /api/v1/learning/courses/{courseId}/sections`
  - **Mục đích:** Tạo section trong course
  - **Body:** `SectionRequest`
  - **Response:** `201 Created`, `ApiResponse<SectionResponse>`
- `GET /api/v1/learning/sections/{sectionId}`
  - **Mục đích:** Lấy chi tiết section
  - **Response:** `200 OK`, `ApiResponse<SectionResponse>`
- `PUT /api/v1/learning/sections/{sectionId}`
  - **Mục đích:** Cập nhật section
  - **Body:** `SectionRequest`
  - **Response:** `200 OK`, `ApiResponse<SectionResponse>`
- `DELETE /api/v1/learning/sections/{sectionId}`
  - **Mục đích:** Xóa section
  - **Response:** `200 OK`, `ApiResponse<Void>`

## 6. Nhóm API — Lesson và Progress

### 6.1 Lesson player, completion, analytics

- `GET /api/v1/learning/sections/{sectionId}/lessons`
  - **Mục đích:** Lấy lesson theo section
  - **Response:** `200 OK`, `ApiResponse<List<LessonSummaryResponse>>`
- `GET /api/v1/learning/lessons/{lessonId}`
  - **Mục đích:** Lấy chi tiết lesson
  - **Response:** `200 OK`, `ApiResponse<LessonResponse>`
- `POST /api/v1/learning/lessons`
  - **Mục đích:** Tạo lesson mới
  - **Body:** `LessonRequest`
  - **Response:** `201 Created`, `ApiResponse<LessonResponse>`
- `POST /api/v1/learning/sections/{sectionId}/lessons`
  - **Mục đích:** Tạo lesson trong section cụ thể
  - **Body:** `LessonRequest`
  - **Response:** `201 Created`, `ApiResponse<LessonResponse>`
  - **Ghi chú:** `sectionId` trong body sẽ bị override bằng path param nếu có
- `PUT /api/v1/learning/lessons/{lessonId}`
  - **Mục đích:** Cập nhật lesson
  - **Body:** `LessonRequest`
  - **Response:** `200 OK`, `ApiResponse<LessonResponse>`
- `DELETE /api/v1/learning/lessons/{lessonId}`
  - **Mục đích:** Xóa lesson
  - **Response:** `200 OK`, `ApiResponse<Void>`
- `PATCH /api/v1/learning/lessons/{lessonId}/status`
  - **Mục đích:** Đổi trạng thái lesson
  - **Body:** `LessonStatusRequest`
  - **Response:** `200 OK`, `ApiResponse<LessonResponse>`
- `POST /api/v1/learning/lessons/{lessonId}/enroll`
  - **Mục đích:** Ghi nhận enroll ở cấp lesson
  - **Response:** `200 OK`, `ApiResponse<LessonResponse>`
  - **Ghi chú:** Đây là route lịch sử khác với enroll course; FE không nên nhầm hai contract này
- `POST /api/v1/learning/lessons/{lessonId}/complete`
  - **Mục đích:** Đánh dấu hoàn thành lesson
  - **Response:** `200 OK`, `ApiResponse<Void>`
- `PATCH /api/v1/learning/lessons/{lessonId}/blocks/{blockId}/progress`
  - **Mục đích:** Cập nhật tiến độ theo block
  - **Body:** `LessonBlockProgressRequest`
  - **Response:** `200 OK`, `ApiResponse<LessonBlockProgressResponse>`
- `GET /api/v1/learning/lessons/{lessonId}/blocks/progress`
  - **Mục đích:** Lấy tiến độ block của lesson theo user hiện tại
  - **Response:** `200 OK`, `ApiResponse<List<LessonBlockProgressResponse>>`
- `GET /api/v1/learning/progress`
  - **Mục đích:** Lấy progress theo course của user hiện tại
  - **Response:** `200 OK`, `ApiResponse<List<CourseProgressResponse>>`
- `GET /api/v1/learning/progress/{userId}`
  - **Mục đích:** Lấy progress của user theo `userId`
  - **Response:** `200 OK`, `ApiResponse<List<CourseProgressResponse>>`
  - **Ghi chú:** Controller chặn xem user khác nếu caller không phải admin
- `GET /api/v1/learning/progress/activity`
  - **Mục đích:** Lấy activity summary gần đây
  - **Query / Path:** `days` mặc định là `7`
  - **Response:** `200 OK`, `ApiResponse<LessonActivitySummaryResponse>`
- `POST /api/v1/learning/progress/streak/ping`
  - **Mục đích:** Cập nhật daily streak
  - **Response:** `200 OK`, `ApiResponse<UserDailyStreak>`

## 7. Nhóm API — AI Feedback và contract nội bộ

### 7.1 Feedback học tập và integration với assessment

- `POST /api/v1/learning/ai-feedback`
  - **Mục đích:** Tạo AI feedback cho learner
  - **Body:** `AiFeedbackCreateRequest`
  - **Response:** `201 Created`, `ApiResponse<AiLearningFeedback>`
- `GET /api/v1/learning/ai-feedback`
  - **Mục đích:** Liệt kê feedback
  - **Response:** `200 OK`, `ApiResponse<List<AiFeedbackResponse>>`
  - **Ghi chú:** Admin thấy toàn bộ; learner chỉ thấy feedback của chính mình
- `PUT /api/v1/learning/ai-feedback/{id}`
  - **Mục đích:** Cập nhật feedback
  - **Body:** `AiFeedbackUpdateRequest`
  - **Response:** `200 OK`, `ApiResponse<AiFeedbackResponse>`
- `DELETE /api/v1/learning/ai-feedback/{id}`
  - **Mục đích:** Xóa feedback
  - **Response:** `200 OK`, `ApiResponse<Void>`
- `GET /api/v1/learning/internal/assessment-access`
  - **Mục đích:** Kiểm tra user có quyền vào assessment của section/lesson hay không
  - **Query / Path:** `userId`, `sectionId`, `lessonId` là optional
  - **Response:** `204 No Content` nếu được phép, `403` nếu bị từ chối
  - **Ghi chú:** Bắt buộc header `X-Internal-Token`
- `POST /api/v1/learning/internal/assessment-result`
  - **Mục đích:** Nhận kết quả assessment từ service khác
  - **Body:** `AssessmentResultSyncRequest`
  - **Response:** `202 Accepted`
  - **Ghi chú:** Hiện tại controller mới log dữ liệu sync và chưa cập nhật domain state rõ ràng ở response

## 8. Webhook / callback (nếu có)

- Không áp dụng

## 9. Hợp đồng với service khác

- Nhận JWT của auth service cho toàn bộ endpoint business
- Cung cấp internal contract cho assessment service qua `X-Internal-Token`
- Upload icon khóa học lên Supabase Storage trong flow `POST /api/v1/learning/courses`
- Hiện contract admin/learner ở controller chưa siết chặt bằng role, nên nếu website/CMS cần tách quyền cứng thì cần bổ sung auth rule ở backend

---

*Cập nhật lần cuối: 2026-04-13 — Backend team*
