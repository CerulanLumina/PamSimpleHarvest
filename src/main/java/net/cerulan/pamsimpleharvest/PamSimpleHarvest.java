package net.cerulan.pamsimpleharvest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.logging.log4j.Logger;

import com.pam.harvestcraft.blocks.BlockRegistry;
import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.FruitRegistry;
import com.pam.harvestcraft.blocks.growables.BlockPamCrop;
import com.pam.harvestcraft.blocks.growables.BlockPamFruit;
import com.pam.harvestcraft.blocks.growables.BlockPamFruitLog;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tehnut.harvest.BlockStack;
import tehnut.harvest.Crop;
import tehnut.harvest.Harvest;
import tehnut.harvest.IReplantHandler;
import tehnut.harvest.ReplantHandlers;

@Mod(modid = PamSimpleHarvest.MODID, name = PamSimpleHarvest.NAME, version = PamSimpleHarvest.VERSION, dependencies = "required-after:harvest;required-after:harvestcraft;")
public class PamSimpleHarvest {
	public static final String MODID = "pamsimpleharvest";
	public static final String NAME = "PamSimpleHarvest";
	public static final String VERSION = "2.0.0";

	static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		
	}	

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		if (Configuration.overrideCrop) {
			logger.info("Replacing SimpleHarvest handler using reflection because asm is bad...");
			try {
				// Using reflection, strip the field of its final modifier and set it
				Field shconfig = ReplantHandlers.class.getDeclaredField("CONFIG");
				Field modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(shconfig, shconfig.getModifiers() & ~Modifier.FINAL);
				shconfig.setAccessible(true);
				shconfig.set(null, PSHHandlers.getHandlerForType(PSHHandlers.CropType.CROP));
			} catch (Exception ex) {
				logger.error("Failed to replace handler");
				ex.printStackTrace();
			}
		}
		
		
		logger.info("Making integrations...");
		if (Configuration.simpleCrops) {
			logger.info("Setting up simple growable crops");
			CropRegistry.getCrops().values().stream().forEach((pamCrop) -> {
				register(pamCrop, PSHHandlers.getHandlerForType(PSHHandlers.CropType.CROP), BlockPamCrop.CROPS_AGE, pamCrop.getMatureAge());
			});
		}
		if (Configuration.fruits) {
			logger.info("Setting up tree fruits");
			FruitRegistry.registeringFruits.keySet().stream()
			.map(str -> {
				try {
					Method m = FruitRegistry.class.getDeclaredMethod("getFruitBlockName", String.class);
					m.setAccessible(true);
					String result = (String)m.invoke(null, str);
					return result;
				} catch (Exception ex) {
					logger.fatal(String.format("Unable to find fruit block name for %s!!!", str));
					ex.printStackTrace();
				}
				return "";
			})
			.map(str -> {
				return BlockRegistry.blocks.stream().filter(block -> {
					return !(block instanceof BlockPamFruitLog) // pam for some reason adds all of the logs to the fruit map anyway so work around.... 
							&& block.getRegistryName().getResourcePath().equals(str);}).findAny();
			})
			.forEach(block -> {
				block.ifPresent((optBlock) -> {
					BlockPamFruit pamFruit = (BlockPamFruit)optBlock;					
					register(pamFruit, PSHHandlers.getHandlerForType(PSHHandlers.CropType.FRUIT), BlockPamFruit.AGE, pamFruit.getMatureAge());
					
				});
			});
		}
		if (Configuration.logs) {
			logger.info("Setting up tree logs");
			FruitRegistry.logs.values().stream().forEach((pamFruitLog) -> {
				register(pamFruitLog, PSHHandlers.getHandlerForType(PSHHandlers.CropType.LOG), BlockPamFruitLog.AGE, pamFruitLog.getMatureAge());
			});
		}
		logger.info("Done");
		
	}
	
	/**
	 * Register a block and handler to SimpleHarvest's crop map
	 * @param block The Minecraft Block the operate on
	 * @param handler An {@link IReplantHandler} to handle this crop
	 * @param propAge The integer age property of the block state for generating a fully grown state
	 * @param matureAge The mature age as an integer
	 */
	private void register(Block block, IReplantHandler handler, PropertyInteger propAge, int matureAge) {
		
		IBlockState newState = block.getDefaultState();
		IBlockState matureState = block.getDefaultState().withProperty(propAge,
				matureAge);
		
		BlockStack initialBlock = new BlockStack(block, block.getMetaFromState(newState));
		BlockStack matureBlock = new BlockStack(block, block.getMetaFromState(matureState));
		Crop crop = new Crop(matureBlock, initialBlock);
		Harvest.config.getCropMap().put(crop.getInitialBlock(), crop);
		Harvest.CUSTOM_HANDLERS.put(block, handler);
		
	}


}
