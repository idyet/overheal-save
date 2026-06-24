package com.overhealsave;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("overhealsave")
public interface OverhealSaveConfig extends Config
{
	@ConfigItem(
		keyName = "enableSound",
		name = "Notification sound",
		description = "Play a notification when overheal decay is imminent.",
		position = 0
	)
	default boolean enableSound()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableOrbRing",
		name = "Flash HP orb",
		description = "Show a pulsing ring outside the hitpoints orb during the warning window.",
		position = 1
	)
	default boolean enableOrbRing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showRingAtFullHp",
		name = "Show ring at full HP",
		description = "Also show the hitpoints orb ring when at full HP, not only when overhealed.",
		position = 2
	)
	default boolean showRingAtFullHp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enablePrayerRing",
		name = "Highlight Rapid Heal",
		description = "Show a pulsing ring around the Rapid Heal prayer icon during the warning window.",
		position = 3
	)
	default boolean enablePrayerRing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "prayerRingOnlyInLeadWindow",
		name = "Rapid Heal ring only in lead window",
		description = "Only show the Rapid Heal prayer ring during the warning lead window, not for the whole overheal cycle.",
		position = 4
	)
	default boolean prayerRingOnlyInLeadWindow()
	{
		return false;
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
		keyName = "leadTicks",
		name = "Warning lead (ticks)",
		description = "Game ticks before the decay tick to start warning. 1 tick = 0.6s.",
		position = 5
	)
	default int leadTicks()
	{
		return 25;
	}
}
