# G-Scores

Ứng dụng tra cứu kết quả kỳ thi tốt nghiệp THPT 2024. Giao diện dùng Thymeleaf và Chart.js. Spring Data JPA truy vấn PostgreSQL, còn Flyway quản lý cấu trúc bảng.

[Demo online trên Railway](https://g-scores-production-299a.up.railway.app/)

## Chạy bằng Docker Compose

Cài Docker Desktop và bật Linux containers. Tải CSV gốc vào `dataset/diem_thi_thpt_2024.csv` như hướng dẫn bên dưới. Thực hiện các lệnh trong thư mục gốc của repo.

1. Nếu chưa có `.env`, tạo file cấu hình local, mở file và đặt `POSTGRES_PASSWORD` thành mật khẩu của bạn. Nếu đã có `.env`, dùng lại file hiện tại. Cổng web mặc định là `8082`, có thể đổi bằng `GSCORES_HTTP_PORT`.

   ```powershell
   Copy-Item .env.example .env
   ```

2. Build image ứng dụng. Docker tự build bằng JDK 21 và Maven Wrapper, sau đó tạo image chạy bằng JRE 21.

   ```powershell
   docker compose build app
   ```

3. Nhập CSV. Compose khởi động PostgreSQL, chờ healthcheck thành công rồi chạy seeder. Khi hoàn tất, seeder báo `Imported 1061605 exam results.` và thoát. Có thể chạy lại lệnh này để xác nhận seeder bỏ qua dữ liệu đã có.

   ```powershell
   docker compose run --rm seed
   ```

4. Khởi động web và theo dõi log. Khi xuất hiện `Tomcat started`, mở [localhost:8082](http://localhost:8082/).

   ```powershell
   docker compose up -d app
   docker compose logs -f app
   ```

   Nhấn `Ctrl+C` để thoát màn hình log. Container vẫn chạy nền. Xem trạng thái bằng `docker compose ps`.

PostgreSQL trong Compose lưu dữ liệu vào named volume. Sau khi đã khởi tạo volume, dùng lại mật khẩu trong `.env` ở các lần chạy sau. Thay giá trị trong `.env` sẽ không tự đổi mật khẩu role đã tồn tại. Image PostgreSQL 18 lưu dữ liệu dưới `/var/lib/postgresql/18/docker`, nên volume được gắn tại `/var/lib/postgresql`. [Tài liệu image PostgreSQL](https://hub.docker.com/_/postgres)

Dừng bộ container bằng `docker compose down`, rồi chạy lại bằng `docker compose up -d app`. Dữ liệu đã seed vẫn còn trong volume. Có thể kiểm tra số dòng bằng lệnh sau.

```powershell
docker compose exec db psql -U gscores -d gscores -c "SELECT count(*) FROM exam_results;"
```

Sau khi sửa source, chạy `docker compose up -d --build app` để build và thay container ứng dụng. Nếu báo không kết nối được Docker daemon hoặc Docker đang paused, mở Docker Desktop và Resume trước khi chạy lại. Nếu cổng web đang bận, đổi `GSCORES_HTTP_PORT` trong `.env`, rồi chạy lại `docker compose up -d app`.

## Chạy local bằng Java

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
   java "-Duser.timezone=UTC" -jar target/g-scores-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed
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
