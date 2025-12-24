const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const tableBody = document.querySelector('table tbody');

function filterTable() {
    const searchTerm = (searchInput.value || '').toLowerCase();
    const rows = tableBody ? tableBody.querySelectorAll('tr') : [];

    rows.forEach(row => {
        const name = (row.cells[1].textContent || '').toLowerCase();
        const email = (row.cells[2].textContent || '').toLowerCase();
        const role = (row.cells[3].textContent || '').toLowerCase();

        const match = name.includes(searchTerm) || email.includes(searchTerm) || role.includes(searchTerm);
        row.style.display = match ? '' : 'none';
    });
}

if (searchBtn) searchBtn.addEventListener('click', filterTable);
if (searchInput) searchInput.addEventListener('keyup', function(e) {
    if (e.key === 'Enter') filterTable();
});