package dev.mikeya.barracudahelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BarracudaHelperPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BarracudaHelperPlugin.class);
		RuneLite.main(args);
	}
}