
const FEEDBACK_API = "http://localhost:8080/SWP_back_war_exploded/api/AdminBusinessFeedback/";

document.addEventListener("DOMContentLoaded", () => {
    let currentPage = 1;
    let pageSize = 10;

    flatpickr('#startDate', { dateFormat: 'Y-m-d' });
    flatpickr('#endDate', { dateFormat: 'Y-m-d' });

    // Initialize feedback list
    fetchFeedback(currentPage, pageSize);

    // Real-time filter with debouncing
    const debounceFilter = debounce(() => {
        currentPage = 1;
        fetchFeedback(currentPage, pageSize);
    }, 300);

    ["startDate", "endDate", "minAvgFeedback", "maxAvgFeedback"].forEach(id => {
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
            fetchFeedback(currentPage, pageSize);
        });
    }

    // Pagination controls
    const prevPage = document.getElementById("prevPage");
    if (prevPage) {
        prevPage.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                fetchFeedback(currentPage, pageSize);
            }
        });
    }

    const nextPage = document.getElementById("nextPage");
    if (nextPage) {
        nextPage.addEventListener("click", () => {
            currentPage++;
            fetchFeedback(currentPage, pageSize);
        });
    }
});

async function fetchFeedback(page, pageSize) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const minAvgFeedback = document.getElementById("minAvgFeedback")?.value || "";
    const maxAvgFeedback = document.getElementById("maxAvgFeedback")?.value || "";

    // Basic validation
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }
    if (minAvgFeedback && maxAvgFeedback && Number(minAvgFeedback) > Number(maxAvgFeedback)) {
        showError("Minimum average feedback must be less than maximum");
        return;
    }

    const params = new URLSearchParams({
        page,
        pageSize,
        startDate,
        endDate,
        minAvgFeedback,
        maxAvgFeedback
    });

    const url = `${FEEDBACK_API}?${params.toString()}`;
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
        if (data.status === "success" && data.feedbacks) {
            renderFeedback(data.feedbacks);
            updateOverview(data.overview);
            updatePagination(data.totalPages, data.page, data.pageSize, data.totalItems);
        } else {
            throw new Error(data.message || "Failed to fetch feedback");
        }
    } catch (error) {
        showError(`Error fetching feedback: ${error.message}`);
        console.error("Error:", error);
    }
}

function renderFeedback(feedbacks) {
    const tbody = document.getElementById("feedbackTableBody");
    if (!tbody) {
        console.error("Feedback table body not found");
        return;
    }

    tbody.innerHTML = feedbacks.length === 0
        ? '<tr><td colspan="9" class="text-center">No feedback found</td></tr>'
        : "";

    feedbacks.forEach(fb => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${fb.feedbackId || "-"}</td>
            <td><strong>${fb.patient?.fullName || "-"}</strong></td>
            <td>${fb.content || "-"}</td>
            <td>${fb.serviceRating || "-"}<i class="fas fa-star" style="color: gold;"></i></td>
            <td>${fb.doctorRating || "-"}<i class="fas fa-star" style="color: gold;"></i></td>
            <td>${fb.receptionistRating || "-"}<i class="fas fa-star" style="color: gold;"></i></td>
            <td>${fb.pharmacistRating || "-"}<i class="fas fa-star" style="color: gold;"></i></td>
            <td>${fb.avgFeedback ? fb.avgFeedback.toFixed(1) : "-"}<i class="fas fa-star" style="color: gold;"></i></td>
            <td>${formatDateTime(fb.createdAt) || "-"}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateOverview(overview) {
    const totalFeedback = document.getElementById("totalFeedback");
    const avgDoctorRating = document.getElementById("avgDoctorRating");
    const negativeFeedback = document.getElementById("negativeFeedback");

    if (totalFeedback) {
        totalFeedback.textContent = overview.totalFeedback || 0;
    }
    if (avgDoctorRating) {
        avgDoctorRating.textContent = overview.avgDoctorRating ? overview.avgDoctorRating.toFixed(1) : 0;
    }
    if (negativeFeedback) {
        negativeFeedback.textContent = overview.negativeFeedbackPercentage ? `${overview.negativeFeedbackPercentage.toFixed(1)}%` : "0%";
    }
}

async function exportReport(type) {
    const startDate = document.getElementById("startDate")?.value || "";
    const endDate = document.getElementById("endDate")?.value || "";
    const minAvgFeedback = document.getElementById("minAvgFeedback")?.value || "";
    const maxAvgFeedback = document.getElementById("maxAvgFeedback")?.value || "";

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError("Start date must be before end date");
        return;
    }
    if (minAvgFeedback && maxAvgFeedback && Number(minAvgFeedback) > Number(maxAvgFeedback)) {
        showError("Minimum average feedback must be less than maximum");
        return;
    }

    const params = new URLSearchParams({
        startDate,
        endDate,
        minAvgFeedback,
        maxAvgFeedback,
        exportType: type
    });

    const url = `${FEEDBACK_API}export?${params.toString()}`;
    console.log("Exporting URL:", url);

    try {
        const response = await fetch(url, {
            method: "GET"
            // Removed Content-Type header as it's unnecessary for GET requests
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = downloadUrl;
        link.download = `feedback_report.${type === "pdf" ? "pdf" : "xls"}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);

        // Use Swal if available, otherwise fall back to alert
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

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return "-";
    const [datePart] = dateTimeStr.split(" ");
    const [year, month, day] = datePart.split("-");
    return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
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
    const allPatient = document.getElementById("all_feedback");

    if (allPatient) allPatient.value = totalItems;

    if (pageInfo) {
        pageInfo.textContent = totalPages === 0
            ? "No feedback found"
            : `Page ${currentPageVal} of ${totalPages} (Total: ${totalItems || 0})`;
    }
    if (prevPage) prevPage.disabled = currentPageVal <= 1;
    if (nextPage) nextPage.disabled = currentPageVal >= (totalPages || 1);
    const itemsPerPage = document.getElementById("itemsPerPage");
    if (itemsPerPage) itemsPerPage.value = pageSizeVal;
}