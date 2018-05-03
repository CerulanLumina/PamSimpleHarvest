package net.cerulan.pamsimpleharvest;

import net.minecraftforge.common.config.Config;

@Config(modid = PamSimpleHarvest.MODID)
public class Configuration {
	
	@Config.Comment("Should setup handlers for simple crops (tomato, cucumber, etc...)")
	public static boolean simpleCrops = true;
	
	@Config.Comment("Should setup handlers for tree fruits (papaya, orange, etc...)")
	public static boolean fruits = true;

	@Config.Comment("Should setup handlers for tree logs (cinnamon, paperbark, etc...)")
	public static boolean logs = true;

}
