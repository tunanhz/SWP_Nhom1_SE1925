const BASE_API = "http://localhost:8080/SWP_back_war_exploded/api/admin/receptionist";

document.addEventListener("DOMContentLoaded", () => {
    let currentPage = 1;
    let pageSize = 10;

    // Initialize receptionist list
    fetchReceptionists(currentPage, pageSize);

    // Event delegation for edit and delete buttons
    document.getElementById("receptionist_infor").addEventListener("click", (event) => {
        const target = event.target.closest(".edit-btn, .delete-btn");
        if (!target) return;

        const accountStaffId = target.dataset.accountStaffId;
        if (target.classList.contains("edit-btn")) {
            handleEdit(accountStaffId);
        } else if (target.classList.contains("delete-btn")) {
            Swal.fire({
                title: "Are you sure?",
                text: `You want to ${target.classList.contains("enable-btn") ? "enable" : "disable"} this receptionist?`,
                icon: "warning",
                showCancelButton: true,
                backdrop: `rgba(60,60,60,0.8)`,
                confirmButtonText: "Yes, update status!",
                confirmButtonColor: "#c03221",
            }).then(async (result) => {
                if (result.isConfirmed) {
                    handleDelete(accountStaffId);
                }
            });
        }
    });

    // Real-time search with debouncing
    const debounceSearch = debounce(() => {
        currentPage = 1;
        fetchReceptionists(currentPage, pageSize);
    }, 300);

    ["receptionist-search", "status-search"].forEach((id) => {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener(id === "status-search" ? "change" : "input", debounceSearch);
        }
    });

    // Items per page change
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) {
        itemsPerPage.addEventListener("change", (e) => {
            pageSize = e.target.value === "All" ? parseInt(document.getElementById("all_receptionists").value, 10) : parseInt(e.target.value, 10);
            currentPage = 1;
            fetchReceptionists(currentPage, pageSize);
        });
    }

    // Pagination controls
    const prevPage = document.getElementById("prevPage");
    if (prevPage) {
        prevPage.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                fetchReceptionists(currentPage, pageSize);
            }
        });
    }

    const nextPage = document.getElementById("nextPage");
    if (nextPage) {
        nextPage.addEventListener("click", () => {
            currentPage++;
            fetchReceptionists(currentPage, pageSize);
        });
    }

    // Form submissions
    const editReceptionistForm = document.getElementById("editReceptionistForm");
    if (editReceptionistForm) {
        editReceptionistForm.addEventListener("submit", (e) => {
            e.preventDefault();
            handleUpdate();
        });
    }

    const addReceptionistForm = document.getElementById("addReceptionist");
    if (addReceptionistForm) {
        addReceptionistForm.addEventListener("submit", (e) => {
            e.preventDefault();
            handleAdd();
        });
    }
});

async function fetchReceptionists(page, pageSize) {
    const searchQuery = document.getElementById("receptionist-search")?.value.trim() || "";
    const statusFilter = document.getElementById("status-search")?.value || "";

    const params = new URLSearchParams({
        page,
        pageSize,
        searchQuery,
        statusFilter,
    });

    const url = `${BASE_API}/list?${params.toString()}`;
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
        if (data.receptionists) {
            renderReceptionists(data.receptionists);
            updatePagination(data.totalPages, data.currentPage, data.pageSize, data.totalReceptionists);
        } else {
            throw new Error(data.message || "Failed to fetch receptionists");
        }
    } catch (error) {
        showError(`Error fetching receptionists: ${error.message}`);
        console.error("Error:", error);
    }
}

function renderReceptionists(receptionists) {
    const tbody = document.getElementById("receptionist_infor");
    if (!tbody) {
        console.error("Receptionist table body not found");
        return;
    }

    tbody.innerHTML = receptionists.length === 0
        ? '<tr><td colspan="9" class="text-center">No receptionists found</td></tr>'
        : "";

    receptionists.forEach((receptionist, index) => {
        const isEnabled = receptionist.status === "Enable";
        const row = document.createElement("tr");
        row.innerHTML = `
            <th scope="row">${index + 1}</th>
            <td>${receptionist.accountStaffId || "-"}</td>
            <td><h6 class="mb-0 text-body fw-500">${receptionist.accountStaff?.username || "-"}</h6></td>
            <td>${receptionist.fullName || "-"}</td>
            <td>${receptionist.accountStaff?.email || "-"}</td>
            <td>${receptionist.phone || "-"}</td>
            <td><img src="${receptionist.accountStaff?.img || 'https://res.cloudinary.com/dnoyqme5b/image/upload/v1752978933/avatars/1_xaytga.jpg'}" alt="receptionist" class="avatar-40 rounded-pill"></td>
            <td>${receptionist.status || "-"}</td>
            <td>
                <a data-bs-toggle="offcanvas" href="#offcanvasReceptionistEdit" aria-controls="offcanvasReceptionistEdit" class="d-inline-block pe-2 edit-btn" data-account-staff-id="${receptionist.accountStaffId}">
                    <span class="text-success">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9.31055 14.3321H14.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M8.58501 1.84609C9.16674 1.15084 10.2125 1.04889 10.9222 1.6188C10.9614 1.64972 12.2221 2.62909 12.2221 2.62909C13.0017 3.10039 13.244 4.10233 12.762 4.86694C12.7365 4.90789 5.60896 13.8234 5.60896 13.8234C5.37183 14.1192 5.01187 14.2938 4.62718 14.298L1.89765 14.3323L1.28265 11.7292C1.1965 11.3632 1.28265 10.9788 1.51978 10.683L8.58501 1.84609Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M7.26562 3.50073L11.3548 6.64108" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
                <a href="#" class="d-inline-block ps-2 delete-btn ${isEnabled ? '' : 'enable-btn'}" data-account-staff-id="${receptionist.accountStaffId}">
                    <span class="${isEnabled ? 'text-danger' : 'text-success'}">
                        <svg class="icon-22" width="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16.4232 9.4478V7.3008C16.4232 4.7878 14.3852 2.7498 11.8722 2.7498C9.35925 2.7388 7.31325 4.7668 7.30225 7.2808V7.3008V9.4478" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M15.683 21.2497H8.042C5.948 21.2497 4.25 19.5527 4.25 17.4577V13.1687C4.25 11.0737 5.948 9.37671 8.042 9.37671H15.683C17.777 9.37671 19.475 11.0737 19.475 13.1687V17.4577C19.475 19.5527 17.777 21.2497 15.683 21.2497Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path d="M11.8628 14.2026V16.4236" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </span>
                </a>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function handleEdit(accountStaffId) {
    try {
        const response = await fetch(`${BASE_API}/${accountStaffId}`, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const receptionist = data.receptionist;
        if (receptionist) {
            document.getElementById("accountStaffId").value = receptionist.accountStaffId || "";
            document.getElementById("receptionistId").value = receptionist.receptionistId || "";
            document.getElementById("username").value = receptionist.accountStaff?.username || "";
            document.getElementById("password").value = receptionist.accountStaff?.password || "";
            document.getElementById("fullName").value = receptionist.fullName || "";
            document.getElementById("email").value = receptionist.accountStaff?.email || "";
            document.getElementById("phone").value = receptionist.phone || "";
            document.getElementById("status").value = receptionist.status || "Enable";
        } else {
            throw new Error(data.message || "Failed to fetch receptionist details");
        }
    } catch (error) {
        showError(`Error fetching receptionist: ${error.message}`);
        console.error("Error:", error);
    }
}

async function handleUpdate() {
    const form = document.querySelector("#offcanvasReceptionistEdit form");
    if (!form) {
        Swal.fire("Error!", "Edit form not found", "error");
        return;
    }

    const accountStaffId = form.querySelector("#accountStaffId").value;
    const receptionistId = form.querySelector("#receptionistId").value;
    const username = form.querySelector("#username").value.trim();
    const password = form.querySelector("#password").value.trim();
    const fullName = form.querySelector("#fullName").value.trim();
    const email = form.querySelector("#email").value.trim();
    const phone = form.querySelector("#phone").value.trim();
    const status = form.querySelector("#status").value;

    // Validation
    if (!accountStaffId || isNaN(accountStaffId) || !receptionistId || isNaN(receptionistId)) {
        Swal.fire("Error!", "Invalid account staff ID or receptionist ID", "error");
        return;
    }

    if (!username || username.length < 3 || username.length > 50 || !/^[a-zA-Z0-9_]+$/.test(username)) {
        Swal.fire("Error!", "Username must be 3-50 characters, alphanumeric or underscore only", "error");
        return;
    }

    if (!password || password.length < 6 || password.length > 50) {
        Swal.fire("Error!", "Password must be 6-50 characters", "error");
        return;
    }

    if (!fullName || fullName.length > 100 || /\s{2,}/.test(fullName)) {
        Swal.fire("Error!", "Full name must be 1-100 characters with single spaces", "error");
        return;
    }

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        Swal.fire("Error!", "Invalid email format", "error");
        return;
    }

    if (!phone || !/^[0][0-9]{9}$/.test(phone)) {
        Swal.fire("Error!", "Phone number must be 10 digits starting with 0", "error");
        return;
    }

    try {
        const receptionistData = {
            accountStaffId: parseInt(accountStaffId),
            receptionistId: parseInt(receptionistId),
            username,
            password,
            fullName,
            email,
            phone,
            status,
        };

        const response = await fetch(`${BASE_API}/update`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(receptionistData),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            if (errorData.errorCode === "DUPLICATE_USERNAME") {
                throw new Error("Username already exists");
            } else if (errorData.errorCode === "DUPLICATE_EMAIL") {
                throw new Error("Email already exists");
            } else if (errorData.errorCode === "DUPLICATE_PHONE") {
                throw new Error("Phone already exists");
            } else {
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }
        }

        Swal.fire("Success!", "Receptionist updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasReceptionistEdit")).hide();
        await fetchReceptionists(currentPage, document.getElementById("itemsPerPage").value);
    } catch (error) {
        Swal.fire("Error!", `Could not update receptionist: ${error.message}`, "error");
        console.error("Update error:", error);
    }
}

async function handleAdd() {
    const form = document.querySelector("#offcanvasReceptionistAdd form");
    if (!form) {
        Swal.fire("Error!", "Add form not found", "error");
        return;
    }

    const username = form.querySelector("#username1").value.trim();
    const password = form.querySelector("#password1").value.trim();
    const fullName = form.querySelector("#fullName1").value.trim();
    const email = form.querySelector("#email1").value.trim();
    const phone = form.querySelector("#phone1").value.trim();
    const status = form.querySelector("#status1").value;

    // Validation
    if (!username || username.length < 3 || username.length > 50 || !/^[a-zA-Z0-9_]+$/.test(username)) {
        Swal.fire("Error!", "Username must be 3-50 characters, alphanumeric or underscore only", "error");
        return;
    }

    if (!password || password.length < 6 || password.length > 50) {
        Swal.fire("Error!", "Password must be 6-50 characters", "error");
        return;
    }

    if (!fullName || fullName.length > 100 || /\s{2,}/.test(fullName)) {
        Swal.fire("Error!", "Full name must be 1-100 characters with single spaces", "error");
        return;
    }

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        Swal.fire("Error!", "Invalid email format", "error");
        return;
    }

    if (!phone || !/^[0][0-9]{9}$/.test(phone)) {
        Swal.fire("Error!", "Phone number must be 10 digits starting with 0", "error");
        return;
    }

    // Disable form to prevent multiple submissions
    const submitButton = form.querySelector("button[type='submit']");
    submitButton.disabled = true;

    try {
        const receptionistData = {
            username,
            password,
            fullName,
            email,
            phone,
            status,
        };

        const response = await fetch(`${BASE_API}/create`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(receptionistData),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            if (errorData.errorCode === "DUPLICATE_USERNAME") {
                throw new Error("Username already exists");
            } else if (errorData.errorCode === "DUPLICATE_EMAIL") {
                throw new Error("Email already exists");
            } else if (errorData.errorCode === "DUPLICATE_PHONE") {
                throw new Error("Phone already exists");
            } else {
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }
        }

        Swal.fire("Success!", "Receptionist added successfully!", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasReceptionistAdd")).hide();
        fetchReceptionists(1, document.getElementById("itemsPerPage").value);
    } catch (error) {
        Swal.fire("Error!", `Failed to add receptionist: ${error.message}`, "error");
        console.error("Add error:", error);
    } finally {
        submitButton.disabled = false;
    }
}

async function handleDelete(accountStaffId) {
    try {
        const response = await fetch(`${BASE_API}/delete`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ accountStaffId: parseInt(accountStaffId) }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        Swal.fire("Success!", "Receptionist status updated successfully.", "success");
        fetchReceptionists(currentPage, document.getElementById("itemsPerPage").value);
    } catch (error) {
        showError(`Error updating receptionist status: ${error.message}`);
        console.error("Error:", error);
    }
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
    const allReceptionists = document.getElementById("all_receptionists");

    if (allReceptionists) allReceptionists.value = totalItems;

    if (pageInfo)
        pageInfo.textContent = `Page ${currentPageVal} of ${totalPages || 1} (Total: ${totalItems || 0})`;
    if (prevPage) prevPage.disabled = currentPageVal <= 1;
    if (nextPage) nextPage.disabled = currentPageVal >= (totalPages || 1);
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) itemsPerPage.value = pageSizeVal;
}