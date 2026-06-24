package com.overhealsave;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Overheal Save",
	description = "Warns when overheal HP is about to decay so you can flick rapid heal to reset the timer.",
	tags = {"combat", "hp", "health", "hitpoints", "prayer", "regen", "overheal", "save", "decay", "rapid", "heal", "pvm", "pvp", "orb"}
)
public class OverhealSavePlugin extends Plugin
{
	private static final int HP_REGEN_TICKS = 100;
	private static final int HP_REGEN_TICKS_RAPID = 50;

	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private OverhealSaveConfig config;

	@Inject
	private HpOrbOverlay hpOrbOverlay;

	@Inject
	private PrayerRingOverlay prayerRingOverlay;

	private int ticksSinceHpRegen = -2;
	private boolean prevWarningActive;

	// False when the plugin is enabled mid-session: we can't know the regen
	// cycle phase until a deterministic reset (login, hop, or Rapid Heal flick).
	@Getter
	private boolean timerKnown;

	@Getter
	private boolean warningActive;

	@Getter
	private boolean overhealed;

	@Getter
	private boolean atFullHp;

	@Getter
	private double decayProgress;

	@Provides
	OverhealSaveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OverhealSaveConfig.class);
	}

	@Override
	protected void startUp()
	{
		ticksSinceHpRegen = -2;
		// Enabled mid-session (already logged in) ⇒ indeterminate. Enabled at the
		// login screen / fresh start ⇒ the upcoming login is a valid sync point.
		timerKnown = client.getGameState() != GameState.LOGGED_IN;
		prevWarningActive = false;
		warningActive = false;
		overhealed = false;
		atFullHp = false;
		decayProgress = 0;
		overlayManager.add(hpOrbOverlay);
		overlayManager.add(prayerRingOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(hpOrbOverlay);
		overlayManager.remove(prayerRingOverlay);
		warningActive = false;
		prevWarningActive = false;
		overhealed = false;
		atFullHp = false;
		decayProgress = 0;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged ev)
	{
		if (ev.getGameState() == GameState.HOPPING || ev.getGameState() == GameState.LOGIN_SCREEN)
		{
			ticksSinceHpRegen = -2;
			// Logout/hop edge: the next login resets the in-game regen timer, so we
			// re-sync. This edge never fires on a region load, so indeterminate is
			// preserved across teleports.
			timerKnown = true;
			prevWarningActive = false;
			warningActive = false;
			overhealed = false;
			atFullHp = false;
			decayProgress = 0;
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged ev)
	{
		if (ev.getVarbitId() == VarbitID.PRAYER_RAPIDHEAL)
		{
			// Flicking Rapid Heal resets the in-game regen timer: a deterministic sync.
			ticksSinceHpRegen = 0;
			timerKnown = true;
		}
	}

	@Subscribe
	private void onGameTick(GameTick ev)
	{
		final boolean rapidHealActive = client.getVarbitValue(VarbitID.PRAYER_RAPIDHEAL) == 1;
		final int cycleLen = rapidHealActive ? HP_REGEN_TICKS_RAPID : HP_REGEN_TICKS;
		ticksSinceHpRegen = (ticksSinceHpRegen + 1) % cycleLen;

		final int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
		final int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
		overhealed = currentHp > maxHp;
		atFullHp = currentHp == maxHp;

		final int effectiveLead = Math.min(config.leadTicks(), cycleLen);
		final boolean inLeadWindow = ticksSinceHpRegen >= cycleLen - effectiveLead;

		warningActive = timerKnown && overhealed && inLeadWindow;
		decayProgress = timerKnown && (overhealed || atFullHp) && ticksSinceHpRegen >= 0
			? 1.0 - (double) ticksSinceHpRegen / cycleLen
			: 0;

		if (warningActive && !prevWarningActive && config.enableSound() && rapidHealActive)
		{
			final double seconds = (cycleLen - ticksSinceHpRegen) * 0.6;
			notifier.notify(String.format("Overheal decay in ~%.1fs — flick rapid heal!", seconds));
		}

		prevWarningActive = warningActive;
	}
}
