package com.scorch.core.utils;

import java.awt.Image;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class QRMap extends MapRenderer {
	private Image qrCode;

	public QRMap(final Image qrCode) {
		this.qrCode = qrCode;
	}

	public void render(final MapView mapView, final MapCanvas mapCanvas, final Player player) {
		mapCanvas.drawImage(0, 0, this.qrCode);
		for (int i = 0; i < mapCanvas.getCursors().size(); i++) {
			mapCanvas.getCursors().removeCursor(mapCanvas.getCursors().getCursor(i));
		}
	}
}
