const API_URL = 'http://localhost:8080/SWP_back_war_exploded/api/prescription/';
let currentPage = 1;
let pageSize = 10;
let totalItems = 0;
let currentStatus = ''; // Biến lưu trạng thái hiện tại

async function fetchPrescriptions(page, size, status = '') {
    try {
        // Tạo URL với tham số page, size và status (nếu có)
        let url = `${API_URL}?page=${page}&size=${size}`;
        if (status) {
            url += `&status=${encodeURIComponent(status)}`;
        }

        const response = await fetch(url, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        displayPrescriptions(data);
        updatePagination(data.length);
        hideError();
    } catch (error) {
        showError(`Failed to load prescriptions: ${error.message}`);
        console.error(error);
    }
}

function displayPrescriptions(prescriptions) {
    const tableBody = document.getElementById('prescription-table');
    tableBody.innerHTML = '';

    prescriptions.forEach(p => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${p.prescriptionId}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${p.patientName}<br>
                <span class="text-gray-500">${p.patientPhone}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${p.doctorName}<br>
                <span class="text-gray-500">${p.doctorPhone}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${p.doctorDepartment}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${p.prescriptionDate}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span class="px-2 py-1 rounded-full text-xs ${
                    p.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                    p.status === 'Dispensed' ? 'bg-green-100 text-green-800' :
                    'bg-red-100 text-red-800'
                }">${p.status}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <button onclick="viewDetails(${p.prescriptionId})" class="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">View Details</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function viewDetails(prescriptionId) {
    window.location.href = `prescriptionDetails.html?id=${prescriptionId}`;
}

function updatePagination(itemsReturned) {
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');

    prevButton.disabled = currentPage === 1;
    nextButton.disabled = itemsReturned < pageSize;

    pageInfo.textContent = `Page ${currentPage}`;
}

function showError(message) {
    const errorDiv = document.getElementById('error-message');
    errorDiv.textContent = message;
    errorDiv.classList.remove('hidden');
}

function hideError() {
    const errorDiv = document.getElementById('error-message');
    errorDiv.classList.add('hidden');
}

// Event Listeners
document.getElementById('page-size').addEventListener('change', (e) => {
    pageSize = parseInt(e.target.value);
    currentPage = 1;
    fetchPrescriptions(currentPage, pageSize, currentStatus);
});

document.getElementById('prev-page').addEventListener('click', () => {
    if (currentPage > 1) {
        currentPage--;
        fetchPrescriptions(currentPage, pageSize, currentStatus);
    }
});

document.getElementById('next-page').addEventListener('click', () => {
    currentPage++;
    fetchPrescriptions(currentPage, pageSize, currentStatus);
});

// Thêm event listener cho bộ lọc trạng thái
document.getElementById('status-filter').addEventListener('change', (e) => {
    currentStatus = e.target.value;
    currentPage = 1; // Reset về trang 1 khi thay đổi bộ lọc
    fetchPrescriptions(currentPage, pageSize, currentStatus);
});

// Initial fetch
fetchPrescriptions(currentPage, pageSize, currentStatus);