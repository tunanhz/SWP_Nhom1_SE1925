const accountString = localStorage.getItem('account');
const account = JSON.parse(accountString);

const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patient/?accountPatientId=${account.accountPatientId}`;
let pageSize = 6;
const state1 = {
    currentPage: 1,
    currentNameSearch: "",
    currentDob: "",
    currentGender: ""
};

function sanitizeHTML(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
}

function formatDateToYYYYMMDD(dateStr) {
    if (!dateStr) return "N/A";

    const date = new Date(dateStr);
    if (isNaN(date)) return "Invalid date";

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

function formatDateTimeConfirm(dateTime) {
    return dateTime ? new Date(dateTime).toLocaleDateString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' }) : "N/A";
}

function createPatientRow(patient, index) {
    const renderActionButtons = () => {
        return `<a class="d-inline-block pe-2 edit-btn1" data-bs-toggle="offcanvas"
                   href="#offcanvasEncounterEditConfirm" aria-controls="offcanvasEncounterEditConfirm" aria-label="Edit Appointment"
                   data-patient='${JSON.stringify(patient)}'>
                    <span class="text-success">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9.31055 14.3321H14.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path fill-rule="evenodd" clip-rule="evenodd"
                                  d="M8.58501 1.84609C9.16674 1.15084 10.2125 1.04889 10.9222 1.6188C10.9614 1.64972 12.2221 2.62909 12.2221 2.62909C13.0017 3.10039 13.244 4.10233 12.762 4.86694C12.7365 4.90789 5.60896 13.8234 5.60896 13.8234C5.37183 14.1192 5.01187 14.2938 4.62718 14.298L1.89765 14.3323L1.28265 11.7292C1.1965 11.3632 1.28265 10.9788 1.51978 10.683L8.58501 1.84609Z"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M7.26562 3.50073L11.3548 6.64108" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
                <a href="#" class="d-inline-block ps-2 delete-btn1" data-id="${patient.id}" aria-label="Delete Patient">
                    <span class="text-danger">
                        <svg width="15" height="16" viewBox="0 0 15 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12.4938 6.10107C12.4938 6.10107 12.0866 11.1523 11.8503 13.2801C11.7378 14.2963 11.1101 14.8918 10.0818 14.9106C8.12509 14.9458 6.16609 14.9481 4.21009 14.9068C3.22084 14.8866 2.60359 14.2836 2.49334 13.2853C2.25559 11.1388 1.85059 6.10107 1.85059 6.10107"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M13.5312 3.67969H0.812744" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M11.0804 3.67974C10.4917 3.67974 9.98468 3.26349 9.86918 2.68674L9.68693 1.77474C9.57443 1.35399 9.19343 1.06299 8.75918 1.06299H5.58443C5.15018 1.06299 4.76918 1.35399 4.65668 1.77474L4.47443 2.68674C4.35893 3.26349 3.85193 3.67974 3.26318 3.67974"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>`;
    };
    return `
        <tr data-item="list">
            <th scope="row">${index + 1}</th>
            <td>${sanitizeHTML(patient.fullName)}</td>
            <td>
                <h6 class="mb-0 text-body fw-normal">${formatDateTimeConfirm(patient.dob)}</h6>
            </td>
            <td>${sanitizeHTML(patient.phone)}</td>
            <td>${sanitizeHTML(patient.gender)}</td>
            <td>${sanitizeHTML(patient.address)}</td>
            <td>${renderActionButtons()}</td>
        </tr>
    `;
}

async function displayPatient(page = 1, nameSearch = state1.currentNameSearch, dateOfBirthSearch = state1.currentDob, genderSearch = state1.currentGender) {
    try {
        // Update state
        state1.currentPage = page;
        state1.currentNameSearch = nameSearch;
        state1.currentDob = dateOfBirthSearch;
        state1.currentGender = genderSearch;

        // Build API URL
        let apiUrl = `${baseAPI}&page=${state1.currentPage}&pageSize=${pageSize}`;
        if (nameSearch) apiUrl += `&name=${encodeURIComponent(nameSearch)}`;
        if (dateOfBirthSearch) apiUrl += `&dob=${encodeURIComponent(dateOfBirthSearch)}`;
        if (genderSearch) apiUrl += `&gender=${encodeURIComponent(genderSearch)}`;

        // Show loading state
        const container = document.getElementById("infor-patient");
        if (!container) throw new Error("Container element not found");
        container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

        // Fetch data
        const response = await fetch(apiUrl, { method: "GET" });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const patients = (data.patients || []).filter(patient => patient.status !== 'Disable'); // Lọc ở frontend
        const totalPages = data.totalPages || 1;

        // Render table
        let patientTable = `
        <div class="card-body pt-0">
            <div class="table-responsive">
                <table class="table border-end border-start align-middle mb-0 rounded">
                    <thead class="table-dark">
                        <tr>
                            <th>No.</th>
                            <th>Patient Name</th>
                            <th>Date Of Birth</th>
                            <th>Phone Number</th>
                            <th>Gender</th>
                            <th>Address</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${patients.map((patient, index) => createPatientRow(patient, index)).join("")}
                    </tbody>
                </table>
            </div>
        </div>
        `;

        // Render pagination
        const paginationHTML = `
        <div class="card-footer pt-0 row">
            <div class="d-flex justify-content-start col-md-6 mt-4">
                <button class="btn btn-secondary text-white me-3" type="button"
                        ${state1.currentPage === 1 ? "disabled" : ""}
                        data-page="${state1.currentPage - 1}">
                    <span class="btn-inner">
                        <span class="text d-inline-block align-middle">Previous</span>
                        <span class="icon d-inline-block align-middle ms-1 ps-2">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                <path fill-rule="evenodd" d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
                            </svg>
                        </span>
                    </span>
                </button>
                <span class="align-self-center me-3">Page ${state1.currentPage} of ${totalPages}</span>
                <button class="btn btn-primary me-3" type="button"
                        ${state1.currentPage === totalPages ? "disabled" : ""}
                        data-page="${state1.currentPage + 1}">
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
                <option value="6" ${pageSize === 6 ? 'selected' : ''}>6</option>
                <option value="10" ${pageSize === 10 ? 'selected' : ''}>10</option>
                <option value="15" ${pageSize === 15 ? 'selected' : ''}>15</option>
                <option value="20" ${pageSize === 20 ? 'selected' : ''}>20</option>
                <option value="30" ${pageSize === 30 ? 'selected' : ''}>All</option>
            </select>
            </div>
            </div>
        </div>
        
        `;

        // Update container
        container.innerHTML = patients.length ? patientTable + paginationHTML : '<p>No Patients found.</p>';

        // Attach event listeners for edit buttons
        container.querySelectorAll(".edit-btn1").forEach(button => {
            button.addEventListener("click", function(e) {
                const patient = JSON.parse(this.dataset.patient);
                populateEditForm(patient);
            });
        });

        // Attach event listeners for delete buttons
        container.querySelectorAll(".delete-btn1").forEach(button => {
            button.addEventListener("click", async function(e) {
                e.preventDefault();
                const id = this.dataset.id;
                console.log("Deleting patient with ID:", id);
                Swal.fire({
                    title: "Are you sure?",
                    text: "You want to delete this patient?",
                    icon: "error",
                    showCancelButton: true,
                    backdrop: `rgba(60,60,60,0.8)`,
                    confirmButtonText: "Yes, delete it!",
                    confirmButtonColor: "#c03221"
                }).then(async (result) => {
                    if (result.isConfirmed) {
                        try {
                            const response = await fetch(`${baseAPI.split('?')[0]}/${id}`, {
                                method: "DELETE"
                            });
                            if (!response.ok) {
                                const errorData = await response.json().catch(() => ({}));
                                if (response.status === 404) {
                                    Swal.fire("Error!", "Patient not found.", "error");
                                } else if (response.status === 400) {
                                    Swal.fire("Error!", "Invalid request.", "error");
                                } else {
                                    Swal.fire("Error!", errorData.error || "Could not delete patient.", "error");
                                }
                                throw new Error(`Failed to delete patient: ${response.status}`);
                            }
                            Swal.fire("Deleted!", "Your patient has been deleted.", "success");
                            // Làm mới danh sách với các tham số tìm kiếm hiện tại
                            await displayPatient(state1.currentPage, state1.currentNameSearch, state1.currentDob, state1.currentGender);
                        } catch (error) {
                            console.error("Delete error:", error);
                            Swal.fire("Error!", "Could not delete patient.", "error");
                        }
                    }
                });
            });
        });

        // Attach event listeners for pagination
        container.querySelectorAll("button[data-page]").forEach(button => {
            button.addEventListener("click", () => {
                const newPage = parseInt(button.dataset.page);
                displayPatient(newPage, state1.currentNameSearch, state1.currentDob, state1.currentGender);
            });
        });

        const pageSizeSelect = container.querySelector("#pageSize");
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener("change", (e) => {
                pageSize = parseInt(e.target.value);
                displayPatient(1, state1.currentNameSearch, state1.currentDob, state1.currentGender);
            });
        }

    } catch (error) {
        console.error("Error fetching or displaying patients:", error);
        const container = document.getElementById("infor-patient");
        if (container) {
            container.innerHTML = `<p class="text-danger">Error: ${error.message}. <button class="btn btn-link p-0" onclick="displayPatient()">Retry</button></p>`;
        }
    }
}

function populateEditForm(patient) {
    const namePatient = document.getElementById("namePatient");
    const dateOfBirth = document.getElementById("dateOfBirth");
    const gender = document.getElementById("gender");
    const phonePatient = document.getElementById("phonePatient");
    const address = document.getElementById("address");

    const form = document.querySelector("#offcanvasEncounterEditConfirm form");

    if (namePatient) namePatient.value = `${sanitizeHTML(patient.fullName)}`;
    if (dateOfBirth) dateOfBirth.value = formatDateToYYYYMMDD(patient.dob);
    if (gender) gender.value = sanitizeHTML(patient.gender || "");
    if (phonePatient) phonePatient.value = sanitizeHTML(patient.phone);
    if (address) address.value = sanitizeHTML(patient.address);

    let hiddenIdInput = form ? form.querySelector("#patientId") : null;
    if (!hiddenIdInput && form) {
        hiddenIdInput = document.createElement("input");
        hiddenIdInput.type = "hidden";
        hiddenIdInput.id = "patientId";
        form.appendChild(hiddenIdInput);
    }
    if (hiddenIdInput) hiddenIdInput.value = patient.id;
}

async function handleFormSubmission(event) {
    event.preventDefault();
    const form = event.target;
    const patientId = form.querySelector("#patientId").value;
    const namePatient = form.querySelector("#namePatient").value;
    const dob = form.querySelector("#dateOfBirth").value;
    const gender = form.querySelector("#gender").value;
    const phonePatient = form.querySelector("#phonePatient").value;
    const address = form.querySelector("#address").value;

    if (!patientId || isNaN(patientId)) {
        Swal.fire("Error!", "Invalid patient ID", "error");
        return;
    }

    if (!namePatient || namePatient.length > 100 || /\s{2,}/.test(namePatient)) {
        Swal.fire("Error!", "Full name must be max 100 characters and contain single spaces only", "error");
        return;
    }

    if (!phonePatient || !/^[0][1-9]{9}$/.test(phonePatient)) {
        Swal.fire("Error!", "Phone number must be exactly 10 digits and start with 0", "error");
        return;
    }

    if (address.length > 225) {
        Swal.fire("Error!", "Address must not exceed 225 characters", "error");
        return;
    }

    try {
        const patientData = {
            fullName: namePatient,
            dob: dob,
            gender: gender || null,
            phone: phonePatient,
            address: address,
            status : 'Enable'
        };
        const url = `http://localhost:8080/SWP_back_war_exploded/api/patient/${patientId}`;
        
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(patientData)
        });

        if (response.status == 500) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Patient already ${patientData.fullName}`);
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        Swal.fire("Success!", "Patient updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasEncounterEditConfirm")).hide();
        await displayPatient(state1.currentPage, state1.currentNameSearch, state1.currentDob, state1.currentGender);
    } catch (error) {
        console.error("Update error:", error);
        Swal.fire("Error!", `Could not update patient: ${error.message}`, "error");
    }
}

async function handleAddPatientSubmission(event) {
    event.preventDefault();
    const form = event.target;
    const namePatient = form.querySelector("#namePatient1").value;
    const dob = form.querySelector("#dateOfBirth1").value;
    const gender = form.querySelector("#gender1").value;
    const phonePatient = form.querySelector("#phonePatient1").value;
    const address = form.querySelector("#address1").value;

    // Validation
    if (!namePatient || namePatient.length > 100 || /\s{2,}/.test(namePatient)) {
        Swal.fire("Error!", "Full name must be max 100 characters and contain single spaces only", "error");
        return;
    }

    if (!dob) {
        Swal.fire("Error!", "Date of birth is required", "error");
        return;
    }

    if (!gender || gender === "") {
        Swal.fire("Error!", "Gender is required", "error");
        return;
    }

    if (!phonePatient || !/^[0][1-9]{9}$/.test(phonePatient)) {
        Swal.fire("Error!", "Phone number must be exactly 10 digits and start with 0", "error");
        return;
    }

    if (!address || address.length > 225) {
        Swal.fire("Error!", "Address must not exceed 225 characters", "error");
        return;
    }

    try {
        const patientData = {
            fullName: namePatient,
            dob: dob,
            gender: gender,
            phone: phonePatient,
            address: address,
            status: "Enable"
        };
        const url = `http://localhost:8080/SWP_back_war_exploded/api/patient/?accountPatientId=${account.accountPatientId}`;
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(patientData)
        });

        if (response.status == 500) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Patient already ${patientData.fullName}`);
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        Swal.fire("Success!", "Patient added successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasPatientAdd")).hide();
        await displayPatient(state1.currentPage, state1.currentNameSearch, state1.currentDob, state1.currentGender);
    } catch (error) {
        console.error("Add error:", error);
        Swal.fire("Error!", `Could not add patient: ${error.message}`, "error");
    }
}

function triggerSearch() {
    const nameInput = document.getElementById("patient-search-name");
    const dobInput = document.getElementById("patient-search-date");
    const genderInput = document.getElementById("gender-search");

    const nameValue = nameInput?.value.trim() || "";
    const dobValue = dobInput?.value.trim() || "";
    const genderValue = genderInput?.value.trim() || "";

    displayPatient(1, nameValue, dobValue, genderValue);
}

document.addEventListener("DOMContentLoaded", () => {
    displayPatient(1);

    // Attach search event listeners
    const searchButton = document.getElementById("search-button");
    if (searchButton) {
        searchButton.addEventListener("click", triggerSearch);
    }

    // Debounce search inputs
    const debounce = (func, delay) => {
        let timeoutId;
        return (...args) => {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func(...args), delay);
        };
    };

    const inputs = [
        document.getElementById("patient-search-name"),
        document.getElementById("patient-search-date"),
        document.getElementById("gender-search")
    ];
    inputs.forEach(input => {
        if (input) {
            input.addEventListener("input", debounce(triggerSearch, 300));
        }
    });
    const editForm1 = document.querySelector("#offcanvasEncounterEditConfirm form");
    if (editForm1) {
        editForm1.addEventListener("submit", handleFormSubmission);
    }

    const editForm2 = document.querySelector("#offcanvasPatientAdd form");
    if (editForm2) {
        editForm2.addEventListener("submit", handleAddPatientSubmission);
    }

    // Attach event listener for page size change
    const pageSizeSelect = document.getElementById("pageSize");
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener("change", (e) => {
            pageSize = parseInt(e.target.value);
            displayPatient(1, state1.currentNameSearch, state1.currentDob, state1.currentGender);
        });
    }

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