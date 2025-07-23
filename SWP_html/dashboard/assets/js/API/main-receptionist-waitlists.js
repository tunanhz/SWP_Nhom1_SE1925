const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/waitlist';
let currentPage = 1;
    let pageSize = 10;
    let sortBy = 'waitlist_id';
    let sortOrder = 'ASC';
document.addEventListener('DOMContentLoaded', function () {
    

    // Initialize Flatpickr for date inputs
    flatpickr('#startDate', { dateFormat: 'Y-m-d' });
    flatpickr('#endDate', { dateFormat: 'Y-m-d' });

    // Fetch initial data
    fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);

    // Event delegation for update buttons
    document.getElementById('waitlist-table-body').addEventListener('click', function (event) {
        if (event.target.classList.contains('btn-update')) {
            const waitlistId = event.target.dataset.waitlistId;
            const estimatedTime = event.target.dataset.estimatedTime || '';
            showUpdateModal(waitlistId, estimatedTime, event.target);
        }
    });

    // Real-time search and filters with debounce
    const debounceSearch = debounce(() => {
        currentPage = 1; // Reset to first page on new search
        fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);
    }, 300);

    document.getElementById('searchQuery').addEventListener('input', debounceSearch);
    document.getElementById('startDate').addEventListener('change', debounceSearch);
    document.getElementById('endDate').addEventListener('change', debounceSearch);
    document.getElementById('filterStatus').addEventListener('change', debounceSearch);
    document.getElementById('filterVisitType').addEventListener('change', debounceSearch);
    document.getElementById('itemsPerPage').addEventListener('change', (e) => {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);
    });

    // Pagination
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        currentPage++;
        fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);
    });

    // Sorting on table headers
    document.querySelectorAll('.sortable').forEach(header => {
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
            header.querySelector('.sort-icon').className = `sort-icon ${sortOrder.toLowerCase()}`;
            currentPage = 1;
            fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder);
        });
    });

    // Logout links
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

function fetchWaitlistEntries(page, pageSize, sortBy, sortOrder) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';

    const searchQuery = document.getElementById('searchQuery').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const status = document.getElementById('filterStatus').value;
    const visitType = document.getElementById('filterVisitType').value;

    const url = `${baseAPI}?page=${page}&pageSize=${pageSize}&searchQuery=${encodeURIComponent(searchQuery || '')}&startDate=${encodeURIComponent(startDate || '')}&endDate=${encodeURIComponent(endDate || '')}&status=${encodeURIComponent(status || '')}&visitType=${encodeURIComponent(visitType || '')}&sortBy=${encodeURIComponent(sortBy)}&sortOrder=${encodeURIComponent(sortOrder)}`;

    console.log('Đang lấy dữ liệu từ URL:', url); // Debug request

    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Lỗi HTTP! Trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('Dữ liệu nhận được:', data); // Debug response
                renderWaitlistEntries(data.waitlistEntries);
                updatePagination(data.totalPages, data.currentPage, data.pageSize, data.totalEntries);
            } else {
                showError(data.message || 'Không thể lấy danh sách chờ');
            }
        })
        .catch(error => {
            showError(`Lỗi khi lấy danh sách chờ: ${error.message}`);
            console.error('Error:', error);
        });
}

function renderWaitlistEntries(entries) {
    const tbody = document.getElementById('waitlist-table-body');
    tbody.innerHTML = '';

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">No waitlist entries found</td></tr>';
        return;
    }

    console.log('Đang hiển thị danh sách chờ:', entries.length);
    entries.forEach((entry, index) => {
        if (!entry || !entry.waitlistId) {
            console.warn('Bỏ qua mục danh sách chờ không hợp lệ:', entry);
            return;
        }
        const statusClass = getStatusClass(entry.status);
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${entry.patientName || '-'}</td>
            <td>${entry.doctorName || '-'}</td>
            <td>${entry.roomName || '-'}</td>
            <td>${formatDateTime(entry.registeredAt) || '-'}</td>
            <td>${formatDateTime(entry.estimatedTime) || '-'}</td>
            <td>${entry.visitType || '-'}</td>
            <td><span class="status-badge ${statusClass}">${entry.status || '-'}</span></td>
            <td>
                ${entry.visitType === 'Initial' && entry.status === 'Waiting' ?
                `<button class="btn btn-update" data-waitlist-id="${entry.waitlistId}" data-estimated-time="${entry.estimatedTime || ''}">Cập Nhật</button>`
                : '-'}
            </td>
        `;
        tbody.appendChild(row);
    });
}

function showUpdateModal(waitlistId, estimatedTime, button) {
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'updateEstimatedTimeModal';
    modal.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Cập Nhật Thời Gian Dự Kiến</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label for="estimatedTimeInput">Thời Gian Dự Kiến</label>
                        <input type="datetime-local" class="form-control" id="estimatedTimeInput" 
                               value="${estimatedTime ? new Date(estimatedTime).toISOString().slice(0, 16) : ''}">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="button" class="btn btn-primary" onclick="handleUpdateEstimatedTime(${waitlistId}, this)">Lưu</button>
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    modal.addEventListener('hidden.bs.modal', () => modal.remove());
}

function handleUpdateEstimatedTime(waitlistId, button) {
    button.disabled = true;
    const errorMessage = document.getElementById('error-message');
    errorMessage.style.display = 'none';

    const estimatedTime = document.getElementById('estimatedTimeInput').value;
    if (!estimatedTime) {
        showError('Vui lòng chọn thời gian dự kiến.');
        button.disabled = false;
        return;
    }

    fetch(`${baseAPI}/update-estimated-time`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ waitlistId: Number(waitlistId), estimatedTime })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Lỗi HTTP! Trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert('Cập nhật thời gian dự kiến thành công!');
                bootstrap.Modal.getInstance(document.getElementById('updateEstimatedTimeModal')).hide();
                fetchWaitlistEntries(currentPage, pageSize, sortBy, sortOrder); // Refresh the table
            } else {
                showError(data.message || 'Không thể cập nhật thời gian dự kiến');
                button.disabled = false;
            }
        })
        .catch(error => {
            showError(`Lỗi trong quá trình cập nhật: ${error.message}`);
            button.disabled = false;
            console.error('Error:', error);
        });
}

function getStatusClass(status) {
    switch ((status || '').toLowerCase()) {
        case 'waiting':
            return 'status-waiting';
        case 'inprogress':
            return 'status-inprogress';
        case 'skipped':
            return 'status-skipped';
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

function updatePagination(totalPages, currentPage, pageSize, totalEntries) {
    const pageInfo = document.getElementById('pageInfo');
    const prevPage = document.getElementById('prevPage');
    const nextPage = document.getElementById('nextPage');
    if (pageInfo) pageInfo.textContent = `Trang ${currentPage} / ${totalPages || 1} (Tổng: ${totalEntries || 0})`;
    if (prevPage) prevPage.classList.toggle('disabled', currentPage <= 1);
    if (nextPage) nextPage.classList.toggle('disabled', currentPage >= (totalPages || 1));
    document.getElementById('itemsPerPage').value = pageSize;
}