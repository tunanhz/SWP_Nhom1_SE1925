const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/adminBusinessReport';
let currentPage = 1;
let pageSize = 10;

document.addEventListener('DOMContentLoaded', function () {
    localStorage.removeItem('patient_id');
    console.log('Cleared patient_id:', localStorage.getItem('patient_id'));

    flatpickr('#startDate', { dateFormat: 'Y-m-d' });
    flatpickr('#endDate', { dateFormat: 'Y-m-d' });

    fetchPatientRecords(currentPage, pageSize);

    const debounceSearch = debounce(() => {
        currentPage = 1; 
        fetchPatientRecords(currentPage, pageSize);
    }, 300);

    document.getElementById('searchQuery').addEventListener('input', debounceSearch);
    document.getElementById('startDate').addEventListener('change', debounceSearch);
    document.getElementById('endDate').addEventListener('change', debounceSearch);
    document.getElementById('filterGender').addEventListener('change', debounceSearch);
    document.getElementById('itemsPerPage').addEventListener('change', (e) => {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        fetchPatientRecords(currentPage, pageSize);
    });

    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            fetchPatientRecords(currentPage, pageSize);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        currentPage++;
        fetchPatientRecords(currentPage, pageSize);
    });
});

function fetchPatientRecords(page, pageSize) {
    const errorMessage = document.getElementById('error-message');
    if (errorMessage) errorMessage.style.display = 'none';

    const tbody = document.getElementById('patientTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center">Loading...</td></tr>';

    const searchQuery = document.getElementById('searchQuery').value.trim();
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const gender = document.getElementById('filterGender').value;

    if (!validateDates(startDate, endDate)) {
        tbody.innerHTML = '';
        return;
    }

    // Build query parameters
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', pageSize);
    if (searchQuery) params.append('patientName', searchQuery);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (gender) params.append('gender', gender);

    const url = `${baseAPI}?${params.toString()}`;

    console.log('Fetching URL:', url);

    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Received data:', data);
            renderPatientRecords(data.records || [], data.totalRecords || 0, data.totalPages || 1);
        })
        .catch(error => {
            showError(error.message);
            console.error('Error:', error);
            tbody.innerHTML = '';
        });
}

async function renderPatientRecords(records, totalRecords, totalPages) {
    const tbody = document.getElementById('patientTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center">Loading...</td></tr>';

    if (!records || records.length === 0) {
        tbody.innerHTML = '<h3 class="text-center">No patient records found.</h3>';
        return;
    }

    tbody.innerHTML = ''; 
    records.forEach((record, index) => {
        if (!record || !record.patientId) {
            console.warn('Skipping invalid patient record:', record);
            return;
        }
        const row = createTableRow(record, index);
        tbody.appendChild(row);
    });

    attachFeedbackListeners();
    updatePagination(totalPages, currentPage, pageSize, totalRecords);
}

function createTableRow(record, index) {
    const row = document.createElement('tr');
    row.setAttribute('data-patient-id', record.patientId);
    row.innerHTML = `
        <td>${sanitizeHTML(index + 1)}</td>
        <td>${sanitizeHTML(record.patientName || '-')}</td>
        <td>${calculateAge(record.dob)}</td>
        <td>${sanitizeHTML(record.gender || '-')}</td>
        <td>${sanitizeHTML(record.disease || '-')}</td>
        <td>${formatDateTime(record.appointmentDatetime)}</td>
        <td><span class="badge bg-success-subtle p-2 text-success">Complete</span></td>
        <td>
            <div class="d-flex gap-2">
                <a href="adminbusiness-view-recordPatient.html" data-patient-id="${record.patientId}" class="btn btn-primary btn-sm rounded-pill feedback-link" data-bs-toggle="tooltip" title="View Profile" aria-label="View Profile">
                    <svg class="icon-20" width="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" fill="currentColor"/>
                    </svg>
                </a>
            </div>
        </td>
    `;
    return row;
}

function attachFeedbackListeners() {
    document.querySelectorAll('.feedback-link').forEach(link => {
        link.removeEventListener('click', handleFeedbackClick);
        link.addEventListener('click', handleFeedbackClick);
    });
}

function handleFeedbackClick(e) {
    const patientId = e.currentTarget.getAttribute('data-patient-id');
    localStorage.setItem('patient_id', patientId);
}

function calculateAge(dob) {
    if (!dob) return '-';
    const birthDate = new Date(dob);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
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
    const errorMessage = document.getElementById('error-message');
    if (errorMessage) {
        errorMessage.textContent = message || 'An error occurred';
        errorMessage.style.display = 'block';
    }
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function updatePagination(totalPages, currentPage, pageSize, totalRecords) {
    const pageInfo = document.getElementById('pageInfo');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    if (pageInfo) {
        pageInfo.textContent = `Page ${currentPage} of ${totalPages} (Total: ${totalRecords})`;
    }
    if (prevPage) {
        prevPage.classList.toggle('disabled', currentPage <= 1);
    }
    if (nextPage) {
        nextPage.classList.toggle('disabled', currentPage >= totalPages);
    }
    document.getElementById('itemsPerPage').value = pageSize;
}

function validateDates(startDate, endDate) {
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError('Start date must be before end date');
        return false;
    }
    return true;
}

function sanitizeHTML(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}