package com.overhealsave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class HpOrbOverlay extends Overlay
{
	private static final double ORB_DIAMETER = 26D;
	private static final int ORB_OFFSET = 27;
	private static final double RING_PADDING = 3D;
	private static final float RING_STROKE = 3f;
	private static final long PULSE_HALF_PERIOD_MS = 400L;

	private final Client client;
	private final OverhealSavePlugin plugin;
	private final OverhealSaveConfig config;

	@Inject
	HpOrbOverlay(Client client, OverhealSavePlugin plugin, OverhealSaveConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!config.enableOrbGlow() || !plugin.isWarningActive())
		{
			return null;
		}

		Widget orb = client.getWidget(InterfaceID.Orbs.ORB_HEALTH);
		if (orb == null || orb.isHidden())
		{
			orb = client.getWidget(InterfaceID.OrbsNomap.ORB_HEALTH);
		}
		if (orb == null || orb.isHidden())
		{
			return null;
		}

		Rectangle bounds = orb.getBounds();
		double diameter = ORB_DIAMETER + RING_PADDING * 2;
		double x = bounds.x + ORB_OFFSET - RING_PADDING;
		double y = bounds.y + (bounds.height / 2.0) - diameter / 2.0;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(new BasicStroke(RING_STROKE));
		g.setColor(pulseColor());
		g.draw(new Ellipse2D.Double(x, y, diameter, diameter));
		return null;
	}

	private static Color pulseColor()
	{
		long phase = (System.currentTimeMillis() / PULSE_HALF_PERIOD_MS) & 1L;
		return phase == 0 ? Color.RED : Color.ORANGE;
	}
}
