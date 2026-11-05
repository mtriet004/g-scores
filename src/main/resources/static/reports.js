const rows = Array.from(document.querySelectorAll('#score-bands tbody tr'));
const canvas = document.getElementById('score-chart');

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
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
    }
});
