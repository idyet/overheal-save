package com.overhealsave;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
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
	private static final Stroke ARC_STROKE = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

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
		if (!config.enableOrbGlow() || !plugin.isOverhealed())
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
		double x = bounds.x + ORB_OFFSET;
		double y = bounds.y + (bounds.height / 2.0 - ORB_DIAMETER / 2.0);

		Arc2D.Double arc = new Arc2D.Double(x, y, ORB_DIAMETER, ORB_DIAMETER,
			90.0, -360.0 * plugin.getDecayProgress(), Arc2D.OPEN);

		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(ARC_STROKE);
		g.setColor(OverhealSaveColors.arcColor(plugin.isWarningActive()));
		g.draw(arc);
		return null;
	}
}
