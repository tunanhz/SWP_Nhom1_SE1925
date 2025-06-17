const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;

// const helloPatient = document.querySelector('#name-patient');
// helloPatient.innerHTML = `Hello! ${account}`;

async function displayThreeAppointment() {
  try {
    

    const container = document.getElementById("three-Appointment");
    container.innerHTML = "<p>No Upcoming Appointment...</p>";

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
  } catch (error) {
    const container = document.getElementById("three-Appointment");
    if (container) {
      container.innerHTML = `<p class="text-danger">Error: ${error.message}. Please try again later.</p>`;
    //   window.location.href = "./errors/error404.html";
    }
  }
}
document.addEventListener("DOMContentLoaded", () => {
  displayThreeAppointment();
  const account = JSON.parse(localStorage.getItem('account'));
  if (account) {
      document.getElementById('username').innerHTML = `Hello! ${account.username}`;
  }

  document.getElementById('logoutLink').addEventListener('click', function (event) {
      event.preventDefault();
      localStorage.removeItem('account'); 
      window.location.href = '../SWP_html/frontend/login.html';
  });

  document.getElementById('logoutModalLink').addEventListener('click', function (event) {
      event.preventDefault();
      localStorage.removeItem('account'); 
      window.location.href = '../SWP_html/frontend/login.html';
  });
});
