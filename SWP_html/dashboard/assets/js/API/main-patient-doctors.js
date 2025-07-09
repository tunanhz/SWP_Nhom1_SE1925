const accountString = localStorage.getItem("account");
const account = JSON.parse(accountString);
const baseAPI = "http://localhost:8080/SWP_back_war_exploded/api/doctors/";

let currentPage = 1;
let pageSize = 8;
let currentNameSearch = "";
let currentDeptSearch = "";

async function populateDepartments() {
  try {
    const response = await fetch(`${baseAPI}departments`, { method: "GET" });
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    const departments = await response.json();
    const deptSelect = document.getElementById("department-search");
    if (deptSelect) {
      deptSelect.innerHTML = '<option value="" selected>All departments</option>';
      departments.forEach(dept => {
        deptSelect.innerHTML += `<option value="${dept}">${dept}</option>`;
      });
    }
  } catch (error) {
    console.error("Error fetching departments:", error);
    const deptSelect = document.getElementById("department-search");
    if (deptSelect) {
      deptSelect.innerHTML = '<option value="">Error loading departments</option>';
      window.location.href = "./errors/error404.html";
    }
  }
}

async function displayDoctors(page = 1, nameSearch = currentNameSearch, deptSearch = currentDeptSearch) {
  window.scrollTo({ top: 0, behavior: 'smooth' });
  try {
    // Update current state
    currentPage = page;
    currentNameSearch = nameSearch;
    currentDeptSearch = deptSearch;

    // Build API URL
    let apiUrl = `${baseAPI}?page=${currentPage}&size=${pageSize}`;

    if (nameSearch) {
      apiUrl += `&name=${encodeURIComponent(nameSearch)}`;
    }
    if (deptSearch) {
      apiUrl += `&department=${encodeURIComponent(deptSearch)}`;
    }

    // Show loading state
    const container = document.getElementById("infor_doctor");
    if (!container) throw new Error("Container element not found");
    container.innerHTML = '<p>Loading doctors...</p>';

    // Fetch doctors
    const response = await fetch(apiUrl, {
      method: "GET",
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
    }

    const data = await response.json();

    const doctors = data.doctors || [];
    const totalPages = data.totalPages || 1;
    const totalDoctors = data.totalDoctors;
    // Render doctor cards
    let doctorCards = "";
    doctors.forEach((doctor) => {
      doctorCards += `
        <div class="col-xl-3 col-lg-4 col-sm-6">
          <div class="p-5 card text-center">
            <div class="mt-5">
              <img src="${doctor.img}" alt="Doctor image"
                class="img-fluid rounded-circle p-1 border border-danger avatar" height="100" width="100" loading="lazy">
            </div>
            <div>
              <div class="mt-5 d-inline-block bg-primary-subtle px-3 py-2 rounded-pill">
                <span class="fw-500">${doctor.department}</span>
              </div>
            </div>
            <h3 class="mt-4 mb-2">${doctor.fullName}</h3>
            <h6 class="mx-0 text-body fw-normal">${doctor.eduLevel}</h6>
            <div class="d-flex flex-wrap align-items-center justify-content-center gap-3 mb-3 mt-5">
              <a class="btn btn-primary" href="patient-appointment.html">
                <span class="btn-inner">
                  <span class="text d-inline-block align-middle">Book Appointment</span>
                  <span class="icon d-inline-block align-middle ms-1 ps-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="8" height="8" viewBox="0 0 8 8" fill="none">
                      <path d="M7.32046 4.70834H4.74952V7.25698C4.74952 7.66734 4.41395 8 4 8C3.58605 8 3.25048 7.66734 3.25048 7.25698V4.70834H0.679545C0.293423 4.6687 0 4.34614 0 3.96132C0 3.5765 0.293423 3.25394 0.679545 3.21431H3.24242V0.673653C3.28241 0.290878 3.60778 0 3.99597 0C4.38416 0 4.70954 0.290878 4.74952 0.673653V3.21431H7.32046C7.70658 3.25394 8 3.5765 8 3.96132C8 4.34614 7.70658 4.6687 7.32046 4.70834Z" fill="currentColor"/>
                    </svg>
                  </span>
                </span>
              </a>
              <a class="btn btn-secondary" href="patient-doctor-profile.html?id=${doctor.ID}">
                <span class="btn-inner">
                  <span class="text d-inline-block align-middle">View Profile</span>
                  <span class="icon d-inline-block align-middle ms-1 ps-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="8" height="8" viewBox="0 0 8 8" fill="none">
                      <path d="M7.32046 4.70834H4.74952V7.25698C4.74952 7.66734 4.41395 8 4 8C3.58605 8 3.25048 7.66734 3.25048 7.25698V4.70834H0.679545C0.293423 4.6687 0 4.34614 0 3.96132C0 3.5765 0.293423 3.25394 0.679545 3.21431H3.24242V0.673653C3.28241 0.290878 3.60778 0 3.99597 0C4.38416 0 4.70954 0.290878 4.74952 0.673653V3.21431H7.32046C7.70658 3.25394 8 3.5765 8 3.96132C8 4.34614 7.70658 4.6687 7.32046 4.70834Z" fill="currentColor"/>
                    </svg>
                  </span>
                </span>
              </a>
            </div>
          </div>
        </div>
      `;
    });

    // Render pagination
    const paginationHTML = `
      <div class="card-footer pt-0 row">
        <div class="d-flex justify-content-start col-md-6 mt-4">
          <button class="btn btn-secondary text-white me-3" type="button"
            data-page="${currentPage - 1}"
            ${currentPage === 1 ? "disabled" : ""}>
            <span class="btn-inner">
              <span class="text d-inline-block align-middle">Previous</span>
              <span class="icon d-inline-block align-middle ms-1 ps-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                  <path fill-rule="evenodd" d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
                </svg>
              </span>
            </span>
          </button>
          <span class="align-self-center me-3">Page ${currentPage} of ${totalPages}</span>
          <button class="btn btn-primary me-3" type="button"
            data-page="${currentPage + 1}"
            ${currentPage === totalPages ? "disabled" : ""}>
            <span class="btn-inner">
              <span class="text d-inline-block align-middle">Next</span>
              <span class="icon d-inline-block align-middle ms-1 ps-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                  <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                </svg>
              </span>
            </span>
          </button>
        </div>
        <div class="col-md-6 d-flex justify-content-center justify-content-md-end mt-4">
          <div class="input-group input-group-sm w-auto">
            <label class="input-group-text text-bg-info" for="pageSize">Items per page</label>
            <select class="form-select" name="page" id="pageSize">
            <option value="4" ${pageSize === 4 ? 'selected' : ''}>4</option>
              <option value="8" ${pageSize === 8 ? 'selected' : ''}>8</option>
              <option value="12" ${pageSize === 12 ? 'selected' : ''}>12</option>
              <option value="16" ${pageSize === 16 ? 'selected' : ''}>16</option>
              <option value="${totalDoctors}" ${pageSize === totalDoctors ? 'selected' : ''}>All</option>
            </select>
          </div>
        </div>
      </div>`;

    // Update container
    container.innerHTML = doctorCards || '<p>No doctors found.</p>';
    if (doctorCards) {
      container.innerHTML += paginationHTML;
    }

    // Attach event listeners for pagination buttons
    container.querySelectorAll("button[data-page]").forEach(button => {
      button.addEventListener("click", () => {
        const newPage = parseInt(button.dataset.page);
        displayDoctors(newPage, currentNameSearch, currentDeptSearch);
      });
    });

    // Attach event listener for page size change
    const pageSizeSelect = container.querySelector("#pageSize");
    if (pageSizeSelect) {
      pageSizeSelect.addEventListener("change", (e) => {
        pageSize = parseInt(e.target.value) || 30;
        displayDoctors(1, currentNameSearch, currentDeptSearch);
      });
    } else {
      console.warn("Warning: Could not find element with ID 'pageSize'. Ensure the select element is rendered.");
    }

  } catch (error) {
    console.error("Error fetching or displaying doctors:", error);
    const container = document.getElementById("infor_doctor");
    if (container) {
      container.innerHTML = `<p class="text-danger">Error: ${error.message}. <button class="btn btn-link p-0" onclick="displayDoctors()">Retry</button></p>`;
      window.location.href = "./errors/error404.html";
    }
  }
}

function setupSearch() {
  const nameInput = document.getElementById("doctor-search");
  const deptInput = document.getElementById("department-search");

  if (nameInput) {
    nameInput.addEventListener("input", debounce((e) => {
      const nameValue = e.target.value.trim();
      const deptValue = deptInput ? deptInput.value.trim() : currentDeptSearch;
      displayDoctors(1, nameValue, deptValue);
    }, 300));
  }

  if (deptInput) {
    deptInput.addEventListener("input", debounce((e) => {
      const deptValue = e.target.value.trim();
      const nameValue = nameInput ? nameInput.value.trim() : currentNameSearch;
      displayDoctors(1, nameValue, deptValue);
    }, 300));
  }

}

function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

function triggerSearch() {
  const nameInput = document.getElementById("doctor-search");
  const deptInput = document.getElementById("department-search");
  const nameValue = nameInput ? nameInput.value.trim() : "";
  const deptValue = deptInput ? deptInput.value.trim() : "";
  displayDoctors(1, nameValue, deptValue);
}

document.addEventListener("DOMContentLoaded", () => {
  setupSearch();
  populateDepartments();
  displayDoctors(1);
});