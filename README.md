# G-Scores

Ứng dụng tra cứu kết quả kỳ thi tốt nghiệp THPT 2024. Giao diện dùng Thymeleaf và Chart.js. Spring Data JPA truy vấn PostgreSQL, còn Flyway quản lý cấu trúc bảng.

## Chạy local

Yêu cầu JDK 21 trở lên và PostgreSQL đang chạy. Maven Wrapper nằm sẵn trong repo. Các lệnh dưới đây dùng PowerShell tại thư mục gốc của project. Trên macOS hoặc Linux, dùng `./mvnw` và `export` để đặt biến môi trường.

1. Tạo database bằng tài khoản quản trị PostgreSQL. Thay mật khẩu ví dụ trước khi chạy.

   ```sql
   CREATE ROLE gscores LOGIN PASSWORD 'your-local-password';
   CREATE DATABASE gscores OWNER gscores;
   CREATE DATABASE gscores_test OWNER gscores;
   ```

2. Tải [CSV gốc của Golden Owl](https://github.com/GoldenOwlAsia/webdev-intern-assignment-3/blob/main/dataset/diem_thi_thpt_2024.csv) bằng nút **Raw** và lưu vào `dataset/diem_thi_thpt_2024.csv`. Cấu hình kết nối và đường dẫn CSV.

   ```powershell
   $env:GSCORES_DB_URL = 'jdbc:postgresql://localhost:5432/gscores'
   $env:GSCORES_DB_USER = 'gscores'
   $env:GSCORES_DB_PASSWORD = 'your-local-password'
   $env:GSCORES_CSV = (Resolve-Path 'dataset/diem_thi_thpt_2024.csv').Path
   ```

3. Đóng gói và chạy seed. Flyway tạo bảng trước khi seeder nhập dữ liệu. Lần nhập thành công báo `Imported 1061605 exam results.`. Chạy lại trên database đã seed sẽ báo bỏ qua.

   ```powershell
   .\mvnw.cmd -DskipTests package
   java -jar target/g-scores-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed
   ```

4. Chạy ứng dụng và mở [G-Scores](http://localhost:8080/).

   ```powershell
   java -jar target/g-scores-0.0.1-SNAPSHOT.jar
   ```

   | Trang | Thao tác thử |
   | --- | --- |
   | `/` | Tra cứu `01000001`. Kết quả Toán là `8.4` và Lịch sử hiển thị `—`. |
   | `/reports` | Xem phân bố điểm Toán, sau đó chọn Vật lí và bấm **Xem báo cáo**. |
   | `/top-10` | Kiểm tra bảng xếp hạng theo tổng Toán, Vật lí và Hóa học. |

## Kiểm thử

Dùng database kiểm thử riêng và để bảng `exam_results` trống. Bộ test tự chạy migration và rollback dữ liệu mẫu sau mỗi test.

```powershell
$env:GSCORES_TEST_DB_URL = 'jdbc:postgresql://localhost:5432/gscores_test'
.\mvnw.cmd test
```

## Demo

[Video demo ba màn hình](docs/demo.mp4)
