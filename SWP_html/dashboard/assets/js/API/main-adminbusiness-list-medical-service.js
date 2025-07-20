document.addEventListener('DOMContentLoaded', () => {
    const BASE_URL = 'http://localhost:8080/SWP_back_war_exploded/api/admin/business';
    let currentPage = 1;
    let itemsPerPage = parseInt(document.getElementById('itemsPerPage').value);
    let totalPages = 1;

    // Fetch services and populate table
    const fetchServices = async (page = 1, searchQuery = '', minPrice = null, maxPrice = null) => {
        try {
            const params = new URLSearchParams({
                page,
                pageSize: itemsPerPage,
                searchQuery: searchQuery || '',
                minPrice: minPrice || '',
                maxPrice: maxPrice || ''
            }).toString();
            const response = await fetch(`${BASE_URL}/services?${params}`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();

            if (data.success) {
                totalPages = data.totalPages;
                document.getElementById('pageInfo').textContent = `Page ${page} of ${totalPages}`;
                populateTable(data.services);
                updatePaginationButtons();
            } else {
                console.error('Failed to fetch services:', data.message);
                Swal.fire('Error', data.message, 'error');
            }
        } catch (error) {
            console.error('Error fetching services:', error);
            Swal.fire('Error', `An error occurred while fetching services: ${error.message}`, 'error');
        }
    };

    // Populate table with service data
    const populateTable = (services) => {
        const tbody = document.getElementById('medicalServiceTableBody');
        tbody.innerHTML = '';
        services.forEach((service, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <th scope="row">${(currentPage - 1) * itemsPerPage + index + 1}</th>
                <td>${service.name}</td>
                <td>${service.description}</td>
                <td>$${service.price.toFixed(2)}</td>
                <td>${service.status}</td>
                <td>
                    <a class="d-inline-block pe-2 edit-btn" data-bs-toggle="offcanvas" href="#offcanvasMedicalServiceEdit" aria-controls="offcanvasMedicalServiceEdit" data-id="${service.serviceId}">
                        <span class="text-success">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M9.31055 14.3321H14.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                                <path fill-rule="evenodd" clip-rule="evenodd" d="M8.58501 1.84609C9.16674 1.15084 10.2125 1.04889 10.9222 1.6188C10.9614 1.64972 12.2221 2.62909 12.2221 2.62909C13.0017 3.10039 13.244 4.10233 12.762 4.86694C12.7365 4.90789 5.60896 13.8234 5.60896 13.8234C5.37183 14.1192 5.01187 14.2938 4.62718 14.298L1.89765 14.3323L1.28265 11.7292C1.1965 11.3632 1.28265 10.9788 1.51978 10.683L8.58501 1.84609Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                                <path d="M7.26562 3.50073L11.3548 6.64108" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </span>
                    </a>
                    <a href="#" class="d-inline-block ps-2 delete-btn" data-id="${service.serviceId}">
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
            tbody.appendChild(row);
        });

        // Re-attach event listeners for edit and delete buttons
        attachEventListeners();
    };

    // Update pagination buttons state
    const updatePaginationButtons = () => {
        document.getElementById('prevPage').disabled = currentPage === 1;
        document.getElementById('nextPage').disabled = currentPage === totalPages;
    };

    // Attach event listeners for edit and delete buttons
    const attachEventListeners = () => {
        document.querySelectorAll('.edit-btn').forEach(button => {
            button.removeEventListener('click', handleEditService); // Remove existing listeners to prevent duplicates
            button.addEventListener('click', handleEditService);
        });

        document.querySelectorAll('.delete-btn').forEach(button => {
            button.removeEventListener('click', handleDeleteService); // Remove existing listeners
            button.addEventListener('click', handleDeleteService);
        });
    };

    // Handle pagination
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            fetchServices(currentPage, document.getElementById('searchQuery').value,
                document.getElementById('minPrice').value || null,
                document.getElementById('maxPrice').value || null);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            fetchServices(currentPage, document.getElementById('searchQuery').value,
                document.getElementById('minPrice').value || null,
                document.getElementById('maxPrice').value || null);
        }
    });

    // Handle items per page change
    document.getElementById('itemsPerPage').addEventListener('change', (e) => {
        itemsPerPage = parseInt(e.target.value);
        currentPage = 1;
        fetchServices(currentPage, document.getElementById('searchQuery').value,
            document.getElementById('minPrice').value || null,
            document.getElementById('maxPrice').value || null);
    });

    // Handle search
    document.getElementById('searchButton').addEventListener('click', () => {
        currentPage = 1;
        fetchServices(currentPage, document.getElementById('searchQuery').value,
            document.getElementById('minPrice').value || null,
            document.getElementById('maxPrice').value || null);
    });

    // Handle price filter
    document.getElementById('filterPriceButton').addEventListener('click', () => {
        currentPage = 1;
        const minPrice = document.getElementById('minPrice').value || null;
        const maxPrice = document.getElementById('maxPrice').value || null;
        if (minPrice && maxPrice && parseFloat(minPrice) > parseFloat(maxPrice)) {
            Swal.fire('Error', 'Min price cannot be greater than max price', 'error');
            return;
        }
        fetchServices(currentPage, document.getElementById('searchQuery').value, minPrice, maxPrice);
    });

    // Handle add service
    document.getElementById('saveMedicalServiceButton').addEventListener('click', async () => {
        const name = document.getElementById('addServiceName').value.trim();
        const description = document.getElementById('addDescription').value.trim();
        const price = parseFloat(document.getElementById('addPrice').value);
        const status = document.getElementById('addStatus').value;

        if (!name || !description || isNaN(price) || price < 0) {
            Swal.fire('Error', 'Please fill all required fields with valid data (price must be non-negative).', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/services/create`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, description, price, status })
            });
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();

            if (data.success) {
                Swal.fire('Success', data.message, 'success');
                document.getElementById('addMedicalServiceForm').reset();
                document.getElementById('nameExistsError').style.display = 'none';
                document.getElementById('offcanvasMedicalServiceAdd').querySelector('[data-bs-dismiss="offcanvas"]').click();
                fetchServices(currentPage, document.getElementById('searchQuery').value,
                    document.getElementById('minPrice').value || null,
                    document.getElementById('maxPrice').value || null);
            } else {
                document.getElementById('nameExistsError').style.display = data.message === 'Duplicate name detected' ? 'block' : 'none';
                Swal.fire('Error', data.message, 'error');
            }
        } catch (error) {
            console.error('Error adding service:', error);
            Swal.fire('Error', `An error occurred while adding the service: ${error.message}`, 'error');
        }
    });

    // Handle edit service
    const handleEditService = async (e) => {
        const button = e.currentTarget;
        const serviceId = parseInt(button.getAttribute('data-id'));
        try {
            // Fetch all services to find the specific service
            const response = await fetch(`${BASE_URL}/services?page=1&pageSize=100`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();

            if (data.success) {
                const service = data.services.find(s => s.serviceId === serviceId);
                if (service) {
                    document.getElementById('editServiceId').value = service.serviceId;
                    document.getElementById('editServiceName').value = service.name;
                    document.getElementById('editDescription').value = service.description;
                    document.getElementById('editPrice').value = service.price.toFixed(2);
                    document.getElementById('editStatus').value = service.status;
                    document.getElementById('editNameExistsError').style.display = 'none';
                } else {
                    Swal.fire('Error', `Service with ID ${serviceId} not found.`, 'error');
                }
            } else {
                Swal.fire('Error', data.message || 'Failed to fetch service details.', 'error');
            }
        } catch (error) {
            console.error('Error fetching service:', error);
            Swal.fire('Error', `An error occurred while fetching service details: ${error.message}`, 'error');
        }
    };

    document.getElementById('updateMedicalServiceButton').addEventListener('click', async () => {
        const serviceId = parseInt(document.getElementById('editServiceId').value);
        const name = document.getElementById('editServiceName').value.trim();
        const description = document.getElementById('editDescription').value.trim();
        const price = parseFloat(document.getElementById('editPrice').value);
        const status = document.getElementById('editStatus').value;

        if (!serviceId || !name || !description || isNaN(price) || price < 0) {
            Swal.fire('Error', 'Please fill all required fields with valid data (price must be non-negative).', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/services/update`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ serviceId, name, description, price, status })
            });
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();

            if (data.success) {
                Swal.fire('Success', data.message, 'success');
                document.getElementById('editNameExistsError').style.display = 'none';
                document.getElementById('offcanvasMedicalServiceEdit').querySelector('[data-bs-dismiss="offcanvas"]').click();
                fetchServices(currentPage, document.getElementById('searchQuery').value,
                    document.getElementById('minPrice').value || null,
                    document.getElementById('maxPrice').value || null);
            } else {
                document.getElementById('editNameExistsError').style.display = data.message === 'Duplicate name detected' ? 'block' : 'none';
                Swal.fire('Error', data.message, 'error');
            }
        } catch (error) {
            console.error('Error updating service:', error);
            Swal.fire('Error', `An error occurred while updating the service: ${error.message}`, 'error');
        }
    });

    // Handle delete service
    const handleDeleteService = async (e) => {
        e.preventDefault();
        const button = e.currentTarget;
        const serviceId = parseInt(button.getAttribute('data-id'));
        Swal.fire({
            title: 'Are you sure?',
            text: 'This will disable the service.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, disable it!',
            cancelButtonText: 'No, cancel'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const response = await fetch(`${BASE_URL}/services/delete`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ serviceId })
                    });
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    const data = await response.json();

                    if (data.success) {
                        Swal.fire('Success', data.message, 'success');
                        fetchServices(currentPage, document.getElementById('searchQuery').value,
                            document.getElementById('minPrice').value || null,
                            document.getElementById('maxPrice').value || null);
                    } else {
                        Swal.fire('Error', data.message, 'error');
                    }
                } catch (error) {
                    console.error('Error deleting service:', error);
                    Swal.fire('Error', `An error occurred while disabling the service: ${error.message}`, 'error');
                }
            }
        });
    };

    // Initial load
    fetchServices(currentPage);
});