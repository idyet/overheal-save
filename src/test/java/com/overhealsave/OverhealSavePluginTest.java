package com.overhealsave;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class OverhealSavePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(OverhealSavePlugin.class);
		RuneLite.main(args);
	}
}
