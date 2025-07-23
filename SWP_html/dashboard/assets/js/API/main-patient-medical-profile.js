let patientRecord = null;

async function fetchPatientData() {
    let patientId = localStorage.getItem('patient_id');
    try {
        const response = await fetch(`http://localhost:8080/SWP_back_war_exploded/api/patient_records/?patientId=${patientId}`);
        if (!response.ok) {
            throw new Error('Lỗi khi lấy dữ liệu: ' + response.status);
        }
        const data = await response.json();
        patientRecord = data.records[0]; // Lấy record đầu tiên
        return patientRecord;
    } catch (error) {
        console.error('Đã xảy ra lỗi:', error);
        return null;
    }
}

function renderPatientInfo() {
    const patientInfo = document.getElementById("patient-info");
    if (!patientRecord) return;
    
    patientInfo.innerHTML = `
        <p><strong>Tên:</strong> ${patientRecord.patientName}</p>
        <p><strong>Ngày sinh:</strong> ${patientRecord.dob}</p>
        <p><strong>Giới tính:</strong> ${patientRecord.gender}</p>
        <p><strong>Điện thoại:</strong> ${patientRecord.phone}</p>
        <p><strong>Địa chỉ:</strong> ${patientRecord.address}</p>
    `;
}

function renderMedicalRecords() {
    const medicalRecords = document.getElementById("medical-records");
    if (!patientRecord) return;

    const html = `
        <div class="mb-6">
            <h4 class="text-lg font-medium mb-2">Chẩn đoán</h4>
            <div class="overflow-x-auto">
                <table class="min-w-full bg-white border">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Ngày</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Bác sĩ</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Bệnh</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Phần kết luận</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Kế hoạch điều trị</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="py-2 px-4 border-b">${formatDateTimeConfirm(patientRecord.diagnosisDate)}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.diagnosisDoctorName}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.disease}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.conclusion}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.treatmentPlan}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <h4 class="text-lg font-medium mb-2 mt-4">Kết quả thi</h4>
            <div class="overflow-x-auto">
                <table class="min-w-full bg-white border">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Ngày</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Bác sĩ</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Triệu chứng</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Chẩn đoán sơ bộ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="py-2 px-4 border-b">${formatDateTimeConfirm(patientRecord.examDate)}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.examDoctorName}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.symptoms}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.preliminaryDiagnosis}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;
    medicalRecords.innerHTML = html;
}

function renderAppointments() {
    const appointmentsBody = document.getElementById("appointments-body");
    if (!patientRecord) return;

    const html = `
        <tr>
            <td class="py-2 px-4 border-b">${formatDateTime(patientRecord.appointmentDatetime)}</td>
            <td class="py-2 px-4 border-b">${patientRecord.appointmentDoctorName}</td>
            <td class="py-2 px-4 border-b">${patientRecord.shift}</td>
            <td class="py-2 px-4 border-b">${patientRecord.appointmentStatus}</td>
        </tr>
    `;
    appointmentsBody.innerHTML = html;
}

function renderPrescriptions() {
    const prescriptions = document.getElementById("prescriptions");
    if (!patientRecord) return;

    const html = `
        <div class="mb-6">
            <h3 class="text-xl font-semibold mb-2">
                Đơn thuốc (Ngày: ${patientRecord.prescriptionDate}, Trạng thái: ${patientRecord.prescriptionStatus})
            </h3>
            <div class="overflow-x-auto">
                <table class="min-w-full bg-white border">
                    <thead>
                        <tr>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Thuốc</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Số lượng</th>
                            <th class="py-2 px-4 border-b text-left text-gray-600">Liều dùng</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="py-2 px-4 border-b">${patientRecord.medicineName}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.medicineQuantity}</td>
                            <td class="py-2 px-4 border-b">${patientRecord.medicineDosage}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;
    prescriptions.innerHTML = html;
}

async function generatePDF() {
    if (!patientRecord) {
        alert("No patient data available to generate PDF.");
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/SWP_back_war_exploded/api/generate-pdf', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(patientRecord)
        });

        if (!response.ok) {
            throw new Error('Error generating PDF: ' + response.status);
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `patient_record_${patientRecord.patientName}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Error generating PDF:', error);
        alert('Failed to generate PDF. Please try again.');
    }
}

async function init() {
    await fetchPatientData();
    if (patientRecord) {
        renderPatientInfo();
        renderMedicalRecords();
        renderAppointments();
        renderPrescriptions();
    }

    // Add event listener for PDF generation
    const generatePdfBtn = document.getElementById('generate-pdf-btn');
    if (generatePdfBtn) {
        generatePdfBtn.addEventListener('click', generatePDF);
    }
}

function formatDateTime(dateTime) {
    if (!dateTime) return "N/A";
    const options = {
        timeZone: 'Asia/Ho_Chi_Minh',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
    };
    const date = new Date(dateTime);
    const timePart = date.toLocaleTimeString('en-US', options);
    const day = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', day: '2-digit' });
    const month = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', month: '2-digit' });
    const year = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric' });

    return `${timePart}, ${day}/${month}/${year}`;
}

function formatDateTimeConfirm(dateTime) {
    if (!dateTime) return "N/A";
    const options = {
        timeZone: 'Asia/Ho_Chi_Minh',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
    };
    const date = new Date(dateTime);
    const day = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', day: '2-digit' });
    const month = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', month: '2-digit' });
    const year = date.toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric' });

    return `${day}/${month}/${year}`;
}

// Run initialization
init();