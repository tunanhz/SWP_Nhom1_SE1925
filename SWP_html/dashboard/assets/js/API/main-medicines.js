const API_URL = 'http://localhost:8080/SWP_back_war_exploded/api/medicines';
        let currentPage = 1;
        const pageSize = 10;
        let totalPages = 1;

        // Tải danh sách warehouseName
        async function loadWarehouseFilter() {
            try {
                const response = await fetch(`${API_URL}?getWarehouses=true`);
                const warehouseNames = await response.json();
                const warehouseFilter = document.getElementById("warehouseFilter");
                warehouseNames.forEach(name => {
                    const option = document.createElement("option");
                    option.value = name;
                    option.textContent = name;
                    warehouseFilter.appendChild(option);
                });
            } catch (error) {
                console.error("Lỗi khi tải danh sách kho:", error);
            }
        }

        // Tải danh sách usage
        async function loadUsageFilter() {
            try {
                const response = await fetch(`${API_URL}?getUsages=true`);
                const usages = await response.json();
                const usageFilter = document.getElementById("usageFilter");
                usages.forEach(usage => {
                    const option = document.createElement("option");
                    option.value = usage;
                    option.textContent = usage;
                    usageFilter.appendChild(option);
                });
            } catch (error) {
                console.error("Lỗi khi tải danh sách công dụng:", error);
            }
        }

        async function loadMedicines(search = "", usage = "", warehouseName = "", page = 1) {
            const messageDiv = document.getElementById("message");
            messageDiv.className = "mb-4 hidden";

            try {
                // Xây dựng URL với các tham số lọc
                let url = `${API_URL}?page=${page}&size=${pageSize}`;
                if (search) url += `&name=${encodeURIComponent(search)}`;
                if (usage) url += `&usage=${encodeURIComponent(usage)}`;
                if (warehouseName) url += `&warehouseName=${encodeURIComponent(warehouseName)}`;

                const response = await fetch(url);
                if (!response.ok) {
                    throw new Error(`Lỗi API: ${response.status} - ${response.statusText}`);
                }
                const data = await response.json();
                const table = document.getElementById("medicineTable");
                table.innerHTML = "";

                if (!data || data.length === 0) {
                    table.innerHTML = `<tr><td colspan="8" class="text-center py-4">Không có dữ liệu</td></tr>`;
                    updatePagination(0, page);
                    return;
                }

                data.forEach(med => {
                    const row = `
                        <tr>
                            <td class="px-4 py-2">${med.medicineId}</td>
                            <td class="px-4 py-2">${med.name}</td>
                            <td class="px-4 py-2">${med.quantity}</td>
                            <td class="px-4 py-2">${med.price.toLocaleString("vi-VN")} VND</td>
                            <td class="px-4 py-2">${med.usage || 'N/A'}</td>
                            <td class="px-4 py-2">${new Date(med.manuDate).toLocaleDateString("vi-VN")}</td>
                            <td class="px-4 py-2">${new Date(med.expDate).toLocaleDateString("vi-VN")}</td>
                            <td class="px-4 py-2">
                                <button onclick="prefill(${med.medicineId}, ${med.quantity})" class="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 mr-1">Chọn</button>
                                <button onclick='showMedicineDetail(${JSON.stringify(med)})' class="bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600">Xem chi tiết</button>
                            </td>
                        </tr>
                    `;
                    table.innerHTML += row;
                });

                totalPages = data.length < pageSize ? page : page + 1; // Cần API riêng để lấy tổng số bản ghi
                updatePagination(data.length, page);
            } catch (error) {
                messageDiv.textContent = `Lỗi: ${error.message}`;
                messageDiv.className = "mb-4 text-red-600";
                console.error(error);
            }
        }

        function updatePagination(dataLength, page) {
            const prevBtn = document.getElementById("prevPageBtn");
            const nextBtn = document.getElementById("nextPageBtn");
            const pageInfo = document.getElementById("pageInfo");

            prevBtn.disabled = page === 1;
            nextBtn.disabled = dataLength < pageSize;
            pageInfo.textContent = `Trang ${page}${totalPages > 1 ? ` / ${totalPages}` : ''}`;
            currentPage = page;
        }

        function prefill(id, quantity) {
            document.getElementById("updateId").value = id;
            document.getElementById("newQuantity").value = quantity;
        }

        document.getElementById("updateBtn").addEventListener("click", async () => {
            const id = document.getElementById("updateId").value;
            const quantity = document.getElementById("newQuantity").value;
            const msg = document.getElementById("updateMsg");

            try {
                const res = await fetch(`${API_URL}/${id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ quantity: parseInt(quantity) })
                });
                const result = await res.json();
                msg.textContent = result.message || result.error;
                msg.className = result.message ? "text-green-600" : "text-red-600";
                loadMedicines(
                    document.getElementById("search").value,
                    document.getElementById("usageFilter").value,
                    document.getElementById("warehouseFilter").value,
                    currentPage
                );
            } catch (e) {
                msg.textContent = "Cập nhật thất bại";
                msg.className = "text-red-600";
            }
        });

        // Lắng nghe sự kiện onchange cho các bộ lọc
        function applyFilters() {
            currentPage = 1;
            const searchVal = document.getElementById("search").value;
            const usageVal = document.getElementById("usageFilter").value;
            const warehouseVal = document.getElementById("warehouseFilter").value;
            loadMedicines(searchVal, usageVal, warehouseVal, currentPage);
        }

        document.getElementById("search").addEventListener("input", applyFilters);
        document.getElementById("usageFilter").addEventListener("change", applyFilters);
        document.getElementById("warehouseFilter").addEventListener("change", applyFilters);

        document.getElementById("refreshBtn").addEventListener("click", () => {
            document.getElementById("search").value = "";
            document.getElementById("usageFilter").value = "";
            document.getElementById("warehouseFilter").value = "";
            currentPage = 1;
            loadMedicines("", "", "", currentPage);
        });

        document.getElementById("prevPageBtn").addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                loadMedicines(
                    document.getElementById("search").value,
                    document.getElementById("usageFilter").value,
                    document.getElementById("warehouseFilter").value,
                    currentPage
                );
            }
        });

        document.getElementById("nextPageBtn").addEventListener("click", () => {
            currentPage++;
            loadMedicines(
                document.getElementById("search").value,
                document.getElementById("usageFilter").value,
                document.getElementById("warehouseFilter").value,
                currentPage
            );
        });

        window.onload = () => {
            loadWarehouseFilter();
            loadUsageFilter();
            loadMedicines("", "", "", currentPage);
        };

        // Thêm hàm hiển thị chi tiết thuốc
        function showMedicineDetail(med) {
            // Nếu đã có modal thì xóa trước
            let oldModal = document.getElementById('medicineDetailModal');
            if (oldModal) oldModal.remove();
            // Tạo modal
            const modal = document.createElement('div');
            modal.id = 'medicineDetailModal';
            modal.style.position = 'fixed';
            modal.style.top = '0';
            modal.style.left = '0';
            modal.style.width = '100vw';
            modal.style.height = '100vh';
            modal.style.background = 'rgba(0,0,0,0.4)';
            modal.style.display = 'flex';
            modal.style.alignItems = 'center';
            modal.style.justifyContent = 'center';
            modal.style.zIndex = '9999';
            modal.innerHTML = `
                <div style="background: #fff; border-radius: 16px; max-width: 500px; width: 100%; padding: 2rem; position: relative; box-shadow: 0 8px 32px rgba(0,0,0,0.2); animation: fadeIn 0.3s;">
                    <button id="closeMedicineDetailModal" style="position: absolute; top: 12px; right: 16px; background: none; border: none; font-size: 1.5rem; cursor: pointer;">&times;</button>
                    <h2 style="margin-bottom: 1rem; color: #667eea;">Chi tiết thuốc</h2>
                    <table style="width: 100%; border-collapse: collapse;">
                        <tr><td style="font-weight: bold; padding: 6px 0;">ID:</td><td>${med.medicineId}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Tên:</td><td>${med.name}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Số lượng:</td><td>${med.quantity}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Giá:</td><td>${med.price ? med.price.toLocaleString('vi-VN') + ' VND' : ''}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Công dụng:</td><td>${med.usage || 'N/A'}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Thành phần:</td><td>${med.ingredient || 'N/A'}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Bảo quản:</td><td>${med.preservation || 'N/A'}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Ngày sản xuất:</td><td>${med.manuDate ? new Date(med.manuDate).toLocaleDateString('vi-VN') : ''}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Ngày hết hạn:</td><td>${med.expDate ? new Date(med.expDate).toLocaleDateString('vi-VN') : ''}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Kho:</td><td>${med.warehouseName || ''}</td></tr>
                        <tr><td style="font-weight: bold; padding: 6px 0;">Vị trí kho:</td><td>${med.warehouseLocation || ''}</td></tr>
                    </table>
                </div>
            `;
            document.body.appendChild(modal);
            document.getElementById('closeMedicineDetailModal').onclick = function() {
                modal.remove();
            };
            // Đóng modal khi click ra ngoài
            modal.onclick = function(e) {
                if (e.target === modal) modal.remove();
            };
        }