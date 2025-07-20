// Table Center Enhancements JavaScript
// Cải thiện trải nghiệm table với các tính năng tương tác

document.addEventListener('DOMContentLoaded', function() {
    
    // ===== AUTO-ADJUST TABLE WIDTH =====
    function adjustTableWidth() {
        const tables = document.querySelectorAll('.table-responsive table');
        const windowWidth = window.innerWidth;
        
        tables.forEach(table => {
            const tableWidth = table.scrollWidth;
            const containerWidth = table.closest('.table-responsive').offsetWidth;
            
            if (tableWidth > containerWidth) {
                table.style.minWidth = tableWidth + 'px';
            } else {
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
    
    // ===== ENHANCED HOVER EFFECTS =====
    const tableRows = document.querySelectorAll('.table tbody tr');
    tableRows.forEach((row, index) => {
        // Add staggered animation delay
        row.style.animationDelay = (index * 0.1) + 's';
        
        // Enhanced hover effects
        row.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px) scale(1.01)';
            this.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.15)';
            this.style.zIndex = '10';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
            this.style.zIndex = '';
        });
        
        // Click effect
        row.addEventListener('click', function() {
            this.style.transform = 'scale(0.98)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
    
    // ===== SMOOTH SCROLLING =====
    const tableResponsive = document.querySelectorAll('.table-responsive');
    tableResponsive.forEach(container => {
        let isScrolling = false;
        
        container.addEventListener('wheel', function(e) {
            if (e.deltaY !== 0) {
                e.preventDefault();
                this.scrollLeft += e.deltaY;
            }
        });
        
        // Touch scrolling for mobile
        let startX, startY, scrollLeft;
        
        container.addEventListener('touchstart', function(e) {
            startX = e.touches[0].pageX - this.offsetLeft;
            startY = e.touches[0].pageY - this.offsetTop;
            scrollLeft = this.scrollLeft;
        });
        
        container.addEventListener('touchmove', function(e) {
            if (!startX) return;
            
            const x = e.touches[0].pageX - this.offsetLeft;
            const y = e.touches[0].pageY - this.offsetTop;
            const walkX = (x - startX) * 2;
            const walkY = Math.abs(y - startY);
            
            // Only scroll horizontally if horizontal movement is greater
            if (walkX > walkY) {
                e.preventDefault();
                this.scrollLeft = scrollLeft - walkX;
            }
        });
        
        container.addEventListener('touchend', function() {
            startX = null;
        });
    });
    
    // ===== KEYBOARD NAVIGATION =====
    let currentRowIndex = -1;
    
    document.addEventListener('keydown', function(e) {
        const tableRows = document.querySelectorAll('.table tbody tr');
        if (tableRows.length === 0) return;
        
        switch(e.key) {
            case 'ArrowDown':
                e.preventDefault();
                if (currentRowIndex < tableRows.length - 1) {
                    currentRowIndex++;
                    highlightRow(tableRows[currentRowIndex]);
                }
                break;
            case 'ArrowUp':
                e.preventDefault();
                if (currentRowIndex > 0) {
                    currentRowIndex--;
                    highlightRow(tableRows[currentRowIndex]);
                }
                break;
            case 'Enter':
                if (currentRowIndex >= 0) {
                    tableRows[currentRowIndex].click();
                }
                break;
        }
    });
    
    function highlightRow(row) {
        // Remove previous highlight
        document.querySelectorAll('.table tbody tr').forEach(r => {
            r.classList.remove('keyboard-highlight');
        });
        
        // Add highlight to current row
        row.classList.add('keyboard-highlight');
        row.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    
    // ===== PROGRESS BAR ANIMATION =====
    const progressBars = document.querySelectorAll('.progress-bar');
    progressBars.forEach(bar => {
        const width = bar.getAttribute('aria-valuenow') || '0';
        bar.style.width = '0%';
        
        setTimeout(() => {
            bar.style.width = width + '%';
        }, 500);
    });
    
    // ===== STATUS BADGE ANIMATIONS =====
    const statusBadges = document.querySelectorAll('.text-info, .text-danger, .text-success, .text-primary, .text-warning');
    statusBadges.forEach(badge => {
        badge.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.1)';
            this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        });
        
        badge.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
        });
    });
    
    // ===== AVATAR HOVER EFFECTS =====
    const avatars = document.querySelectorAll('.avatar-40');
    avatars.forEach(avatar => {
        avatar.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.15) rotate(5deg)';
            this.style.boxShadow = '0 8px 20px rgba(102, 126, 234, 0.3)';
        });
        
        avatar.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
        });
    });
    
    // ===== MEDIA GROUP HOVER EFFECTS =====
    const mediaGroups = document.querySelectorAll('.iq-media-group');
    mediaGroups.forEach(group => {
        const icons = group.querySelectorAll('.iq-icon-box-3');
        
        icons.forEach((icon, index) => {
            icon.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.3) translateY(-3px)';
                this.style.boxShadow = '0 8px 20px rgba(102, 126, 234, 0.4)';
                
                // Stagger effect for other icons
                icons.forEach((otherIcon, otherIndex) => {
                    if (otherIndex !== index) {
                        otherIcon.style.transform = 'scale(1.1)';
                    }
                });
            });
            
            icon.addEventListener('mouseleave', function() {
                this.style.transform = '';
                this.style.boxShadow = '';
                
                icons.forEach(otherIcon => {
                    otherIcon.style.transform = '';
                });
            });
        });
    });
    
    // ===== LOADING STATE MANAGEMENT =====
    function showLoadingState(tableContainer) {
        tableContainer.classList.add('table-loading');
    }
    
    function hideLoadingState(tableContainer) {
        tableContainer.classList.remove('table-loading');
    }
    
    // ===== SEARCH FUNCTIONALITY (if search input exists) =====
    const searchInput = document.querySelector('input[type="search"], .search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const tableRows = document.querySelectorAll('.table tbody tr');
            
            tableRows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                    row.style.animation = 'fadeInUp 0.3s ease';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
    
    // ===== EXPORT FUNCTIONALITY (if export button exists) =====
    const exportBtn = document.querySelector('.export-btn, [data-export]');
    if (exportBtn) {
        exportBtn.addEventListener('click', function() {
            const table = document.querySelector('.table');
            const rows = Array.from(table.querySelectorAll('tr'));
            
            let csv = [];
            rows.forEach(row => {
                const cols = Array.from(row.querySelectorAll('th, td'));
                const rowData = cols.map(col => {
                    // Remove HTML tags and get text content
                    return '"' + col.textContent.replace(/"/g, '""') + '"';
                });
                csv.push(rowData.join(','));
            });
            
            const csvContent = csv.join('\n');
            const blob = new Blob([csvContent], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'table-export.csv';
            a.click();
            window.URL.revokeObjectURL(url);
        });
    }
    
    // ===== PRINT FUNCTIONALITY (if print button exists) =====
    const printBtn = document.querySelector('.print-btn, [data-print]');
    if (printBtn) {
        printBtn.addEventListener('click', function() {
            window.print();
        });
    }
    
    // ===== AUTO-REFRESH CAPABILITY =====
    let autoRefreshInterval;
    
    function startAutoRefresh(interval = 30000) { // 30 seconds default
        autoRefreshInterval = setInterval(() => {
            // Trigger table refresh (implement based on your data loading logic)
            console.log('Auto-refreshing table...');
            // location.reload(); // or your custom refresh function
        }, interval);
    }
    
    function stopAutoRefresh() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
        }
    }
    
    // ===== PERFORMANCE OPTIMIZATION =====
    // Debounce resize events
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(adjustTableWidth, 250);
    });
    
    // Intersection Observer for lazy loading
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '50px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Observe table rows for lazy loading
    tableRows.forEach(row => {
        row.style.opacity = '0';
        row.style.transform = 'translateY(20px)';
        observer.observe(row);
    });
    
    // ===== ACCESSIBILITY IMPROVEMENTS =====
    // Add ARIA labels and roles
    const tables = document.querySelectorAll('.table');
    tables.forEach(table => {
        table.setAttribute('role', 'table');
        table.setAttribute('aria-label', 'Data table');
        
        const headers = table.querySelectorAll('th');
        headers.forEach(header => {
            header.setAttribute('role', 'columnheader');
        });
        
        const cells = table.querySelectorAll('td');
        cells.forEach(cell => {
            cell.setAttribute('role', 'cell');
        });
    });
    
    // ===== ERROR HANDLING =====
    window.addEventListener('error', function(e) {
        console.error('Table enhancement error:', e.error);
    });
    
    // ===== CLEANUP =====
    window.addEventListener('beforeunload', function() {
        stopAutoRefresh();
        observer.disconnect();
    });
    
    console.log('Table Center Enhancements loaded successfully!');
});

// ===== UTILITY FUNCTIONS =====
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
} 