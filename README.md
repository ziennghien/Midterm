# 📚 StudentManagementApp

Ứng dụng quản lý sinh viên và nhân sự dành cho các vai trò **Admin**, **Manager**, và **Employee**, được xây dựng trên nền tảng Android sử dụng **Firebase** làm backend.

---

## 🚀 Chức năng chính

### ✔️ Authentication (Xác thực tài khoản)
- Đăng nhập theo vai trò: `admin`, `manager`, `employee`
- Giao diện phân quyền sau khi đăng nhập
- Reset mật khẩu qua email (Firebase hỗ trợ gửi email tự động)

### ✔️ Quản lý người dùng
- Thêm, sửa, xoá người dùng (chỉ admin và manager)
- Gán vai trò (`admin`, `manager`, `employee`)
- Ẩn chức năng chỉnh sửa với tài khoản `admin` và `employee` để bảo mật

### ✔️ Quản lý sinh viên
- Thêm, sửa, xoá sinh viên (theo quyền)

### ✔️ Quản lý lịch sử đăng nhập
- Ghi lại thời gian đăng nhập của người dùng
- Hiển thị avatar, ID, role, timestamp login

### ✔️ Hồ sơ cá nhân (Profile)
- Người dùng có thể xem và chỉnh sửa thông tin cá nhân (tuỳ vai trò)
- Thay đổi ảnh đại diện

---

## 🔧 Công nghệ sử dụng

| Công nghệ | Vai trò |
|----------|---------|
| **Java (Android)** | Phát triển ứng dụng mobile |
| **Firebase Authentication** | Xác thực người dùng, reset mật khẩu |
| **Firebase Realtime Database** | Lưu trữ dữ liệu người dùng, sinh viên, login history |
| **Firebase Storage** | Lưu trữ ảnh đại diện |
| **Glide** | Load ảnh nhanh từ URL |
| **Material Design** | Giao diện thân thiện, hiện đại |

---

## 👥 Tài khoản mẫu để đăng nhập

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@gmail.com` | `admin123` |
| **Manager** | `manager@gmail.com` | `manager123` |
| **Employee** | `employee@gmail.com` | `employee123` |

---

## 📷 Demo
- 
---

## 📌 Lưu ý
- Tài khoản `admin` không thể bị xoá hoặc chỉnh sửa thông tin bởi admin khác.
- Tài khoản `employee` chỉ được phép xem thông tin, thay ảnh đại diện và đặt lại mật khẩu.
- Khi tạo user mới, hệ thống sẽ tạo tài khoản Firebase Auth và lưu thông tin tương ứng vào Realtime Database.
- Để thử chức năng Reset Password, vui lòng tạo tài khoản với Username là email còn đang sử dụng để có thể nhận được link Reset Password
- Realtime Database được thiết lập cho việc đọc và ghi cho đến hết ngày 12/6/2025. Nếu sau thời gian trên, vui lòng liên hệ nhóm phát triển để thiết lập lại thời gian
---

## 🛠️ Cài đặt và chạy dự án

1. Clone dự án về máy:
   ```bash
   git clone https://github.com/ziennghien/Midterm.git

## Thông tin liên lạc
- Nguyễn Mỹ Duyên (Nhóm trưởng): 52200190@student.tdtu.edu.vn
- Nguyễn Minh Khánh (Thành viên): 52200187@student.tdtu.edu.vn
- Lê Thành Khang (Thành viên): 52200161@student.tdtu.edu.vn