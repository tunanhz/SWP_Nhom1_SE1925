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
        let doctorWorkingDates = [];

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

        // Check if date is a working date
        function isWorkingDate(year, month, day) {
            const dateStr = `${year}-${String(parseInt(month) + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            return doctorWorkingDates.includes(dateStr);
        }

        // Generate dynamic calendar
        function generateCalendar(year, month) {
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            const daysInMonth = lastDay.getDate();
            const startingDay = firstDay.getDay();
            let table = '<table><tr><th>T2</th><th>T3</th><th>T4</th><th>T5</th><th>T6</th><th>T7</th><th>CN</th></tr><tr>';
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

        // Update time slots
        async function updateTimeSlots() {
            const timeSlots = document.querySelectorAll('.time-slot');
            const selectedDateStr = selectedDate.value;
            const doctorId = doctorSelect.value;
            const today = new Date();
            const isToday = new Date(selectedDateStr).toDateString() === today.toDateString();

            // Reset all slots to enabled
            timeSlots.forEach(slot => {
                slot.classList.remove('disabled');
                if (isToday && isPastTime(slot.value, selectedDateStr)) {
                    slot.classList.add('disabled');
                }
            });

            // Validate parameters
            if (!doctorId || !selectedDateStr) {
                console.warn('Skipping fetch: doctorId or date is missing', { doctorId, selectedDateStr });
                return;
            }

            try {
                const url = `http://localhost:8080/SWP_back_war_exploded/api/doctor-availability?doctorId=${encodeURIComponent(doctorId)}&date=${encodeURIComponent(selectedDateStr)}`;
                const response = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    }
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error(`Failed to fetch booked times: HTTP ${response.status} - ${response.statusText}`, errorText);
                    return;
                }

                const bookedTimes = await response.json();
                timeSlots.forEach(slot => {
                    if (bookedTimes.includes(slot.value)) {
                        slot.classList.add('disabled');
                    }
                });
            } catch (error) {
                console.error('Error fetching booked times:', error.message, error);
            }
        }

        // Reset time selection
        function resetTimeSelection() {
            const timeSlots = document.querySelectorAll('.time-slot');
            timeSlots.forEach(slot => slot.classList.remove('selected'));
            selectedTime.value = '';
            if (selectedSlot) selectedSlot.classList.remove('selected');
            selectedSlot = null;
        }

        // Fetch doctor's working dates
        async function fetchDoctorWorkingDates(doctorId) {
            if (!doctorId) {
                doctorWorkingDates = [];
                generateCalendar(yearSelect.value, monthSelect.value);
                return;
            }

            try {
                const url = `http://localhost:8080/SWP_back_war_exploded/api/doctor-availability?doctorId=${encodeURIComponent(doctorId)}&action=getWorkingDates`;
                const response = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    }
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error(`Failed to fetch working dates: HTTP ${response.status} - ${response.statusText}`, errorText);
                    doctorWorkingDates = [];
                } else {
                    doctorWorkingDates = await response.json();
                }
                generateCalendar(yearSelect.value, monthSelect.value);
            } catch (error) {
                console.error('Error fetching working dates:', error.message, error);
                doctorWorkingDates = [];
                generateCalendar(yearSelect.value, monthSelect.value);
            }
        }

        // Initial setup
        populateYears();
        populateMonths();
        generateCalendar(new Date().getFullYear(), new Date().getMonth());

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

        // Load departments
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

        // Load doctors
        function loadDoctors(department = "") {
            doctorSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
            doctorSelect.disabled = true;

            let url = "http://localhost:8080/SWP_back_war_exploded/api/all-doctors";
            if (department) url += `?department=${encodeURIComponent(department)}`;

            fetch(url)
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
                });
        }

        departmentSelect.addEventListener('change', () => {
            loadDoctors(departmentSelect.value);
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

        let selectedSlot = null;
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

            const selectedDateTime = new Date(`${selectedDate.value}T${selectedTime.value}`);
            if (selectedDateTime < new Date()) {
                alert('Không thể chọn ngày hoặc giờ trong quá khứ.');
                return;
            }

            const formData = {
                doctorId: doctorSelect.value,
                date: selectedDate.value,
                time: selectedTime.value,
                note: document.getElementById('note').value,
                department: departmentSelect.value
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
                    alert('Đặt lịch thành công!');
                    appointmentForm.reset();
                    dateForm.style.display = 'none';
                    timeForm.style.display = 'none';
                    resetTimeSelection();
                    selectedDate.value = '';
                    doctorSelect.disabled = true;
                    doctorWorkingDates = [];
                    generateCalendar(yearSelect.value, monthSelect.value);
                } else {
                    alert(`Lỗi: ${data.error || 'Không thể đặt lịch.'}`);
                }
            } catch (error) {
                alert('Đã có lỗi xảy ra. Vui lòng thử lại.');
                console.error('Error:', error);
            }
        });