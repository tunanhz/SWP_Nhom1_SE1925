const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/appointments';

document.addEventListener('DOMContentLoaded', function () {
    let currentPage = 1;
    let pageSize = 10;
    fetchAppointments(currentPage, pageSize);

    flatpickr('#startDate', { dateFormat: 'Y-m-d' });
    flatpickr('#endDate', { dateFormat: 'Y-m-d' });
    // Event delegation for check-in buttons
    document.getElementById('checkin-table-body').addEventListener('click', function (event) {
        if (event.target.classList.contains('btn-checkin')) {
            const appointmentId = event.target.dataset.appointmentId;
            handleCheckIn(appointmentId, event.target);
        }
    });

    // Real-time search and filters
    const debounceSearch = debounce(() => {
        currentPage = 1; // Reset to first page on new search
        fetchAppointments(currentPage, pageSize);
    }, 300);

    document.getElementById('searchQuery').addEventListener('input', debounceSearch);
    document.getElementById('startDate').addEventListener('change', debounceSearch);
    document.getElementById('endDate').addEventListener('change', debounceSearch);
    document.getElementById('filterStatus').addEventListener('change', debounceSearch);
    document.getElementById('itemsPerPage').addEventListener('change', (e) => {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        fetchAppointments(currentPage, pageSize);
    });

    // Pagination
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            fetchAppointments(currentPage, pageSize);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        currentPage++;
        fetchAppointments(currentPage, pageSize);
    });
});

function fetchAppointments(page, pageSize) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';

    const searchQuery = document.getElementById('searchQuery').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const status = document.getElementById('filterStatus').value;

    const url = `${baseAPI}?page=${page}&pageSize=${pageSize}&searchQuery=${encodeURIComponent(searchQuery || '')}&startDate=${encodeURIComponent(startDate || '')}&endDate=${encodeURIComponent(endDate || '')}&status=${encodeURIComponent(status || '')}&sortBy=appointment_id&sortOrder=ASC`;

    console.log('Fetching URL:', url); // Debug request

    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('Received data:', data); // Debug response
                renderAppointments(data.appointments);
                updatePagination(data.totalPages, data.currentPage, data.pageSize, data.totalAppointments);
            } else {
                showError(data.message || 'Failed to fetch appointments');
            }
        })
        .catch(error => {
            showError(`Error fetching appointments: ${error.message}`);
            console.error('Error:', error);
        });
}

function renderAppointments(appointments) {
    const tbody = document.getElementById('checkin-table-body');
    tbody.innerHTML = '';

    if (appointments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No appointments found</td></tr>';
        return;
    }

    console.log('Rendering appointments:', appointments.length); // Debug rendered count
    appointments.forEach(appt => {
        if (!appt || !appt.appointmentId) {
            console.warn('Skipping invalid appointment:', appt); // Debug invalid rows
            return;
        }
        const row = document.createElement('tr');
        const statusClass = getStatusClass(appt.status);
        row.innerHTML = `
            <td>${appt.patientName || '-'}</td>
            <td>${appt.doctorName || '-'}</td>
            <td>${formatDateTime(appt.appointmentDatetime) || '-'}</td>
            <td>${appt.shift || '-'}</td>
            <td>${appt.note || '-'}</td>
            <td><span class="status-badge ${statusClass}">${appt.status || '-'}</span></td>
            <td>
                ${appt.status === 'Pending' ?
                `<button class="btn btn-checkin" data-appointment-id="${appt.appointmentId || 0}">Check-in</button>`
                : '-'}
            </td>
        `;
        tbody.appendChild(row);
    });
}

function handleCheckIn(appointmentId, button) {
    button.disabled = true;
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';

    const account = JSON.parse(localStorage.getItem('account'));
    const receptionistId = account?.accountStaffId || 1; // Fallback to 1 if not found

    fetch(`${baseAPI}/checkin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ appointmentId: Number(appointmentId), receptionistId })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert('Check-in successful!');
                fetchAppointments(1, 6); // Refresh the table
            } else {
                showError(data.message || 'Check-in failed!');
                button.disabled = false;
            }
        })
        .catch(error => {
            showError(`Error during check-in: ${error.message}`);
            button.disabled = false;
            console.error('Error:', error);
        });
}

function getStatusClass(status) {
    switch ((status || '').toLowerCase()) {
        case 'pending':
            return 'status-pending';
        case 'confirmed':
            return 'status-confirmed';
        case 'cancelled':
            return 'status-cancelled';
        case 'completed':
            return 'status-completed';
        default:
            return '';
    }
}

function formatDateTime(dateTime) {
    if (!dateTime) return '-';
    const date = new Date(dateTime);
    return date.toLocaleString('en-GB', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
}

function showError(message) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function updatePagination(totalPages, currentPage, pageSize, totalAppointments) {
    const pageInfo = document.getElementById('pageInfo');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    if (pageInfo) pageInfo.textContent = `Page ${currentPage} of ${totalPages || 1} (Total: ${totalAppointments || 0})`;
    if (prevPage) prevPage.classList.toggle('disabled', currentPage <= 1);
    if (nextPage) nextPage.classList.toggle('disabled', currentPage >= (totalPages || 1));
    document.getElementById('itemsPerPage').value = pageSize;
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