const BASE_API = "http://localhost:8080/SWP_back_war_exploded/api/patient";

document.addEventListener("DOMContentLoaded", () => {
    let currentPage = 1;
    let pageSize = 10;

    // Initialize patient list
    fetchPatients(currentPage, pageSize);

    // Event delegation for edit and delete buttons
    document
        .getElementById("patient_infor")
        .addEventListener("click", (event) => {
            const target = event.target.closest(".edit-btn, .delete-btn");
            if (!target) return;

            const patientId = target.dataset.patientId;
            if (target.classList.contains("edit-btn")) {
                handleEdit(patientId);
            } else if (target.classList.contains("delete-btn")) {
                Swal.fire({
                    title: "Are you sure?",
                    text: "You want to update status this patient?",
                    icon: "error",
                    showCancelButton: true, 
                    backdrop: `rgba(60,60,60,0.8)`,
                    confirmButtonText: "Yes, update it!",
                    confirmButtonColor: "#c03221",
                }).then(async (result) => {
                    if (result.isConfirmed) {
                        handleDelete(patientId);
                    }
                });
            }
        });

    // Real-time search with debouncing
    const debounceSearch = debounce(() => {
        currentPage = 1;
        fetchPatients(currentPage, pageSize);
    }, 300);

    ["patient-search-name", "patient-search-date", "gender-search", "status-search"].forEach(
        (id) => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener(
                    id === "gender-search" || id === "status-search" ? "change" : "input",
                    debounceSearch
                );
            }
        }
    );

    // Items per page change
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) {
        itemsPerPage.addEventListener("change", (e) => {
            pageSize = parseInt(e.target.value, 10);
            currentPage = 1;
            fetchPatients(currentPage, pageSize);
        });
    }

    // Pagination controls
    const prevPage = document.getElementById("prevPage");
    if (prevPage) {
        prevPage.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                fetchPatients(currentPage, pageSize);
            }
        });
    }

    const nextPage = document.getElementById("nextPage");
    if (nextPage) {
        nextPage.addEventListener("click", () => {
            currentPage++;
            fetchPatients(currentPage, pageSize);
        });
    }

    // Form submissions
    const editPatientForm = document.getElementById("editPatientForm");
    if (editPatientForm) {
        editPatientForm.addEventListener("submit", (e) => {
            e.preventDefault();
            handleUpdate();
        });
    }

    const addPatientForm = document.getElementById("addPatient");
    if (addPatientForm) {
        addPatientForm.addEventListener("submit", (e) => {
            e.preventDefault();
            handleAdd();
        });
    }
});

async function fetchPatients(page, pageSize) {
    const searchName = document.getElementById("patient-search-name")?.value.trim() || "";
    const searchDobInput = document.getElementById("patient-search-date")?.value.trim() || "";
    const searchGender = document.getElementById("gender-search")?.value || "";
    const searchStatus = document.getElementById("status-search")?.value || "";

    const params = new URLSearchParams({
        viewType: "all",
        page,
        pageSize,
        name: searchName,
        dob: searchDobInput,
        gender: searchGender,
        status: searchStatus,
    });

    const url = `${BASE_API}?${params.toString()}`;
    console.log("Fetching URL:", url);

    try {
        const response = await fetch(url, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        if (data.patients) {
            renderPatients(data.patients);
            updatePagination(
                data.totalPages,
                data.currentPage,
                data.pageSize,
                data.totalPatient
            );
        } else {
            throw new Error(data.error || "Failed to fetch patients");
        }
    } catch (error) {
        showError(`Error fetching patients: ${error.message}`);
        console.error("Error:", error);
    }
}

function renderPatients(patients) {
    const tbody = document.getElementById("patient_infor");
    if (!tbody) {
        console.error("Patient table body not found");
        return;
    }

    tbody.innerHTML =
        patients.length === 0
            ? '<tr><td colspan="10" class="text-center">No patients found</td></tr>'
            : "";

    patients.forEach((patient, index) => {
        const enable = patient.status === 'Enable';

        const row = document.createElement("tr");
        row.innerHTML = `
            <th scope="row">${index + 1}</th>
            <td>${patient.id || "-"}</td>
            <td><h6 class="mb-0 text-body fw-500">${patient.fullName || "-"}</h6></td>
            <td>${calculateAge(patient.dob)}</td>
            <td>${patient.phone || "-"}</td>
            <td>${formatDate(patient.dob)}</td>
            <td>${patient.gender || "-"}</td>
            <td>${patient.address || "-"}</td>
            <td>${patient.status || "-"}</td>
            <td>
                <a data-bs-toggle="offcanvas" href="#offcanvasPatientEdit" aria-controls="offcanvasPatientEdit" class="d-inline-block pe-2 edit-btn" data-patient-id="${patient.id}">
                    <span class="text-success">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9.31055 14.3321H14.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M8.58501 1.84609C9.16674 1.15084 10.2125 1.04889 10.9222 1.6188C10.9614 1.64972 12.2221 2.62909 12.2221 2.62909C13.0017 3.10039 13.244 4.10233 12.762 4.86694C12.7365 4.90789 5.60896 13.8234 5.60896 13.8234C5.37183 14.1192 5.01187 14.2938 4.62718 14.298L1.89765 14.3323L1.28265 11.7292C1.1965 11.3632 1.28265 10.9788 1.51978 10.683L8.58501 1.84609Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M7.26562 3.50073L11.3548 6.64108" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
                ${enable ? 
                `<a href="#" class="d-inline-block ps-2 delete-btn" data-patient-id="${patient.id}">
                    <span class="text-danger">
                        <svg class="icon-22" width="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16.4232 9.4478V7.3008C16.4232 4.7878 14.3852 2.7498 11.8722 2.7498C9.35925 2.7388 7.31325 4.7668 7.30225 7.2808V7.3008V9.4478" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M15.683 21.2497H8.042C5.948 21.2497 4.25 19.5527 4.25 17.4577V13.1687C4.25 11.0737 5.948 9.37671 8.042 9.37671H15.683C17.777 9.37671 19.475 11.0737 19.475 13.1687V17.4577C19.475 19.5527 17.777 21.2497 15.683 21.2497Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path d="M11.8628 14.2026V16.4236" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </span>
                </a>`:
                `<a href="#" class="d-inline-block ps-2 delete-btn" data-patient-id="${patient.id}">
                    <span class="text-success">
                        <svg class="icon-22" width="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16.4242 5.56204C15.8072 3.78004 14.1142 2.50004 12.1222 2.50004C9.60925 2.49004 7.56325 4.51804 7.55225 7.03104V7.05104V9.19804" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M15.933 21.0005H8.292C6.198 21.0005 4.5 19.3025 4.5 17.2075V12.9195C4.5 10.8245 6.198 9.12646 8.292 9.12646H15.933C18.027 9.12646 19.725 10.8245 19.725 12.9195V17.2075C19.725 19.3025 18.027 21.0005 15.933 21.0005Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path d="M12.1128 13.9526V16.1746" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </span>
                </a>`}
                
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function handleEdit(patientId) {
    try {
        const response = await fetch(`${BASE_API}/${patientId}`, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const patient = data.patient;
        if (patient) {
            document.getElementById("patientId").value = patient.id || "";
            document.getElementById("namePatient").value = patient.fullName || "";
            document.getElementById("dateOfBirth").value = patient.dob || "";
            document.getElementById("gender").value = patient.gender || "";
            document.getElementById("phonePatient").value = patient.phone || "";
            document.getElementById("address").value = patient.address || "";
        } else {
            throw new Error(data.message || "Failed to fetch patient details");
        }
    } catch (error) {
        showError(`Error fetching patient: ${error.message}`);
        console.error("Error:", error);
    }
}

async function handleUpdate() {
    const form = document.querySelector("#offcanvasPatientEdit form");
    if (!form) {
        Swal.fire("Error!", "Edit form not found", "error");
        return;
    }

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
        Swal.fire(
            "Error!",
            "Full name must be max 100 characters and contain single spaces only",
            "error"
        );
        return;
    }

    const dobValidation = isPastOrPresentDate(dob);
    if (!dobValidation.isValid) {
        return;
    }

    if (!phonePatient || !/^[0][0-9]{9}$/.test(phonePatient)) {
        Swal.fire(
            "Error!",
            "Phone number must be exactly 10 digits and start with 0",
            "error"
        );
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
            status: "Enable",
        };
        const url = `${BASE_API}/${patientId}`;

        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(patientData),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        Swal.fire("Success!", "Patient updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(
            document.getElementById("offcanvasPatientEdit")
        ).hide();
        await fetchPatients(1, document.getElementById("itemsPerPage").value);
    } catch (error) {
        console.error("Update error:", error);
        Swal.fire("Error!", `Could not update patient: ${error.message}`, "error");
    }
}

async function handleAdd() {
    const form = document.querySelector("#offcanvasPatientAdd form");
    if (!form) {
        Swal.fire("Error!", "Add form not found", "error");
        return;
    }

    const namePatient = form.querySelector("#namePatient1").value.trim();
    const dob = form.querySelector("#dateOfBirth1").value;
    const gender = form.querySelector("#gender1").value;
    const phonePatient = form.querySelector("#phonePatient1").value.trim();
    const address = form.querySelector("#address1").value.trim();

    // Validation
    if (!namePatient || namePatient.length > 100 || /\s{2,}/.test(namePatient)) {
        Swal.fire(
            "Error",
            "Full name must be 1-100 characters with single spaces",
            "error"
        );
        return;
    }

    const dobValidation = isPastOrPresentDate(dob);
    if (!dobValidation.isValid) {
        return;
    }

    if (!phonePatient || !/^[0][0-9]{9}$/.test(phonePatient)) {
        Swal.fire(
            "Error",
            "Phone number must be 10 digits starting with 0",
            "error"
        );
        return;
    }

    if (!address || address.length > 225) {
        Swal.fire("Error", "Address must be 1-225 characters", "error");
        return;
    }

    // Disable form to prevent multiple submissions
    const submitButton = form.querySelector("button[type='submit']");
    submitButton.disabled = true;

    try {
        const patientData = {
            fullName: namePatient,
            dob: dob,
            gender: gender,
            phone: phonePatient,
            address: address,
            status: "Enable",
        };

        const response = await fetch(`http://localhost:8080/SWP_back_war_exploded/api/AdminBusinessPatient`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patientData),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(
                `HTTP error! Status: ${response.status}, Message: ${errorText}`
            );
        }

        Swal.fire("Success", "Patient added successfully!", "success");
        bootstrap.Offcanvas.getInstance(
            document.getElementById("offcanvasPatientAdd")
        ).hide();
        fetchPatients(1, document.getElementById("itemsPerPage").value);
    } catch (error) {
        Swal.fire("Error", `Failed to add patient: ${error.message}`, "error");
    } finally {
        submitButton.disabled = false;
    }
}

async function handleDelete(patientId) {
    try {
        const response = await fetch(`http://localhost:8080/SWP_back_war_exploded/api/AdminBusinessPatient/${patientId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        Swal.fire("Update status!", "Your patient has been update.", "success");
        fetchPatients(1, document.getElementById("itemsPerPage").value);
    } catch (error) {
        showError(`Error deleting patient: ${error.message}`);
        console.error("Error:", error);
    }
}

function formatDate(dateStr) {
    if (!dateStr) return "-";
    const [year, month, day] = dateStr.split("-");
    return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
}

function calculateAge(dobStr) {
    if (!dobStr) return "-";
    const currentDate = new Date();
    const birthDate = new Date(dobStr);
    let age = currentDate.getFullYear() - birthDate.getFullYear();
    const monthDiff = currentDate.getMonth() - birthDate.getMonth();
    if (
        monthDiff < 0 ||
        (monthDiff === 0 && currentDate.getDate() < birthDate.getDate())
    ) {
        age--;
    }
    return age >= 0 ? age : "-";
}

function showError(message) {
    const errorDiv = document.createElement("div");
    errorDiv.className = "alert alert-danger";
    errorDiv.textContent = message;
    const cardBody = document.querySelector(".card-body");
    if (cardBody) {
        cardBody.prepend(errorDiv);
        setTimeout(() => errorDiv.remove(), 5000);
    }
}

function isPastOrPresentDate(dateString) {
    if (!dateString || !/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
        Swal.fire("Error", "Invalid date format. Please use YYYY-MM-DD.", "error");
        return { isValid: false, message: "Invalid date format. Please use YYYY-MM-DD." };
    }

    try {
        const inputDate = new Date(dateString);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (isNaN(inputDate.getTime())) {
            Swal.fire("Error", "Invalid date.", "error");
            return { isValid: false, message: "Invalid date." };
        }

        if (inputDate > today) {
            Swal.fire("Error", "Only current or past dates can be selected.", "error");
            return { isValid: false, message: "Only current or past dates can be selected." };
        }

        return { isValid: true, message: "Valid date.", date: dateString };
    } catch (error) {
        Swal.fire("Error", `Error processing date: ${error.message}`, "error");
        return { isValid: false, message: `Error processing date: ${error.message}` };
    }
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function updatePagination(totalPages, currentPageVal, pageSizeVal, totalItems) {
    const pageInfo = document.getElementById("pageInfo");
    const prevPage = document.getElementById("prevPage");
    const nextPage = document.getElementById("nextPage");
    const allPatient = document.getElementById("all_patient");

    if (allPatient) allPatient.value = totalItems;

    if (pageInfo)
        pageInfo.textContent = `Page ${currentPageVal} of ${totalPages || 1} (Total: ${totalItems || 0})`;
    if (prevPage) prevPage.disabled = currentPageVal <= 1;
    if (nextPage) nextPage.disabled = currentPageVal >= (totalPages || 1);
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) itemsPerPage.value = pageSizeVal;
}