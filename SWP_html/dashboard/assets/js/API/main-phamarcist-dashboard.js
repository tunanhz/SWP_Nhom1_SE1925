const API_URL = 'http://localhost:8080/SWP_back_war_exploded/api/medicines';
let currentPage = 1;
let pageSize = 10;
let totalItems = 0;
let currentStatus = ''; // Biến lưu trạng thái hiện tại

async function fetchMedicines(page, size, status = '') {
    try {
        // Gọi song song 2 API: thuốc sắp hết hạn và thuốc đã hết hạn
        let nearExpiryUrl = `${API_URL}?nearExpiry=true&days=30&page=${page}&size=${size}`;
        let expiredUrl = `${API_URL}?expired=true`;
        // KHÔNG truyền status vào API nữa

        // Gọi song song
        const [nearExpiryRes, expiredRes] = await Promise.all([
            fetch(nearExpiryUrl, { headers: { 'Accept': 'application/json' } }),
            fetch(expiredUrl, { headers: { 'Accept': 'application/json' } })
        ]);

        if (!nearExpiryRes.ok || !expiredRes.ok) {
            throw new Error(`HTTP error! status: ${nearExpiryRes.status}, ${expiredRes.status}`);
        }

        const nearExpiryData = await nearExpiryRes.json();
        const expiredData = await expiredRes.json();

        // Gộp 2 mảng, ưu tiên thuốc hết hạn lên đầu danh sách
        let allMedicines = [...expiredData, ...nearExpiryData];

        // Lọc trên client theo trạng thái
        if (status) {
            allMedicines = allMedicines.filter(medicine => {
                const expiryDate = new Date(medicine.expDate);
                const today = new Date();
                const daysUntilExpiry = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24));
                if (status === 'expired') return daysUntilExpiry < 0;
                if (status === 'expiring-soon') return daysUntilExpiry >= 0 && daysUntilExpiry <= 30;
                if (status === 'valid') return daysUntilExpiry > 30;
                return true;
            });
        }

        displayMedicines(allMedicines);
        updatePagination(allMedicines.length);
        hideError();
    } catch (error) {
        showError(`Không thể tải danh sách thuốc: ${error.message}`);
        console.error(error);
    }
}

function displayMedicines(medicines) {
    const tableBody = document.getElementById('medicine-table');
    tableBody.innerHTML = '';

    if (!medicines || medicines.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td colspan="10" class="px-6 py-4 text-center">
                <div class="empty-state">
                    Không có thuốc nào sắp hết hạn trong 30 ngày tới
                </div>
            </td>
        `;
        tableBody.appendChild(row);
        return;
    }

    medicines.forEach(medicine => {
        // Tính số ngày đến hạn
        const expiryDate = new Date(medicine.expDate);
        const today = new Date();
        const daysUntilExpiry = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24));
        
        let statusClass = '';
        let statusText = '';
        
        if (daysUntilExpiry < 0) {
            statusClass = 'status-expired';
            statusText = 'Đã hết hạn';
        } else if (daysUntilExpiry <= 30) {
            statusClass = 'status-expiring-soon';
            statusText = 'Sắp hết hạn';
        } else {
            statusClass = 'status-valid';
            statusText = 'Còn hạn';
        }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${medicine.medicineId || 'N/A'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div class="font-medium">${medicine.name || 'N/A'}</div>
                <div class="text-gray-500">${medicine.categoryId || 'N/A'}</div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-900">
                <div class="max-w-xs truncate">${medicine.ingredient || 'Không có mô tả'}</div>
                <div class="text-gray-500">${medicine.usage || ''}</div>
                <div class="text-gray-500">${medicine.preservation || ''}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="font-medium">${medicine.quantity || 0}</span>
                <span class="text-gray-500">${medicine.unitId || ''}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div class="font-medium">${medicine.expDate || 'N/A'}</div>
                <div class="text-gray-500">${daysUntilExpiry >= 0 ? `Còn ${daysUntilExpiry} ngày` : `Hết hạn ${Math.abs(daysUntilExpiry)} ngày`}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span class="${statusClass}">${statusText}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${medicine.price ? medicine.price.toLocaleString('vi-VN') + ' đ' : ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${medicine.warehouseName || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">${medicine.warehouseLocation || ''}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <button onclick="viewMedicineDetails(${medicine.medicineId})" class="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">Xem chi tiết</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function viewMedicineDetails(medicineId) {
    // Có thể mở modal hoặc chuyển đến trang chi tiết
    alert(`Xem chi tiết thuốc có ID: ${medicineId}`);
    // window.location.href = `medicineDetails.html?id=${medicineId}`;
}

function updatePagination(itemsReturned) {
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');

    prevButton.disabled = currentPage === 1;
    nextButton.disabled = itemsReturned < pageSize;

    pageInfo.textContent = `Trang ${currentPage}`;
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

// Fetch and display low stock medicines
async function fetchLowStockMedicines() {
    const lowStockUrl = 'http://localhost:8080/SWP_back_war_exploded//api/medicines?lowStock=true&quantityThreshold=20';
    try {
        const response = await fetch(lowStockUrl, {
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        displayLowStockMedicines(data);
    } catch (error) {
        const container = document.getElementById('low-stock-medicines');
        container.innerHTML = `<div class="empty-state">Không thể tải danh sách thuốc sắp hết hàng: ${error.message}</div>`;
        console.error(error);
    }
}

function displayLowStockMedicines(medicines) {
    const container = document.getElementById('low-stock-medicines');
    if (!medicines || medicines.length === 0) {
        container.innerHTML = `<div class="empty-state">Không có thuốc nào sắp hết hàng (số lượng dưới 20)</div>`;
        return;
    }
    let html = `<div class="bg-white shadow-md rounded-lg p-4 mb-4">
        <h2 class="text-xl font-bold mb-3 text-red-600 flex items-center">
            <svg xmlns='http://www.w3.org/2000/svg' class='inline-block mr-2' width='24' height='24' fill='none' viewBox='0 0 24 24'><path fill='currentColor' d='M12 2a10 10 0 1 1 0 20 10 10 0 0 1 0-20Zm0 2a8 8 0 1 0 0 16A8 8 0 0 0 12 4Zm0 4a1 1 0 0 1 1 1v4a1 1 0 0 1-2 0V9a1 1 0 0 1 1-1Zm0 8a1.25 1.25 0 1 1 0 2.5A1.25 1.25 0 0 1 12 16Z'/></svg>
            Thuốc sắp hết hàng 
        </h2>
        <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên thuốc</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thành phần</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số lượng</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Kho</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
    `;
    medicines.forEach(med => {
        html += `<tr>
            <td class="px-4 py-2">${med.medicineId}</td>
            <td class="px-4 py-2">${med.name}</td>
            <td class="px-4 py-2">${med.ingredient || ''}</td>
            <td class="px-4 py-2 font-bold text-red-600">${med.quantity}</td>
            <td class="px-4 py-2">${med.warehouseName || ''}</td>
        </tr>`;
    });
    html += `</tbody></table></div></div>`;
    container.innerHTML = html;
}

// Event Listeners
document.getElementById('page-size').addEventListener('change', (e) => {
    pageSize = parseInt(e.target.value);
    currentPage = 1;
    fetchMedicines(currentPage, pageSize, currentStatus);
});

document.getElementById('prev-page').addEventListener('click', () => {
    if (currentPage > 1) {
        currentPage--;
        fetchMedicines(currentPage, pageSize, currentStatus);
    }
});

document.getElementById('next-page').addEventListener('click', () => {
    currentPage++;
    fetchMedicines(currentPage, pageSize, currentStatus);
});

// Thêm event listener cho bộ lọc trạng thái
document.getElementById('status-filter').addEventListener('change', (e) => {
    currentStatus = e.target.value;
    currentPage = 1; // Reset về trang 1 khi thay đổi bộ lọc
    fetchMedicines(currentPage, pageSize, currentStatus);
});

// Initial fetch
fetchMedicines(currentPage, pageSize, currentStatus);
fetchLowStockMedicines();