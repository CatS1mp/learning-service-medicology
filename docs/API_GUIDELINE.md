# Learning Service API Guideline

## 1. Mục đích tài liệu

Tài liệu này mô tả toàn bộ endpoint hiện có trong `learning-service-medicology`, gồm:

- Công dụng của endpoint
- Input request
- Output response
- Màn hình hoặc flow nên sử dụng
- Lưu ý triển khai FE/BE

Base URL:

- Local: `http://localhost:8081`
- Production: `https://learning-service-medicology-eae21d20151f.herokuapp.com`

Swagger:

- `GET /swagger-ui.html`
- `GET /api-docs`

## 2. Authentication và quy ước chung

### 2.1 Authentication

- Tất cả endpoint dưới `/api/v1/learning/**` hiện tại đều yêu cầu JWT Bearer token.
- Chỉ các route Swagger và `/api/v1/auth/**` được mở public.

Header chuẩn:

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### 2.2 Kiểu lỗi hiện tại

Error response hiện tại:

```json
{
  "status": 500,
  "message": "Hệ thống gặp sự cố bất ngờ. Vui lòng thử lại sau!",
  "timestamp": "2026-04-07T20:42:39.6455854"
}
```

Lưu ý:

- `IllegalArgumentException` đang được map thành HTTP `401 Unauthorized`, kể cả khi lỗi thực chất là không tìm thấy dữ liệu.
- Các endpoint hiện chưa thống nhất `ApiResponse<T>`, đa số trả thẳng object hoặc list.

### 2.3 Quy ước response hiện tại

- `CourseResponse` trả course và có thể kèm `sections`
- `SectionResponse` trả section và có thể kèm `lessons`
- `LessonResponse` trả đầy đủ nội dung bài học
- `UserLessonProgressResponse` là DTO rút gọn cho màn hình progress, tránh vòng lặp dữ liệu

## 3. Tóm tắt mapping theo màn hình

| Màn hình / flow | Endpoint chính |
|---|---|
| Trang danh sách khóa học | `GET /api/v1/learning/courses` |
| Trang learning path | `GET /api/v1/learning/courses/path` |
| Trang chi tiết khóa học | `GET /api/v1/learning/courses/{courseId}` |
| Trang chi tiết section | `GET /api/v1/learning/sections/{sectionId}` |
| Trang lesson player / lesson detail | `GET /api/v1/learning/lessons/{lessonId}` |
| Danh sách lesson theo section | `GET /api/v1/learning/sections/{sectionId}/lessons` |
| Màn hình tiến độ học tập của user | `GET /api/v1/learning/progress` |
| Admin/CMS tạo sửa course | `POST/PUT/DELETE /api/v1/learning/courses...` |
| Admin/CMS tạo sửa section | `POST/PUT/DELETE /api/v1/learning/courses/{courseId}/sections`, `/api/v1/learning/sections/{sectionId}` |
| Admin/CMS tạo sửa lesson | `POST/PUT/DELETE /api/v1/learning/lessons...` |
| Màn hình AI feedback sau quiz/luyện tập | `POST /api/v1/learning/ai-feedback` |
| Admin/CMS quản lý feedback AI | `GET/PUT/DELETE /api/v1/learning/ai-feedback...` |
| Daily streak / check-in học tập | `POST /api/v1/learning/progress/streak/ping` |

## 4. Course APIs

### 4.1 GET `/api/v1/learning/courses`

Mục đích:

- Lấy toàn bộ course để hiển thị landing page học tập hoặc course catalog.

Màn hình sử dụng:

- Trang danh sách khóa học
- Trang chọn lộ trình học
- Dashboard học tập

Input:

- Không có body
- Không có query param

Output:

- `200 OK`
- Kiểu trả về: `List<CourseResponse>`

Ví dụ response:

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Tim mạch cơ bản",
    "slug": "tim-mach-co-ban",
    "description": "Khóa học nhập môn tim mạch",
    "iconFileName": "heart.png",
    "colorCode": "#EF4444",
    "orderIndex": 1,
    "sections": [
      {
        "id": "22222222-2222-2222-2222-222222222222",
        "courseId": "11111111-1111-1111-1111-111111111111",
        "name": "Nhập môn tim mạch",
        "slug": "nhap-mon-tim-mach",
        "orderIndex": 1,
        "estimatedDurationMinutes": 25,
        "lessons": []
      }
    ],
    "createdAt": "2026-04-07T10:00:00",
    "updatedAt": "2026-04-07T10:00:00"
  }
]
```

Lưu ý:

- Response có thể khá nặng vì mỗi course có lồng `sections`, và mỗi section lại có `lessons`.

### 4.2 GET `/api/v1/learning/courses/{courseId}`

Mục đích:

- Lấy chi tiết một course.

Màn hình sử dụng:

- Trang chi tiết khóa học
- Trang outline khóa học

Path param:

- `courseId: UUID`

Output:

- `200 OK`
- `CourseResponse`

Lưu ý:

- Nếu không tìm thấy course, hệ thống đang trả `401` với message kiểu `Course not found with ID: ...`

### 4.3 GET `/api/v1/learning/courses/path`

Mục đích:

- Lấy learning path tổng hợp.

Màn hình sử dụng:

- Màn hình lộ trình học
- Trang home learning

Output thực tế:

```json
{
  "courses": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "name": "Tim mạch cơ bản"
    }
  ]
}
```

Lưu ý:

- Endpoint này hiện chỉ bọc `courses` trong một object, chưa có metadata học tập riêng.

### 4.4 POST `/api/v1/learning/courses`

Mục đích:

- Tạo course mới.

Màn hình sử dụng:

- CMS/Admin tạo khóa học

Input body:

- `Content-Type: multipart/form-data`
- Các field form-data:
  - `name`: `Tim mach co ban`
  - `slug`: `tim-mach-co-ban`
  - `description`: `Khoa hoc nhap mon tim mach`
  - `colorCode`: `#EF4444`
  - `orderIndex`: `1`
  - `iconFile`: file anh nguoi dung chon (`png/jpg/...`)

Vi du request:

```text
name=Tim mach co ban
slug=tim-mach-co-ban
description=Khoa hoc nhap mon tim mach
colorCode=#EF4444
orderIndex=1
iconFile=<heart.png>
```

Output:

- `201 Created`
- `CourseResponse`
- `iconFileName` trong response la public URL cua anh tren Supabase Storage, khong con la ten file thuần.

### 4.5 PUT `/api/v1/learning/courses/{courseId}`

Mục đích:

- Cập nhật course.

Màn hình sử dụng:

- CMS/Admin sửa khóa học

Input body:

- Cùng cấu trúc `CourseRequest`

Output:

- `200 OK`
- `CourseResponse`

### 4.6 DELETE `/api/v1/learning/courses/{courseId}`

Mục đích:

- Xóa course.

Màn hình sử dụng:

- CMS/Admin quản trị khóa học

Output:

- `204 No Content`

## 5. Section APIs

### 5.1 GET `/api/v1/learning/courses/{courseId}/sections`

Mục đích:

- Lấy danh sách section thuộc một course.

Màn hình sử dụng:

- Course detail
- Sidebar nội dung khóa học

Output:

- `200 OK`
- `List<SectionResponse>`

Ví dụ response:

```json
[
  {
    "id": "22222222-2222-2222-2222-222222222222",
    "courseId": "11111111-1111-1111-1111-111111111111",
    "name": "Nhập môn tim mạch",
    "slug": "nhap-mon-tim-mach",
    "orderIndex": 1,
    "estimatedDurationMinutes": 25,
    "lessons": [
      {
        "id": "33333333-3333-3333-3333-333333333333",
        "sectionId": "22222222-2222-2222-2222-222222222222",
        "name": "Giải phẫu tim",
        "description": "Tổng quan giải phẫu tim",
        "slug": "giai-phau-tim",
        "orderIndex": 1,
        "estimatedDurationMinutes": 10,
        "difficultyLevel": "beginner",
        "isActive": true,
        "content": "{\"blocks\":[]}"
      }
    ]
  }
]
```

### 5.2 POST `/api/v1/learning/courses/{courseId}/sections`

Mục đích:

- Tạo section mới trong một course.

Màn hình sử dụng:

- CMS/Admin quản lý outline khóa học

Input body:

```json
{
  "name": "Nhập môn tim mạch",
  "slug": "nhap-mon-tim-mach",
  "orderIndex": 1,
  "estimatedDurationMinutes": 25
}
```

Lưu ý:

- `courseId` lấy từ path, không cần gửi trong body.

Output:

- `201 Created`
- `SectionResponse`

### 5.3 GET `/api/v1/learning/sections/{sectionId}`

Mục đích:

- Lấy chi tiết một section và các lesson của section đó.

Màn hình sử dụng:

- Section detail
- Nội dung chương học

Output:

- `200 OK`
- `SectionResponse`

### 5.4 PUT `/api/v1/learning/sections/{sectionId}`

Mục đích:

- Cập nhật section.

Màn hình sử dụng:

- CMS/Admin sửa section

Input body:

```json
{
  "courseId": "11111111-1111-1111-1111-111111111111",
  "name": "Nhập môn tim mạch",
  "slug": "nhap-mon-tim-mach",
  "orderIndex": 1,
  "estimatedDurationMinutes": 25
}
```

Lưu ý:

- Khi update phải truyền `courseId` trong body.

### 5.5 DELETE `/api/v1/learning/sections/{sectionId}`

Mục đích:

- Xóa section.

Màn hình sử dụng:

- CMS/Admin

Output:

- `204 No Content`

## 6. Lesson APIs

### 6.1 GET `/api/v1/learning/sections/{sectionId}/lessons`

Mục đích:

- Lấy danh sách lesson theo section.

Màn hình sử dụng:

- Lesson list trong section
- Sidebar lesson

Output:

- `200 OK`
- `List<LessonResponse>`

### 6.2 GET `/api/v1/learning/lessons/{lessonId}`

Mục đích:

- Lấy chi tiết lesson.

Màn hình sử dụng:

- Lesson player
- Lesson reading screen
- Quiz preparation screen

Output:

- `200 OK`
- `LessonResponse`

Ví dụ response:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "sectionId": "22222222-2222-2222-2222-222222222222",
  "name": "Giải phẫu tim",
  "description": "Tổng quan giải phẫu tim",
  "slug": "giai-phau-tim",
  "orderIndex": 1,
  "estimatedDurationMinutes": 10,
  "difficultyLevel": "beginner",
  "isActive": true,
  "content": "{\"blocks\":[]}",
  "createdAt": "2026-04-07T10:00:00",
  "updatedAt": "2026-04-07T10:00:00"
}
```

### 6.3 POST `/api/v1/learning/lessons`

Mục đích:

- Tạo lesson mới.

Màn hình sử dụng:

- CMS/Admin tạo lesson

Input body:

```json
{
  "sectionId": "22222222-2222-2222-2222-222222222222",
  "name": "Giải phẫu tim",
  "description": "Tổng quan giải phẫu tim",
  "slug": "giai-phau-tim",
  "orderIndex": 1,
  "estimatedDurationMinutes": 10,
  "difficultyLevel": "beginner",
  "isActive": true,
  "content": "{\"blocks\":[]}"
}
```

Output:

- `201 Created`
- `LessonResponse`

### 6.4 POST `/api/v1/learning/sections/{sectionId}/lessons`

Mục đích:

- Tạo lesson mới bằng cách truyền `sectionId` trên URL.

Màn hình sử dụng:

- CMS/Admin khi đang đứng trong trang quản lý section cụ thể

Input body:

```json
{
  "name": "Giải phẫu tim",
  "description": "Tổng quan giải phẫu tim",
  "slug": "giai-phau-tim",
  "orderIndex": 1,
  "estimatedDurationMinutes": 10,
  "difficultyLevel": "beginner",
  "isActive": true,
  "content": "{\"blocks\":[]}"
}
```

Lưu ý:

- Nếu body có `sectionId`, controller sẽ override bằng path param.

### 6.5 PUT `/api/v1/learning/lessons/{lessonId}`

Mục đích:

- Cập nhật lesson.

Màn hình sử dụng:

- CMS/Admin sửa lesson

Input body:

- Cùng cấu trúc `LessonRequest`

### 6.6 PATCH `/api/v1/learning/lessons/{lessonId}/status`

Mục đích:

- Bật/tắt trạng thái active của lesson.

Màn hình sử dụng:

- CMS/Admin publish/unpublish lesson

Input body:

```json
{
  "isActive": false
}
```

Output:

- `200 OK`
- `LessonResponse`

### 6.7 DELETE `/api/v1/learning/lessons/{lessonId}`

Mục đích:

- Xóa lesson.

Màn hình sử dụng:

- CMS/Admin

Output:

- `204 No Content`

### 6.8 POST `/api/v1/learning/lessons/{lessonId}/enroll`

Mục đích:

- Theo tên gọi là enroll lesson cho user hiện tại.

Màn hình sử dụng dự kiến:

- Nút bắt đầu học
- CTA enroll lesson

Output thực tế hiện tại:

- `200 OK`
- Chỉ trả `LessonResponse` của lesson
- Chưa ghi nhận enrollment xuống database

Lưu ý quan trọng:

- Endpoint này hiện chưa thực hiện nghiệp vụ enroll thật.
- FE không nên dùng endpoint này như một nguồn sự thật cho trạng thái đã enroll.

### 6.9 POST `/api/v1/learning/courses/{lessonId}/enroll`

Mục đích:

- Alias của endpoint enroll bên trên.

Lưu ý quan trọng:

- Path đang dùng tên `courses/{lessonId}/enroll`, dễ gây nhầm lẫn vì param thực chất là `lessonId`.
- Nên xem đây là route legacy/không khuyến khích dùng cho FE mới.

## 7. Progress APIs

### 7.1 GET `/api/v1/learning/progress`

Mục đích:

- Lấy danh sách course chưa hoàn thành mà user đã có học dở.
- Dùng cho FE hiển thị card progress theo course.

Màn hình sử dụng:

- Trang My Learning
- Continue learning theo course
- Dashboard học tập cá nhân

Output:

- `200 OK`
- `List<CourseProgressResponse>`

Ví dụ response:

```json
[
  {
    "courseId": "11111111-1111-1111-1111-111111111111",
    "courseName": "Tim mạch cơ bản",
    "courseSlug": "tim-mach-co-ban",
    "lastStudiedAt": "2026-04-07T20:42:39",
    "completionPercent": 35
  }
]
```

Lưu ý:

- Kết quả được nhóm theo course, không còn trả progress theo lesson.
- Chỉ trả các course chưa hoàn thành.
- Trong code hiện tại, do chưa có enrollment thật từ `UserCourse`, "đã đăng ký" đang được hiểu là user đã có ít nhất một `UserLesson` trong course đó.

### 7.2 GET `/api/v1/learning/progress/{userId}`

Mục đích:

- Lấy course progress summary của một user theo `userId`.

Màn hình sử dụng:

- Admin support
- Internal dashboard

Lưu ý:

- Vì vẫn yêu cầu JWT, endpoint này phù hợp hơn cho công cụ nội bộ/admin.

### 7.3 POST `/api/v1/learning/progress/streak/ping`

Mục đích:

- Ghi nhận user có hoạt động học trong ngày và cập nhật streak.

Màn hình sử dụng:

- Sau khi hoàn thành lesson
- Khi mở app và bắt đầu phiên học
- Sau khi submit quiz hoặc tương tác học hợp lệ

Output:

- `200 OK`
- Trả object `UserDailyStreak`

Ví dụ response:

```json
{
  "userId": "11111111-1111-1111-1111-111111111001",
  "currentStreak": 5,
  "longestStreak": 8,
  "lastActivityDate": "2026-04-07",
  "streakStartedAt": "2026-04-03",
  "totalActiveDays": 12,
  "createdAt": "2026-04-01T08:00:00",
  "updatedAt": "2026-04-07T20:42:39"
}
```

Lưu ý:

- Nếu gọi nhiều lần trong cùng ngày, logic hiện tại không tăng thêm streak.

## 8. AI Feedback APIs

### 8.1 POST `/api/v1/learning/ai-feedback`

Mục đích:

- Sinh feedback AI cho câu trả lời của user.

Màn hình sử dụng:

- Quiz review
- Practice answer review
- Self-learning explanation popup

Input body thực tế:

```json
{
  "referenceId": "33333333-3333-3333-3333-333333333333",
  "referenceType": "LESSON",
  "questionContent": "Tim có bao nhiêu buồng?",
  "userAnswer": "2 buồng",
  "isCorrect": false
}
```

Giải thích field:

- `referenceId`: ID lesson/question/reference liên quan
- `referenceType`: kiểu reference, hiện là string tự do
- `questionContent`: nội dung câu hỏi
- `userAnswer`: câu trả lời của user
- `isCorrect`: backend dựa vào kết quả này để sinh giải thích đúng/sai

Output thực tế:

- `200 OK`
- Trả entity `AiLearningFeedback`

Ví dụ response:

```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "userId": "11111111-1111-1111-1111-111111111001",
  "referenceId": "33333333-3333-3333-3333-333333333333",
  "referenceType": "LESSON",
  "questionContent": "Tim có bao nhiêu buồng?",
  "userAnswer": "2 buồng",
  "isCorrect": false,
  "aiExplanation": "[Mock AI] Câu trả lời của bạn chưa đủ ý sách giáo khoa. Hãy xem lại chương Hệ Hô Hấp.",
  "createdAt": "2026-04-07T20:50:00"
}
```

Lưu ý quan trọng:

- Endpoint đang dùng `Map<String, Object>` thay vì DTO typed.
- `aiExplanation` hiện là mock text, chưa gọi AI thật.
- FE nên xem đây là API stub/placeholder.

### 8.2 GET `/api/v1/learning/ai-feedback`

Mục đích:

- Lấy toàn bộ feedback AI đã lưu.

Màn hình sử dụng:

- Admin review feedback
- Lịch sử giải thích AI

Output:

- `200 OK`
- `List<AiFeedbackResponse>`

### 8.3 PUT `/api/v1/learning/ai-feedback/{id}`

Mục đích:

- Sửa nội dung feedback AI hoặc trạng thái đúng/sai.

Màn hình sử dụng:

- Admin/CMS chỉnh feedback

Input body:

```json
{
  "aiExplanation": "Giải thích cập nhật",
  "isCorrect": true
}
```

Output:

- `200 OK`
- `AiFeedbackResponse`

### 8.4 DELETE `/api/v1/learning/ai-feedback/{id}`

Mục đích:

- Xóa feedback AI.

Màn hình sử dụng:

- Admin/CMS

Output:

- `204 No Content`

## 9. Root và Theme

### 9.1 GET `/`

Mục đích:

- Redirect root path sang Swagger UI.

Output:

- Redirect tới `/swagger-ui/index.html`

### 9.2 Theme APIs

Trạng thái hiện tại:

- `ThemeController.java` là file legacy rỗng
- `ThemeRequest.java` và `ThemeResponse.java` hiện không có endpoint active tương ứng

Kết luận:

- Hiện tại service này chưa publish API theme để FE sử dụng

## 10. Đề xuất dùng cho FE

### 10.1 Cho app người học

Nên dùng:

- `GET /api/v1/learning/courses`
- `GET /api/v1/learning/courses/{courseId}`
- `GET /api/v1/learning/sections/{sectionId}`
- `GET /api/v1/learning/lessons/{lessonId}`
- `GET /api/v1/learning/progress`
- `POST /api/v1/learning/progress/streak/ping`
- `POST /api/v1/learning/ai-feedback`

Không nên phụ thuộc mạnh vào:

- `POST /api/v1/learning/lessons/{lessonId}/enroll`
- `POST /api/v1/learning/courses/{lessonId}/enroll`

### 10.2 Cho CMS/Admin

Nên dùng:

- Tất cả endpoint `POST/PUT/DELETE` của course/section/lesson
- `GET /api/v1/learning/ai-feedback`
- `PUT /api/v1/learning/ai-feedback/{id}`
- `DELETE /api/v1/learning/ai-feedback/{id}`

## 11. Known Issues / Caveats

- Response format chưa thống nhất theo `ApiResponse<T>`
- `IllegalArgumentException` đang trả `401` thay vì `400` hoặc `404`
- `POST /ai-feedback` trả entity, không phải DTO
- `POST /ai-feedback` hiện dùng mock AI response
- Route `POST /courses/{lessonId}/enroll` bị đặt tên path gây hiểu nhầm
- Route enroll hiện chưa lưu enrollment thực tế
- `GET /courses` và `GET /courses/{id}` có thể trả payload sâu vì lồng section và lesson
- `Theme` chưa có API active dù vẫn còn request/response class

## 12. Gợi ý chuẩn hóa sau này

- Chuẩn hóa toàn bộ response về `ApiResponse<T>`
- Tách DTO riêng cho toàn bộ endpoint, không trả entity trực tiếp
- Chuẩn hóa mã lỗi: `400`, `404`, `401`, `403`, `500`
- Đổi `POST /ai-feedback` sang DTO request typed
- Tách rõ API learner và API admin
- Sửa hoặc bỏ route enroll legacy gây nhầm lẫn
