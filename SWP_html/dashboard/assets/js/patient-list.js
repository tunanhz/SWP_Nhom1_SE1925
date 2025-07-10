// Patient List Management JavaScript
class PatientListManager {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 8;
        this.totalPages = 1;
        this.totalPatients = 0;
        this.searchFilters = {
            name: '',
            dob: '',
            gender: ''
        };
        this.apiBaseUrl = 'http://localhost:8080/SWP_back/api/patient';
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadPatients();
    }

    bindEvents() {
        // Search functionality
        const searchInput = document.getElementById('searchPatient');
        const dobFilter = document.getElementById('dobFilter');
        const genderFilter = document.getElementById('genderFilter');
        
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.searchFilters.name = e.target.value;
                this.currentPage = 1;
                this.loadPatients();
            });
        }

        if (dobFilter) {
            dobFilter.addEventListener('change', (e) => {
                this.searchFilters.dob = e.target.value;
                this.currentPage = 1;
                this.loadPatients();
            });
        }

        if (genderFilter) {
            genderFilter.addEventListener('change', (e) => {
                this.searchFilters.gender = e.target.value;
                this.currentPage = 1;
                this.loadPatients();
            });
        }

        // Add patient form submission
        const addPatientForm = document.getElementById('addPatientForm');
        if (addPatientForm) {
            addPatientForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.addPatient();
            });
        }

        // Edit patient form submission
        const editPatientForm = document.getElementById('editPatientForm');
        if (editPatientForm) {
            editPatientForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.updatePatient();
            });
        }
    }

    async loadPatients() {
        try {
            this.showLoading();
            
            const params = new URLSearchParams({
                viewType: 'all', // For admin/staff view
                page: this.currentPage,
                pageSize: this.pageSize
            });

            // Add filters if they exist
            if (this.searchFilters.name) {
                params.append('name', this.searchFilters.name);
            }
            if (this.searchFilters.dob) {
                params.append('dob', this.searchFilters.dob);
            }
            if (this.searchFilters.gender) {
                params.append('gender', this.searchFilters.gender);
            }

            const response = await fetch(`${this.apiBaseUrl}?${params}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            
            this.totalPages = data.totalPages;
            this.totalPatients = data.totalPatient;
            
            this.renderPatients(data.patients);
            this.renderPagination();
            this.updatePatientCount();
            
        } catch (error) {
            console.error('Error loading patients:', error);
            this.showError('Failed to load patients. Please try again.');
        } finally {
            this.hideLoading();
        }
    }

    renderPatients(patients) {
        const tbody = document.querySelector('#patientTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!patients || patients.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="11" class="text-center py-4">
                        <div class="text-muted">
                            <i class="fas fa-users fa-3x mb-3"></i>
                            <p>No patients found</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        patients.forEach((patient, index) => {
            const row = this.createPatientRow(patient, index);
            tbody.appendChild(row);
        });
    }

    createPatientRow(patient, index) {
        const row = document.createElement('tr');
        row.setAttribute('data-item', 'list');
        
        // Calculate age from DOB
        const age = this.calculateAge(patient.dob);
        
        row.innerHTML = `
            <th scope="row">${(this.currentPage - 1) * this.pageSize + index + 1}</th>
            <td>#${patient.id}</td>
            <td>
                <h6 class="mb-0 text-body fw-normal">${this.escapeHtml(patient.fullName)}</h6>
            </td>
            <td>${age}</td>
            <td>${this.escapeHtml(patient.phone || 'N/A')}</td>
            <td>${this.formatDate(patient.dob)}</td>
            <td>${this.escapeHtml(patient.gender)}</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            <td>
                <a href="#" class="d-inline-block pe-2 edit-btn" data-patient-id="${patient.id}">
                    <span class="text-success">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9.31055 14.3321H14.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M8.58501 1.84609C9.16674 1.15084 10.2125 1.04889 10.9222 1.6188C10.9614 1.64972 12.2221 2.62909 12.2221 2.62909C13.0017 3.10039 13.244 4.10233 12.762 4.86694C12.7365 4.90789 5.60896 13.8234 5.60896 13.8234C5.37183 14.1192 5.01187 14.2938 4.62718 14.298L1.89765 14.3323L1.28265 11.7292C1.1965 11.3632 1.28265 10.9788 1.51978 10.683L8.58501 1.84609Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M7.26562 3.50073L11.3548 6.64108" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
                <a href="#" class="d-inline-block ps-2 delete-btn" data-patient-id="${patient.id}">
                    <span class="text-danger">
                        <svg width="15" height="16" viewBox="0 0 15 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12.4938 6.10107C12.4938 6.10107 12.0866 11.1523 11.8503 13.2801C11.7378 14.2963 11.1101 14.8918 10.0818 14.9106C8.12509 14.9458 6.16609 14.9481 4.21009 14.9068C3.22084 14.8866 2.60359 14.2836 2.49334 13.2853C2.25559 11.1388 1.85059 6.10107 1.85059 6.10107" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M13.5312 3.67969H0.812744" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            <path d="M11.0804 3.67974C10.4917 3.67974 9.98468 3.26349 9.86918 2.68674L9.68693 1.77474C9.57443 1.35399 9.19343 1.06299 8.75918 1.06299H5.58443C5.15018 1.06299 4.76918 1.35399 4.65668 1.77474L4.47443 2.68674C4.35893 3.26349 3.85193 3.67974 3.26318 3.67974" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </a>
            </td>
        `;

        // Add event listeners for edit and delete buttons
        const editBtn = row.querySelector('.edit-btn');
        const deleteBtn = row.querySelector('.delete-btn');

        editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            this.editPatient(patient);
        });

        deleteBtn.addEventListener('click', (e) => {
            e.preventDefault();
            this.deletePatient(patient.id);
        });

        return row;
    }

    calculateAge(dob) {
        if (!dob) return 'N/A';
        const birthDate = new Date(dob);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        
        return age;
    }

    formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    renderPagination() {
        const paginationContainer = document.getElementById('paginationContainer');
        if (!paginationContainer) return;

        let paginationHtml = '<nav aria-label="Patient list pagination"><ul class="pagination justify-content-center">';
        
        // Previous button
        paginationHtml += `
            <li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${this.currentPage - 1}">Previous</a>
            </li>
        `;
        
        // Page numbers
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.totalPages, this.currentPage + 2);
        
        for (let i = startPage; i <= endPage; i++) {
            paginationHtml += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i}</a>
                </li>
            `;
        }
        
        // Next button
        paginationHtml += `
            <li class="page-item ${this.currentPage === this.totalPages ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${this.currentPage + 1}">Next</a>
            </li>
        `;
        
        paginationHtml += '</ul></nav>';
        
        paginationContainer.innerHTML = paginationHtml;
        
        // Add click events to pagination links
        paginationContainer.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(e.target.getAttribute('data-page'));
                if (page && page !== this.currentPage && page >= 1 && page <= this.totalPages) {
                    this.currentPage = page;
                    this.loadPatients();
                }
            });
        });
    }

    updatePatientCount() {
        const countElement = document.getElementById('patientCount');
        if (countElement) {
            countElement.textContent = `Showing ${this.totalPatients} patients`;
        }
    }

    showLoading() {
        const loadingElement = document.getElementById('loadingIndicator');
        if (loadingElement) {
            loadingElement.style.display = 'block';
        }
    }

    hideLoading() {
        const loadingElement = document.getElementById('loadingIndicator');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
    }

    showError(message) {
        // You can implement a toast notification or alert here
        console.error(message);
        alert(message);
    }

    async addPatient() {
        // Implementation for adding a new patient
        console.log('Add patient functionality to be implemented');
    }

    async editPatient(patient) {
        // Implementation for editing a patient
        console.log('Edit patient functionality to be implemented', patient);
    }

    async updatePatient() {
        // Implementation for updating a patient
        console.log('Update patient functionality to be implemented');
    }

    async deletePatient(patientId) {
        if (!confirm('Are you sure you want to delete this patient?')) {
            return;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/${patientId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.loadPatients(); // Reload the list
                alert('Patient deleted successfully');
            } else {
                throw new Error('Failed to delete patient');
            }
        } catch (error) {
            console.error('Error deleting patient:', error);
            this.showError('Failed to delete patient. Please try again.');
        }
    }
}

// Initialize the patient list manager when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    new PatientListManager();
});
