const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;
if(account){
    document.getElementById('username').innerHTML = `Xin Chào! ${account.username}`;
}

function formatDateTimeConfirm(dateTime) {
    if (!dateTime) return "N/A";

    const date = new Date(dateTime);
    if (isNaN(date)) return "Invalid date";

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
}

function formatToVND(numberString) {
    const formatted = Math.floor(parseFloat(numberString)).toLocaleString('vi-VN');
    return `${formatted} VND`;
}

function createAppointmentCompletedRow(appointment, index) {
    return `<tr>
        <td>${index + 1}</td>
        <td>
            <div class="d-flex align-items-center gap-3">
                <img class="img-fluid flex-shrink-0 icon-40 object-fit-cover"
                    src="${appointment.doctor.img}">
                <h5 class="mb-0">Dr.${appointment.doctor.fullName} </h5>
            </div>
        </td>
        <td>${appointment.doctor.email}</td>
        <td>${appointment.doctor.department}</td>
    </tr>`;
}


async function displayThreeAppointment() {
    try {

        const container = document.getElementById("three-Appointment");
        container.innerHTML = "<p>Không có cuộc hẹn sắp tới...</p>";

        const container1 = document.getElementById("three-AppointmentCompleted");
        container1.innerHTML = "<p>Không có cuộc hẹn đã hoàn thành...</p>";

        const container2 = document.getElementById('infor-three-payment');
        container2.innerHTML = "<p>Không có thanh toán...</p>";

        const response = await fetch(baseAPI, {
            method: "GET",
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(
                errorData.error || `HTTP error! Status: ${response.status}`
            );
        }

        const data = await response.json();
        const threeAppointmentsUpcomings = data.threeAppointmentsUpcoming;
        const threeCompletedAppointments = data.threeAppointmentComplete;
        const threePaymentPendings = data.threePaymentPending;

        let threeAppointmentCards = "";
        threeAppointmentsUpcomings.forEach((ap) => {
            threeAppointmentCards += `<li class="d-flex mb-4 align-items-center pb-5 mb-5 border-bottom">
                                <div class="ms-4 flex-grow-1">
                                    <h5>${ap.note}</h5>
                                    <ul class="list-inline m-0 p-0 d-flex gap-2 align-items-center flex-wrap">
                                        <li><h6 class="mb-0 text-body fw-normal">${ap.message} ${ap.shift === 'Morning' ? 'AM' : 'PM'}</h6></li>
                                        <li class="bg-light rounded p-1"></li>
                                        <li><h6 class="mb-0 text-body fw-normal">Dr.${ap.doctorName}</h6></li>
                                    </ul>
                                </div>
                                <div>
                                    <a class="dropdown btn border-0 p-0" href="./patient-appointment.html"> 
                                        <svg width="15" viewBox="0 0 24 24" fill="none"
                                            xmlns="http://www.w3.org/2000/svg">
                                            <path d="M8.5 5L15.5 12L8.5 19" stroke="currentColor" stroke-width="1.5"
                                                stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </a>
                                </div>
                            </li>`;

            container.innerHTML = threeAppointmentCards || '<p>Không có cuộc hẹn sắp tới.</p>';
        });

        let threePaymentCards = "";
        threePaymentPendings.forEach((pd) => {
            threePaymentCards += `<li class="d-flex flex-sm-row flex-column align-items-sm-center align-items-start justify-content-between flex-wrap gap-1 mb-4 bg-primary-subtle py-3 px-4 rounded">
                        <div class="d-flex flex-sm-row flex-column align-items-sm-center align-items-start flex-wrap gap-4">
                           <svg width="32" height="32" viewBox="0 0 32 32" fill="none"
                                xmlns="http://www.w3.org/2000/svg">
                              <g clip-path="url(#clip0_483_2650)">
                                 <path
                                         d="M22 11C22 10.7348 22.1054 10.4804 22.2929 10.2929C22.4804 10.1054 22.7348 10 23 10H27C27.2652 10 27.5196 10.1054 27.7071 10.2929C27.8946 10.4804 28 10.7348 28 11V13C28 13.2652 27.8946 13.5196 27.7071 13.7071C27.5196 13.8946 27.2652 14 27 14H23C22.7348 14 22.4804 13.8946 22.2929 13.7071C22.1054 13.5196 22 13.2652 22 13V11Z"
                                         fill="currentColor" />
                                 <path
                                         d="M4 4C2.93913 4 1.92172 4.42143 1.17157 5.17157C0.421427 5.92172 0 6.93913 0 8L0 24C0 25.0609 0.421427 26.0783 1.17157 26.8284C1.92172 27.5786 2.93913 28 4 28H28C29.0609 28 30.0783 27.5786 30.8284 26.8284C31.5786 26.0783 32 25.0609 32 24V8C32 6.93913 31.5786 5.92172 30.8284 5.17157C30.0783 4.42143 29.0609 4 28 4H4ZM30 8V18H2V8C2 7.46957 2.21071 6.96086 2.58579 6.58579C2.96086 6.21071 3.46957 6 4 6H28C28.5304 6 29.0391 6.21071 29.4142 6.58579C29.7893 6.96086 30 7.46957 30 8ZM28 26H4C3.46957 26 2.96086 25.7893 2.58579 25.4142C2.21071 25.0391 2 24.5304 2 24V22H30V24C30 24.5304 29.7893 25.0391 29.4142 25.4142C29.0391 25.7893 28.5304 26 28 26Z"
                                         fill="currentColor" />
                              </g>
                              <defs>
                                 <clipPath id="clip0_483_2650">
                                    <rect width="32" height="32" fill="white" />
                                 </clipPath>
                              </defs>
                           </svg>
                           <div>
                              <h5 class="mb-0">Bệnh nhân: ${pd.patient.fullName}</h5>
                              <h6 class="text-body fw-normal mb-0 mt-2">${formatDateTimeConfirm(pd.issueDate)}</h6>
                           </div>
                        </div>
                        <h5 class="mb-0 text-primary mt-sm-0 mt-3">${formatToVND(pd.invoiceTotalAmount)}</h5>
                        <div class="dropdown text-end">
                           <button class="dropdown btn border-0 p-0">
                              <svg width="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                 <path d="M8.5 5L15.5 12L8.5 19" stroke="currentColor" stroke-width="1.5"
                                       stroke-linecap="round" stroke-linejoin="round"></path>
                              </svg>
                           </button>
                        </div>
                     </li>`;
            container2.innerHTML = threePaymentCards || '<p>Không thanh toán.</p>';
        });



        // Render table
        let appointmentTable = `
        <div class="card-header d-flex  align-items-center justify-content-between flex-wrap gap-2 mb-3">
                <div>
                    <h4 class="mb-0">Đã đến thăm bác sĩ</h4>
                </div>
                <div class="dropdown text-end">
                    <a href="patient-encounters.html" class="dropdown btn border-0 p-0"
                        aria-expanded="false">
                        <span class="fw-500">Xem tất cả</span>
                        <svg width="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="transform-icon transform-down">
                            <path d="M8.5 5L15.5 12L8.5 19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </a>
                </div>
            </div>
             <div class="card-body pt-0">
                <div class="table-responsive">
                    <table class="table py-3 mb-0" role="grid">
                        <thead class="bg-primary-subtle">
                            <tr class="text-dark">
                                <th class="border-bottom">No.</th>
                                <th class="border-bottom">Tên</th>
                                <th class="border-bottom">Email</th>
                                <th class="border-bottom">chuyên khoa</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${threeCompletedAppointments.map((appointment, index) => createAppointmentCompletedRow(appointment, index)).join("")}
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        container1.innerHTML = appointmentTable;


    } catch (error) {
        const container = document.getElementById("three-Appointment");
        if (container) {
            container.innerHTML = `<p class="text-danger">Error: ${error.message}. Please try again later.</p>`;
            window.location.href = "./errors/error404.html";
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    displayThreeAppointment();
});
