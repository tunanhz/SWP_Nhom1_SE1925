const accountString = localStorage.getItem('account');
const account = JSON.parse(accountString);

const baseAPI = `http://localhost:8080/SWP_back_war_exploded/api/patientPayment/?accountPatientId=${account.accountPatientId}`;
let pageSize = 6;
const state = {
    currentPage: 1,
    currentIssueDate: "",
    currentStatus: ""
};

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

function formatToVND(numberString) {
    const formatted = Math.floor(parseFloat(numberString)).toLocaleString('vi-VN');
    return `${formatted} VND`;
}

function createPaymentRow(payment, index) {
    const status = payment.invoiceStatus;
    const isPaid = status === "Paid";
    const isPending = status === "Pending";

    const renderStatus = () =>{
        if(isPaid) {
            return `<span class="badge bg-success-subtle p-2 text-success">Paid</span>`;
        }else{
            return `<span class="badge bg-warning-subtle p-2 text-warning">Pending</span>`;
        }

    }

    const renderButton = () =>{
        if(!isPaid) {
            return `<a class="d-inline-block pe-2" data-bs-toggle="offcanvas"
                href="#offcanvasPatientPaymentPending" aria-controls="offcanvasPatientPaymentPending">
                <span class="btn btn-outline-primary">
                    pay
                </span>
            </a>`;
        }else{
            return `<a class="d-inline-block pe-2" data-bs-toggle="offcanvas"
                href="#offcanvasPatientPaymentPaid" aria-controls="offcanvasPatientPaymentPaid">
                <span class="btn btn-outline-success">
                    View
                </span>
            </a>`;
        }
    }

    return `
        <tr data-item="list">
        <th scope="row">${index + 1}</th>
        <td>${payment.patient.fullName}</td>
        <td>${payment.patient.phone}</td>
        <td>${formatDateTimeConfirm(payment.issueDate)}</td>
        <td>${formatToVND(payment.invoiceTotalAmount)}</td>
        <td>${renderStatus()}</td>
        <td>
            ${renderButton()}
        </td>
    </tr>`;
}

async function displayPayment(page = 1, issueDateSearch = state.currentIssueDate, statusSearch = state.currentStatus) {
    try {
        // Update state
        state.currentPage = page;
        state.currentIssueDate = issueDateSearch;
        state.currentStatus = statusSearch;

        // Build API URL
        let apiUrl = `${baseAPI}&page=${state.currentPage}&pageSize=${pageSize}`;
        if (issueDateSearch) apiUrl += `&issueDate=${encodeURIComponent(issueDateSearch)}`;
        if (statusSearch) apiUrl += `&status=${encodeURIComponent(statusSearch)}`;

        // Show loading state
        const container = document.getElementById("infor-payment");
        if (!container) throw new Error("Container element not found");
        container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

        // Fetch data
        const response = await fetch(apiUrl, { method: "GET" });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const payments = data.invoices || [];
        const totalPages = data.totalPages || 1;

        // Render table
        let paymentTable = `
            <div class="table-responsive">
                    <table class="table border-end border-start align-middle mb-0 rounded">
                        <thead class="table-dark">
                            <tr>
                                <th scope="col">No.</th>
                                <th scope="col">Name Patient</th>
                                <th scope="col">Phone Patient</th>
                                <th scope="col">Issue Date</th>
                                <th scope="col">Total Cost</th>
                                <th scope="col">Status</th>
                                <th scope="col">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${payments.map((payment, index) => createPaymentRow(payment, index)).join("")}
                       </tbody>
                    </table>
                </div>`;

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
        </div>`;

        // Update container
        container.innerHTML = payments.length ? paymentTable + paginationHTML : '<p>No Appointments found.</p>';

        // Attach event listeners for edit buttons
        container.querySelectorAll(".view-btn").forEach(button => {
            button.addEventListener("click", function(e) {
                const appointment = JSON.parse(this.dataset.appointment);
                populateView(appointment);
            });
        });

        // Attach event listeners for pagination
        container.querySelectorAll("button[data-page]").forEach(button => {
            button.addEventListener("click", () => {
                const newPage = parseInt(button.dataset.page);
                displayPayment(newPage, state.currentIssueDate, state.currentStatus);
            });
        });

        const pageSizeSelect = container.querySelector("#pageSize");
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener("change", (e) => {
                pageSize = parseInt(e.target.value);
                displayPayment(1, state.currentIssueDate, state.currentStatus);
            });
        }

    } catch (error) {
        console.error("Error fetching or displaying invoices:", error);
        const container = document.getElementById("infor-payment");
        if (container) {
            container.innerHTML = `<p class="text-danger">Error: ${error.message}. <button class="btn btn-link p-0" onclick="displayPayment()">Retry</button></p>`;
        }
    }
}

function triggerSearch() {
    const dateInput = document.getElementById("invoice-search-date");
    const statusInput = document.getElementById("status-search");

    const dateValue = dateInput?.value.trim() || "";
    const statusValue = statusInput?.value.trim() || "";

    displayPayment(1, dateValue, statusValue);
}

async function checkPaid(price, content) {
    try {
        const response = await fetch(`https://script.google.com/macros/s/AKfycbxzewZ3gxh3LYB05C6o-IxcGvNsX8C_SRAQu1_kwMT0t4zmLSibXyTJx6gbTcziCs_i/exec`);
        const data = await response.json();
        const lastPaid = data.data[data.data.lengh - 1];
        lastPrice = lastPaid['Giá trị'];
        lastContent = lastPaid['Mô tả'];
        if(lastPrice >= price && lastContent.includes(content)){
            //thanh toán thành công
        }else{
            //thanh toán không thành công
        }

    } catch (error) {
        console.error(error);
    }

}

document.addEventListener("DOMContentLoaded", () => {
    displayPayment(1);

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
        document.getElementById("invoice-search-date"),
        document.getElementById("status-search")
    ];
    inputs.forEach(input => {
        if (input) {
            input.addEventListener("input", debounce(triggerSearch, 300));
        }
    });

    // Attach event listener for page size change
    const pageSizeSelect = document.getElementById("pageSize");
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener("change", (e) => {
            pageSize = parseInt(e.target.value);
            displayPayment(1, state.currentIssueDate, state.currentStatus);
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