const baseAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/patients';
const appointmentAPI = 'http://localhost:8080/SWP_back_war_exploded/api/receptionist/book-appointment';
const doctorAPI = 'http://localhost:8080/SWP_back_war_exploded/api/all-doctors';
const departmentAPI = 'http://localhost:8080/SWP_back_war_exploded/api/doctors/departments';
const availabilityAPI = 'http://localhost:8080/SWP_back_war_exploded/api/doctor-availability';
let currentPage = 1;
let pageSize = 10;
let sortBy = 'patient_id';
let sortOrder = 'ASC';
let totalPages = 1;
let doctorWorkingDates = [];
let selectedSlot = null;

document.addEventListener('DOMContentLoaded', function () {
    fetchPatients(currentPage, pageSize, sortBy, sortOrder);

    const debounceSearch = debounce(() => {
        currentPage = 1;
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

    document.querySelectorAll('.sort-link').forEach(header => {
        header.addEventListener('click', () => {
            const newSortBy = header.getAttribute('data-sort');
            if (sortBy === newSortBy) {
                sortOrder = sortOrder === 'ASC' ? 'DESC' : 'ASC';
            } else {
                sortBy = newSortBy;
                sortOrder = 'ASC';
            }
            document.querySelectorAll('.sort-icon').forEach(icon => icon.className = 'sort-icon');
            const sortIcon = header.querySelector('.sort-icon');
            if (sortIcon) {
                sortIcon.className = `sort-icon ${sortOrder.toLowerCase()}`;
            }
            currentPage = 1;
            fetchPatients(currentPage, pageSize, sortBy, sortOrder);
        });
    });

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

    initializeAppointmentForm();
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

    console.log('Đang lấy dữ liệu từ URL:', url);

    fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Lỗi HTTP! Trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('Dữ liệu nhận được:', data);
                renderPatients(data.patients);
                updatePagination(data.totalPages, data.currentPage, data.pageSize, data.totalPatients);
            } else {
                showError(data.message || 'Không thể lấy danh sách bệnh nhân');
                tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không tìm thấy bệnh nhân</td></tr>';
                if (prevPage) prevPage.disabled = currentPage <= 1;
                if (nextPage) nextPage.disabled = currentPage >= totalPages;
            }
        })
        .catch(error => {
            showError(`Lỗi khi lấy danh sách bệnh nhân: ${error.message}`);
            console.error('Error:', error);
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">Lỗi khi tải danh sách bệnh nhân</td></tr>';
            if (prevPage) prevPage.disabled = currentPage <= 1;
            if (nextPage) nextPage.disabled = true;
        });
}

function renderPatients(patients) {
    const tbody = document.getElementById('patient-table-body');
    tbody.innerHTML = '';

    if (patients.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không tìm thấy bệnh nhân</td></tr>';
        return;
    }

    console.log('Đang hiển thị danh sách bệnh nhân:', patients.length);
    patients.forEach((patient, index) => {
        if (!patient || !patient.id) {
            console.warn('Bỏ qua bệnh nhân không hợp lệ:', patient);
            return;
        }
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${patient.fullName || '-'}</td>
            <td>${patient.dob || '-'}</td>
            <td>${patient.gender || '-'}</td>
            <td>${patient.phone || '-'}</td>
            <td>${patient.address || '-'}</td>
            <td>
                <button class="btn btn-checkin" onclick="bookAppointment(${patient.id})">Đặt Lịch</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function bookAppointment(patientId) {
    console.log(`Bắt đầu đặt lịch cho bệnh nhân ID: ${patientId}`);
    const offcanvasElement = document.getElementById('offcanvasAppointmentBook');
    const offcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement) || new bootstrap.Offcanvas(offcanvasElement);
    document.getElementById('patientId').value = patientId;
    offcanvas.show();
}

function initializeAppointmentForm() {
    const departmentSelect = document.getElementById('departmentSelect');
    const doctorSelect = document.getElementById('doctorSelect');
    const dateForm = document.getElementById('dateForm');
    const timeForm = document.getElementById('timeForm');
    const selectedTime = document.getElementById('selectedTime');
    const selectedDate = document.getElementById('selectedDate');
    const calendar = document.getElementById('calendar');
    const yearSelect = document.getElementById('yearSelect');
    const monthSelect = document.getElementById('monthSelect');
    const appointmentForm = document.getElementById('appointmentForm');

    function populateYears() {
        const currentYear = new Date().getFullYear();
        for (let i = currentYear; i <= currentYear + 1; i++) {
            const option = document.createElement('option');
            option.value = i;
            option.textContent = i;
            yearSelect.appendChild(option);
        }
        yearSelect.value = currentYear;
    }

    function populateMonths() {
        const months = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        const currentMonth = new Date().getMonth();
        months.forEach((month, index) => {
            const option = document.createElement('option');
            option.value = index;
            option.textContent = month;
            monthSelect.appendChild(option);
        });
        monthSelect.value = currentMonth;
    }

    function isPastDate(year, month, day) {
        const selectedDate = new Date(year, month, day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selectedDate < today;
    }

    function isPastTime(time, selectedDateStr) {
        const today = new Date();
        const selected = new Date(`${selectedDateStr}T${time}`);
        return selected < today;
    }

    function isWorkingDate(year, month, day) {
        const dateStr = `${year}-${String(parseInt(month) + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        return doctorWorkingDates.includes(dateStr);
    }

    function generateCalendar(year, month) {
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDay = firstDay.getDay();
        let table = '<table><tr><th>Mon</th><th>Tue</th><th>Wed</th><th>Thu</th><th>Fri</th><th>Sat</th><th>Sun</th></tr><tr>';
        let dayCount = 1;

        for (let i = 0; i < 6; i++) {
            for (let j = 0; j < 7; j++) {
                if ((i === 0 && j < startingDay) || dayCount > daysInMonth) {
                    table += '<td></td>';
                } else {
                    const isPast = isPastDate(year, month, dayCount);
                    const isWorking = isWorkingDate(year, month, dayCount);
                    table += `<td class="${isPast || !isWorking ? 'disabled' : ''}">${dayCount}</td>`;
                    dayCount++;
                }
            }
            table += '</tr><tr>';
            if (dayCount > daysInMonth) break;
        }
        table += '</tr></table>';
        calendar.innerHTML = table;

        const days = document.querySelectorAll('#calendar table td');
        days.forEach(day => {
            if (day.textContent && !day.classList.contains('disabled')) {
                day.addEventListener('click', () => {
                    days.forEach(d => d.classList.remove('selected'));
                    day.classList.add('selected');
                    timeForm.style.display = 'block';
                    resetTimeSelection();
                    const selectedDay = day.textContent.padStart(2, '0');
                    const selectedMonth = String(parseInt(month) + 1).padStart(2, '0');
                    selectedDate.value = `${year}-${selectedMonth}-${selectedDay}`;
                    updateTimeSlots();
                });
            }
        });
    }

    async function updateTimeSlots() {
        const timeSlots = document.querySelectorAll('.time-slot');
        const selectedDateStr = selectedDate.value;
        const doctorId = doctorSelect.value;
        const today = new Date();
        const isToday = new Date(selectedDateStr).toDateString() === today.toDateString();

        timeSlots.forEach(slot => {
            slot.classList.remove('disabled');
            if (isToday && isPastTime(slot.value, selectedDateStr)) {
                slot.classList.add('disabled');
            }
        });

        if (!doctorId || !selectedDateStr) {
            console.warn('Bỏ qua lấy dữ liệu: thiếu doctorId hoặc ngày', { doctorId, selectedDateStr });
            return;
        }

        try {
            const url = `${availabilityAPI}?doctorId=${encodeURIComponent(doctorId)}&date=${encodeURIComponent(selectedDateStr)}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error(`Không thể lấy danh sách thời gian đã đặt: HTTP ${response.status} - ${response.statusText}`, errorText);
                return;
            }

            const bookedTimes = await response.json();
            timeSlots.forEach(slot => {
                if (bookedTimes.includes(slot.value)) {
                    slot.classList.add('disabled');
                }
            });
        } catch (error) {
            console.error('Lỗi khi lấy danh sách thời gian đã đặt:', error.message, error);
            showError(`Lỗi khi lấy danh sách thời gian đã đặt: ${error.message}`);
        }
    }

    function resetTimeSelection() {
        const timeSlots = document.querySelectorAll('.time-slot');
        timeSlots.forEach(slot => slot.classList.remove('selected'));
        selectedTime.value = '';
        if (selectedSlot) selectedSlot.classList.remove('selected');
        selectedSlot = null;
    }

    async function fetchDoctorWorkingDates(doctorId) {
        if (!doctorId) {
            doctorWorkingDates = [];
            generateCalendar(yearSelect.value, monthSelect.value);
            return;
        }

        try {
            const url = `${availabilityAPI}?doctorId=${encodeURIComponent(doctorId)}&action=getWorkingDates`;
            const response = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error(`Không thể lấy danh sách ngày làm việc: HTTP ${response.status} - ${response.statusText}`, errorText);
                doctorWorkingDates = [];
            } else {
                doctorWorkingDates = await response.json();
            }
            generateCalendar(yearSelect.value, monthSelect.value);
        } catch (error) {
            console.error('Lỗi khi lấy danh sách ngày làm việc:', error.message, error);
            doctorWorkingDates = [];
            generateCalendar(yearSelect.value, monthSelect.value);
            showError(`Lỗi khi lấy danh sách ngày làm việc: ${error.message}`);
        }
    }

    function loadDepartments() {
        fetch(departmentAPI, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
            .then(res => res.json())
            .then(data => {
                data.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept;
                    option.textContent = dept;
                    departmentSelect.appendChild(option);
                });
                loadDoctors();
            })
            .catch(error => {
                console.error('Lỗi khi lấy danh sách khoa:', error);
                showError(`Lỗi khi lấy danh sách khoa: ${error.message}`);
            });
    }

    function loadDoctors(department = "") {
        doctorSelect.innerHTML = '<option value="">-- Chọn Bác Sĩ --</option>';
        doctorSelect.disabled = true;

        let url = doctorAPI;
        if (department) url += `?department=${encodeURIComponent(department)}`;

        fetch(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
            .then(res => res.json())
            .then(data => {
                data.doctors.forEach(doc => {
                    const option = document.createElement('option');
                    option.value = doc.ID;
                    option.textContent = doc.fullName;
                    option.dataset.department = doc.department;
                    doctorSelect.appendChild(option);
                });
                doctorSelect.disabled = false;
            })
            .catch(error => {
                console.error('Lỗi khi lấy danh sách bác sĩ:', error);
                showError(`Lỗi khi lấy danh sách bác sĩ: ${error.message}`);
            });
    }

    populateYears();
    populateMonths();
    generateCalendar(new Date().getFullYear(), new Date().getMonth());
    loadDepartments();

    yearSelect.addEventListener('change', () => {
        generateCalendar(yearSelect.value, monthSelect.value);
        timeForm.style.display = 'none';
        resetTimeSelection();
        selectedDate.value = '';
    });

    monthSelect.addEventListener('change', () => {
        generateCalendar(yearSelect.value, monthSelect.value);
        timeForm.style.display = 'none';
        resetTimeSelection();
        selectedDate.value = '';
    });

    departmentSelect.addEventListener('change', () => {
        loadDoctors(departmentSelect.value);
        dateForm.style.display = 'none';
        timeForm.style.display = 'none';
        resetTimeSelection();
        selectedDate.value = '';
        doctorWorkingDates = [];
        generateCalendar(yearSelect.value, monthSelect.value);
    });

    doctorSelect.addEventListener('change', () => {
        if (doctorSelect.value) {
            const selectedOption = doctorSelect.querySelector(`option[value="${doctorSelect.value}"]`);
            if (selectedOption) departmentSelect.value = selectedOption.dataset.department;
            dateForm.style.display = 'block';
            fetchDoctorWorkingDates(doctorSelect.value);
        } else {
            dateForm.style.display = 'none';
            timeForm.style.display = 'none';
            resetTimeSelection();
            selectedDate.value = '';
            doctorWorkingDates = [];
            generateCalendar(yearSelect.value, monthSelect.value);
        }
    });

    const timeSlots = document.querySelectorAll('.time-slot');
    timeSlots.forEach(slot => {
        slot.addEventListener('click', () => {
            if (slot.classList.contains('disabled')) return;
            if (selectedSlot) selectedSlot.classList.remove('selected');
            slot.classList.add('selected');
            selectedSlot = slot;
            selectedTime.value = slot.value;
        });
    });

    appointmentForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        appointmentForm.classList.remove('was-validated');

        if (!appointmentForm.checkValidity()) {
            appointmentForm.classList.add('was-validated');
            return;
        }

        const doctorId = doctorSelect.value;
        const date = selectedDate.value;
        const time = selectedTime.value;
        const patientId = document.getElementById('patientId').value;

        if (!doctorId) {
            showError('Vui lòng chọn bác sĩ.');
            return;
        }
        if (!date) {
            showError('Vui lòng chọn ngày.');
            return;
        }
        if (!time) {
            showError('Vui lòng chọn giờ.');
            return;
        }

        const selectedDateTime = new Date(`${date}T${time}`);
        if (selectedDateTime < new Date()) {
            showError('Không thể chọn ngày hoặc giờ trong quá khứ.');
            return;
        }

        const account = JSON.parse(localStorage.getItem('account') || '{}');
        const accountStaffId = account.accountStaffId;

        const formData = {
            patientId: patientId,
            doctorId: doctorId,
            date: date,
            time: time,
            note: document.getElementById('note').value,
            department: departmentSelect.value,
            accountStaffId: accountStaffId
        };

        try {
            const response = await fetch(appointmentForm.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            const data = await response.json();
            if (response.ok) {
                const offcanvasElement = document.getElementById('offcanvasAppointmentBook');
                const offcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement) || new bootstrap.Offcanvas(offcanvasElement);
                offcanvas.hide();
                appointmentForm.reset();
                dateForm.style.display = 'none';
                timeForm.style.display = 'none';
                resetTimeSelection();
                selectedDate.value = '';
                doctorSelect.disabled = true;
                doctorWorkingDates = [];
                generateCalendar(yearSelect.value, monthSelect.value);
                showSuccess('Đặt lịch khám thành công!');
            } else {
                showError(data.error || 'Không thể đặt lịch khám.');
            }
        } catch (error) {
            showError(`Lỗi khi đặt lịch khám: ${error.message}`);
            console.error('Error:', error);
        }
    });
}

function showSuccess(message) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.textContent = message;
    errorMessage.style.color = '#28a745';
    errorMessage.style.display = 'block';
    setTimeout(() => {
        errorMessage.style.display = 'none';
    }, 3000);
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
            ? `Trang ${currentPage} / ${totalPages} (Tổng: ${totalEntries})`
            : 'Không tìm thấy bệnh nhân';
    }
    if (prevPage) prevPage.disabled = currentPage <= 1;
    if (nextPage) nextPage.disabled = currentPage >= totalPages;
    document.getElementById('itemsPerPage').value = pageSize;
}

document.getElementById('addPatient').addEventListener('submit', function (event) {
    event.preventDefault();
    this.classList.remove('was-validated');

    if (!this.checkValidity()) {
        this.classList.add('was-validated');
        return;
    }

    const namePatient = document.getElementById('namePatient1').value.trim();
    const dateOfBirth = document.getElementById('dateOfBirth1').value;
    const gender = document.getElementById('gender1').value;
    const phonePatient = document.getElementById('phonePatient1').value.trim();
    const address = document.getElementById('address1').value.trim();

    if (!namePatient) {
        showError('Tên bệnh nhân là bắt buộc');
        return;
    }
    if (!dateOfBirth || !/^\d{4}-\d{2}-\d{2}$/.test(dateOfBirth)) {
        showError('Định dạng ngày sinh không hợp lệ (yyyy-MM-dd)');
        return;
    }
    if (!['Male', 'Female', 'Other'].includes(gender)) {
        showError('Vui lòng chọn giới tính hợp lệ');
        return;
    }
    if (!phonePatient || !/^\d{10,12}$/.test(phonePatient)) {
        showError('Số điện thoại phải có 10-12 chữ số');
        return;
    }
    if (!address) {
        showError('Địa chỉ là bắt buộc');
        return;
    }

    const patientData = {
        fullName: namePatient,
        dob: dateOfBirth,
        gender: gender,
        phone: phonePatient,
        address: address
    };

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
                throw new Error(`Lỗi HTTP! Trạng thái: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                const offcanvasElement = document.getElementById('offcanvasPatientAdd');
                const offcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement) || new bootstrap.Offcanvas(offcanvasElement);
                offcanvas.hide();
                this.reset();
                this.classList.remove('was-validated');
                currentPage = 1;
                fetchPatients(currentPage, pageSize, sortBy, sortOrder);
                showSuccess('Thêm bệnh nhân thành công!');
            } else {
                showError(data.message || 'Không thể thêm bệnh nhân');
            }
        })
        .catch(error => {
            showError(`Lỗi khi thêm bệnh nhân: ${error.message}`);
            console.error('Lỗi:', error);
        });
});