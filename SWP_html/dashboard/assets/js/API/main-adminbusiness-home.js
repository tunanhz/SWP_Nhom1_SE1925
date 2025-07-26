const chartScript = document.createElement('script');
chartScript.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.min.js';
document.head.appendChild(chartScript);

const API_BASE = 'http://localhost:8080/SWP_back_war_exploded/api/adminbusiness-revenue-reports';
const totalRevenueChartCanvas = document.getElementById('totalRevenueChart');

let chart = null;

async function fetchData() {
    try {
        const response = await fetch(`${API_BASE}/total`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) throw new Error('Failed to fetch data');
        const data = await response.json();
        console.log('API response:', data); // Log for debugging
        return data.data; // Expected: { totalRevenue: { totalRevenue: number } }
    } catch (error) {
        console.error('Error fetching data:', error);
        return { totalRevenue: { totalRevenue: 0 } }; // Fallback
    }
}

function formatCurrency(value) {
    return value.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

function drawBarChart(canvas, labels, data, label, title) {
    if (chart) chart.destroy();
    chart = new Chart(canvas, {
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
            maintainAspectRatio: false, // Allow chart to fill container height
            plugins: {
                legend: { display: false },
                title: { display: true, text: title },
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

async function updateTotalRevenueChart() {
    const data = await fetchData();
    const revenue = data && data.totalRevenue && typeof data.totalRevenue.totalRevenue === 'number' 
        ? data.totalRevenue.totalRevenue 
        : 0;
    drawBarChart(
        totalRevenueChartCanvas,
        ['Tổng Doanh Thu'],
        [revenue],
        'Doanh Thu',
        'Tổng Doanh Thu'
    );
    const counterElement = document.querySelector('.counter');
    if (counterElement) {
        counterElement.textContent = formatCurrency(revenue);
    } else {
        console.warn('Counter element not found');
    }
}

// Wait for Chart.js to load before updating the chart
chartScript.onload = () => {
    updateTotalRevenueChart();
};
chartScript.onerror = () => {
    console.error('Failed to load Chart.js');
};