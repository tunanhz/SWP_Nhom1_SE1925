const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/patients';
let currentPage = 1;
let pageSize = 10;
let sortBy = 'patient_id';
let sortOrder = 'ASC';
let totalPages = 1; // Initialize to prevent undefined issues

document.addEventListener('DOMContentLoaded', function () {
    // Fetch initial data
    fetchPatients(currentPage, pageSize, sortBy, sortOrder);

    // Real-time search and filters with debounce
    const debounceSearch = debounce(() => {
        currentPage = 1; // Reset to first page on new search
        fetchPatients(currentPage, pageSize, sortBy, sortOrder);
    }, 300);

    document.getElementById('searchQuery').addEventListener('input', debounceSearch);
    document.getElementById('dob').addEventListener('input', debounceSearch);
    document.getElementById('gender').addEventListener('change', debounceSearch);
    document.getElementById('itemsPerPage').addEventListener('change', (e) => {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        fetchPatients(currentPage, pageSize, sortBy, sortOrder);
    });

    // Pagination
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            fetchPatients(currentPage, pageSize, sortBy, sortOrder);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            fetchPatients(currentPage, pageSize, sortBy, sortOrder);
        }
    });

    // Sorting on table headers
    document.querySelectorAll('.sort-link').forEach(header => {
        header.addEventListener('click', () => {
            const newSortBy = header.getAttribute('data-sort');
            if (sortBy === newSortBy) {
                sortOrder = sortOrder === 'ASC' ? 'DESC' : 'ASC';
            } else {
                sortBy = newSortBy;
                sortOrder = 'ASC';
            }

            // Update sort icons
            document.querySelectorAll('.sort-icon').forEach(icon => icon.className = 'sort-icon');
            const sortIcon = header.querySelector('.sort-icon');
            if (sortIcon) {
                sortIcon.className = `sort-icon ${sortOrder.toLowerCase()}`;
            }
            currentPage = 1;
            fetchPatients(currentPage, pageSize, sortBy, sortOrder);
        });
    });

    // Logout links
    document.getElementById('logoutLink')?.addEventListener('click', function (event) {
        event.preventDefault();
        localStorage.removeItem('account');
        window.location.href = '/frontend/login.html';
    });

    document.getElementById('logoutModalLink')?.addEventListener('click', function (event) {
        event.preventDefault();
        localStorage.removeItem('account');
        window.location.href = '/frontend/login.html';
    });
});

function fetchPatients(page, pageSize, sortBy, sortOrder) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';
    const tbody = document.getElementById('patient-table-body');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');

    const searchQuery = document.getElementById('searchQuery').value;
    const dob = document.getElementById('dob').value;
    const gender = document.getElementById('gender').value;

    const url = `${baseAPI}?page=${page}&pageSize=${pageSize}&searchQuery=${encodeURIComponent(searchQuery || '')}&dob=${encodeURIComponent(dob || '')}&gender=${encodeURIComponent(gender || '')}&sortBy=${encodeURIComponent(sortBy)}&sortOrder=${encodeURIComponent(sortOrder)}`;

    console.log('Fetching URL:', url);

    fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('Received data:', data);
                renderPatients(data.patients);
                updatePagination(data.totalPages, data.currentPage, data.pageSize, data.totalPatients);
            } else {
                showError(data.message || 'Failed to fetch patients');
                tbody.innerHTML = '<tr><td colspan="6" class="text-center">No patients found</td></tr>';
                if (prevPage) prevPage.disabled = currentPage <= 1;
                if (nextPage) nextPage.disabled = currentPage >= totalPages;
            }
        })
        .catch(error => {
            showError(`Error fetching patients: ${error.message}`);
            console.error('Error:', error);
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">Error loading patients</td></tr>';
            if (prevPage) prevPage.disabled = currentPage <= 1;
            if (nextPage) nextPage.disabled = true;
        });
}

function renderPatients(patients) {
    const tbody = document.getElementById('patient-table-body');
    tbody.innerHTML = '';

    if (patients.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No patients found</td></tr>';
        return;
    }

    console.log('Rendering patients:', patients.length); // Debug rendered count
    patients.forEach(patient => {
        if (!patient || !patient.id) {
            console.warn('Skipping invalid patient:', patient); // Debug invalid rows
            return;
        }
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${patient.fullName || '-'}</td>
            <td>${patient.dob || '-'}</td>
            <td>${patient.gender || '-'}</td>
            <td>${patient.phone || '-'}</td>
            <td>${patient.address || '-'}</td>
            <td>
                <button class="btn btn-checkin" onclick="bookAppointment(${patient.id})">Book</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}



function bookAppointment(patientId) {
    console.log(`Initiating booking for patient ID: ${patientId}`); // Debug booking
    alert(`Booking appointment for patient ID: ${patientId}`);
    // Implement booking logic here (e.g., redirect to booking form or open modal)
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

function updatePagination(totalPagesReceived, currentPageReceived, pageSizeReceived, totalEntries) {
    totalPages = totalPagesReceived || 1; // Ensure totalPages is at least 1
    currentPage = currentPageReceived || 1; // Ensure currentPage is valid
    pageSize = pageSizeReceived || pageSize; // Maintain current pageSize if not provided
    const pageInfo = document.getElementById('pageInfo');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    if (pageInfo) {
        pageInfo.textContent = totalEntries > 0
            ? `Page ${currentPage} of ${totalPages} (Total: ${totalEntries})`
            : 'No patients found';
    }
    if (prevPage) prevPage.disabled = currentPage <= 1;
    if (nextPage) nextPage.disabled = currentPage >= totalPages;
    document.getElementById('itemsPerPage').value = pageSize;
}

//add patient


document.getElementById('addPatient').addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission

    // Reset previous validation feedback
    const form = this;
    form.classList.remove('was-validated');

    // Validate form
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    // Get form values
    const namePatient = document.getElementById('namePatient1').value.trim();
    const dateOfBirth = document.getElementById('dateOfBirth1').value; // Already in yyyy-MM-dd
    const gender = document.getElementById('gender1').value;
    const phonePatient = document.getElementById('phonePatient1').value.trim();
    const address = document.getElementById('address1').value.trim();

    // Additional client-side validation
    if (!namePatient) {
        showError('Patient name is required');
        return;
    }
    if (!dateOfBirth || !/^\d{4}-\d{2}-\d{2}$/.test(dateOfBirth)) {
        showError('Invalid date of birth format (yyyy-MM-dd)');
        return;
    }
    if (!['Male', 'Female', 'Other'].includes(gender)) {
        showError('Please select a valid gender');
        return;
    }
    if (!phonePatient || !/^\d{10,12}$/.test(phonePatient)) {
        showError('Phone number must be 10-12 digits');
        return;
    }
    if (!address) {
        showError('Address is required');
        return;
    }

    // Prepare payload
    const patientData = {
        fullName: namePatient,
        dob: dateOfBirth,
        gender: gender,
        phone: phonePatient,
        address: address
    };

    // Send POST request
    fetch(baseAPI, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(patientData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Close offcanvas
                const offcanvasElement = document.getElementById('offcanvasPatientAdd');
                const offcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement) || new bootstrap.Offcanvas(offcanvasElement);
                offcanvas.hide();

                // Reset form
                form.reset();
                form.classList.remove('was-validated');

                // Refresh patient list
                currentPage = 1; // Reset to first page
                fetchPatients(currentPage, pageSize, sortBy, sortOrder);

                // Show success message (optional)
                alert('Patient added successfully!');
            } else {
                showError(data.message || 'Failed to add patient');
            }
        })
        .catch(error => {
            showError(`Error adding patient: ${error.message}`);
            console.error('Error:', error);
        });
});

// Existing functions (unchanged, included for completeness)
function bookAppointment(patientId) {
    console.log(`Initiating booking for patient ID: ${patientId}`);
    alert(`Booking appointment for patient ID: ${patientId}`);
    // Implement booking logic here
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

function updatePagination(totalPagesReceived, currentPageReceived, pageSizeReceived, totalEntries) {
    totalPages = totalPagesReceived || 1;
    currentPage = currentPageReceived || 1;
    pageSize = pageSizeReceived || pageSize;
    const pageInfo = document.getElementById('pageInfo');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    if (pageInfo) {
        pageInfo.textContent = totalEntries > 0
            ? `Page ${currentPage} of ${totalPages} (Total: ${totalEntries})`
            : 'No patients found';
    }
    if (prevPage) prevPage.disabled = currentPage <= 1;
    if (nextPage) nextPage.disabled = currentPage >= totalPages;
    document.getElementById('itemsPerPage').value = pageSize;
}