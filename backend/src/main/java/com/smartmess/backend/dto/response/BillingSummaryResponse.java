package com.smartmess.backend.dto.response;

import java.math.BigDecimal;

public class BillingSummaryResponse {
	
	private Long totalBills;

	private Long paidBills;

	private Long unpaidBills;

	private BigDecimal totalRevenue;

	private BigDecimal collectedRevenue;

	private BigDecimal pendingRevenue;

	public Long getTotalBills() {
		return totalBills;
	}

	public void setTotalBills(Long totalBills) {
		this.totalBills = totalBills;
	}

	public Long getPaidBills() {
		return paidBills;
	}

	public void setPaidBills(Long paidBills) {
		this.paidBills = paidBills;
	}

	public Long getUnpaidBills() {
		return unpaidBills;
	}

	public void setUnpaidBills(Long unpaidBills) {
		this.unpaidBills = unpaidBills;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public BigDecimal getCollectedRevenue() {
		return collectedRevenue;
	}

	public void setCollectedRevenue(BigDecimal collectedRevenue) {
		this.collectedRevenue = collectedRevenue;
	}

	public BigDecimal getPendingRevenue() {
		return pendingRevenue;
	}

	public void setPendingRevenue(BigDecimal pendingRevenue) {
		this.pendingRevenue = pendingRevenue;
	}
}
