# BÀI TẬP 1: SỬA LỖI HARDCODE URL TRONG RESTTEMPLATE CỦA ORDER-SERVICE


1) Ba lỗi và hậu quả
- Lỗi 1: Không dùng @LoadBalanced RestTemplate
  - Hậu quả: Không thể resolve service-id (ví dụ `http://product-service`) qua Spring Cloud LoadBalancer/Eureka. Trong production khi chỉ dùng service-id, yêu cầu sẽ không tìm được địa chỉ instance.
- Lỗi 2: Hardcode IP:port thay vì dùng service-id
  - Hậu quả: Không có load-balancing, không chịu thay đổi khi deploy nhiều instance hoặc khi IP thay đổi (staging/production). Dễ gây single point of failure.
- Lỗi 3: Không cấu hình timeout (connect/read)
  - Hậu quả: Gọi mạng có thể block thread vô thời hạn khi mạng chậm hoặc service đơ, dẫn tới cạn thread pool và giảm khả năng phục vụ.

2) Các sửa đã thực hiện
- Thêm bean RestTemplate có @LoadBalanced để dùng service-id (hợp tác với Spring Cloud LoadBalancer / Eureka).
- Cấu hình timeouts: connectTimeout = 2000 ms, readTimeout = 3000 ms.
- Thay URL hardcode bằng URL dùng service-id: `http://product-service/api/products/{id}`.
- Xử lý lỗi phân biệt trong ProductServiceClientRT:
  - ResourceAccessException (thường do timeout/I/O) → trả về fallback ProductInfo (id, "UNKNOWN", 0)
  - HttpClientErrorException.NotFound (404) → ném ProductNotFoundException để caller xử lý
  - Các RestClientException khác → trả về fallback
