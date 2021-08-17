package com.scorch.core.events.reports;

import java.util.UUID;

import com.scorch.core.modules.report.Report;

public class ReportHandleEvent extends ReportEvent {

	private UUID handler;

	public ReportHandleEvent(Report report, UUID handler) {
		super(report);
		this.handler = handler;
	}

	public UUID getHandler() {
		return handler;
	}

}
