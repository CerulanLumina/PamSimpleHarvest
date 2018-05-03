package net.cerulan.pamsimpleharvest;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.FruitRegistry;
import com.pam.harvestcraft.blocks.growables.BlockPamCrop;
import com.pam.harvestcraft.blocks.growables.BlockPamFruit;
import com.pam.harvestcraft.blocks.growables.BlockPamFruitLog;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
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
	public static final String VERSION = "1.0.0";

	private static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logger.info("Making integrations...");
		if (Configuration.simpleCrops) {
			logger.info("Setting up simple growable crops");
			for (BlockPamCrop pamCrop : CropRegistry.getCrops().values()) {
				IBlockState newState = pamCrop.getDefaultState();
				IBlockState matureState = pamCrop.getDefaultState().withProperty(BlockPamCrop.CROPS_AGE,
						pamCrop.getMatureAge());
				BlockStack initialBlock = new BlockStack(pamCrop, pamCrop.getMetaFromState(newState));
				BlockStack matureBlock = new BlockStack(pamCrop, pamCrop.getMetaFromState(matureState));
				Crop crop = new Crop(matureBlock, initialBlock);
				Harvest.config.getCropMap().put(crop.getInitialBlock(), crop);
				Harvest.CUSTOM_HANDLERS.put(pamCrop, ReplantHandlers.CONFIG);
			}
		}
		if (Configuration.fruits) {
			logger.info("Setting up tree fruits");
			for (BlockPamFruit pamFruit : FruitRegistry.fruits) {
				IBlockState newState = pamFruit.getDefaultState();
				IBlockState matureState = pamFruit.getDefaultState().withProperty(BlockPamFruit.AGE,
						pamFruit.getMatureAge());
				BlockStack initialBlock = new BlockStack(pamFruit, pamFruit.getMetaFromState(newState));
				BlockStack matureBlock = new BlockStack(pamFruit, pamFruit.getMetaFromState(matureState));
				Crop crop = new Crop(matureBlock, initialBlock);
				Harvest.config.getCropMap().put(crop.getInitialBlock(), crop);
				Harvest.CUSTOM_HANDLERS.put(pamFruit, fruit_handler);
			}
		}
		if (Configuration.logs) {
			logger.info("Setting up tree logs");
			for (BlockPamFruitLog pamFruitLog : FruitRegistry.logs.values()) {
				IBlockState newState = pamFruitLog.getDefaultState();
				IBlockState matureState = pamFruitLog.getDefaultState().withProperty(BlockPamFruitLog.AGE,
						pamFruitLog.getMatureAge());
				BlockStack initialBlock = new BlockStack(pamFruitLog, pamFruitLog.getMetaFromState(newState));
				BlockStack matureBlock = new BlockStack(pamFruitLog, pamFruitLog.getMetaFromState(matureState));
				Crop crop = new Crop(matureBlock, initialBlock);
				Harvest.config.getCropMap().put(crop.getInitialBlock(), crop);
				Harvest.CUSTOM_HANDLERS.put(pamFruitLog, fruit_log_handler);
			}
		}
		logger.info("Done");
	}

	@SuppressWarnings("deprecation")
	private static final IReplantHandler fruit_handler = (world, pos, state, player, tileEntity) -> {
		BlockStack worldBlock = BlockStack.getStackFromPos(world, pos);
		BlockStack newBlock = Harvest.config.getCropMap().get(worldBlock).getFinalBlock();
		// deprecated, but Pam's Harvestcraft uses it...
		List<ItemStack> drops = worldBlock.getBlock().getDrops(world, pos, state, 0);
		if (!world.isRemote) {
			world.setBlockState(pos, newBlock.getState());
			for (ItemStack drop : drops) {
				EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						drop);
				entityItem.setPickupDelay(0);
				world.spawnEntity(entityItem);
			}
		}
	};

	@SuppressWarnings("deprecation")
	private static final IReplantHandler fruit_log_handler = (world, pos, state, player, tileEntity) -> {
		BlockStack worldBlock = BlockStack.getStackFromPos(world, pos);
		BlockStack newBlock = Harvest.config.getCropMap().get(worldBlock).getFinalBlock();
		// deprecated, but Pam's Harvestcraft uses it...
		List<ItemStack> drops = worldBlock.getBlock().getDrops(world, pos, state, 0);
		if (!world.isRemote) {
			world.setBlockState(pos, newBlock.getState());
			for (ItemStack drop : drops) {
				EntityItem entityItem = new EntityItem(world, player.posX, player.posY, player.posZ, drop);
				entityItem.setPickupDelay(0);
				world.spawnEntity(entityItem);
			}
		}
	};

}
