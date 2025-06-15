
const storedAccountId = localStorage.getItem('accountId');
const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientAppointment/?accountPatientId=${storedAccountId}`;
const pageSize = 6;
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
    return dateTime ? new Date(dateTime).toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' }) : "N/A";
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

    const renderActionButtons = () => {
        if (isCompleted) {
            return `
                <a class="d-inline-block pe-2" data-bs-toggle="offcanvas"
                   href="#offcanvasEncounterView" aria-controls="offcanvasEncounterView" aria-label="View Appointment">
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
                <a class="d-inline-block pe-2 edit-btn" data-bs-toggle="offcanvas"
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
        } else {
            return `
                <a class="d-inline-block pe-2 edit-btn" data-bs-toggle="offcanvas"
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
        }
    };

    return `
        <tr data-item="list">
            <th scope="row">${index + 1}</th>
            <td>
                <div class="d-flex align-items-center gap-3">
                    <img src="${sanitizeHTML(appointment.doctor.img)}" 
                         class="img-fluid flex-shrink-0 icon-40 object-fit-cover"
                         alt="doctor-image">
                    <h5 class="mb-0">Dr.${sanitizeHTML(appointment.doctor.fullName)}</h5>
                </div>
            </td>
            <td>${sanitizeHTML(appointment.fullName)}</td>
            <td>${sanitizeHTML(appointment.doctor.department)}</td>
            <td>${formatDateTime(appointment.appointmentDateTime)}</td>
            <td>${sanitizeHTML(appointment.note || "N/A")}</td>
            <td>${isCompleted ? formatDateTime(appointment.appointmentDateTime) : "N/A"}</td>
            <td>${sanitizeHTML(appointment.appointmentStatus)}</td>
            <td>
                ${renderActionButtons()}
            </td>
        </tr>
    `;
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
                                <th scope="col">Doctors</th>
                                <th scope="col">Patient</th>
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
            <div class="d-flex justify-content-center mt-4">
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
        `;

        // Update container
        container.innerHTML = appointments.length ? appointmentTable + paginationHTML : '<p>No Appointments found.</p>';

        // Attach event listeners for edit buttons
        container.querySelectorAll(".edit-btn").forEach(button => {
            button.addEventListener("click", function(e) {
                const appointment = JSON.parse(this.dataset.appointment);
                populateEditFormConfirm(appointment);
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
                            this.closest('[data-item="list"]').remove();
                            Swal.fire("Deleted!", "Your appointment has been deleted.", "success");
                            if (container.querySelectorAll('[data-item="list"]').length === 0 && state.currentPage > 1) {
                                displayAppointment(state.currentPage - 1);
                            }
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

async function handleFormSubmission(event) {
    event.preventDefault();
    const form = event.target;
    const patientId = form.querySelector("#patientId").value;
    const namePatient = form.querySelector("#namePatient").value;
    const dateOfBirth = form.querySelector("#dateOfBirth").value;
    const gender = form.querySelector("#gender").value;
    const phonePatient = form.querySelector("#phonePatient").value;
    const address = form.querySelector("#address").value;
    try {
        const formData = new FormData();
        formData.append("fullName", namePatient);
        formData.append("dob", dateOfBirth);
        formData.append("gender", gender);
        formData.append("phone", phonePatient);
        formData.append("address", address);

        const response = await fetch(`${baseAPI.split('?')[0]}/${patientId}`, {
            method: "PUT",
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || "Failed to update patient update");
        }

        Swal.fire("Success!", "Patient updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasEncounterEditConfirm")).hide();
        displayAppointment(state.currentPage); // Refresh table
    } catch (error) {
        Swal.fire("Error!", `Could not update patient: ${error.message}`, "error");
        console.error("Update error:", error);
    }
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
    const editForm = document.querySelector("#offcanvasEncounterEditConfirm form");
    if (editForm) {
        editForm.addEventListener("submit", handleFormSubmission);
    }
});