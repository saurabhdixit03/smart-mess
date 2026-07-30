package com.smartmess.backend.dto.response;

import java.util.List;

public class BillingOverviewResponse {
	
	private BillingSummaryResponse summary;

	private List<BillResponse> bills;

	public BillingSummaryResponse getSummary() {
		return summary;
	}

	public void setSummary(BillingSummaryResponse summary) {
		this.summary = summary;
	}

	public List<BillResponse> getBills() {
		return bills;
	}

	public void setBills(List<BillResponse> bills) {
		this.bills = bills;
	}

}
