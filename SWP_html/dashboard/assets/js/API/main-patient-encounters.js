const accountString = localStorage.getItem('account');
const account = JSON.parse(accountString);

const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${account.accountPatientId}`;
let pageSize = 6;
const state = {
    currentPage: 1,
    currentNameSearch: "",
    currentDateAppointment: "",
    currentStatus: ""
};

function sanitizeHTML(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
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
    return dateTime ? new Date(dateTime).toLocaleDateString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' }) : "N/A";
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

function createAppointmentRow(appointment, index) {
    const status = appointment.appointmentStatus;
    const isCompleted = status === "Completed";
    const isConfirmed = status === "Confirmed";
    const isPending = status === "Pending";
    const renderActionButtons = () => {
        if (isCompleted) {
            return `
                <a class="d-inline-block pe-2 view-btn" data-bs-toggle="offcanvas"
                   href="#offcanvasEncounterView" aria-controls="offcanvasEncounterView" aria-label="View Appointment"
                   data-appointment='${JSON.stringify(appointment)}'>
                    <span class="text-success">
                        <svg class="icon-32" width="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path fill-rule="evenodd" clip-rule="evenodd" 
                                  d="M15.1614 12.0531C15.1614 13.7991 13.7454 15.2141 11.9994 15.2141C10.2534 15.2141 8.83838 13.7991 8.83838 12.0531C8.83838 10.3061 10.2534 8.89111 11.9994 8.89111C13.7454 8.89111 15.1614 10.3061 15.1614 12.0531Z" 
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path fill-rule="evenodd" clip-rule="evenodd" 
                                  d="M11.998 19.355C15.806 19.355 19.289 16.617 21.25 12.053C19.289 7.48898 15.806 4.75098 11.998 4.75098H12.002C8.194 4.75098 4.711 7.48898 2.75 12.053C4.711 16.617 8.194 19.355 12.002 19.355H11.998Z" 
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </span>
                </a>
            `;
        } else if (isConfirmed) {
            return `
                <a class="d-inline-block pe-2 edit-btn1" data-bs-toggle="offcanvas"
                   href="#offcanvasEncounterEditConfirm" aria-controls="offcanvasEncounterEditConfirm" aria-label="Edit Appointment"
                   data-appointment='${JSON.stringify(appointment)}'>
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
            `;
        } else if(isPending) {
            return `
                <a class="d-inline-block pe-2 edit-btn2" data-bs-toggle="offcanvas"
                   href="#offcanvasEncounterEditPending" aria-controls="offcanvasEncounterEditPending" aria-label="Edit Appointment"
                   data-appointment='${JSON.stringify(appointment)}'>
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
                <a href="#" class="d-inline-block ps-2 delete-btn" data-id="${appointment.appointmentId}" aria-label="Delete Appointment">
                    <span class="text-danger">
                        <svg width="15" height="16" viewBox="0 0 15 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12.4938 6.10107C12.4938 6.10107 12.0866 11.1523 11.8503 13.2801C11.7378 14.2963 11.1101 14.8918 10.0818 14.9106C8.12509 14.9458 6.16609 14.9481 4.21009 14.9068C3.22084 14.8866 2.60359 14.2836 2.49334 13.2853C2.25559 11.1388 1.85059 6.10107 1.85059 6.10107"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M13.5312 3.67969H0.812744" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M11.0804 3.67974C10.4917 3.67974 9.98468 3.26349 9.86918 2.68674L9.68693 1.77474C9.57443 1.35399 9.19343 1.06299 8.75918 1.06299H5.58443C5.15018 1.06299 4.76918 1.35399 4.65668 1.77474L4.47443 2.68674C4.35893 3.26349 3.85193 3.67974 3.26318 3.67974"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
            `;
        } else {
            return `<a class="d-inline-block pe-2" data-bs-toggle="offcanvas">
                    <span class="text-warning">
                    <svg class="icon-32" width="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path opacity="0.4" d="M4.72251 21.1672C4.70951 21.1672 4.69751 21.1672 4.68351 21.1662C4.36851 21.1502 4.05951 21.0822 3.76551 20.9632C2.31851 20.3752 1.62051 18.7222 2.20751 17.2762L9.52851 4.45025C9.78051 3.99425 10.1625 3.61225 10.6285 3.35425C11.9935 2.59825 13.7195 3.09525 14.4745 4.45925L21.7475 17.1872C21.9095 17.5682 21.9785 17.8782 21.9955 18.1942C22.0345 18.9502 21.7765 19.6752 21.2705 20.2362C20.7645 20.7972 20.0695 21.1282 19.3145 21.1662L4.79451 21.1672H4.72251Z" fill="currentColor"></path>
                    <path fill-rule="evenodd" clip-rule="evenodd" d="M11.1245 10.0208C11.1245 9.53875 11.5175 9.14575 11.9995 9.14575C12.4815 9.14575 12.8745 9.53875 12.8745 10.0208V12.8488C12.8745 13.3318 12.4815 13.7238 11.9995 13.7238C11.5175 13.7238 11.1245 13.3318 11.1245 12.8488V10.0208ZM11.1245 16.2699C11.1245 15.7849 11.5175 15.3899 11.9995 15.3899C12.4815 15.3899 12.8745 15.7799 12.8745 16.2589C12.8745 16.7519 12.4815 17.1449 11.9995 17.1449C11.5175 17.1449 11.1245 16.7519 11.1245 16.2699Z" fill="currentColor"></path>
                    </svg>
                    </span>
                </a>`
        }
    };

    const renderStatus = () =>{
        if(isCompleted){
            return `<span class="badge bg-success-subtle p-2 text-success">Complete</span>`;
        } else if (isConfirmed){
            return `<span class="badge bg-primary-subtle p-2 text-primary">Confirmed</span>`;
        }else if(isPending){
            return `<span class="badge bg-warning-subtle p-2 text-warning">Pending</span>`;
        }else{
            return `<span class="badge bg-danger-subtle p-2 text-danger">Cancelled</span>`;
        }
    };

    return `
        <tr data-item="list">
            <th scope="row">${index + 1}</th>
            <td>
                <div class="d-flex align-items-center gap-3">
                    <h5 class="mb-0">${sanitizeHTML(appointment.fullName)}</h5>
                </div>
            </td>
            <td>
                <div class="d-flex align-items-center gap-3">
                    <img src="${sanitizeHTML(appointment.doctor.img)}" 
                         class="img-fluid flex-shrink-0 icon-40 object-fit-cover"
                         alt="doctor-image">
                    <p class="mb-0">Dr.${sanitizeHTML(appointment.doctor.fullName)}</p>
                </div>
            </td>
            <td>${sanitizeHTML(appointment.doctor.department)}</td>
            <td>${formatDateTime(appointment.appointmentDateTime)}</td>
            <td>${sanitizeHTML(appointment.note || "N/A")}</td>
            <td>${isCompleted ? formatDateTime(appointment.appointmentDateTime) : "N/A"}</td>
            <td>${renderStatus()}</td>
            <td>
                ${renderActionButtons()}
            </td>
        </tr>`;
}

async function displayAppointment(page = 1, nameSearch = state.currentNameSearch, dateAppointmentSearch = state.currentDateAppointment, statusSearch = state.currentStatus) {
    try {
        // Update state
        state.currentPage = page;
        state.currentNameSearch = nameSearch;
        state.currentDateAppointment = dateAppointmentSearch;
        state.currentStatus = statusSearch;

        // Build API URL
        let apiUrl = `${baseAPI}&page=${state.currentPage}&pageSize=${pageSize}`;
        if (nameSearch) apiUrl += `&name=${encodeURIComponent(nameSearch)}`;
        if (dateAppointmentSearch) apiUrl += `&appointmentDateTime=${encodeURIComponent(dateAppointmentSearch)}`;
        if (statusSearch) apiUrl += `&status=${encodeURIComponent(statusSearch)}`;

        // Show loading state
        const container = document.getElementById("infor-Appointment");
        if (!container) throw new Error("Container element not found");
        container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

        // Fetch data
        const response = await fetch(apiUrl, { method: "GET" });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const appointments = data.appointments || [];
        const totalPages = data.totalPages || 1;

        // Render table
        let appointmentTable = `
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table border-end border-start align-middle mb-0 rounded">
                        <thead class="table-dark">
                            <tr>
                                <th scope="col">No.</th>
                                <th scope="col">Patient</th>
                                <th scope="col">Doctors</th>
                                <th scope="col">Clinic Name</th>
                                <th scope="col">Appointment Date</th>
                                <th scope="col">Add Note</th>
                                <th scope="col">Last Visit</th>
                                <th scope="col">Status</th>
                                <th scope="col">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${appointments.map((appointment, index) => createAppointmentRow(appointment, index)).join("")}
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
                        ${state.currentPage === 1 ? "disabled" : ""}
                        data-page="${state.currentPage - 1}">
                    <span class="btn-inner">
                        <span class="text d-inline-block align-middle">Previous</span>
                        <span class="icon d-inline-block align-middle ms-1 ps-2">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                <path fill-rule="evenodd" d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
                            </svg>
                        </span>
                    </span>
                </button>
                <span class="align-self-center me-3">Page ${state.currentPage} of ${totalPages}</span>
                <button class="btn btn-primary me-3" type="button"
                        ${state.currentPage === totalPages ? "disabled" : ""}
                        data-page="${state.currentPage + 1}">
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
        container.innerHTML = appointments.length ? appointmentTable + paginationHTML : '<p>No Appointments found.</p>';

        // Attach event listeners for edit buttons
        container.querySelectorAll(".edit-btn1").forEach(button => {
            button.addEventListener("click", function(e) {
                const appointment = JSON.parse(this.dataset.appointment);
                populateEditFormConfirm(appointment);
            });
        });

        // Attach event listeners for edit buttons
        container.querySelectorAll(".edit-btn2").forEach(button => {
            button.addEventListener("click", function(e) {
                const appointment = JSON.parse(this.dataset.appointment);
                populateEditForm(appointment);
            });
        });


        // Attach event listeners for edit buttons
        container.querySelectorAll(".view-btn").forEach(button => {
            button.addEventListener("click", function(e) {
                const appointment = JSON.parse(this.dataset.appointment);
                populateView(appointment);
            });
        });

        // Attach event listeners for delete buttons
        container.querySelectorAll(".delete-btn").forEach(button => {
            button.addEventListener("click", async function(e) {
                e.preventDefault();
                const appointmentId = this.dataset.id;
                Swal.fire({
                    title: "Are you sure?",
                    text: "You want to delete this appointment?",
                    icon: "error",
                    showCancelButton: true,
                    backdrop: `rgba(60,60,60,0.8)`,
                    confirmButtonText: "Yes, delete it!",
                    confirmButtonColor: "#c03221"
                }).then(async (result) => {
                    if (result.isConfirmed) {
                        try {
                            const response = await fetch(`${baseAPI.split('?')[0]}/${appointmentId}`, {
                                method: "DELETE"
                            });
                            if (!response.ok) {
                                throw new Error("Failed to delete appointment");
                            }
                            Swal.fire("Deleted!", "Your appointment has been deleted.", "success");
                            await displayAppointment(state.currentPage, state.currentNameSearch, state.currentDateAppointment, state.currentStatus);
                        } catch (error) {
                            Swal.fire("Error!", "Could not delete appointment. Please try again.", "error");
                            console.error("Delete error:", error);
                        }
                    }
                });
            });
        });

        // Attach event listeners for pagination
        container.querySelectorAll("button[data-page]").forEach(button => {
            button.addEventListener("click", () => {
                const newPage = parseInt(button.dataset.page);
                displayAppointment(newPage, state.currentNameSearch, state.currentDateAppointment, state.currentStatus);
            });
        });

        const pageSizeSelect = container.querySelector("#pageSize");
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener("change", (e) => {
                pageSize = parseInt(e.target.value);
                displayAppointment(1, state.currentNameSearch, state.currentDateAppointment, state.currentStatus);
            });
        }

    } catch (error) {
        console.error("Error fetching or displaying appointments:", error);
        const container = document.getElementById("infor-Appointment");
        if (container) {
            container.innerHTML = `<p class="text-danger">Error: ${error.message}. <button class="btn btn-link p-0" onclick="displayAppointment()">Retry</button></p>`;
        }
    }
}

function populateEditFormConfirm(appointment) {
    // Populate form fields
    const namePatient = document.getElementById("namePatient");
    const dateOfBirth = document.getElementById("dateOfBirth");
    const gender = document.getElementById("gender");
    const phonePatient = document.getElementById("phonePatient");
    const address = document.getElementById("address");

    const form = document.querySelector("#offcanvasEncounterEditConfirm form");

    if (namePatient) namePatient.value = `${sanitizeHTML(appointment.fullName)}`;
    if (dateOfBirth) dateOfBirth.value = formatDateToYYYYMMDD(appointment.dob);
    if (gender) gender.value = sanitizeHTML(appointment.gender || "");
    if (phonePatient) phonePatient.value = sanitizeHTML(appointment.phone);
    if (address) address.value = sanitizeHTML(appointment.address);

    // Store patient ID in a hidden input
    let hiddenIdInput = form ? form.querySelector("#patientId") : null;
    if (!hiddenIdInput && form) {
        hiddenIdInput = document.createElement("input");
        hiddenIdInput.type = "hidden";
        hiddenIdInput.id = "patientId";
        form.appendChild(hiddenIdInput);
    }
    if (hiddenIdInput) hiddenIdInput.value = appointment.patientId;
}

async function handleFormSubmissionConfirm(event) {
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
        if (!patientId || isNaN(patientId)) {
            throw new Error("Invalid patient ID");
        }

        const patientData = {
            fullName: namePatient,
            dob: dob,
            gender: gender || null,
            phone: phonePatient,
            address: address
        };
        const url = `http://localhost:8080/SWP_back_war_exploded/api/patient/${patientId}`;
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(patientData)
        });

        if (!response) {
            throw new Error("No response received from server");
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        Swal.fire("Success!", "Patient updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasEncounterEditConfirm")).hide();
        displayAppointment(state.currentPage);
    } catch (error) {
        console.error("Update error:", error);
        Swal.fire("Error!", `Could not update patient: ${error.message}`, "error");
    }
}

function populateView(appointment) {
    // Populate form fields
    const namePatient1 = document.getElementById("namePatient1");
    const dateOfBirth1 = document.getElementById("dateOfBirth1");
    const gender1 = document.getElementById("gender1");
    const phonePatient1 = document.getElementById("phonePatient1");
    const address1 = document.getElementById("address1");
    const nameDoctor1 = document.getElementById("nameDoctor1");
    const phoneDoctor1 = document.getElementById("phoneDoctor1");
    const emailDoctor1 = document.getElementById("emailDoctor1");
    const status1 = document.getElementById("status1");

    if (namePatient1) namePatient1.value = `${sanitizeHTML(appointment.fullName)}`;
    if (dateOfBirth1) dateOfBirth1.value = formatDateToYYYYMMDD(appointment.dob);
    if (gender1) gender1.value = sanitizeHTML(appointment.gender || "");
    if (phonePatient1) phonePatient1.value = sanitizeHTML(appointment.phone);
    if (address1) address1.value = sanitizeHTML(appointment.address);
    if (nameDoctor1) nameDoctor1.value = sanitizeHTML(`Dr.${appointment.doctor.fullName}`);
    if (phoneDoctor1) phoneDoctor1.value = sanitizeHTML(appointment.doctor.phone);
    if (emailDoctor1) emailDoctor1.value = sanitizeHTML(appointment.doctor.email);
    if (status1) status1.value = sanitizeHTML(appointment.appointmentStatus);
}

function triggerSearch() {
    const nameInput = document.getElementById("appointment-search-name");
    const dateInput = document.getElementById("appointment-search-date");
    const statusInput = document.getElementById("status-search");

    const nameValue = nameInput?.value.trim() || "";
    const dateValue = dateInput?.value.trim() || "";
    const statusValue = statusInput?.value.trim() || "";

    displayAppointment(1, nameValue, dateValue, statusValue);
}

document.addEventListener("DOMContentLoaded", () => {
    displayAppointment(1);

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
        document.getElementById("appointment-search-name"),
        document.getElementById("appointment-search-date"),
        document.getElementById("status-search")
    ];
    inputs.forEach(input => {
        if (input) {
            input.addEventListener("input", debounce(triggerSearch, 300));
        }
    });

    // Attach form submission handler
    const editForm1 = document.querySelector("#offcanvasEncounterEditConfirm form");
    if (editForm1) {
        editForm1.addEventListener("submit", handleFormSubmissionConfirm);
    }
    // Attach event listener for page size change
    const pageSizeSelect = document.getElementById("pageSize");
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener("change", (e) => {
            pageSize = parseInt(e.target.value);
            displayAppointment(1, state.currentNameSearch, state.currentDateAppointment, state.currentStatus);
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


const API_BASE_URL = 'http://localhost:8080/SWP_back_war_exploded/api';
// DOM elements
const departmentSelect = document.getElementById('departmentSelect');
const doctorSelect = document.getElementById('doctorSelect');
const dateForm = document.getElementById('dateForm');
const timeForm = document.getElementById('timeForm');
const selectedTime = document.getElementById('selectedTime');
const selectedDate = document.getElementById('selectedDate');
const calendar = document.getElementById('calendar');
const yearSelect = document.getElementById('yearSelect');
const monthSelect = document.getElementById('monthSelect');
const editPendingAppointmentForm = document.getElementById('editPendingAppointmentForm');
const appointmentIdInput = document.getElementById('appointmentId');
const noteInput = document.getElementById('note');

const patientIdInput = document.getElementById('patientId9');
const shiftSelect = document.getElementById('shiftSelect');
const receptionistIdInput = document.getElementById('receptionistId');

let doctorWorkingDates = [];
let selectedSlot = null;

// Populate year options
function populateYears() {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i <= currentYear + 1; i++) {
        const option = document.createElement('option');
        option.value = i;
        option.textContent = i;
        yearSelect.appendChild(option);
    }
    yearSelect.value = currentYear;
}

// Populate month options
function populateMonths() {
    const months = [
        'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
        'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
    ];
    const currentMonth = new Date().getMonth();
    months.forEach((month, index) => {
        const option = document.createElement('option');
        option.value = index;
        option.textContent = month;
        monthSelect.appendChild(option);
    });
    monthSelect.value = currentMonth;
}

// Check if date is in the past
function isPastDate(year, month, day) {
    const selectedDate = new Date(Date.UTC(year, month, day));
    const today = new Date();
    today.setUTCHours(0, 0, 0, 0);
    return selectedDate < today;
}

// Check if time is in the past for today
function isPastTime(time, selectedDateStr) {
    const today = new Date();
    const selected = new Date(`${selectedDateStr}T${time}:00+07:00`);
    return selected < today;
}

// Check if date is a working date
function isWorkingDate(year, month, day) {
    const dateStr = `${year}-${String(parseInt(month) + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return doctorWorkingDates.includes(dateStr);
}

// Generate dynamic calendar
function generateCalendar(year, month) {
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDay = firstDay.getDay();
    let table = '<table><tr><th>T2</th><th>T3</th><th>T4</th><th>T5</th><th>T6</th><th>T7</th><th>CN</th></tr><tr>';
    let dayCount = 1;

    for (let i = 0; i < 6; i++) {
        for (let j = 0; j < 7; j++) {
            if ((i === 0 && j < startingDay) || dayCount > daysInMonth) {
                table += '<td></td>';
            } else {
                const isPast = isPastDate(year, month, dayCount);
                const isWorking = isWorkingDate(year, month, dayCount);
                table += `<td role="button" tabindex="0" class="${isPast || !isWorking ? 'disabled' : ''}">${dayCount}</td>`;
                dayCount++;
            }
        }
        table += '</tr><tr>';
        if (dayCount > daysInMonth) break;
    }
    table += '</tr></table>';
    calendar.innerHTML = table;

    const days = document.querySelectorAll('#calendar table td');
    days.forEach(day => {
        if (day.textContent && !day.classList.contains('disabled')) {
            const handleSelection = () => {
                days.forEach(d => d.classList.remove('selected'));
                day.classList.add('selected');
                timeForm.style.display = 'block';
                resetTimeSelection();
                const selectedDay = day.textContent.padStart(2, '0');
                const selectedMonth = String(parseInt(month) + 1).padStart(2, '0');
                selectedDate.value = `${year}-${selectedMonth}-${selectedDay}`;
                updateTimeSlots();
            };
            day.addEventListener('click', handleSelection);
            day.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleSelection();
                }
            });
        }
    });
}

// Debounce function to prevent rapid API calls
function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

// Update time slots
async function updateTimeSlots() {
    const timeSlots = document.querySelectorAll('.time-slot');
    const selectedDateStr = selectedDate.value;
    const doctorId = doctorSelect.value;
    const today = new Date();
    const isToday = new Date(selectedDateStr).toDateString() === today.toDateString();

    timeSlots.forEach(slot => {
        slot.classList.remove('disabled');
        if (isToday && isPastTime(slot.value, selectedDateStr)) {
            slot.classList.add('disabled');
        }
    });

    if (!doctorId || !selectedDateStr) {
        console.warn('Skipping fetch: doctorId or date is missing', { doctorId, selectedDateStr });
        return;
    }

    try {
        const url = `${API_BASE_URL}/doctor-availability?doctorId=${encodeURIComponent(doctorId)}&date=${encodeURIComponent(selectedDateStr)}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${await response.text()}`);
        }

        const bookedTimes = await response.json();
        timeSlots.forEach(slot => {
            if (bookedTimes.includes(slot.value)) {
                slot.classList.add('disabled');
            }
        });
    } catch (error) {
        console.error('Error fetching booked times:', error.message, error);
        alert('Không thể tải khung giờ. Vui lòng thử lại sau.');
    }
}

const debouncedUpdateTimeSlots = debounce(updateTimeSlots, 300);

// Reset time selection
function resetTimeSelection() {
    const timeSlots = document.querySelectorAll('.time-slot');
    timeSlots.forEach(slot => slot.classList.remove('selected'));
    selectedTime.value = '';
    if (selectedSlot) selectedSlot.classList.remove('selected');
    selectedSlot = null;
}

// Fetch doctor's working dates
async function fetchDoctorWorkingDates(doctorId) {
    if (!doctorId) {
        doctorWorkingDates = [];
        generateCalendar(yearSelect.value, monthSelect.value);
        return;
    }

    try {
        const url = `${API_BASE_URL}/doctor-availability?doctorId=${encodeURIComponent(doctorId)}&action=getWorkingDates`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${await response.text()}`);
        }

        doctorWorkingDates = await response.json();
        generateCalendar(yearSelect.value, monthSelect.value);
    } catch (error) {
        console.error('Error fetching working dates:', error.message, error);
        alert('Không thể tải ngày làm việc. Vui lòng thử lại sau.');
        doctorWorkingDates = [];
        generateCalendar(yearSelect.value, monthSelect.value);
    }
}

// Load doctors
async function loadDoctors(department = "") {
    doctorSelect.innerHTML = '<option value="">Đang tải...</option>';
    doctorSelect.disabled = true;

    let url = `${API_BASE_URL}/all-doctors`;
    if (department) url += `?department=${encodeURIComponent(department)}`;

    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${await response.text()}`);
        }
        const data = await response.json();
        doctorSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
        data.doctors.forEach(doc => {
            const option = document.createElement('option');
            option.value = doc.ID;
            option.textContent = doc.fullName;
            option.dataset.department = doc.department;
            doctorSelect.appendChild(option);
        });
        doctorSelect.disabled = false;
    } catch (error) {
        console.error('Error loading doctors:', error.message, error);
        alert('Không thể tải danh sách bác sĩ. Vui lòng thử lại sau.');
        doctorSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
    }
}

function populateEditForm(appointment) {
    appointmentIdInput.value = appointment.appointmentId || '';
    noteInput.value = appointment.note || '';
    const department = appointment.doctor?.department || '';
    departmentSelect.value = department;
    shiftSelect.value = appointment.shift || '';
    patientIdInput.value = appointment.patientId || '';
    receptionistIdInput.value = appointment.receptionistId || '';

    loadDoctors(department).then(() => {
        doctorSelect.value = appointment.doctor?.ID || '';
        doctorSelect.disabled = false;

        fetchDoctorWorkingDates(appointment.doctor?.ID).then(() => {
    
            if (appointment.appointmentDateTime) {
                const date = new Date(appointment.appointmentDateTime);
                if (isNaN(date)) {
                    console.error('Invalid appointmentDateTime:', appointment.appointmentDateTime);
                    return;
                }

                yearSelect.value = date.getFullYear();
                monthSelect.value = date.getMonth();

                
                generateCalendar(yearSelect.value, monthSelect.value);

                
                const day = String(date.getDate());
                const selectedMonth = String(date.getMonth() + 1).padStart(2, '0');
                selectedDate.value = `${date.getFullYear()}-${selectedMonth}-${day}`;

                const dayCell = Array.from(document.querySelectorAll('#calendar td')).find(
                    td => td.textContent === day && !td.classList.contains('disabled')
                );
                if (dayCell) {
                    dayCell.classList.add('selected');
                    dateForm.style.display = 'block';
                    timeForm.style.display = 'block';
                } else {
                    console.warn('Selected day is disabled or not found:', day);
                }

                debouncedUpdateTimeSlots().then(() => {
                    const hours = String(date.getHours()).padStart(2, '0');
                    const minutes = String(date.getMinutes()).padStart(2, '0');
                    const timeValue = `${hours}:${minutes}`;
                    const slot = Array.from(document.querySelectorAll('.time-slot')).find(
                        slot => slot.value === timeValue && !slot.classList.contains('disabled')
                    );
                    if (slot) {
                        slot.classList.add('selected');
                        selectedSlot = slot;
                        selectedTime.value = timeValue;
                    } else {
                        console.warn('Selected time slot is disabled or not found:', timeValue);
                    }
                });
            }
        });
    });
}

function resetEditForm() {
    editPendingAppointmentForm.reset();
    appointmentIdInput.value = '';
    departmentSelect.value = '';
    doctorSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
    doctorSelect.disabled = true;
    dateForm.style.display = 'none';
    timeForm.style.display = 'none';
    resetTimeSelection();
    selectedDate.value = '';
    doctorWorkingDates = [];
    generateCalendar(yearSelect.value, monthSelect.value);
}

populateYears();
populateMonths();
generateCalendar(new Date().getFullYear(), new Date().getMonth());

fetch(`${API_BASE_URL}/doctors/departments`)
    .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        return res.json();
    })
    .then(data => {
        data.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept;
            option.textContent = dept;
            departmentSelect.appendChild(option);
        });
    })
    .catch(error => {
        console.error('Error loading departments:', error.message, error);
        alert('Không thể tải danh sách chuyên khoa. Vui lòng thử lại sau.');
    });

departmentSelect.addEventListener('change', () => {
    loadDoctors(departmentSelect.value);
});

doctorSelect.addEventListener('change', () => {
    if (doctorSelect.value) {
        const selectedOption = doctorSelect.querySelector(`option[value="${doctorSelect.value}"]`);
        if (selectedOption) departmentSelect.value = selectedOption.dataset.department;
        dateForm.style.display = 'block';
        fetchDoctorWorkingDates(doctorSelect.value);
    } else {
        dateForm.style.display = 'none';
        timeForm.style.display = 'none';
        resetTimeSelection();
        selectedDate.value = '';
        doctorWorkingDates = [];
        generateCalendar(yearSelect.value, monthSelect.value);
    }
});

yearSelect.addEventListener('change', () => {
    generateCalendar(yearSelect.value, monthSelect.value);
    timeForm.style.display = 'none';
    resetTimeSelection();
    selectedDate.value = '';
});

monthSelect.addEventListener('change', () => {
    generateCalendar(yearSelect.value, monthSelect.value);
    timeForm.style.display = 'none';
    resetTimeSelection();
    selectedDate.value = '';
});

const timeSlots = document.querySelectorAll('.time-slot');
timeSlots.forEach(slot => {
    slot.addEventListener('click', () => {
        if (slot.classList.contains('disabled')) return;
        if (selectedSlot) selectedSlot.classList.remove('selected');
        slot.classList.add('selected');
        selectedSlot = slot;
        selectedTime.value = slot.value;
    });
});


editPendingAppointmentForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!doctorSelect.value) {
        Swal.fire("Error!", "Please select a doctor", "error");
        return;
    }
    if (!selectedDate.value) {
        Swal.fire("Error!", "Please select a date", "error");
        return;
    }
    if (!selectedTime.value) {
        Swal.fire("Error!", "Please select time", "error");
        return;
    }
    if (noteInput.value.length > 500) {
        Swal.fire("Error!", "Notes must not exceed 500 characters", "error");
        return;
    }

    const dateTimeString = `${selectedDate.value} ${selectedTime.value}`;
    let selectedDateTime;
    try {
        selectedDateTime = new Date(`${dateTimeString}:00+07:00`);
        if (isNaN(selectedDateTime.getTime())) {
            throw new Error("Invalid date or time format");
        }
    } catch (error) {
        Swal.fire("Error!", "Invalid date or time format", "error");
        return;
    }

    const currentDateTime = new Date();
    if (selectedDateTime <= currentDateTime) {
        Swal.fire("Error!", "Cannot select date or time in the past", "error");
        return;
    }

    const formData = {
        appointmentId: appointmentIdInput.value,
        doctorId: doctorSelect.value,
        patientId: patientIdInput?.value || null, 
        date: selectedDate.value, 
        time: selectedTime.value, 
        shift: shiftSelect?.value || null, 
        receptionistId: receptionistIdInput?.value || null, 
        note: noteInput.value || null
    };

    try {
        const response = await fetch(`${API_BASE_URL}/patientAppointment/${appointmentIdInput.value}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (response.ok) {
            Swal.fire("Success!", "Appointment update successful.", "success");
            bootstrap.Offcanvas.getInstance(document.getElementById('offcanvasEncounterEditPending')).hide();
            resetEditForm();
            displayAppointment();
        } else {
            // Hiển thị thông báo lỗi chi tiết từ server
            Swal.fire("Error!", data.error || "Unable to update appointment", "error");
        }
    } catch (error) {
        Swal.fire("Error!", `Unable to update appointment: ${error.message}`, "error");
        console.error('Unable to update appointment:', error);
    }
});

document.getElementById('offcanvasEncounterEditPending').addEventListener('show.bs.offcanvas', (event) => {
    const appointmentData = event.relatedTarget.dataset.appointment
        ? JSON.parse(event.relatedTarget.dataset.appointment)
        : {};
    populateEditForm(appointmentData);
});

document.getElementById('offcanvasEncounterEditPending').addEventListener('hidden.bs.offcanvas', () => {
    resetEditForm();
});