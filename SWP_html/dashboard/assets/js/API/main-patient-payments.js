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
    if (!dateTime) return "N/A";

    const date = new Date(dateTime);
    if (isNaN(date)) return "Invalid date";

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
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

    const renderStatus = () => {
        if (isPaid) {
            return `<span class="badge bg-success-subtle p-2 text-success">Paid</span>`;
        } else {
            return `<span class="badge bg-warning-subtle p-2 text-warning">Pending</span>`;
        }
    }

    const renderButton = () => {
        if (!isPaid) {
            return `<a class="d-inline-block pe-2 edit-btn1" data-bs-toggle="offcanvas"
                href="#offcanvasPatientPaymentPending" aria-controls="offcanvasPatientPaymentPending"
                data-payment='${JSON.stringify(payment)}'>
                <button class="btn btn-primary text-white select-patient-btn" data-action="view">
                <i class="fas fa-edit me-1"></i>Pay
                </button>
            </a>`;
        } else {
            return `<a class="d-inline-block pe-2 edit-btn2" data-bs-toggle="offcanvas"
                href="#offcanvasPatientPaymentPaid" aria-controls="offcanvasPatientPaymentPaid"
                data-payment='${JSON.stringify(payment)}'>
                <button class="btn btn-success text-white select-patient-btn" data-action="view">
                <i class="fas fa-eye me-1"></i>View
                </button>
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
        state.currentPage = page;
        state.currentIssueDate = issueDateSearch;
        state.currentStatus = statusSearch;

        let apiUrl = `${baseAPI}&page=${state.currentPage}&pageSize=${pageSize}`;
        if (issueDateSearch) apiUrl += `&issueDate=${encodeURIComponent(issueDateSearch)}`;
        if (statusSearch) apiUrl += `&status=${encodeURIComponent(statusSearch)}`;

        const container = document.getElementById("infor-payment");
        if (!container) throw new Error("Container element not found");
        container.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

        const response = await fetch(apiUrl, { method: "GET" });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const payments = data.invoices || [];
        const totalPages = data.totalPages || 1;
        const totalInvoice = data.totalInvoice;
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
                        <option value="${totalInvoice}" ${pageSize === totalInvoice ? 'selected' : ''}>All</option>
                    </select>
                </div>
            </div>
        </div>`;

        container.innerHTML = payments.length ? paymentTable + paginationHTML :'<h3 class="text-center" >No Payment found.</h3>';
        
        container.querySelectorAll(".edit-btn1").forEach(button => {
            button.addEventListener("click", function (e) {
                const payment = JSON.parse(this.dataset.payment);
                populateView2(payment);
            });
        });

        container.querySelectorAll(".edit-btn2").forEach(button => {
            button.addEventListener("click", function (e) {
                const payment = JSON.parse(this.dataset.payment);
                populateView1(payment);
            });
        });

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

function populateView1(payment) {
    const namePatient = document.getElementById("patient-name");
    const phonePatient = document.getElementById("patient-phone");
    const issueDate = document.getElementById("issueDate");
    const serviceDetail = document.getElementById("serviceDetail");
    const medicineDetail = document.getElementById("medicineDetail");
    const invoiceTotalAmount = document.getElementById("invoiceTotalAmount");
    const status = document.getElementById("status");

    if (namePatient) namePatient.value = payment.patient.fullName;
    if (phonePatient) phonePatient.value = payment.patient.phone;
    if (issueDate) issueDate.value = formatDateToYYYYMMDD(payment.issueDate);
    if (serviceDetail) serviceDetail.value = `${payment.serviceDetail} VND`;
    if (medicineDetail) medicineDetail.value = `${payment.medicineDetail} VND`;
    if (invoiceTotalAmount) invoiceTotalAmount.value = `${formatToVND(payment.invoiceTotalAmount)}`;
    if (status) status.value = payment.invoiceStatus;
}

function populateView2(payment) {
    const namePatient = document.getElementById("patient-name1");
    const phonePatient = document.getElementById("patient-phone1");
    const issueDate = document.getElementById("issueDate1");
    const serviceDetail = document.getElementById("serviceDetail1");
    const medicineDetail = document.getElementById("medicineDetail1");
    const invoiceTotalAmount = document.getElementById("invoiceTotalAmount1");
    const amount1 = document.getElementById("amount");
    const message = document.getElementById("message");
    const invoiceId = document.getElementById("invoiceId");

    if (namePatient) namePatient.value = payment.patient.fullName;
    if (phonePatient) phonePatient.value = payment.patient.phone;
    if (issueDate) issueDate.value = formatDateToYYYYMMDD(payment.issueDate);
    if (serviceDetail) serviceDetail.value = `${payment.serviceDetail} VND`;
    if (medicineDetail) medicineDetail.value = `${payment.medicineDetail} VND`;
    if (invoiceTotalAmount) invoiceTotalAmount.value = `${formatToVND(payment.invoiceTotalAmount)}`;
    if (amount1) amount1.value = parseFloat(payment.invoiceTotalAmount);
    if (message) message.value = `${parseFloat(payment.invoiceTotalAmount)}${payment.patient.id}`;
    if (invoiceId) invoiceId.value = payment.invoiceId;

}

//main
document.addEventListener("DOMContentLoaded", () => {
    displayPayment(1);

    const searchButton = document.getElementById("search-button");
    if (searchButton) {
        searchButton.addEventListener("click", triggerSearch);
    }

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

let countdownInterval;

async function checkPaid(price, content) {
    const api_KEY = "AK_CS.4c6d2bf0534411f0a5a1fd736f0c57ae.nTTG1gClRwhVsT7Br3bXuF78WldPBhqmFmMLS9yS8ziX8IwjmJOZApBIVUl9ddzDpkcwI1q3";
    const api_get_paid = 'https://oauth.casso.vn/v2/transactions';
    try {
        const response = await fetch(`${api_get_paid}/?sort=DESC`, {
            headers: {
                Authorization: `apikey ${api_KEY}`,
                "Content-Type": "application/json"
            }
        },
        );
        if (!response.ok) {
            throw new Error("Failed to fetch payment data");
        }
        const data = await response.json();
        const records = data.data.records;
        const isPaid = records.some(record =>
            record.amount === price &&
            record.description.toLowerCase().includes(content.toLowerCase())
        );
        return isPaid;
    } catch (error) {
        console.error("Payment check error:", error);
        throw error;
    }
}

async function startCountdown(seconds) {
    let timeLeft = seconds;
    const countdownElement = document.getElementById("countdown");
    const price = parseFloat(document.getElementById("amount").value);
    const content = document.getElementById("message").value;
    const qrModalElement = document.getElementById("qrCodeModal");
    let isAlertShown = false;
    let isPaymentConfirmed = false;
    let isModalOpen = true;
    let countdownInterval = null;

    if (countdownInterval) {
        clearTimeout(countdownInterval);
        countdownInterval = null;
    }

    qrModalElement.addEventListener('hidden.bs.modal', () => {
        isModalOpen = false;
        clearTimeout(countdownInterval);
        countdownInterval = null;
    }, { once: true });

    async function checkPayment() {
        if (!isModalOpen || timeLeft <= 0) {
            clearTimeout(countdownInterval);
            countdownInterval = null;
            if (timeLeft <= 0 && !isAlertShown && !isPaymentConfirmed) {
                isAlertShown = true;
                try {
                    const modalInstance = bootstrap.Modal.getInstance(qrModalElement);
                    if (modalInstance) {
                        modalInstance.hide();
                    }
                    Swal.fire({
                        icon: "error",
                        title: "Hết thời gian!",
                        text: "Thời gian thanh toán đã hết. Vui lòng thử lại.",
                        timer: 3000,
                        timerProgressBar: true,
                        showConfirmButton: false
                    });
                } catch (error) {
                    console.error("Lỗi khi đóng modal khi hết thời gian:", error);
                }
            }
            return;
        }

        timeLeft--;
        if (countdownElement) {
            countdownElement.textContent = timeLeft;
        }

        if (!isPaymentConfirmed) {
            try {
                const isPaid = await checkPaid(price, content);
                console.log("Kết quả checkPaid:", isPaid, "at", new Date().toISOString());
                if (isPaid) {
                    isPaymentConfirmed = true;
                    clearTimeout(countdownInterval);
                    countdownInterval = null;
                    try {
                        const modalInstance = bootstrap.Modal.getInstance(qrModalElement);
                        if (modalInstance) {
                            modalInstance.hide();
                        }
                        Swal.fire({
                            icon: "success",
                            title: "Thành công!",
                            text: "Thanh toán của bạn đã được thực hiện thành công!",
                            timer: 3000,
                            timerProgressBar: true,
                            showConfirmButton: false,
                            willClose: () => {
                                const form = document.getElementById("paymentForm");
                                if (form) {
                                    handleFormSubmission({ target: form });
                                } else {
                                    console.error("Form element not found in startCountdown");
                                    Swal.fire("Error!", "Form element not found.", "error");
                                }
                            }
                        });
                    } catch (error) {
                        console.error("Lỗi khi đóng modal:", error);
                    }
                } else if (timeLeft > 0 && isModalOpen) {
                    countdownInterval = setTimeout(checkPayment, 1000);
                }
            } catch (error) {
                console.error("Lỗi khi kiểm tra thanh toán:", error, "at", new Date().toISOString());
                if (timeLeft > 0 && isModalOpen && !isPaymentConfirmed) {
                    countdownInterval = setTimeout(checkPayment, 1000);
                }
            }
        }
    }

    countdownInterval = setTimeout(checkPayment, 1000);
}

document.getElementById("qrCodeModal").addEventListener("hidden.bs.modal", function () {
    isModalOpen = false;
    if (countdownInterval) {
        clearTimeout(countdownInterval);
        countdownInterval = null;
    }
    Swal.close();
});

document.getElementById("payButton").addEventListener("click", async function () {
    const errorElement = document.getElementById("error");
    const loadingElement = document.getElementById("loading");
    const qrImage = document.getElementById("qrImage");

    errorElement.style.display = "none";
    qrImage.classList.add("hidden");
    loadingElement.style.display = "block";

    const form = document.getElementById("paymentForm");
    if (!form) {
        console.error("Form element not found in payButton click");
        errorElement.textContent = "Form element not found.";
        errorElement.style.display = "block";
        loadingElement.style.display = "none";
        return;
    }

    const formData = new FormData(form);
    document.getElementById("qrBankCode").textContent = formData.get("bankCode");
    document.getElementById("qrBankAccount").textContent = formData.get("bankAccount");
    document.getElementById("qrAmount").textContent = formData.get("amount");
    document.getElementById("qrMessage").textContent = formData.get("message");

    const qrModal = new bootstrap.Modal(document.getElementById("qrCodeModal"));
    qrModal.show();

    clearTimeout(countdownInterval);
    document.getElementById("countdown").textContent = "120";
    isModalOpen = true;
    startCountdown(120);

    try {
        const params = new URLSearchParams({
            bankCode: formData.get("bankCode"),
            bankAccount: formData.get("bankAccount"),
            amount: formData.get("amount"),
            message: formData.get("message")
        });

        const response = await fetch(`http://localhost:8080/SWP_back_war_exploded/api/generateQR/?${params}`, {
            method: "GET"
        });

        if (!response.ok) {
            throw new Error(`Lỗi ${response.status}: ${await response.text()}`);
        }

        const blob = await response.blob();
        const imageUrl = URL.createObjectURL(blob);
        qrImage.src = imageUrl;
        qrImage.classList.remove("hidden");
    } catch (error) {
        errorElement.textContent = error.message;
        errorElement.style.display = "block";
    } finally {
        loadingElement.style.display = "none";
    }
});

document.getElementById("confirmPayment").addEventListener("click", async function () {
    clearTimeout(countdownInterval);
    const modal = bootstrap.Modal.getInstance(document.getElementById("qrCodeModal"));
    modal.hide();

    const price = parseFloat(document.getElementById("amount").value);
    const content = document.getElementById("message").value;

    try {
        const isPaid = await checkPaid(price, content);
        if (isPaid) {
            const form = document.getElementById("paymentForm");
            if (form) {
                handleFormSubmission({ target: form });
            } else {
                console.error("Form element not found in confirmPayment");
                Swal.fire("Error!", "Form element not found.", "error");
            }
        } else {
            Swal.fire("Error!", "Payment not confirmed. Please ensure the correct amount and description.", "error");
        }
    } catch (error) {
        Swal.fire("Error!", `Payment verification failed: ${error.message}`, "error");
    }
});

async function handleFormSubmission(event) {
    const form = event.target;
    const invoiceIdInput = form.querySelector("#invoiceId");
    const invoiceId = invoiceIdInput.value.trim();
    try {
        const url = `${baseAPI.split('?')[0].replace(/\/+$/, '')}/${invoiceId}`;
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": localStorage.getItem('account') ? `Bearer ${JSON.parse(localStorage.getItem('account')).token}` : ''
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Failed to update payment: HTTP ${response.status}`);
        }
        const data = await response.json();
        Swal.fire("Success!", data.message || "Invoice updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasPatientPaymentPending")).hide();
        displayPayment(1);
    } catch (error) {
        console.error("Update error:", error);
        Swal.fire("Error!", `Could not update payment: ${error.message}`, "error");
    }
}