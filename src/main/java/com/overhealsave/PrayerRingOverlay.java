package com.overhealsave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
	private static final float RING_STROKE = 2f;
	private static final int RING_PADDING = 4;
	private static final long PULSE_HALF_PERIOD_MS = 400L;

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
		if (!config.enablePrayerRing() || !plugin.isWarningActive())
		{
			return null;
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

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(RING_STROKE));
		g.setColor(pulseColor());
		g.drawOval(
			bounds.x - RING_PADDING,
			bounds.y - RING_PADDING,
			bounds.width + RING_PADDING * 2 - 1,
			bounds.height + RING_PADDING * 2 - 1);
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

	private static Color pulseColor()
	{
		long phase = (System.currentTimeMillis() / PULSE_HALF_PERIOD_MS) & 1L;
		return phase == 0 ? Color.RED : Color.ORANGE;
	}
}
