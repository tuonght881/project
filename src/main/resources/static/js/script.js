
// Cập nhật hình ảnh dựa trên kết quả quẻ 1 và quẻ 2
const que1Image = document.getElementById('que1Image');
que1Image.src = `/images/que${data.que1}.png`;
que1Image.classList.remove('hidden');
que1Image.classList.add('que-image'); // Thêm lớp để áp dụng căn chỉnh

const que2Image = document.getElementById('que2Image');
que2Image.src = `/images/que${data.que2}.png`;
que2Image.classList.remove('hidden');
que2Image.classList.add('que-image'); // Thêm lớp để áp dụng căn chỉnh

function uploadFile() {
	
    var form = $('#uploadForm')[0];
    var data = new FormData(form);
	// Xử lý thông báo lỗi nếu có
	//alert("Có lỗi xảy ra khi xử lý file Excel!");
    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/upload",
        data: data,
        processData: false,
        contentType: false,
        cache: false,
        success: function (response) {
            var excelDataBody = $('#excelDataBody');
            excelDataBody.empty();
			// Xoá danh sách lỗi hiện tại
			$('#errorList').empty();
            if (response.errors.length > 0) {
				//alert(response.errors);
				const errors = response.errors;
				response.errors = [];
				// Kiểm tra nếu có lỗi thì hiển thị toast và thêm các thông báo lỗi vào danh sách
				$(document).ready(function() {
				        const errorList = $('#errorList');
						
				        // Hiển thị thời gian hiện tại trong toast
				        const currentTime = new Date().toLocaleTimeString();
				        document.getElementById('toastTime').textContent = currentTime;

				        // Thêm các thông báo lỗi vào danh sách
				        errors.forEach(error => {
				            errorList.append('<li>' + error + '</li>');
				        });
				        
				        // Hiển thị toast
				        $('#errorToast').toast('show');
				});
            }

            if (response.rows.length > 0) {
                // Đổ dữ liệu vào bảng từ response.rows
                response.rows.forEach(row => {
					var formattedGia = formatNumber(row.gia);
                    var newRow = '<tr>' +
                        '<td>' + row.sdt + '</td>' +
                        '<td>' + row.cleanedSdt + '</td>' +
                        '<td>' + row.que1 + '</td>' +
                        '<td>' + row.que2 + '</td>' +
                        '<td>' + row.que3 + '</td>' +
                        '<td>' + row.que + '</td>' +
						'<td>' + formattedGia  + '</td>' +
                        '</tr>';
                    excelDataBody.append(newRow);
                });
            }
        },
        error: function (e) {
            $('#excelDataBody').text("Error: " + e.responseText);
        }
    });
}

function formatNumber(number) {
    // Kiểm tra xem number có phải là một số hay không
    if (!isNaN(number)) {
        return Number(number).toLocaleString('en-US');
    }
    return number; // Trả lại giá trị gốc nếu không phải là số
}
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