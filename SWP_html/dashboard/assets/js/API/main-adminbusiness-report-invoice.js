// Thêm CDN Chart.js
const chartScript = document.createElement('script');
chartScript.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.min.js';
document.head.appendChild(chartScript);

const API_BASE = 'http://localhost:8080/SWP_back_war_exploded/api/adminbusiness-revenue-reports';

// Lấy các phần tử DOM
const startDateInput = document.getElementById('startDate');
const endDateInput = document.getElementById('endDate');
const searchTermInput = document.getElementById('searchTerm');
const getReportBtn = document.querySelector('.btn-primary');
const exportExcelBtn = document.getElementById('exportExcel');
const errorMessage = document.getElementById('errorMessage');
const errorText = document.getElementById('errorText');
const retryBtn = errorMessage.querySelector('.btn-primary');

// Phần tử cho các bảng và phân trang
const totalRevenueElement = document.getElementById('totalRevenue');
const topServicesTable = document.getElementById('topServicesTable');
const prevPageServicesBtn = document.getElementById('prevPageServices');
const nextPageServicesBtn = document.getElementById('nextPageServices');
const pageInfoServices = document.getElementById('pageInfoServices');
const itemsPerPageServices = document.getElementById('itemsPerPageServices');

const topDoctorsTable = document.getElementById('topDoctorsTable');
const prevPageDoctorsBtn = document.getElementById('prevPageDoctors');
const nextPageDoctorsBtn = document.getElementById('nextPageDoctors');
const pageInfoDoctors = document.getElementById('pageInfoDoctors');
const itemsPerPageDoctors = document.getElementById('itemsPerPageDoctors');

const revenueByDepartmentTable = document.getElementById('revenueByDepartmentTable');
const prevPageDepartmentsBtn = document.getElementById('prevPageDepartments');
const nextPageDepartmentsBtn = document.getElementById('nextPageDepartments');
const pageInfoDepartments = document.getElementById('pageInfoDepartments');
const itemsPerPageDepartments = document.getElementById('itemsPerPageDepartments');

// Phần tử cho biểu đồ
const totalRevenueChartCanvas = document.getElementById('totalRevenueChart');
const topRevenueMonthsChartCanvas = document.getElementById('topRevenueMonthsChart');
const revenueByTypeChartCanvas = document.getElementById('revenueByTypeChart');
const invoiceStatusChartCanvas = document.getElementById('invoiceStatusChart');
const topServicesChartCanvas = document.getElementById('topServicesChart');
const topDoctorsChartCanvas = document.getElementById('topDoctorsChart');
const revenueByDepartmentChartCanvas = document.getElementById('revenueByDepartmentChart');

// Đối tượng Chart để quản lý
let charts = {
    totalRevenue: null,
    topRevenueMonths: null,
    topServices: null,
    topDoctors: null,
    revenueByType: null,
    invoiceStatus: null,
    revenueByDepartment: null
};

// State quản lý phân trang và bộ lọc
let state = {
    filters: {
        startDate: '',
        endDate: '',
        searchTerm: ''
    },
    topServices: {
        page: 1,
        pageSize: 5,
        totalRecords: 0,
        data: []
    },
    topDoctors: {
        page: 1,
        pageSize: 5,
        totalRecords: 0,
        data: []
    },
    revenueByDepartment: {
        page: 1,
        pageSize: 5,
        totalRecords: 0,
        data: []
    },
    totalRevenue: 0,
    topRevenueMonths: [],
    revenueByType: [],
    invoiceStatus: []
};

// Hàm hiển thị lỗi
function showError(message) {
    errorText.textContent = message;
    errorMessage.classList.remove('d-none');
}

// Hàm ẩn lỗi
function hideError() {
    errorMessage.classList.add('d-none');
}

// Hàm validate ngày
function validateDate(dateStr) {
    if (!dateStr) return true;
    const datePattern = /^\d{4}-\d{2}-\d{2}$/;
    if (!datePattern.test(dateStr)) return false;
    try {
        new Date(dateStr);
        return true;
    } catch {
        return false;
    }
}

// Hàm lấy dữ liệu từ API
async function fetchData(endpoint, params, section) {
    try {
        const queryString = new URLSearchParams(params).toString();
        const response = await fetch(`${API_BASE}/${endpoint}?${queryString}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(`Failed to fetch ${endpoint}: ${errorData.error || response.statusText}`);
        }

        const data = await response.json();
        return { section, data: data.data };
    } catch (error) {
        throw new Error(`Failed to fetch ${endpoint}: ${error.message}`);
    }
}

// Hàm hủy biểu đồ cũ
function destroyChart(chart) {
    if (chart) {
        chart.destroy();
    }
}

// Hàm định dạng tiền tệ
function formatCurrency(value) {
    return value.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

// Hàm vẽ biểu đồ cột
function drawBarChart(canvas, labels, data, label, title) {
    return new Chart(canvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.6)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: true,
                    text: title
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ${formatCurrency(context.raw)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrency(value);
                        }
                    }
                }
            }
        }
    });
}

// Hàm cập nhật biểu đồ tổng doanh thu
function updateTotalRevenueChart(data) {
    destroyChart(charts.totalRevenue);
    const revenue = data.totalRevenue && typeof data.totalRevenue.totalRevenue === 'number' 
        ? data.totalRevenue.totalRevenue 
        : 0;
    charts.totalRevenue = drawBarChart(
        totalRevenueChartCanvas,
        ['Tổng Doanh Thu'],
        [revenue],
        'Doanh Thu',
        'Tổng Doanh Thu'
    );
}

// Hàm cập nhật biểu đồ doanh thu theo tháng
function updateTopRevenueMonthsChart(data) {
    destroyChart(charts.topRevenueMonths);
    const labels = data.map(item => `${item.month}/${item.year}`);
    const values = data.map(item => item.monthlyRevenue || 0);
    charts.topRevenueMonths = drawBarChart(
        topRevenueMonthsChartCanvas,
        labels,
        values,
        'Doanh Thu',
        'Tháng Có Doanh Thu Cao Nhất'
    );
}

// Hàm cập nhật biểu đồ top dịch vụ
function updateTopServicesChart(data) {
    destroyChart(charts.topServices);
    const labels = data.items.map(item => item.serviceName || 'N/A');
    const values = data.items.map(item => item.totalServiceRevenue || 0);
    charts.topServices = drawBarChart(
        topServicesChartCanvas,
        labels,
        values,
        'Doanh Thu',
        'Top Dịch Vụ'
    );
}

// Hàm cập nhật biểu đồ top bác sĩ
function updateTopDoctorsChart(data) {
    destroyChart(charts.topDoctors);
    const labels = data.items.map(item => item.doctorName || 'N/A');
    const values = data.items.map(item => item.totalRevenue || 0);
    charts.topDoctors = drawBarChart(
        topDoctorsChartCanvas,
        labels,
        values,
        'Doanh Thu',
        'Top Bác Sĩ'
    );
}

// Hàm cập nhật biểu đồ doanh thu theo loại thanh toán
function updateRevenueByTypeChart(data) {
    destroyChart(charts.revenueByType);
    const labels = data.map(item => item.paymentType || 'N/A');
    const values = data.map(item => item.totalRevenueByType || 0);
    charts.revenueByType = drawBarChart(
        revenueByTypeChartCanvas,
        labels,
        values,
        'Doanh Thu',
        'Doanh Thu Theo Loại Thanh Toán'
    );
}

// Hàm cập nhật biểu đồ trạng thái hóa đơn
function updateInvoiceStatusChart(data) {
    destroyChart(charts.invoiceStatus);
    const labels = data.map(item => item.status || 'N/A');
    const values = data.map(item => item.totalAmount || 0);
    charts.invoiceStatus = drawBarChart(
        invoiceStatusChartCanvas,
        labels,
        values,
        'Tổng Số Tiền',
        'Tỷ Lệ Hoàn Thành Hóa Đơn'
    );
}

// Hàm cập nhật biểu đồ doanh thu theo khoa
function updateRevenueByDepartmentChart(data) {
    destroyChart(charts.revenueByDepartment);
    const labels = data.items.map(item => item.department || 'N/A');
    const values = data.items.map(item => item.totalRevenueByDepartment || 0);
    charts.revenueByDepartment = drawBarChart(
        revenueByDepartmentChartCanvas,
        labels,
        values,
        'Doanh Thu',
        'Doanh Thu Theo Khoa'
    );
}

// Hàm cập nhật bảng và phân trang
function updateTableAndPagination(section, data, tableBody, pageInfo, prevBtn, nextBtn) {
    const { items, totalRecords } = data;
    state[section].data = items || [];
    state[section].totalRecords = totalRecords || 0;

    tableBody.classList.remove('loading');
    tableBody.innerHTML = '';

    if (!items || items.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="${section === 'topServices' ? 3 : section === 'topDoctors' ? 3 : 2}" class="text-center">Không có dữ liệu</td></tr>`;
    } else {
        items.forEach(item => {
            const row = document.createElement('tr');
            if (section === 'topServices') {
                row.innerHTML = `
                    <td>${item.serviceName || 'N/A'}</td>
                    <td>${item.totalQuantity || 0}</td>
                    <td>${formatCurrency(item.totalServiceRevenue || 0)}</td>
                `;
            } else if (section === 'topDoctors') {
                row.innerHTML = `
                    <td>${item.doctorName || 'N/A'}</td>
                    <td>${item.department || 'N/A'}</td>
                    <td>${formatCurrency(item.totalRevenue || 0)}</td>
                `;
            } else if (section === 'revenueByDepartment') {
                row.innerHTML = `
                    <td>${item.department || 'N/A'}</td>
                    <td>${formatCurrency(item.totalRevenueByDepartment || 0)}</td>
                `;
            }
            tableBody.appendChild(row);
        });
    }

    const totalPages = Math.ceil(totalRecords / state[section].pageSize);
    pageInfo.textContent = `Trang ${state[section].page} / ${totalPages || 1}`;
    prevBtn.disabled = state[section].page === 1;
    nextBtn.disabled = state[section].page >= totalPages;

    if (section === 'topServices') {
        updateTopServicesChart(data);
    } else if (section === 'topDoctors') {
        updateTopDoctorsChart(data);
    } else if (section === 'revenueByDepartment') {
        updateRevenueByDepartmentChart(data);
    }
}

// Hàm cập nhật tổng doanh thu
function updateTotalRevenue(data) {
    totalRevenueElement.classList.remove('loading');
    const revenue = data.totalRevenue && typeof data.totalRevenue.totalRevenue === 'number' 
        ? data.totalRevenue.totalRevenue 
        : 0;
    totalRevenueElement.textContent = formatCurrency(revenue);
    updateTotalRevenueChart(data);
}

async function exportToExcel() {
    const { startDate, endDate, searchTerm } = state.filters;

    // Validate ngày
    if (startDate && !validateDate(startDate)) {
        showError('Ngày bắt đầu không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (endDate && !validateDate(endDate)) {
        showError('Ngày kết thúc không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError('Ngày bắt đầu phải trước hoặc bằng ngày kết thúc');
        return;
    }

    const params = new URLSearchParams({
        startDate: startDate || '',
        endDate: endDate || '',
        searchTerm: searchTerm || '',
        exportType: 'xlsx'
    });

    try {
        const response = await fetch(`${API_BASE}/export?${params.toString()}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Lỗi khi xuất Excel: ${response.statusText}`);
        }

        const blob = await response.blob();
        const today = new Date();
        const dateStr = today.toISOString().slice(0, 10).replace(/-/g, '');
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BaoCaoDoanhThu_${dateStr}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Hiển thị thông báo thành công
        if (typeof Swal !== 'undefined') {
            Swal.fire('Thành công!', 'Báo cáo đã được xuất dưới dạng XLSX', 'success');
        } else {
            alert('Báo cáo đã được xuất dưới dạng XLSX');
        }
    } catch (error) {
        showError(`Lỗi khi xuất báo cáo: ${error.message}`);
        console.error('Error:', error);
    }
}

// Hàm lấy tất cả dữ liệu
async function fetchAllData() {
    hideError();
    if (!validateDate(state.filters.startDate)) {
        showError('Ngày bắt đầu không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (!validateDate(state.filters.endDate)) {
        showError('Ngày kết thúc không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    try {
        const promises = [
            fetchData('total', state.filters, 'totalRevenue'),
            fetchData('top-services', { ...state.filters, page: state.topServices.page, pageSize: state.topServices.pageSize }, 'topServices'),
            fetchData('top-doctors', { ...state.filters, page: state.topDoctors.page, pageSize: state.topDoctors.pageSize }, 'topDoctors'),
            fetchData('revenue-by-department', { ...state.filters, page: state.revenueByDepartment.page, pageSize: state.revenueByDepartment.pageSize }, 'revenueByDepartment'),
            fetchData('top-months', state.filters, 'topRevenueMonths'),
            fetchData('revenue-by-type', state.filters, 'revenueByType'),
            fetchData('invoice-status', state.filters, 'invoiceStatus')
        ];

        const results = await Promise.all(promises);
        results.forEach(({ section, data }) => {
            if (section === 'totalRevenue') {
                state.totalRevenue = data.totalRevenue && typeof data.totalRevenue.totalRevenue === 'number' 
                    ? data.totalRevenue.totalRevenue 
                    : 0;
                updateTotalRevenue(data);
            } else if (section === 'topServices') {
                updateTableAndPagination('topServices', data, topServicesTable, pageInfoServices, prevPageServicesBtn, nextPageServicesBtn);
            } else if (section === 'topDoctors') {
                updateTableAndPagination('topDoctors', data, topDoctorsTable, pageInfoDoctors, prevPageDoctorsBtn, nextPageDoctorsBtn);
            } else if (section === 'revenueByDepartment') {
                updateTableAndPagination('revenueByDepartment', data, revenueByDepartmentTable, pageInfoDepartments, prevPageDepartmentsBtn, nextPageDepartmentsBtn);
            } else if (section === 'topRevenueMonths') {
                state.topRevenueMonths = data.topRevenueMonths || [];
                updateTopRevenueMonthsChart(state.topRevenueMonths);
            } else if (section === 'revenueByType') {
                state.revenueByType = data.revenueByType || [];
                updateRevenueByTypeChart(state.revenueByType);
            } else if (section === 'invoiceStatus') {
                state.invoiceStatus = data.invoiceStatus || [];
                updateInvoiceStatusChart(state.invoiceStatus);
            }
        });
    } catch (error) {
        showError(error.message);
    }
}

// Xử lý sự kiện thay đổi trang
function handlePageChange(section, direction) {
    if (!validateDate(state.filters.startDate) || !validateDate(state.filters.endDate)) {
        showError('Ngày không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (section === 'topServices') {
        state.topServices.page = Math.max(1, state.topServices.page + direction);
        fetchData('top-services', { ...state.filters, page: state.topServices.page, pageSize: state.topServices.pageSize }, 'topServices')
            .then(({ data }) => updateTableAndPagination('topServices', data, topServicesTable, pageInfoServices, prevPageServicesBtn, nextPageServicesBtn))
            .catch(error => showError(error.message));
    } else if (section === 'topDoctors') {
        state.topDoctors.page = Math.max(1, state.topDoctors.page + direction);
        fetchData('top-doctors', { ...state.filters, page: state.topDoctors.page, pageSize: state.topDoctors.pageSize }, 'topDoctors')
            .then(({ data }) => updateTableAndPagination('topDoctors', data, topDoctorsTable, pageInfoDoctors, prevPageDoctorsBtn, nextPageDoctorsBtn))
            .catch(error => showError(error.message));
    } else if (section === 'revenueByDepartment') {
        state.revenueByDepartment.page = Math.max(1, state.revenueByDepartment.page + direction);
        fetchData('revenue-by-department', { ...state.filters, page: state.revenueByDepartment.page, pageSize: state.revenueByDepartment.pageSize }, 'revenueByDepartment')
            .then(({ data }) => updateTableAndPagination('revenueByDepartment', data, revenueByDepartmentTable, pageInfoDepartments, prevPageDepartmentsBtn, nextPageDepartmentsBtn))
            .catch(error => showError(error.message));
    }
}

// Xử lý sự kiện thay đổi số mục mỗi trang
function handlePageSizeChange(section, pageSize) {
    if (!validateDate(state.filters.startDate) || !validateDate(state.filters.endDate)) {
        showError('Ngày không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (section === 'topServices') {
        state.topServices.pageSize = parseInt(pageSize);
        state.topServices.page = 1;
        fetchData('top-services', { ...state.filters, page: state.topServices.page, pageSize: state.topServices.pageSize }, 'topServices')
            .then(({ data }) => updateTableAndPagination('topServices', data, topServicesTable, pageInfoServices, prevPageServicesBtn, nextPageServicesBtn))
            .catch(error => showError(error.message));
    } else if (section === 'topDoctors') {
        state.topDoctors.pageSize = parseInt(pageSize);
        state.topDoctors.page = 1;
        fetchData('top-doctors', { ...state.filters, page: state.topDoctors.page, pageSize: state.topDoctors.pageSize }, 'topDoctors')
            .then(({ data }) => updateTableAndPagination('topDoctors', data, topDoctorsTable, pageInfoDoctors, prevPageDoctorsBtn, nextPageDoctorsBtn))
            .catch(error => showError(error.message));
    } else if (section === 'revenueByDepartment') {
        state.revenueByDepartment.pageSize = parseInt(pageSize);
        state.revenueByDepartment.page = 1;
        fetchData('revenue-by-department', { ...state.filters, page: state.revenueByDepartment.page, pageSize: state.revenueByDepartment.pageSize }, 'revenueByDepartment')
            .then(({ data }) => updateTableAndPagination('revenueByDepartment', data, revenueByDepartmentTable, pageInfoDepartments, prevPageDepartmentsBtn, nextPageDepartmentsBtn))
            .catch(error => showError(error.message));
    }
}

// Xử lý sự kiện
getReportBtn.addEventListener('click', () => {
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    if (startDate && !validateDate(startDate)) {
        showError('Ngày bắt đầu không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    if (endDate && !validateDate(endDate)) {
        showError('Ngày kết thúc không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    state.filters.startDate = startDate;
    state.filters.endDate = endDate;
    state.filters.searchTerm = searchTermInput.value;
    state.topServices.page = 1;
    state.topDoctors.page = 1;
    state.revenueByDepartment.page = 1;
    fetchAllData();
});

retryBtn.addEventListener('click', fetchAllData);

exportExcelBtn.addEventListener('click', exportToExcel);

prevPageServicesBtn.addEventListener('click', () => handlePageChange('topServices', -1));
nextPageServicesBtn.addEventListener('click', () => handlePageChange('topServices', 1));
itemsPerPageServices.addEventListener('change', (e) => handlePageSizeChange('topServices', e.target.value));

prevPageDoctorsBtn.addEventListener('click', () => handlePageChange('topDoctors', -1));
nextPageDoctorsBtn.addEventListener('click', () => handlePageChange('topDoctors', 1));
itemsPerPageDoctors.addEventListener('change', (e) => handlePageSizeChange('topDoctors', e.target.value));

prevPageDepartmentsBtn.addEventListener('click', () => handlePageChange('revenueByDepartment', -1));
nextPageDepartmentsBtn.addEventListener('click', () => handlePageChange('revenueByDepartment', 1));
itemsPerPageDepartments.addEventListener('change', (e) => handlePageSizeChange('revenueByDepartment', e.target.value));

startDateInput.addEventListener('change', () => {
    const startDate = startDateInput.value;
    if (startDate && !validateDate(startDate)) {
        showError('Ngày bắt đầu không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    state.filters.startDate = startDate;
    state.topServices.page = 1;
    state.topDoctors.page = 1;
    state.revenueByDepartment.page = 1;
    fetchAllData();
});

endDateInput.addEventListener('change', () => {
    const endDate = endDateInput.value;
    if (endDate && !validateDate(endDate)) {
        showError('Ngày kết thúc không đúng định dạng (YYYY-MM-DD)');
        return;
    }
    state.filters.endDate = endDate;
    state.topServices.page = 1;
    state.topDoctors.page = 1;
    state.revenueByDepartment.page = 1;
    fetchAllData();
});

searchTermInput.addEventListener('change', () => {
    state.filters.searchTerm = searchTermInput.value;
    state.topServices.page = 1;
    state.topDoctors.page = 1;
    state.revenueByDepartment.page = 1;
    fetchAllData();
});

// Khởi tạo dữ liệu khi tải trang
fetchAllData();