package com.scorch.core.events.reports;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.report.ReportModule;

public class ReportListener implements Listener {

	private ReportModule rm;

	public ReportListener() {
		rm = ScorchCore.getInstance().getModule("ReportModule", ReportModule.class);

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());
	}

	@EventHandler
	public void onReportCreate(ReportCreateEvent event) {
		rm.addReport(event.getReport());
	}
}
