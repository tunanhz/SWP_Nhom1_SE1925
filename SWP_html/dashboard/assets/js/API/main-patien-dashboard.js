const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;

let imagePath = account?.img ;

if (account) {
  document.getElementById('username').innerHTML = `Hello! ${account.username}`;

  const userImgElement = document.getElementById('userImg');
    if (userImgElement) {
        userImgElement.src = imagePath;
        userImgElement.alt = `Profile of ${account.username || 'User'}`;
    }

    const profileImgElement = document.getElementById('profileImg');
    if (profileImgElement) {
        profileImgElement.src = imagePath;
        profileImgElement.alt = `Profile of ${account.username || 'User'}`;
    }
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
    container.innerHTML = "<p>No Upcoming Appointment...</p>";

    const container1 = document.getElementById("three-AppointmentCompleted");//---

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


    let threeAppointmentCards = "";
    threeAppointmentsUpcomings.forEach((ap) => {
      threeAppointmentCards += `<li class="d-flex mb-4 align-items-center pb-5 mb-5 border-bottom">
                                <div class="ms-4 flex-grow-1">
                                    <h5>Skin Treatment</h5>
                                    <ul class="list-inline m-0 p-0 d-flex gap-2 align-items-center flex-wrap">
                                        <li><h6 class="mb-0 text-body fw-normal">${ap.message} ${ap.shift === 'Morning' ? 'AM' : 'PM'}</h6></li>
                                        <li class="bg-light rounded p-1"></li>
                                        <li><h6 class="mb-0 text-body fw-normal">Dr.${ap.doctorName}</h6></li>
                                        <li class="bg-light rounded p-1"></li>
                                        <li><h6 class="mb-0 text-body fw-normal">${ap.note}</h6></li>
                                    </ul>
                                </div>
                                <div>
                                    <button class="dropdown btn border-0 p-0">
                                        <svg width="15" viewBox="0 0 24 24" fill="none"
                                            xmlns="http://www.w3.org/2000/svg">
                                            <path d="M8.5 5L15.5 12L8.5 19" stroke="currentColor" stroke-width="1.5"
                                                stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </button>
                                </div>
                            </li>`;

      container.innerHTML = threeAppointmentCards || '<p>No Upcoming Appointment.</p>';
    });


    // Render table
    let appointmentTable = `
        <div class="card-header d-flex  align-items-center justify-content-between flex-wrap gap-2 mb-3">
                <div>
                    <h4 class="mb-0">Visited Doctors</h4>
                </div>
                <div class="dropdown text-end">
                    <a href="patient-encounters.html" class="dropdown btn border-0 p-0"
                        aria-expanded="false">
                        <span class="fw-500">View All</span>
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
                                <th class="border-bottom">Names</th>
                                <th class="border-bottom">Email</th>
                                <th class="border-bottom">specializes</th>
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



  document.getElementById('logoutLink').addEventListener('click', function (event) {
    event.preventDefault();
    localStorage.removeItem('account');
    window.location.href = '/frontend/login.html';
  });

  document.getElementById('logoutModalLink').addEventListener('click', function (event) {
    event.preventDefault();
    localStorage.removeItem('account');
    window.location.href = '/frontend/login.html';
  });
});
