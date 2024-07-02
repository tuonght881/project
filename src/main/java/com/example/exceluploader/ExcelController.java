package com.example.exceluploader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ExcelController {

    private List<RowData> rows = new ArrayList<>();


	@GetMapping("/")
	public String index() {
		return "index";
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
	
	@PostMapping("/upload")
	public String uploadExcelFile(@RequestParam("file") MultipartFile file, Model model) {
		//rows.clear(); // Xóa dữ liệu cũ trước khi tải lên file mới
		try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);
			//List<RowData> rows = new ArrayList<>();
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				String sdt = getCellValueAsString(row.getCell(0));
				String cleanedSdt = cleanPhoneNumber(sdt);
				int que1 = calculateQue(cleanedSdt.substring(0, 5));
				int que2 = calculateQue(cleanedSdt.substring(5));
				int que3 = calculateFinalQue(cleanedSdt);
				String que = Integer.toString(que1) + Integer.toString(que2) + Integer.toString(que3);

				rows.add(new RowData(sdt, cleanedSdt, que1, que2, que3, que));
			}
			model.addAttribute("rows", rows);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "index";
	}

	private String cleanPhoneNumber(String phoneNumber) {
	    // Loại bỏ tất cả các ký tự không phải là số
	    String cleanedPhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

	    // Thêm số 0 vào đầu nếu cần thiết
	    if (cleanedPhoneNumber.length() == 9 && !cleanedPhoneNumber.startsWith("0")) {
	        cleanedPhoneNumber = "0" + cleanedPhoneNumber;
	    } else if (cleanedPhoneNumber.length() < 9) {
	        cleanedPhoneNumber = "0" + cleanedPhoneNumber;
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
	    if (cell == null) {
	        return "";
	    }

	    switch (cell.getCellType()) {
	        case STRING:
	            return cell.getStringCellValue();
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
	                return cell.getDateCellValue().toString();
	            } else {
	                // Check if the numeric cell contains decimal places
	                if (cell.getNumericCellValue() % 1 == 0) {
	                    return String.valueOf((long) cell.getNumericCellValue());
	                } else {
	                    return String.valueOf(cell.getNumericCellValue());
	                }
	            }
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue());
	        case FORMULA:
	            return cell.getCellFormula();
	        default:
	            return "";
	    }
	}
	static class RowData {
		private String sdt;
		private String cleanedSdt;
		private int que1;
		private int que2;
		private int que3;
		private String que;

		public RowData(String sdt, String cleanedSdt, int que1, int que2, int que3, String que) {
			this.sdt = sdt;
			this.cleanedSdt = cleanedSdt;
			this.que1 = que1;
			this.que2 = que2;
			this.que3 = que3;
			this.que = que;
		}

		public String getSdt() {
			return sdt;
		}

		public String getCleanedSdt() {
			return cleanedSdt;
		}

		public int getQue1() {
			return que1;
		}

		public int getQue2() {
			return que2;
		}

		public int getQue3() {
			return que3;
		}

		public String getQue() {
			return que;
		}
	}

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dữ liệu từ bảng");

            // Header của bảng
            Row headerRow = sheet.createRow(0);
            String[] tieude = {"SDT", "SDT Đã Làm Chuẩn", "Quẻ 1", "Quẻ 2", "Quẻ 3", "Quẻ"};
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
            }

            // Chuẩn bị trả về file Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "data.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
