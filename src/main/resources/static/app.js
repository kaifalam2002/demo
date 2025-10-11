document.getElementById('billForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const data = {
        email: document.getElementById('email').value,
        customerName: document.getElementById('customerName').value,
        address: document.getElementById('address').value,
        phone: document.getElementById('phone').value,
        gstin: document.getElementById('gstin').value,
        dueDate: document.getElementById('dueDate').value,
        amount: parseFloat(document.getElementById('amount').value)
    };

    console.log('Form data:', data);

    fetch('/api/download-bill', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text) });
        }
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'Customer_Bill.pdf';
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    })
    .catch(error => {
        console.error('Error:', error);
        alert("Error generating bill.");
    });
});
