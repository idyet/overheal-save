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
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class PrayerRingOverlay extends Overlay
{
	private static final int RING_PADDING = 4;
	private static final Stroke ARC_STROKE = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	private final Client client;
	private final OverhealSavePlugin plugin;
	private final OverhealSaveConfig config;

	@Inject
	PrayerRingOverlay(Client client, OverhealSavePlugin plugin, OverhealSaveConfig config)
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
		if (!config.enablePrayerRing() || !plugin.isOverhealed())
		{
			return null;
		}

		final double sweepProgress;
		final boolean flashing;
		if (!plugin.isTimerKnown())
		{
			// Indeterminate: full pulsing ring as a call-to-action to flick.
			// The lead-window gate doesn't apply — there's no accurate window.
			sweepProgress = 1.0;
			flashing = true;
		}
		else
		{
			if (config.prayerRingOnlyInLeadWindow() && !plugin.isWarningActive())
			{
				return null;
			}
			sweepProgress = plugin.getDecayProgress();
			flashing = plugin.isWarningActive();
		}

		Widget rapidHeal = findRapidHealWidget();
		if (rapidHeal == null || rapidHeal.isHidden())
		{
			return null;
		}

		Rectangle bounds = rapidHeal.getBounds();
		if (bounds.width <= 0 || bounds.height <= 0)
		{
			return null;
		}

		double diameter = Math.max(bounds.width, bounds.height) + RING_PADDING * 2.0;
		double cx = bounds.x + bounds.width / 2.0;
		double cy = bounds.y + bounds.height / 2.0;
		Arc2D.Double arc = new Arc2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter,
			90.0, -360.0 * sweepProgress, Arc2D.OPEN);

		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(ARC_STROKE);
		g.setColor(OverhealSaveColors.arcColor(flashing));
		g.draw(arc);
		return null;
	}

	private Widget findRapidHealWidget()
	{
		Widget root = client.getWidget(InterfaceID.Prayerbook.UNIVERSE);
		if (root == null || root.isHidden())
		{
			return null;
		}
		return walkForSprite(root);
	}

	private Widget walkForSprite(Widget w)
	{
		if (w == null)
		{
			return null;
		}
		int sprite = w.getSpriteId();
		if (sprite == SpriteID.Prayeron.RAPID_HEAL || sprite == SpriteID.Prayeroff.RAPID_HEAL_DISABLED)
		{
			return w;
		}
		Widget result;
		Widget[] dyn = w.getDynamicChildren();
		if (dyn != null)
		{
			for (Widget c : dyn)
			{
				if ((result = walkForSprite(c)) != null)
				{
					return result;
				}
			}
		}
		Widget[] stat = w.getStaticChildren();
		if (stat != null)
		{
			for (Widget c : stat)
			{
				if ((result = walkForSprite(c)) != null)
				{
					return result;
				}
			}
		}
		Widget[] nest = w.getNestedChildren();
		if (nest != null)
		{
			for (Widget c : nest)
			{
				if ((result = walkForSprite(c)) != null)
				{
					return result;
				}
			}
		}
		return null;
	}
}
