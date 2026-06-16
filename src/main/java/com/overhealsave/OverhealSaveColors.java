package com.overhealsave;

import java.awt.Color;

final class OverhealSaveColors
{
	private static final Color BASE = brighter(0x9B0703);
	private static final Color FLASH = Color.ORANGE;
	private static final long PULSE_HALF_PERIOD_MS = 400L;

	private OverhealSaveColors()
	{
	}

	static Color arcColor(boolean flashing)
	{
		if (!flashing)
		{
			return BASE;
		}
		long phase = (System.currentTimeMillis() / PULSE_HALF_PERIOD_MS) & 1L;
		return phase == 0 ? BASE : FLASH;
	}

	private static Color brighter(int color)
	{
		float[] hsv = new float[3];
		Color.RGBtoHSB(color >>> 16, (color >> 8) & 0xFF, color & 0xFF, hsv);
		return Color.getHSBColor(hsv[0], 1f, 1f);
	}
}
