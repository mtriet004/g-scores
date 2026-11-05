const rows = Array.from(document.querySelectorAll('#score-bands tbody tr'));
const canvas = document.getElementById('score-chart');
const countFormatter = new Intl.NumberFormat('vi-VN');
const axisFormatter = new Intl.NumberFormat('vi-VN', { notation: 'compact', maximumFractionDigits: 1 });

new Chart(canvas, {
    type: 'bar',
    data: {
        labels: rows.map(row => row.cells[0].textContent),
        datasets: [{
            label: 'Số thí sinh',
            data: rows.map(row => Number(row.cells[1].textContent)),
            backgroundColor: ['#0d766e', '#259b91', '#59bcb2', '#9bdad1'],
            borderRadius: 5,
            maxBarThickness: 96
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false },
            tooltip: {
                callbacks: {
                    label: context => `${countFormatter.format(context.parsed.y)} thí sinh`
                }
            }
        },
        scales: {
            x: { ticks: { autoSkip: false, maxRotation: 0 } },
            y: {
                beginAtZero: true,
                ticks: {
                    precision: 0,
                    maxTicksLimit: 6,
                    callback: value => axisFormatter.format(value)
                }
            }
        }
    }
});
