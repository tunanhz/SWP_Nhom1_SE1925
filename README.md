# SWP_Nhom1_SE1925
MỤC LỤC
Giới thiệu tổng quan
Kiến trúc hệ thống & Công nghệ sử dụng
Phân quyền & Vai trò người dùng
Workflow tổng thể
Chi tiết module & hướng dẫn sử dụng từng vai trò
Admin System
Admin Business
Doctor
Pharmacist
Receptionist
Patient
Chi tiết các trang giao diện chính
API Endpoint & Ví dụ request/response
Cấu trúc Database
Validation, Session, Error Handling
Troubleshooting & FAQ
Cài đặt & Deploy
Đóng góp & Liên hệ
GIỚI THIỆU TỔNG QUAN
Hệ thống quản lý phòng khám đa khoa hỗ trợ toàn diện các nghiệp vụ: quản lý bệnh nhân, đặt lịch hẹn, khám bệnh, chỉ định dịch vụ cận lâm sàng, quản lý kết quả xét nghiệm, quản lý đơn thuốc, quản lý nhân sự (bác sĩ, dược sĩ, nhân viên tiếp tân, quản trị viên), quản lý kho thuốc, hóa đơn, thông báo nội bộ, v.v. Hệ thống hướng tới workflow khép kín, hiện đại, thân thiện với người dùng và hỗ trợ phân quyền chi tiết.

KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ SỬ DỤNG
Backend:
Java 17, Jakarta EE (Servlet API)
Maven (quản lý phụ thuộc)
JDBC, SQL Server (CSDL)
Websocket (thông báo nội bộ)
Thư viện: Jackson, Gson, Lombok, JUnit, javax.mail, org.json
Frontend:
HTML5, CSS3, JavaScript (ES6)
Bootstrap, SweetAlert2, FontAwesome, Flaticon
Các thư viện JS: FullCalendar, SwiperSlider, ApexCharts, v.v.
Khác:
Cấu trúc đa tầng: Controller, DAO, DTO, Model
RESTful API, AJAX
Sơ đồ kiến trúc tổng thể
[Patient] <-> [Receptionist] <-> [Doctor] <-> [Pharmacist]
      |             |               |             |
      v             v               v             v
   [Web UI] <-> [Servlet API] <-> [Database] <-> [Websocket]
PHÂN QUYỀN & VAI TRÒ NGƯỜI DÙNG
Vai trò	Chức năng chính
Admin System	Quản lý tài khoản, phân quyền, cấu hình hệ thống, quản lý nhân sự
Admin Business	Quản lý doanh thu, hóa đơn, báo cáo, thống kê
Doctor	Khám bệnh, chỉ định dịch vụ, xem lịch hẹn, nhập kết quả, kê đơn
Pharmacist	Quản lý đơn thuốc, kiểm tra kho, xác nhận phát thuốc
Receptionist	Quản lý lịch hẹn, xác nhận bệnh nhân, hỗ trợ nhập liệu
Patient	Đăng ký, đặt lịch, xem lịch sử khám, nhận kết quả, thanh toán
WORKFLOW TỔNG THỂ
Bệnh nhân đăng ký tài khoản, đặt lịch khám.
Tiếp tân xác nhận lịch, quản lý danh sách chờ.
Bác sĩ xem danh sách hẹn, khám bệnh, chỉ định dịch vụ cận lâm sàng, kê đơn.
Bác sĩ thực hiện dịch vụ nhập kết quả xét nghiệm.
Dược sĩ xác nhận đơn thuốc, phát thuốc, kiểm tra kho.
Admin quản lý tài khoản, phân quyền, báo cáo, hóa đơn.
Thông báo nội bộ gửi qua websocket đến từng vai trò.
CHI TIẾT MODULE & HƯỚNG DẪN SỬ DỤNG TỪNG VAI TRÒ
ADMIN SYSTEM
Quản lý tài khoản (bác sĩ, dược sĩ, tiếp tân, bệnh nhân)
Phân quyền, reset mật khẩu, khóa/mở tài khoản
Quản lý phòng ban, cấu hình hệ thống
Xem báo cáo tổng hợp
Trang chính: home-adminsys.html, listdoctors-adminsys.html, listpharmacist-adminsys.html, listrecep-adminsys.html, listpatient-adminsys.html
Hướng dẫn:
Đăng nhập với tài khoản Admin System
Truy cập dashboard, chọn module quản lý tương ứng
Thêm/sửa/xóa tài khoản, phân quyền, xuất báo cáo
ADMIN BUSINESS
Quản lý hóa đơn, doanh thu, báo cáo tài chính
Xem thống kê dịch vụ, bệnh nhân, doanh thu theo thời gian
Trang chính: home-adminbusiness.html, invoice-adminbusiness.html, report-adminbusiness.html
Hướng dẫn:
Đăng nhập với tài khoản Admin Business
Truy cập các trang báo cáo, lọc theo thời gian, xuất file
DOCTOR
Xem danh sách cuộc hẹn, chọn bệnh nhân để khám
Nhập triệu chứng, chẩn đoán, chỉ định dịch vụ cận lâm sàng
Chọn bác sĩ thực hiện dịch vụ (nếu là dịch vụ cận lâm sàng)
Xem lịch sử khám, nhập kết quả xét nghiệm (nếu được giao)
Kê đơn thuốc, tạo đơn dịch vụ
Trang chính: home-doctor.html, examination.html, service-order.html, diagnosis.html, assigned-services.html
Hướng dẫn:
Đăng nhập với tài khoản bác sĩ
Vào "Wait List" để xem danh sách bệnh nhân chờ khám
Chọn bệnh nhân, nhập thông tin khám, chỉ định dịch vụ
Vào "Service Orders" để xem, tạo, chỉnh sửa đơn dịch vụ
Vào "Assigned Services" để nhập kết quả xét nghiệm nếu được giao
PHARMACIST
Xem danh sách đơn thuốc, xác nhận phát thuốc
Kiểm tra tồn kho, nhập/xuất kho
Quản lý thông tin thuốc
Trang chính: home-pharmacist.html, prescription-pharmacist.html, inventory-pharmacist.html
Hướng dẫn:
Đăng nhập với tài khoản dược sĩ
Vào "Prescriptions" để xác nhận đơn thuốc
Vào "Inventory" để kiểm tra, cập nhật kho thuốc
RECEPTIONIST
Quản lý lịch hẹn, xác nhận bệnh nhân đến khám
Quản lý danh sách chờ, chuyển bệnh nhân cho bác sĩ
Trang chính: home-receptionist.html, appointment-receptionist.html, waitlist-receptionist.html
Hướng dẫn:
Đăng nhập với tài khoản tiếp tân
Vào "Lịch hẹn" để xác nhận, chỉnh sửa lịch
Vào "Danh sách chờ khám" để chuyển bệnh nhân cho bác sĩ
PATIENT
Đăng ký tài khoản, đăng nhập
Đặt lịch khám, xem lịch sử khám, nhận kết quả
Xem thông tin bác sĩ, dịch vụ, hóa đơn
Trang chính: home-patient.html, appointment-patient.html, patient-doctors.html, patient-invoices.html
Hướng dẫn:
Đăng ký tài khoản, đăng nhập
Đặt lịch khám, theo dõi trạng thái lịch hẹn
Xem kết quả khám, hóa đơn, thanh toán
CHI TIẾT CÁC TRANG GIAO DIỆN CHÍNH
home-adminsys.html: Dashboard quản trị hệ thống
listdoctors-adminsys.html: Quản lý danh sách bác sĩ
listpharmacist-adminsys.html: Quản lý dược sĩ
listrecep-adminsys.html: Quản lý tiếp tân
listpatient-adminsys.html: Quản lý bệnh nhân
home-doctor.html: Dashboard bác sĩ, truy cập nhanh các chức năng
examination.html: Khám bệnh, nhập triệu chứng, chẩn đoán, chỉ định dịch vụ
service-order.html: Quản lý đơn dịch vụ, xem chi tiết, lịch sử
diagnosis.html: Quản lý chẩn đoán, kết quả xét nghiệm
assigned-services.html: Bác sĩ nhập kết quả dịch vụ được giao
home-pharmacist.html: Dashboard dược sĩ
prescription-pharmacist.html: Quản lý đơn thuốc
inventory-pharmacist.html: Quản lý kho thuốc
home-receptionist.html: Dashboard tiếp tân
appointment-receptionist.html: Quản lý lịch hẹn
waitlist-receptionist.html: Danh sách chờ khám
home-patient.html: Dashboard bệnh nhân
appointment-patient.html: Đặt lịch, xem lịch sử
patient-doctors.html: Xem thông tin bác sĩ
patient-invoices.html: Xem hóa đơn, thanh toán
API ENDPOINT & VÍ DỤ REQUEST/RESPONSE
Đăng nhập
POST /api/login
Request: { "username": "user", "password": "pass" }
Response: { "success": true, "message": "Login successful", "redirectUrl": "home-doctor.html" }
Khám bệnh (Doctor)
POST /api/doctor/examination
Request: { "patientId": 1, "symptoms": "...", "diagnosis": "..." }
Response: { "success": true, "message": "Examination saved" }
GET /api/doctor/examination?action=getByMedicineRecord&medicineRecordId=1
Response: { "success": true, "data": { ... } }
Đơn dịch vụ (Service Order)
POST /api/doctor/service-order
Request: { "medicineRecordId": 1, "services": [1,2], "assignedDoctors": [3,4] }
Response: { "success": true, "message": "Service order created" }
GET /api/doctor/service-order?action=getServiceOrder&serviceOrderId=1
Response: { "success": true, "data": { ... } }
Kết quả dịch vụ cận lâm sàng
POST /api/doctor/service-result
Request: { "serviceOrderItemId": 1, "result": "...", "conclusion": "...", "status": "Completed" }
Response: { "success": true, "message": "Result saved" }
Đơn thuốc (Prescription)
GET /api/prescription?patientId=1
Response: { "prescriptions": [ ... ] }
Hóa đơn (Invoice)
GET /invoice?patient_id=1
Response: [ ... ]
Quản lý tài khoản
GET /api/doctors - Lấy danh sách bác sĩ
GET /api/doctors/departments - Lấy danh sách phòng ban
GET /api/patient/detail?patientId=1 - Lấy chi tiết bệnh nhân
Thông báo nội bộ (Websocket)
Endpoint: /ws/announcements?staffId=1
Payload: { "type": "full", "data": [ ... ] }
CẤU TRÚC DATABASE (CÁC BẢNG CHÍNH)
AccountStaff, AccountPharmacist, AccountPatient: Quản lý tài khoản, phân quyền
Doctor, Receptionist, Pharmacist, Patient: Thông tin chi tiết từng loại tài khoản
Appointment: Quản lý lịch hẹn
Waitlist: Danh sách chờ khám
ExamResult: Kết quả khám bệnh
MedicineRecords: Hồ sơ thuốc
ServiceOrder, ServiceOrderItem: Đơn dịch vụ và chi tiết dịch vụ
ListOfMedicalService: Danh sách dịch vụ y tế
ResultsOfParaclinicalServices: Kết quả dịch vụ cận lâm sàng
Prescription: Đơn thuốc
Invoice: Hóa đơn
Announcement: Thông báo nội bộ
VALIDATION, SESSION, ERROR HANDLING
Validation:
Kiểm tra đầy đủ thông tin trước khi lưu (bắt buộc nhập trường quan trọng)
Hiển thị thông báo lỗi rõ ràng cho người dùng
Session:
Yêu cầu đăng nhập, kiểm tra role cho từng API
Hết session sẽ tự động chuyển về trang đăng nhập
Error Handling:
Xử lý lỗi database, lỗi logic, lỗi quyền truy cập
Ghi log chi tiết, trả về thông báo user-friendly
TROUBLESHOOTING & FAQ
Không thể đăng nhập: Kiểm tra tài khoản, mật khẩu, trạng thái tài khoản
Không lưu được dữ liệu: Kiểm tra kết nối database, quyền truy cập
Không hiển thị danh sách dịch vụ: Kiểm tra dữ liệu bảng ListOfMedicalService
Lỗi session: Đảm bảo đã đăng nhập đúng vai trò
Lỗi API: Kiểm tra request/response, debug bằng browser devtools và server log
CÀI ĐẶT & DEPLOY
Yêu cầu:
JDK 17+, Maven 3.6+, SQL Server
Clone project:
git clone <repo-url>
Cấu hình database:
Tạo CSDL HealthCareSystem trên SQL Server
Import các file SQL trong src/main/resources/sql/
Cập nhật thông tin kết nối DB trong dao/DBContext.java nếu cần
Build & chạy:
cd SWP391_Group5
mvn clean package
Deploy file WAR lên Tomcat hoặc server Jakarta EE tương thích
Truy cập:
Mở trình duyệt: http://localhost:8080/SWP391_Group5
ĐÓNG GÓP & LIÊN HỆ
Đóng góp: Pull request, issue trên Github
Liên hệ: [Tên nhóm, email, ...]
PHỤ LỤC: VÍ DỤ WORKFLOW CHI TIẾT
1. Quy trình khám bệnh (Doctor)
Đăng nhập → Vào "Wait List" → Chọn bệnh nhân
Nhập triệu chứng, chẩn đoán sơ bộ
Chỉ định dịch vụ cận lâm sàng (chọn bác sĩ thực hiện)
Lưu kết quả khám, tạo service order
Vào "Service Orders" để xem lịch sử, chi tiết đơn dịch vụ
Nếu được giao, vào "Assigned Services" để nhập kết quả xét nghiệm
Kê đơn thuốc, hoàn thành khám
2. Quy trình tiếp tân
Đăng nhập → Vào "Lịch hẹn" → Xác nhận lịch
Vào "Danh sách chờ khám" → Chuyển bệnh nhân cho bác sĩ
3. Quy trình dược sĩ
Đăng nhập → Vào "Prescriptions" → Xác nhận đơn thuốc
Vào "Inventory" → Kiểm tra, cập nhật kho thuốc
4. Quy trình bệnh nhân
Đăng ký, đăng nhập → Đặt lịch khám
Theo dõi trạng thái lịch hẹn
Xem kết quả khám, hóa đơn, thanh toán
GHI CHÚ KỸ THUẬT & BẢO MẬT
Mã hóa mật khẩu, kiểm tra session, phân quyền API
Sử dụng HTTPS khi deploy thực tế
Định nghĩa rõ ràng các role, kiểm soát truy cập từng chức năng
Giao diện responsive, hỗ trợ mobile
Định dạng ngày giờ chuẩn ISO, hỗ trợ đa ngôn ngữ (nếu cần)
CẤU TRÚC THƯ MỤC CHI TIẾT
SWP391_Group5/
├── src/main/java/
│   ├── controller/         # Servlet xử lý nghiệp vụ
│   ├── dao/               # Data Access Object (truy vấn DB)
│   ├── dto/               # Data Transfer Object
│   ├── model/             # Entity/model dữ liệu
│   ├── socket/            # Websocket (thông báo)
│   └── util/              # Tiện ích
├── src/main/webapp/
│   ├── view/              # Giao diện HTML
│   ├── assets/            # CSS, JS, hình ảnh, vendor
│   └── WEB-INF/           # Cấu hình web.xml, properties
├── src/main/resources/sql/ # File tạo bảng, dữ liệu mẫu
├── pom.xml                # Quản lý phụ thuộc Maven
└── ...
