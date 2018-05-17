package net.cerulan.pamsimpleharvest;

import net.minecraftforge.common.config.Config;

/**
 * The mod's configuration, using the Forge annotated config file system
 */
@Config(modid = PamSimpleHarvest.MODID)
public class Configuration {
	
	@Config.Comment("Should setup handlers for simple crops (tomato, cucumber, etc...)")
	@Config.RequiresMcRestart
	public static boolean simpleCrops = true;
	
	@Config.Comment("Should setup handlers for tree fruits (papaya, orange, etc...)")
	@Config.RequiresMcRestart
	public static boolean fruits = true;

	@Config.Comment("Should setup handlers for tree logs (cinnamon, paperbark, etc...)")
	@Config.RequiresMcRestart
	public static boolean logs = true;
	
	@Config.Comment("Should crop drops be dropped to the player (otherwise on the ground at the crop)")
	public static boolean cropPlayerDrop = false;
	
	@Config.Comment("Override the default SimpleHarvest drop implementation to always drop crops, even JSON-configured ones, at the player")
	@Config.RequiresMcRestart
	public static boolean overrideCrop = false;
	
	@Config.Comment("Should fruit drops be dropped to the player (otherwise at the position of the fruit)")
	public static boolean fruitPlayerDrop = false;

}
