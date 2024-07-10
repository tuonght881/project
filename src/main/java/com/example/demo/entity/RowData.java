package com.example.demo.entity;

public class RowData {
    private String sdt;
    private String cleanedSdt;
    private int que1;
    private int que2;
    private int que3;
    private String que;
    private Double gia;

    public RowData(String sdt, String cleanedSdt, int que1, int que2, int que3, String que, Double gia) {
		super();
		this.sdt = sdt;
		this.cleanedSdt = cleanedSdt;
		this.que1 = que1;
		this.que2 = que2;
		this.que3 = que3;
		this.que = que;
		this.gia = gia;
	}

	// Getters and setters

    public Double getGia() {
		return gia;
	}

	public void setGia(Double gia) {
		this.gia = gia;
	}
    
    public String getSdt() {
        return sdt;
    }

	public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getCleanedSdt() {
        return cleanedSdt;
    }

    public void setCleanedSdt(String cleanedSdt) {
        this.cleanedSdt = cleanedSdt;
    }

    public int getQue1() {
        return que1;
    }

    public void setQue1(int que1) {
        this.que1 = que1;
    }

    public int getQue2() {
        return que2;
    }

    public void setQue2(int que2) {
        this.que2 = que2;
    }

    public int getQue3() {
        return que3;
    }

    public void setQue3(int que3) {
        this.que3 = que3;
    }

    public String getQue() {
        return que;
    }

    public void setQue(String que) {
        this.que = que;
    }
}
