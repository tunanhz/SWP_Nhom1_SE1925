// Table Enhancements JavaScript

document.addEventListener('DOMContentLoaded', function() {
    
    // Auto-adjust table width on window resize
    function adjustTableWidth() {
        const tables = document.querySelectorAll('.table-responsive table');
        const windowWidth = window.innerWidth;
        
        tables.forEach(table => {
            const tableWidth = table.scrollWidth;
            const containerWidth = table.closest('.table-responsive').offsetWidth;
            
            // If table is wider than container, ensure horizontal scroll
            if (tableWidth > containerWidth) {
                table.style.minWidth = tableWidth + 'px';
            } else {
                // Table fits in container, make it full width
                table.style.width = '100%';
                table.style.minWidth = '100%';
            }
        });
    }
    
    // Initialize table adjustments
    adjustTableWidth();
    
    // Re-adjust on window resize
    window.addEventListener('resize', function() {
        setTimeout(adjustTableWidth, 100);
    });
    
    // Add hover effects to table rows
    const tableRows = document.querySelectorAll('.table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
            this.style.transition = 'background-color 0.2s ease';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });
    });
    
    // Improve table scrolling on mobile
    const tableResponsive = document.querySelectorAll('.table-responsive');
    tableResponsive.forEach(container => {
        let isScrolling = false;
        let startX = 0;
        let scrollLeft = 0;
        
        container.addEventListener('touchstart', function(e) {
            isScrolling = true;
            startX = e.touches[0].pageX - container.offsetLeft;
            scrollLeft = container.scrollLeft;
        });
        
        container.addEventListener('touchmove', function(e) {
            if (!isScrolling) return;
            e.preventDefault();
            const x = e.touches[0].pageX - container.offsetLeft;
            const walk = (x - startX) * 2;
            container.scrollLeft = scrollLeft - walk;
        });
        
        container.addEventListener('touchend', function() {
            isScrolling = false;
        });
    });
    
    // Add loading states for table data
    function showTableLoading(tableContainer) {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'table-loading';
        loadingDiv.innerHTML = `
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;
        tableContainer.appendChild(loadingDiv);
    }
    
    function hideTableLoading(tableContainer) {
        const loadingDiv = tableContainer.querySelector('.table-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    }
    
    // Auto-refresh table data (example)
    function setupAutoRefresh(tableContainer, interval = 30000) {
        setInterval(() => {
            // This is where you would typically fetch new data
            // For now, we'll just show a brief loading state
            showTableLoading(tableContainer);
            setTimeout(() => {
                hideTableLoading(tableContainer);
            }, 1000);
        }, interval);
    }
    
    // Initialize auto-refresh for all tables (optional)
    tableResponsive.forEach(container => {
        // Uncomment the line below if you want auto-refresh
        // setupAutoRefresh(container, 60000); // Refresh every minute
    });
    
    // Add keyboard navigation for table
    function setupKeyboardNavigation() {
        const tableRows = document.querySelectorAll('.table tbody tr');
        let currentIndex = -1;
        
        document.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                
                if (e.key === 'ArrowDown') {
                    currentIndex = Math.min(currentIndex + 1, tableRows.length - 1);
                } else {
                    currentIndex = Math.max(currentIndex - 1, 0);
                }
                
                // Remove previous selection
                tableRows.forEach(row => row.classList.remove('table-row-selected'));
                
                // Add selection to current row
                if (currentIndex >= 0) {
                    tableRows[currentIndex].classList.add('table-row-selected');
                    tableRows[currentIndex].scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }
    
    // Initialize keyboard navigation
    setupKeyboardNavigation();
    
    // Add search functionality (if search input exists)
    const searchInputs = document.querySelectorAll('input[type="search"], .table-search');
    searchInputs.forEach(input => {
        input.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const table = this.closest('.card').querySelector('.table tbody');
            const rows = table.querySelectorAll('tr');
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    });
    
    // Add export functionality (if export buttons exist)
    const exportButtons = document.querySelectorAll('.btn-export');
    exportButtons.forEach(button => {
        button.addEventListener('click', function() {
            const table = this.closest('.card').querySelector('.table');
            const tableData = [];
            
            // Get headers
            const headers = Array.from(table.querySelectorAll('thead th')).map(th => th.textContent.trim());
            tableData.push(headers);
            
            // Get rows
            const rows = table.querySelectorAll('tbody tr');
            rows.forEach(row => {
                const rowData = Array.from(row.querySelectorAll('td')).map(td => td.textContent.trim());
                tableData.push(rowData);
            });
            
            // Convert to CSV
            const csvContent = tableData.map(row => row.join(',')).join('\n');
            
            // Download file
            const blob = new Blob([csvContent], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'table-data.csv';
            a.click();
            window.URL.revokeObjectURL(url);
        });
    });
    
    // Add print functionality
    const printButtons = document.querySelectorAll('.btn-print');
    printButtons.forEach(button => {
        button.addEventListener('click', function() {
            const table = this.closest('.card').querySelector('.table-responsive');
            const printWindow = window.open('', '_blank');
            printWindow.document.write(`
                <html>
                    <head>
                        <title>Table Print</title>
                        <style>
                            body { font-family: Arial, sans-serif; }
                            table { width: 100%; border-collapse: collapse; }
                            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                            th { background-color: #f2f2f2; }
                        </style>
                    </head>
                    <body>
                        ${table.outerHTML}
                    </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.print();
        });
    });
    
    // Console log for debugging
    console.log('Table enhancements loaded successfully');
});

// Add CSS for selected row
const style = document.createElement('style');
style.textContent = `
    .table-row-selected {
        background-color: #e3f2fd !important;
        border-left: 4px solid #2196f3 !important;
    }
    
    .table-loading {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(255, 255, 255, 0.9);
        z-index: 1000;
    }
`;
document.head.appendChild(style); 