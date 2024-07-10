package com.example.demo.controller;

import com.example.demo.entity.RowData;

import org.apache.maven.model.Model;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ExcelController {
	
	private List<RowData> rows = new ArrayList<>();
	List<String> errors = new ArrayList<>();

	@Autowired

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/upload")
	@ResponseBody
	public Map<String, Object> uploadExcelFile(@RequestParam("file") MultipartFile file, Model m) {
		Map<String, Object> result = new HashMap<>();
		try {
			if (file.isEmpty()) {
				throw new IOException("Chưa chọn file Excel!");
			}
			try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
				Sheet sheet = workbook.getSheetAt(0);
				errors.clear();
				rows.clear();
				for (int i = 0; i <= sheet.getLastRowNum(); i++) {
					Row row = sheet.getRow(i);
					if (row == null) {
						errors.add("Hàng trống tại vị trí: " + (i + 1));
						continue;
					}

					Double gia = row.getCell(2).getNumericCellValue();// chỉ lấy cột C
					
					String sdt = getCellValueAsString(row.getCell(1));// chỉ lấy cột B
					if (sdt.isEmpty()) {
						sdt = getCellValueAsString(row.getCell(0));
					}

					String cleanedSdt = cleanPhoneNumber(sdt);
					if (cleanedSdt.length() < 10) {
						errors.add("Số điện thoại không hợp lệ: hàng " + (i + 1));
						continue;
					}

					int que1 = calculateQue(cleanedSdt.substring(0, 5));
					int que2 = calculateQue(cleanedSdt.substring(5));
					int que3 = calculateFinalQue(cleanedSdt);
					String que = Integer.toString(que1) + Integer.toString(que2) + Integer.toString(que3);

					rows.add(new RowData(sdt, cleanedSdt, que1, que2, que3, que, gia));
				}

				result.put("rows", rows);
				result.put("errors", errors);
			} catch (IOException e) {
				//e.printStackTrace();
				errors.add("Lỗi khi đọc file Excel: " + e.getMessage());
				result.put("errors", errors);
			}

		} catch (Exception e) {
			//e.printStackTrace();
			errors.add("Lỗi khi xử lý file Excel: " + e.getMessage());
			result.put("errors", errors);
		}
		return result;
	}

	@PostMapping("/calculate")
	@ResponseBody
	public Map<String, Object> tinhQue(@RequestBody String phoneNumber) {
		Map<String, Object> result = new HashMap<>();
		String cleanedPhoneNumber = cleanPhoneNumber(phoneNumber);
		int que1 = calculateQue(cleanedPhoneNumber.substring(0, 5));
		int que2 = calculateQue(cleanedPhoneNumber.substring(5));
		int que3 = calculateFinalQue(cleanedPhoneNumber);
		String que = Integer.toString(que1) + Integer.toString(que2) + Integer.toString(que3);

		result.put("cleanPhoneNumber", cleanedPhoneNumber);
		result.put("que1", que1);
		result.put("que2", que2);
		result.put("que3", que3);
		result.put("que", que);

		return result;
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportToExcel() {
		try (Workbook workbook = new XSSFWorkbook()) {
			DataFormat format = workbook.createDataFormat();
			CellStyle currencyStyle = workbook.createCellStyle();
			currencyStyle.setDataFormat(format.getFormat("#,##0"));
			
			Sheet sheet = workbook.createSheet("Dữ liệu từ bảng");

			// Header của bảng
			Row headerRow = sheet.createRow(0);
			String[] tieude = { "SDT", "SDT Đã Làm Chuẩn", "Quẻ 1", "Quẻ 2", "Quẻ 3", "Quẻ","Giá"};
			for (int i = 0; i < tieude.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(tieude[i]);
			}

			// Lấy dữ liệu từ rows đã lưu khi upload
			int rowNum = 1;
			for (RowData rowData : rows) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(rowData.getSdt());
				row.createCell(1).setCellValue(rowData.getCleanedSdt());
				row.createCell(2).setCellValue(rowData.getQue1());
				row.createCell(3).setCellValue(rowData.getQue2());
				row.createCell(4).setCellValue(rowData.getQue3());
				row.createCell(5).setCellValue(rowData.getQue());
				Cell giaCell = row.createCell(6);
				giaCell.setCellValue(rowData.getGia());
				giaCell.setCellStyle(currencyStyle);
			}

			// Chuẩn bị trả về file Excel
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(
					MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.setContentDispositionFormData("attachment", "data.xlsx");
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	private String cleanPhoneNumber(String phoneNumber) {
	    // Loại bỏ tất cả các ký tự không phải là số
	    String cleanedPhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

	    // Xử lý các trường hợp đặc biệt
	    if (cleanedPhoneNumber.length() <= 8) {
	        // Nếu số không bắt đầu bằng số 0, thêm số 0 vào đầu
	        if (!cleanedPhoneNumber.startsWith("0")) {
	            cleanedPhoneNumber = "0" + cleanedPhoneNumber;
	        }
	        // Thêm số 0 vào phía sau cho đến khi đủ 10 chữ số
	        while (cleanedPhoneNumber.length() < 10) {
	            cleanedPhoneNumber += "0";
	        }
	    } else if (cleanedPhoneNumber.length() == 9) {
	        // Nếu số bắt đầu bằng số 0, thêm số 0 vào cuối
	        if (cleanedPhoneNumber.startsWith("0")) {
	            cleanedPhoneNumber += "0";
	        } else {
	            // Nếu số không bắt đầu bằng số 0, thêm số 0 vào đầu
	            cleanedPhoneNumber = "0" + cleanedPhoneNumber;
	        }
	    }

	    return cleanedPhoneNumber;
	}

	private int calculateQue(String part) {
		int div = Integer.parseInt(part) / 8;
		int mul = div * 8;
		return Integer.parseInt(part) - mul;
	}

	private int calculateFinalQue(String phoneNumber) {
		int part1 = Integer.parseInt(phoneNumber.substring(0, 5));
		int part2 = Integer.parseInt(phoneNumber.substring(5));
		int sum = part1 + part2;
		int div = sum / 6;
		int mul = div * 6;
		return sum - mul;
	}

	private String getCellValueAsString(Cell cell) {
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			if (cell != null) {
				System.out.println(
						"Ô trống tại hàng: " + (cell.getRowIndex() + 1) + ", cột: " + (cell.getColumnIndex() + 1));
			}
			return "";
		}

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				if (cell.getNumericCellValue() % 1 == 0) {
					return String.valueOf((long) cell.getNumericCellValue());
				} else {
					return String.valueOf(cell.getNumericCellValue());
				}
			}
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			try {
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				if (cellValue == null) {
					return "";
				}
				switch (cellValue.getCellType()) {
				case STRING:
					return cellValue.getStringValue();
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						return cell.getDateCellValue().toString();
					} else {
						if (cellValue.getNumberValue() % 1 == 0) {
							return String.valueOf((long) cellValue.getNumberValue());
						} else {
							return String.valueOf(cellValue.getNumberValue());
						}
					}
				case BOOLEAN:
					return String.valueOf(cellValue.getBooleanValue());
				default:
					return "";
				}
			} catch (Exception e) {
				return cell.getCellFormula();
			}
		default:
			return "";
		}
	}
}