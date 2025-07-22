const APPOINTMENT_API = "http://localhost:8080/SWP_back_war_exploded/api/AdminBusinessAppointment/";

document.addEventListener("DOMContentLoaded", () => {
    let currentPage = 1;
    let pageSize = 10;

    flatpickr('#startDate', { dateFormat: 'Y-m-d' });
    flatpickr('#endDate', { dateFormat: 'Y-m-d' });

    // Initialize appointment list
    fetchAppointments(currentPage, pageSize);

    // Real-time filter with debouncing
    const debounceFilter = debounce(() => {
        currentPage = 1;
        fetchAppointments(currentPage, pageSize);
    }, 300);

    ["startDate", "endDate", "status", "searchTerm"].forEach(id => {
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
            if (e.target.value === "All") {
                pageSize = 999999; // Large number to fetch all records
            }
            currentPage = 1;
            fetchAppointments(currentPage, pageSize);
        });
    }

    // Pagination controls
    const prevPage = document.getElementById("prevPage");
    if (prevPage) {
        prevPage.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                fetchAppointments(currentPage, pageSize);
            }
        });
    }

    const nextPage = document.getElementById("nextPage");
    if (nextPage) {
        nextPage.addEventListener("click", () => {
            currentPage++;
            fetchAppointments(currentPage, pageSize);
        });
    }
});

async function fetchAppointments(page, pageSize) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const status = document.getElementById("status")?.value || "";
    const searchTerm = document.getElementById("searchTerm")?.value || "";

    // Basic validation
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }

    const params = new URLSearchParams({
        page,
        pageSize,
        startDate,
        endDate,
        status,
        searchTerm
    });

    const url = `${APPOINTMENT_API}?${params.toString()}`;
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
        if (data.status === "success" && data.appointments) {
            renderAppointments(data.appointments);
            updateOverview(data.overview);
            updatePagination(data.totalPages, data.page, data.pageSize, data.totalItems);
        } else {
            throw new Error(data.message || "Failed to fetch appointments");
        }
    } catch (error) {
        showError(`Error fetching appointments: ${error.message}`);
        console.error("Error:", error);
    }
}

function renderAppointments(appointments) {
    const tbody = document.getElementById("appointmentDetail");
    if (!tbody) {
        console.error("Appointment table body not found");
        return;
    }

    tbody.innerHTML = appointments.length === 0
        ? '<tr><td colspan="9" class="text-center">No appointments found</td></tr>'
        : "";

    appointments.forEach(appt => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${appt.appointmentId || "-"}</td>
            <td>${appt.patientId || "-"}</td>
            <td>${appt.patientName || "-"}</td>
            <td>${formatDateTime(appt.appointmentDateTime) || "-"}</td>
            <td>${appt.shift || "-"}</td>
            <td>${appt.cancellationReason || "-"}</td>
            <td>${appt.appointmentStatus || "-"}</td>
            <td>${appt.doctorId || "-"}</td>
            <td>${appt.doctorName || "-"}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateOverview(overview) {
    const totalAppointments = document.getElementById("totalAppointments");
    const completedAppointments = document.getElementById("completedAppointments");
    const canceledAppointments = document.getElementById("canceledAppointments");
    const noShowAppointments = document.getElementById("noShowAppointments");

    if (totalAppointments) {
        totalAppointments.textContent = overview.totalAppointments || 0;
    }
    if (completedAppointments) {
        completedAppointments.textContent = overview.completedAppointments || 0;
    }
    if (canceledAppointments) {
        canceledAppointments.textContent = overview.canceledAppointments || 0;
    }
    if (noShowAppointments) {
        noShowAppointments.textContent = overview.noShowAppointments || 0;
    }
}

async function exportReport(type) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const status = document.getElementById("status")?.value || "";
    const searchTerm = document.getElementById("searchTerm")?.value || "";

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }

    const params = new URLSearchParams({
        startDate,
        endDate,
        status,
        searchTerm
    });

    const url = `${APPOINTMENT_API}export?${params.toString()}`;
    console.log("Exporting URL:", url);

    try {
        const response = await fetch(url, {
            method: "GET"
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = downloadUrl;
        link.download = `appointments_export.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);

        if (typeof Swal !== 'undefined') {
            Swal.fire("Success!", "Report exported as XLSX", "success");
        } else {
            alert("Report exported as XLSX");
        }
    } catch (error) {
        showError(`Error exporting report: ${error.message}`);
        console.error("Error:", error);
    }
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

function showError(message) {
    if (typeof Swal !== 'undefined') {
        Swal.fire("Error", message, "error");
    } else {
        const errorDiv = document.createElement("div");
        errorDiv.className = "alert alert-danger";
        errorDiv.textContent = message;
        const content = document.querySelector(".content");
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

function updatePagination(totalPages, currentPageVal, pageSizeVal, totalItems) {
    const pageInfo = document.getElementById("pageInfo");
    const prevPage = document.getElementById("prevPage");
    const nextPage = document.getElementById("nextPage");
    const allAppointment = document.getElementById("all_appointment");

    if (allAppointment) allAppointment.value = totalItems;

    if (pageInfo) {
        pageInfo.textContent = totalPages === 0
            ? "No appointments found"
            : `Page ${currentPageVal} of ${totalPages} (Total: ${totalItems || 0})`;
    }
    if (prevPage) prevPage.disabled = currentPageVal <= 1;
    if (nextPage) nextPage.disabled = currentPageVal >= (totalPages || 1);
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) itemsPerPage.value = pageSizeVal;
}