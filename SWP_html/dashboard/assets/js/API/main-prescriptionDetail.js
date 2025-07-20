const API_URL = 'http://localhost:8080/SWP_back_war_exploded/api/prescriptionDetail/';

        // Get prescriptionId from query string
        const urlParams = new URLSearchParams(window.location.search);
        const prescriptionId = urlParams.get('id');

        async function fetchPrescriptionDetails(id) {
            try {
                const response = await fetch(`${API_URL}${id}`, {
                    headers: {
                        'Accept': 'application/json'
                    }
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                displayPrescriptionDetails(data);
                hideError();
                hideSuccess();
            } catch (error) {
                showError(`Failed to load prescription details: ${error.message}`);
                console.error(error);
            }
        }

        function displayPrescriptionDetails(prescriptions) {
            if (!prescriptions || prescriptions.length === 0) {
                showError('No prescription details found.');
                return;
            }

            // Use the first prescription for general details
            const prescription = prescriptions[0];

            document.getElementById('prescription-id').textContent = prescription.prescriptionId;
            document.getElementById('patient-name').textContent = prescription.patientName;
            document.getElementById('doctor-name').textContent = prescription.doctorName;
            document.getElementById('prescription-date').textContent = prescription.prescriptionDate;
            
            const statusSpan = document.getElementById('prescription-status');
            statusSpan.textContent = prescription.status;
            
            // Xóa các class cũ
            statusSpan.classList.remove('bg-yellow-100', 'text-yellow-800', 'bg-red-100', 'text-red-800', 'bg-green-100', 'text-green-800');
            
            // Thêm các class tương ứng
            if (prescription.status === 'Pending') {
                statusSpan.classList.add('bg-yellow-100', 'text-yellow-800');
            } else if (prescription.status === 'Cancelled') {
                statusSpan.classList.add('bg-red-100', 'text-red-800');
            } else {
                statusSpan.classList.add('bg-green-100', 'text-green-800');
            }

            // Set current status in dropdown
            document.getElementById('status-select').value = prescription.status;

            // Display medications
            const medicationList = document.getElementById('medication-list');
            medicationList.innerHTML = '';

            // Loop through all prescriptions to display each medication
            prescriptions.forEach(p => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-900">${p.medicineName || 'N/A'}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-sm text-gray-900">${p.medicineQuantity || 'N/A'}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-900">${p.medicineDosage || 'N/A'}</td>
                    <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-900">${p.medicinePrice ? p.medicinePrice.toLocaleString('vi-VN') + ' VND' : 'N/A'}</td>
                `;
                medicationList.appendChild(row);
            });
        }

        async function updatePrescriptionStatus(id, newStatus) {
            try {
                const response = await fetch(`${API_URL}${id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({ status: newStatus })
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                showSuccess('Prescription status updated successfully.');
                hideError();
                // Refresh prescription details
                fetchPrescriptionDetails(id);
            } catch (error) {
                showError(`Failed to update prescription status: ${error.message}`);
                console.error(error);
            }
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

        function showSuccess(message) {
            const successDiv = document.getElementById('success-message');
            successDiv.textContent = message;
            successDiv.classList.remove('hidden');
        }

        function hideSuccess() {
            const successDiv = document.getElementById('success-message');
            successDiv.classList.add('hidden');
        }

        // Fetch details when page loads
        if (prescriptionId) {
            fetchPrescriptionDetails(prescriptionId);
        } else {
            showError('No prescription ID provided.');
        }

        // Handle update status button click
        document.getElementById('update-status-btn').addEventListener('click', () => {
            const newStatus = document.getElementById('status-select').value;
            if (prescriptionId) {
                updatePrescriptionStatus(prescriptionId, newStatus);
            } else {
                showError('No prescription ID provided.');
            }
        });