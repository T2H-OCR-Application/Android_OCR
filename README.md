# 📄 Android OCR — Ứng dụng Quét & Xử lý Tài liệu thông minh

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?logo=android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple" />
  <img src="https://img.shields.io/badge/AI-Gemini%202.5%20Flash-orange?logo=google" />
  <img src="https://img.shields.io/badge/minSdk-24-red" />
</p>

---

## 📖 Giới thiệu

**Android OCR** là ứng dụng di động Android giúp người dùng **quét, nhận diện và xử lý văn bản** từ tài liệu giấy hoặc file ảnh một cách nhanh chóng và thông minh. Ứng dụng tích hợp công nghệ AI (Google Gemini) để tóm tắt nội dung, hỗ trợ dịch thuật, và đồng bộ dữ liệu lên Google Drive — tất cả trong một giao diện tối giản, hiện đại.

Dự án được xây dựng trong khuôn khổ môn học tại trường, áp dụng kiến trúc **MVVM + Clean Architecture** và các công nghệ Android hiện đại nhất.

---

## ✨ Tính năng

### 📷 Quét tài liệu
- Chụp ảnh tài liệu trực tiếp bằng camera hoặc chọn từ thư viện ảnh
- Tự động phát hiện góc tài liệu và cắt phối cảnh (perspective warp) bằng **OpenCV**
- Hỗ trợ quét nhiều trang, xem lại và xoá từng trang trước khi lưu

### 🔍 Nhận diện văn bản (OCR)
- Nhận diện văn bản từ ảnh đã quét bằng **Google ML Kit Text Recognition**
- Hỗ trợ đa ngôn ngữ (tiếng Việt, tiếng Anh, tiếng Nhật, v.v.)
- Kết quả OCR có thể chỉnh sửa trực tiếp trong ứng dụng

### 🤖 Tóm tắt AI
- Gửi nội dung văn bản lên **Gemini 2.5 Flash** để tóm tắt tự động
- AI tự nhận diện ngôn ngữ, giữ nguyên ngôn ngữ gốc trong bản tóm tắt
- Độ dài tóm tắt khoảng 5–10% so với văn bản gốc, giữ đầy đủ ý chính
- Người dùng có thể **chỉnh sửa** kết quả tóm tắt trước khi lưu
- Lưu kết quả tóm tắt thành file **PDF** và đồng bộ lên Google Drive

### ☁️ Đồng bộ Google Drive
- Tự động upload file PDF scan và PDF tóm tắt lên thư mục riêng trên Google Drive
- Hiển thị trạng thái đồng bộ (đã lên cloud / chỉ lưu cục bộ)
- Xoá file cùng lúc trên máy và trên Drive

### 👤 Xác thực người dùng
- Đăng nhập bằng **Email/Password** hoặc **Google Sign-In**
- Hỗ trợ đăng nhập ẩn danh (anonymous) cho người dùng chưa có tài khoản
- Quản lý hồ sơ cá nhân, đổi mật khẩu, liên kết tài khoản Google

### 📁 Quản lý tài liệu
- Xem toàn bộ file scan và file tóm tắt đã lưu
- Tìm kiếm tài liệu theo tên
- Xem chi tiết nội dung OCR và nội dung tóm tắt
- Xoá tài liệu (xoá cả file local và file Drive cùng lúc)

### ⚙️ Cài đặt
- Nhập và lưu **Gemini API Key** cá nhân để dùng tính năng AI
- Kiểm tra trạng thái kết nối và hợp lệ của API Key

---

## 🛠️ Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| UI Framework | Jetpack Compose (Material 3) |
| Kiến trúc | MVVM + Clean Architecture |
| AI / LLM | Google Gemini 2.5 Flash (REST API) |
| OCR | Google ML Kit Text Recognition |
| Xử lý ảnh | OpenCV for Android |
| Xác thực | Firebase Authentication |
| Cơ sở dữ liệu | Firebase Firestore, JSON local storage |
| Lưu trữ cài đặt | Jetpack DataStore |
| Đồng bộ cloud | Google Drive API v3 |
| Tạo PDF | PDFBox Android (PdfGenerator) |
| Background task | WorkManager |
| Navigation | Manual sealed class `Screen` (AnimatedContent) |
| Dependency | Gradle Version Catalog (libs.versions.toml) |

---

## 🚀 Cài đặt & Chạy ứng dụng

### Yêu cầu
- Android Studio Hedgehog trở lên
- JDK 17
- Android SDK 24+
- Tài khoản Google (để dùng Firebase và Google Drive)

### Các bước cài đặt

**1. Clone repository**
```bash
git clone https://github.com/<your-username>/Android_OCR.git
cd Android_OCR
```

**2. Cấu hình Firebase**
- Vào [Firebase Console](https://console.firebase.google.com), tạo project
- Tải file `google-services.json` và đặt vào thư mục `app/`
- Bật **Firebase Authentication** (Email/Password + Google)
- Bật **Cloud Firestore**

**3. Cấu hình Google Drive API**
- Vào [Google Cloud Console](https://console.cloud.google.com), bật **Google Drive API**
- Tạo OAuth 2.0 credentials cho Android app (package name: `com.t2h.ocr`)

**4. Thêm Gemini API Key**
- Lấy API Key tại [Google AI Studio](https://aistudio.google.com)
- Tạo file `local.properties` ở thư mục gốc (nếu chưa có) và thêm:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
```

**5. Build và chạy**
- Mở project bằng Android Studio
- Sync Gradle
- Chọn thiết bị (máy thật hoặc giả lập API 24+)
- Nhấn **Run** ▶️

### Cách sử dụng

1. Mở app → Đăng nhập hoặc tiếp tục ẩn danh
2. Nhấn nút **Quét** để chụp tài liệu
3. Căn chỉnh góc → Xác nhận → App tự động OCR
4. Xem và chỉnh sửa kết quả văn bản → Lưu
5. Vào tab **AI** → Chọn tài liệu → Nhận bản tóm tắt từ Gemini
6. Chỉnh sửa nếu muốn → Lưu PDF → Tự động đồng bộ Drive

> ⚠️ Để dùng tính năng **Tóm tắt AI**, cần nhập Gemini API Key trong phần **Cài đặt → Cấu hình AI**

---

## 👥 Thành viên & Giảng viên

Dự án được xây dựng và phát triển bởi sinh viên **Đại học Công nghiệp Hà Nội — K18**:

**Giảng viên hướng dẫn:**
- ThS. Vũ Duy Giang

**Sinh viên thực hiện:**
- 2023600859 — Tô Tuấn Huy
- 2023602201 — Nguyễn Văn Trung
- 2023602173 — Nguyễn Minh Tuấn

---

<p align="center">Made with ❤️ by nhóm Android OCR — HaUI K18</p>
