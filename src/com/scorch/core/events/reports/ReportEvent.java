package com.scorch.core.events.reports;

import com.scorch.core.modules.communication.NetworkEvent;
import com.scorch.core.modules.report.Report;

public class ReportEvent extends NetworkEvent {
	private Report report;

	public ReportEvent(Report report) {
		this.report = report;
	}

	public Report getReport() {
		return report;
	}
}
