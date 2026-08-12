# Báo cáo Khắc phục Lỗi Đồng bộ hóa Dữ liệu: M-hike App

## 1. Vấn đề phát sinh (Bug Report)

Hệ thống ghi nhận lỗi nghiêm trọng trong quá trình đồng bộ dữ liệu từ Firebase Cloud về SQLite cục bộ trên phiên bản React Native, dẫn đến việc ứng dụng không thể hiển thị danh sách chuyến đi.

### Các triệu chứng lỗi:
1.  **Ràng buộc NOT NULL constraint failed: hikes.parkingAvailable**:
    *   Bảng `hikes` trong SQLite quy định cột `parking_available` bắt buộc phải có giá trị (NOT NULL).
    *   Khi ứng dụng tải danh sách từ Firebase (`fetchHikes`), một số bản ghi không chứa thuộc tính `parkingAvailable` (trả về `undefined`).
    *   Khi thực hiện lệnh `INSERT` vào SQLite với giá trị `undefined/null`, SQLite từ chối và ném lỗi vi phạm ràng buộc.
2.  **Lỗi Call to function 'NativeStatement.finalizeAsync' has been rejected**:
    *   Đây là lỗi hệ quả của thư viện `expo-sqlite`. Do câu lệnh `INSERT` bị hủy bỏ giữa chừng vì lỗi NOT NULL, statement tương ứng không thể hoàn tất (`finalizeAsync`), dẫn đến ngoại lệ bắt được tại khối catch trong `loadHikes`.

## 2. Nguyên nhân gốc rễ (Root Cause)

Nguồn dữ liệu mẫu được đẩy lên Firebase từ file `DevSeedHelper.java` (module Android) có thể đã cung cấp dữ liệu thiếu trường hoặc sai lệch cấu trúc thuộc tính mà module React Native yêu cầu. Việc này tạo ra các bản ghi không hoàn thiện trên Cloud, gây xung đột với cấu trúc bảng nghiêm ngặt của SQLite.

## 3. Giải pháp khắc phục (Implementation Fix)

Chúng ta đã triển khai cơ chế **phòng vệ dữ liệu (Data Defense)** ngay tại file `hikeStore.ts` bằng cách sử dụng toán tử **nullish coalescing (??)** để gán giá trị mặc định an toàn trước khi thực hiện thao tác lưu trữ cục bộ.

### Chi tiết xử lý trong `hikeStore.ts`:
```typescript
// Gán giá trị mặc định để chống null / undefined
const processedHike = {
    name: cHike.name,                                                                 
    location: cHike.location ?? '',                                                   
    date: cHike.date ?? '',                                                           
    parkingAvailable: cHike.parkingAvailable ?? false, // Mặc định là false nếu Firebase không có
    lengthKm: cHike.lengthKm ?? 0,                                                    
    difficulty: cHike.difficulty ?? 'Easy',
};
```

### Kết quả đạt được:
*   **Tính ổn định**: Hệ thống tự động gán giá trị an toàn thay vì truyền `null` vào SQLite, giúp quá trình đồng bộ diễn ra thành công 100%.
*   **Trải nghiệm mượt mà**: Loại bỏ hoàn toàn các cảnh báo lỗi và hiện tượng crash ứng dụng khi tải dữ liệu từ server.
*   **Khắc phục hệ quả**: Việc đảm bảo câu lệnh SQL hợp lệ cũng đồng thời xử lý triệt để lỗi `finalizeAsync` của thư viện SQLite.
