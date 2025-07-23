const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/dashboard';

document.addEventListener('DOMContentLoaded', function () {
    fetchTop3AppointmentsPerDay();
    fetchTop3WaitlistEntriesPerDay();

    const account = JSON.parse(localStorage.getItem('account'));
    if (account) {
        document.getElementById('username').textContent = `Xin chào lễ tân ${account.username}`;
    }
    // logout
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

function fetchTop3AppointmentsPerDay() {
    const errorMessage = document.getElementById('error-message');
    const tbody = document.getElementById('upcomingAppointmentsTableBody');

    // view loading state
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">Loading...</td></tr>';

    const startDate = new Date().toISOString().split('T')[0]; // today
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + 7); // 7 days ahead
    const endDateStr = endDate.toISOString().split('T')[0];

    const url = `${baseAPI}/top3appointments?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDateStr)}`;

    console.log('Fetching URL:', url);

    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.message || `HTTP error! Status: ${response.status}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Received appointment data:', data);
            if (!data.success) {
                throw new Error(data.message || 'Failed to fetch appointments');
            }
            renderAppointments(data.appointments);
        })
        .catch(error => {
            showError(`Error fetching top 3 appointments: ${error.message}`);
            console.error('Error:', error);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">Failed to load appointments</td></tr>';
        });
}

function fetchTop3WaitlistEntriesPerDay() {
    const errorMessage = document.getElementById('error-message');
    const tbody = document.getElementById('waitlistTableBody');

    // view loading state
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">Loading...</td></tr>';

    const startDate = new Date().toISOString().split('T')[0]; // today
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + 7); // 7 days ahead
    const endDateStr = endDate.toISOString().split('T')[0];

    const url = `${baseAPI}/top3waitlist?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDateStr)}`;

    console.log('Fetching Waitlist URL:', url);

    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.message || `HTTP error! Status: ${response.status}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Received waitlist data:', data);
            if (!data.success) {
                throw new Error(data.message || 'Failed to fetch waitlist entries');
            }
            renderWaitlistEntries(data.waitlistEntries);
        })
        .catch(error => {
            showError(`Error fetching top 3 waitlist entries: ${error.message}`);
            console.error('Error:', error);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">Failed to load waitlist entries</td></tr>';
        });
}

function renderAppointments(appointments) {
    const tbody = document.getElementById('upcomingAppointmentsTableBody');
    tbody.innerHTML = '';

    if (!appointments || appointments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No pending appointments found</td></tr>';
        return;
    }

    let currentDate = '';
    let stt = 0;

    console.log('Rendering appointments:', appointments.length);
    appointments.forEach((appt, index) => {
        if (!appt || !appt.appointmentId) {
            console.warn('Skipping invalid appointment:', appt);
            return;
        }
        const apptDate = formatDateTime(appt.appointmentDatetime).split(' ')[0];
        if (apptDate !== currentDate) {
            currentDate = apptDate;
            stt = 1; // Reset STT 
        } else {
            stt++;
        }
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index+1}</td>
            <td>${appt.patientName || '-'}</td>
            <td>${formatDateTime(appt.appointmentDatetime) || '-'}</td>
            <td>${appt.shift || '-'}</td>
            <td>${appt.note || '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function renderWaitlistEntries(waitlistEntries) {
    const tbody = document.getElementById('waitlistTableBody');
    tbody.innerHTML = '';

    if (!waitlistEntries || waitlistEntries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No waitlist entries found</td></tr>';
        return;
    }

    let currentDate = '';
    let stt = 0;

    console.log('Rendering waitlist entries:', waitlistEntries.length);
    waitlistEntries.forEach((entry,index) => {
        if (!entry || !entry.waitlistId) {
            console.warn('Skipping invalid waitlist entry:', entry);
            return;
        }
        const entryDate = formatDateTime(entry.estimatedTime).split(' ')[0];
        if (entryDate !== currentDate) {
            currentDate = entryDate;
            stt = 1; // Reset STT 
        } else {
            stt++;
        }
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index+1}</td>
            <td>${entry.patientName || '-'}</td>
            <td>${entry.doctorName || '-'}</td>
            <td>${entry.roomName || '-'}</td>
            <td>${formatDateTime(entry.estimatedTime) || '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function formatDateTime(dateTime) {
    if (!dateTime) return '-';
    const date = new Date(dateTime);
    if (isNaN(date.getTime())) return '-';
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
    if (errorMessage) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    } else {
        console.error('Error message element not found:', message);
    }
}