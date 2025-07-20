const INVOICES_API = "http://localhost:8080/SWP_back_war_exploded/api/receptionistInvoices";
let countdownInterval = null;
let isModalOpen = false;

async function checkPaid(price, content) {
    const api_KEY = "AK_CS.4c6d2bf0534411f0a5a1fd736f0c57ae.nTTG1gClRwhVsT7Br3bXuF78WldPBhqmFmMLS9yS8ziX8IwjmJOZApBIVUl9ddzDpkcwI1q3";
    const api_get_paid = 'https://oauth.casso.vn/v2/transactions';
    try {
        const response = await fetch(`${api_get_paid}/?sort=DESC`, {
            headers: {
                Authorization: `apikey ${api_KEY}`,
                "Content-Type": "application/json"
            }
        });
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
    const price = parseFloat(document.getElementById("amount")?.value);
    const content = document.getElementById("message")?.value;
    const qrModalElement = document.getElementById("qrCodeModal");
    let isAlertShown = false;
    let isPaymentConfirmed = false;

    if (countdownInterval) {
        clearTimeout(countdownInterval);
        countdownInterval = null;
    }

    qrModalElement?.addEventListener('hidden.bs.modal', () => {
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

document.addEventListener("DOMContentLoaded", () => {
    let currentPage = 1;
    let pageSize = 10;

    // Initialize invoice list
    fetchInvoices(currentPage, pageSize);

    // Real-time filter with debouncing
    const debounceFilter = debounce(() => {
        currentPage = 1;
        fetchInvoices(currentPage, pageSize);
    }, 300);

    ["startDate", "endDate", "filterStatus"].forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener("input", debounceFilter);
        }
    });

    // Items per page change
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) {
        itemsPerPage.addEventListener("change", (e) => {
            pageSize = parseInt(e.target.value, 10) || 10; // Fallback to 10 if invalid
            currentPage = 1;
            fetchInvoices(currentPage, pageSize);
        });
    }

    // Pagination controls
    const prevPage = document.getElementById("prevPage");
    if (prevPage) {
        prevPage.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                fetchInvoices(currentPage, pageSize);
            }
        });
    }

    const nextPage = document.getElementById("nextPage");
    if (nextPage) {
        nextPage.addEventListener("click", () => {
            currentPage++;
            fetchInvoices(currentPage, pageSize);
        });
    }

    // Download All button
    const downloadButton = document.querySelector(".btn-primary[download]");
    if (downloadButton) {
        downloadButton.addEventListener("click", (e) => {
            e.preventDefault();
            exportInvoices("xlsx");
        });
    }

    // Offcanvas event listeners for populating payment details
    document.getElementById("offcanvasPatientPaymentPending")?.addEventListener("show.bs.offcanvas", (e) => {
        const payment = JSON.parse(e.relatedTarget.getAttribute("data-payment"));
        populateView2(payment);
    });

    document.getElementById("offcanvasPatientPaymentPaid")?.addEventListener("show.bs.offcanvas", (e) => {
        const payment = JSON.parse(e.relatedTarget.getAttribute("data-payment"));
        populateView1(payment);
    });

    // QR code modal and payment events
    document.getElementById("qrCodeModal")?.addEventListener("hidden.bs.modal", () => {
        isModalOpen = false;
        if (countdownInterval) {
            clearTimeout(countdownInterval);
            countdownInterval = null;
        }
        Swal.close();
    });

    document.getElementById("payButton")?.addEventListener("click", async () => {
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

    document.getElementById("confirmPayment")?.addEventListener("click", async () => {
        clearTimeout(countdownInterval);
        const modal = bootstrap.Modal.getInstance(document.getElementById("qrCodeModal"));
        modal.hide();

        const price = parseFloat(document.getElementById("amount")?.value);
        const content = document.getElementById("message")?.value;

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
});

async function fetchInvoices(page, pageSize) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const status = document.getElementById("filterStatus")?.value || "";

    // Basic validation
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }

    const params = new URLSearchParams({
        page,
        pageSize,
        fromDate: startDate,
        toDate: endDate,
        status
    });

    const url = `${INVOICES_API}?${params.toString()}`;
    console.log("Fetching URL:", url);

    try {
        const response = await fetch(url, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        if (data.success && data.invoices) {
            renderInvoices(data.invoices, page, pageSize);
            updatePagination(data.totalInvoices, data.page, data.pageSize);
        } else {
            throw new Error(data.message || "Failed to fetch invoices");
        }
    } catch (error) {
        showError(`Error fetching invoices: ${error.message}`);
        console.error("Error:", error);
    }
}

function renderInvoices(invoices, currentPage, pageSize) {
    const tbody = document.getElementById("infor-payment");
    if (!tbody) {
        console.error("Invoice table body not found");
        return;
    }

    tbody.innerHTML = invoices.length === 0
        ? '<tr><td colspan="7" class="text-center">No invoices found</td></tr>'
        : "";

    invoices.forEach((payment, index) => {
        const isPaid = payment.invoiceStatus === "Paid";
        const isCancelled = payment.invoiceStatus === "Cancelled";
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${(currentPage - 1) * pageSize + index + 1}</td>
            <td><strong>${payment.patient?.fullName || "-"}</strong></td>
            <td>${payment.patient?.phone || "-"}</td>
            <td>${formatDate(payment.issueDate) || "-"}</td>
            <td>${formatCurrency(payment.invoiceTotalAmount) || "-"}</td>
            <td><span class="badge ${getStatusClass(payment.invoiceStatus)}">${payment.invoiceStatus || "-"}</span></td>
            <td>
                ${isCancelled ? `<a class="d-inline-block pe-2 edit-btn2" data-bs-toggle="offcanvas"
                    href="#offcanvasPatientPaymentPaid" aria-controls="offcanvasPatientPaymentPaid"
                    data-payment='${JSON.stringify(payment)}'>
                    <button class="btn btn-success text-white select-patient-btn" data-action="view">
                        <i class="fas fa-eye me-1"></i>View
                    </button>
                </a>` : isPaid ? `
                <a class="d-inline-block pe-2 edit-btn2" data-bs-toggle="offcanvas"
                    href="#offcanvasPatientPaymentPaid" aria-controls="offcanvasPatientPaymentPaid"
                    data-payment='${JSON.stringify(payment)}'>
                    <button class="btn btn-success text-white select-patient-btn" data-action="view">
                        <i class="fas fa-eye me-1"></i>View
                    </button>
                </a>` : `
                <a class="d-inline-block pe-2 edit-btn1" data-bs-toggle="offcanvas"
                    href="#offcanvasPatientPaymentPending" aria-controls="offcanvasPatientPaymentPending"
                    data-payment='${JSON.stringify(payment)}'>
                    <button class="btn btn-primary text-white select-patient-btn" data-action="view">
                        <i class="fas fa-edit me-1"></i>Pay
                    </button>
                </a>`}
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function markInvoiceAsPaid(invoiceId) {
    try {
        const response = await fetch(`INVOICES_API/?invoiceId=${invoiceId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(data.message || `HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        if (data.success) {
            if (typeof Swal !== 'undefined') {
                Swal.fire("Success!", data.message, "success");
            } else {
                alert(data.message);
            }
            fetchInvoices(currentPage, pageSize);
        } else {
            throw new Error(data.message || "Failed to update invoice status");
        }
    } catch (error) {
        showError(`Error updating invoice: ${error.message}`);
        console.error("Error:", error);
    }
}

async function exportInvoices(type) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const status = document.getElementById("filterStatus")?.value || "";

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }

    const params = new URLSearchParams({
        fromDate: startDate,
        toDate: endDate,
        status,
        exportType: type
    });

    const url = `${INVOICES_API}/export?${params.toString()}`;
    console.log("Exporting URL:", url);

    try {
        const response = await fetch(url, {
            method: "GET"
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(data.message || `HTTP error! Status: ${response.status}`);
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = downloadUrl;
        link.download = `invoices_report.${type}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);

        if (typeof Swal !== 'undefined') {
            Swal.fire("Success!", `Report exported as ${type.toUpperCase()}`, "success");
        } else {
            alert(`Report exported as ${type.toUpperCase()}`);
        }
    } catch (error) {
        showError(`Error exporting report: ${error.message}`);
        console.error("Error:", error);
    }
}

async function handleFormSubmission(event) {
    const form = event.target;
    const invoiceIdInput = form.querySelector("#invoiceId");
    const invoiceId = invoiceIdInput.value.trim();
    try {
        const response = await fetch(INVOICES_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Authorization": localStorage.getItem('account') ? `Bearer ${JSON.parse(localStorage.getItem('account')).token}` : ''
            },
            body: `invoiceId=${invoiceId}`
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Failed to update payment: HTTP ${response.status}`);
        }
        const data = await response.json();
        Swal.fire("Success!", data.message || "Invoice updated successfully.", "success");
        bootstrap.Offcanvas.getInstance(document.getElementById("offcanvasPatientPaymentPending"))?.hide();
        fetchInvoices(currentPage, pageSize);
    } catch (error) {
        console.error("Update error:", error);
        Swal.fire("Error!", `Could not update payment: ${error.message}`, "error");
    }
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

function formatDate(dateStr) {
    if (!dateStr) return "-";
    const [year, month, day] = dateStr.split("-");
    return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
}

function formatDateToYYYYMMDD(dateStr) {
    if (!dateStr) return "";
    const [year, month, day] = dateStr.split("-");
    return `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
}

function formatToVND(amount) {
    if (!amount) return "-";
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function formatCurrency(amount) {
    if (!amount) return "-";
    return new Intl.NumberFormat ('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function getStatusClass(status) {
    switch (status) {
        case "Paid": return "bg-success";
        case "Pending": return "bg-warning";
        case "Cancelled": return "bg-danger";
        default: return "bg-secondary";
    }
}

function showError(message) {
    if (typeof Swal !== 'undefined') {
        Swal.fire("Error", message, "error");
    } else {
        const errorDiv = document.createElement("div");
        errorDiv.className = "alert alert-danger";
        errorDiv.textContent = message;
        const content = document.querySelector(".content") || document.querySelector(".card");
        if (content) {
            content.prepend(errorDiv);
            setTimeout(() => errorDiv.remove(), 5000);
        } else {
            alert(message);
        }
    }
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function updatePagination(totalItems, currentPageVal, pageSizeVal) {
    const pageInfo = document.getElementById("pageInfo");
    const prevPage = document.getElementById("prevPage");
    const nextPage = document.getElementById("nextPage");
    const allInvoices = document.getElementById("all_invoices");

    if (allInvoices) allInvoices.value = totalItems;

    const totalPages = Math.ceil(totalItems / pageSizeVal) || 1;
    if (pageInfo) {
        pageInfo.textContent = totalItems === 0
            ? "No invoices found"
            : `Page ${currentPageVal} of ${totalPages} (Total: ${totalItems || 0})`;
    }
    if (prevPage) prevPage.disabled = currentPageVal <= 1;
    if (nextPage) nextPage.disabled = currentPageVal >= totalPages;
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) itemsPerPage.value = pageSizeVal;
}