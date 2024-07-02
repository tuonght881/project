
// Cập nhật hình ảnh dựa trên kết quả quẻ 1 và quẻ 2
const que1Image = document.getElementById('que1Image');
que1Image.src = `/images/que${data.que1}.png`;
que1Image.classList.remove('hidden');
que1Image.classList.add('que-image'); // Thêm lớp để áp dụng căn chỉnh

const que2Image = document.getElementById('que2Image');
que2Image.src = `/images/que${data.que2}.png`;
que2Image.classList.remove('hidden');
que2Image.classList.add('que-image'); // Thêm lớp để áp dụng căn chỉnh

document.getElementById('uploadButton').addEventListener('click', async () => {
	const fileInput = document.getElementById('fileInput');
	const file = fileInput.files[0];

	if (!file) {
		alert('Please select a file.');
		return;
	}

	const formData = new FormData();
	formData.append('file', file);

	try {
		const response = await fetch('http://localhost:8080/api/excel/upload', {
			method: 'POST',
			body: formData
		});

		if (!response.ok) {
			throw new Error('Failed to upload file.');
		}

		const data = await response.json();
		populateTable(data);
	} catch (error) {
		console.error('Error:', error);
	}
});
function populateTable(data) {
	const tableHeader = document.getElementById('tableHeader');
	const tableBody = document.getElementById('tableBody');
	tableHeader.innerHTML = '';
	tableBody.innerHTML = '';

	if (data.length === 0) {
		return;
	}

	// Create table headers
	const headers = Object.keys(data[0]);
	headers.forEach(header => {
		const th = document.createElement('th');
		th.textContent = header;
		tableHeader.appendChild(th);
	});

	// Create table rows
	data.forEach(row => {
		const tr = document.createElement('tr');
		headers.forEach(header => {
			const td = document.createElement('td');
			td.textContent = row[header];
			tr.appendChild(td);
		});
		tableBody.appendChild(tr);
	});
}

document.getElementById('exportButton').addEventListener('click', async () => {
	try {
		const data = getDataFromTable(); // Lấy dữ liệu từ bảng
		const response = await fetch('http://localhost:8080/export', {
			method: 'POST', // Phương thức POST
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data) // Gửi dữ liệu lên server dưới dạng JSON
		});

		if (!response.ok) {
			throw new Error('Không thể xuất file Excel.');
		}

		// Nhận file Excel từ server và tải về
		const blob = await response.blob();
		const url = window.URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.style.display = 'none';
		a.href = url;
		a.download = 'data.xlsx';
		document.body.appendChild(a);
		a.click();
		window.URL.revokeObjectURL(url);
	} catch (error) {
		console.error('Lỗi:', error);
	}
});
function getDataFromTable() {
	const table = document.getElementById('dataTable');
	const headers = Array.from(table.querySelectorAll('thead th')).map(th => th.textContent.trim());
	const rows = Array.from(table.querySelectorAll('tbody tr')).map(row => {
		const rowData = {};
		Array.from(row.cells).forEach((cell, index) => {
			rowData[headers[index]] = cell.textContent.trim();
		});
		return rowData;
	});
	return rows;
}
async function calculateQue() {
	const phoneNumber = document.getElementById('phoneNumberInput').value;

	try {
		const response = await fetch('https://demo-app-sdt-b5284982a2d3.herokuapp.com/calculate', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(phoneNumber)
		});

		if (!response.ok) {
			throw new Error('Không thể tính toán "3 quẻ".');
		}

		const data = await response.json();
		document.getElementById('cleanedPhoneNumberInput').value = data.cleanPhoneNumber;
		document.getElementById('que1Input').value = data.que1;
		document.getElementById('que2Input').value = data.que2;
		document.getElementById('que3Input').value = data.que3;

		// Cập nhật hình ảnh dựa trên kết quả quẻ 1 và quẻ 2
		const que1Image = document.getElementById('que1Image');
		que1Image.src = `/images/que${data.que1 === 0 ? 8 : data.que1}.png`;
		que1Image.classList.remove('hidden');

		const que2Image = document.getElementById('que2Image');
		que2Image.src = `/images/que${data.que2 === 0 ? 8 : data.que2}.png`;
		que2Image.classList.remove('hidden');
	} catch (error) {
		console.error('Lỗi:', error);
	}
}