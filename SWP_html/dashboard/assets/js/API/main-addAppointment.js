
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

    // Populate year options
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

    // Populate month options
    function populateMonths() {
        const months = [
            'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
            'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
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

    // Check if date is in the past
    function isPastDate(year, month, day) {
        const selectedDate = new Date(year, month, day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selectedDate < today;
    }

    // Check if time is in the past for today
    function isPastTime(time, selectedDateStr) {
        const today = new Date();
        const selected = new Date(`${selectedDateStr}T${time}`);
        return selected < today;
    }

    // Generate dynamic calendar with past date validation
    function generateCalendar(year, month) {
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDay = firstDay.getDay();
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        let table = '<table><tr><th>T2</th><th>T3</th><th>T4</th><th>T5</th><th>T6</th><th>T7</th><th>CN</th></tr><tr>';
        let dayCount = 1;

        for (let i = 0; i < 6; i++) {
            for (let j = 0; j < 7; j++) {
                if ((i === 0 && j < startingDay) || dayCount > daysInMonth) {
                    table += '<td></td>';
                } else {
                    const isPast = isPastDate(year, month, dayCount);
                    table += `<td class="${isPast ? 'disabled' : ''}">${dayCount}</td>`;
                    dayCount++;
                    if (dayCount > daysInMonth) break;
                }
            }
            table += '</tr><tr>';
            if (dayCount > daysInMonth) break;
        }
        table += '</tr></table>';
        calendar.innerHTML = table;

        // Reattach event listeners to the new days
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

    // Update time slots based on selected date
    function updateTimeSlots() {
        const timeSlots = document.querySelectorAll('.time-slot');
        const selectedDateStr = selectedDate.value;
        const today = new Date();
        const isToday = new Date(selectedDateStr).toDateString() === today.toDateString();

        timeSlots.forEach(slot => {
            slot.classList.remove('disabled');
            if (isToday && isPastTime(slot.value, selectedDateStr)) {
                slot.classList.add('disabled');
            }
        });
    }

    // Reset time selection
    function resetTimeSelection() {
        const timeSlots = document.querySelectorAll('.time-slot');
        timeSlots.forEach(slot => slot.classList.remove('selected'));
        selectedTime.value = '';
        if (selectedSlot) selectedSlot.classList.remove('selected');
        selectedSlot = null;
    }

    // Initial setup
    populateYears();
    populateMonths();
    generateCalendar(new Date().getFullYear(), new Date().getMonth());

    // Update calendar when year or month changes
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

    // Gọi API danh sách chuyên khoa
    fetch('http://localhost:8080/SWP_back_war_exploded/api/doctors/departments')
        .then(res => res.json())
        .then(data => {
            data.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept;
                option.textContent = dept;
                departmentSelect.appendChild(option);
            });
            loadDoctors();
        });

    // Load danh sách bác sĩ
    function loadDoctors(department = "") {
        doctorSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
        doctorSelect.disabled = true;

        let url = "http://localhost:8080/SWP_back_war_exploded/api/doctors";
        if (department) {
            url += `?department=${encodeURIComponent(department)}`;
        }

        fetch(url)
            .then(res => res.json())
            .then(data => {
                const doctors = data.doctors;
                doctors.forEach(doc => {
                    const option = document.createElement('option');
                    option.value = doc.ID;
                    option.textContent = doc.fullName;
                    doctorSelect.appendChild(option);
                });
                doctorSelect.disabled = false;
            });
    }

    // Khi thay đổi chuyên khoa
    departmentSelect.addEventListener('change', () => {
        const selectedDept = departmentSelect.value;
        loadDoctors(selectedDept);
    });

    // Hiển thị form ngày khi chọn bác sĩ
    doctorSelect.addEventListener('change', () => {
        if (doctorSelect.value) {
            dateForm.style.display = 'block';
        } else {
            dateForm.style.display = 'none';
            timeForm.style.display = 'none';
            resetTimeSelection();
            selectedDate.value = '';
        }
    });

    // Đánh dấu nút giờ khi chọn
    let selectedSlot = null;
    const timeSlots = document.querySelectorAll('.time-slot');
    timeSlots.forEach(slot => {
        slot.addEventListener('click', () => {
            if (slot.classList.contains('disabled')) return;
            if (selectedSlot) {
                selectedSlot.classList.remove('selected');
            }
            slot.classList.add('selected');
            selectedSlot = slot;
            selectedTime.value = slot.value;
        });
    });

    // Handle form submission with validation
    appointmentForm.addEventListener('submit', (e) => {
        e.preventDefault();

        // Validate inputs
        if (!doctorSelect.value) {
            alert('Vui lòng chọn bác sĩ.');
            return;
        }
        if (!selectedDate.value) {
            alert('Vui lòng chọn ngày.');
            return;
        }
        if (!selectedTime.value) {
            alert('Vui lòng chọn giờ.');
            return;
        }

        // Validate date and time not in past
        const selectedDateTime = new Date(`${selectedDate.value}T${selectedTime.value}`);
        if (selectedDateTime < new Date()) {
            alert('Không thể chọn ngày hoặc giờ trong quá khứ.');
            return;
        }

        const formData = new FormData(appointmentForm);

        fetch(appointmentForm.action, {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            alert('Đặt lịch thành công!');
            appointmentForm.reset();
            dateForm.style.display = 'none';
            timeForm.style.display = 'none';
            resetTimeSelection();
            selectedDate.value = '';
            doctorSelect.disabled = true;
        })
        .catch(error => {
            alert('Đã có lỗi xảy ra. Vui lòng thử lại.');
            console.error('Error:', error);
        });
    });
