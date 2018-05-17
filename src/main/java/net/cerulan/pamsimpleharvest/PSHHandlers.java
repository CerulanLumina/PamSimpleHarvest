package net.cerulan.pamsimpleharvest;

import java.util.List;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import tehnut.harvest.BlockStack;
import tehnut.harvest.Harvest;
import tehnut.harvest.IReplantHandler;

public class PSHHandlers {
	
	/**
	 * A simple enum of crop types handled by the mod
	 */
	public static enum CropType {
		CROP,
		FRUIT,
		LOG
	}
	
	/**
	 * Drop item in world at double coordinates
	 * @param drop
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	private static void drop(ItemStack drop, World world, double x, double y, double z) {
		EntityItem entityItem = new EntityItem(world, x, y, z, drop);
		entityItem.setPickupDelay(0);
		world.spawnEntity(entityItem);
	}
	
	/**
	 * Drop item at the player's feet
	 * @param drop
	 * @param player
	 * @param world
	 */
	private static void drop(ItemStack drop, EntityPlayer player, World world) {
		drop(drop, world, player.posX, player.posY, player.posZ);
	}
	
	/**
	 * Drop item at a BlockPos
	 * @param drop
	 * @param pos
	 * @param world
	 */
	private static void drop(ItemStack drop, BlockPos pos, World world) {
		drop(drop, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}
	
	/**
	 * Determine how to drop an item based on its {@link CropType} and the mod's {@link Configuration}
	 * @param type
	 * @param drop
	 * @param pos
	 * @param player
	 * @param world
	 */
	private static void dropBasedOnCropTypeAndConfig(CropType type, ItemStack drop, BlockPos pos, EntityPlayer player, World world) {
		switch (type) {
		case CROP:
			if (Configuration.cropPlayerDrop) drop(drop, player, world);
			else drop(drop, pos, world);			
			break;
		case FRUIT:
			if (Configuration.fruitPlayerDrop) drop(drop, player, world);
			else drop(drop, pos, world);
			break;
		case LOG:
			drop(drop, player, world);						
			break;
		}
	}
	
	/**
	 * Remove a seed from a list of ItemStacks by finding the seed stack programmatically
	 * @author TehNut, adapted by CerulanLumina<br /> <br />
	 * 		   MIT License  <br />
	 * 			 <br />
	 *         Copyright (c) 2017 TehNut  <br />
	 * 			 <br />
	 *         Permission is hereby granted, free of charge, to any person
	 *         obtaining a copy of this software and associated documentation
	 *         files (the "Software"), to deal in the Software without
	 *         restriction, including without limitation the rights to use,
	 *         copy, modify, merge, publish, distribute, sublicense, and/or sell
	 *         copies of the Software, and to permit persons to whom the
	 *         Software is furnished to do so, subject to the following
	 *         conditions:
	 * 			 <br /> <br />
	 *         The above copyright notice and this permission notice shall be
	 *         included in all copies or substantial portions of the Software.
	 * 			 <br /> <br />
	 *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
	 *         EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
	 *         OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	 *         NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
	 *         HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
	 *         WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
	 *         FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
	 *         OTHER DEALINGS IN THE SOFTWARE.
	 */
	private static boolean removeSeed(BlockStack worldBlock, List<ItemStack> drops) {
		boolean foundSeed = false;

        for (ItemStack stack : drops) {
            if (stack.isEmpty())
                continue;

            if (stack.getItem() instanceof IPlantable) {
                stack.shrink(1);
                foundSeed = true;
                break;
            }
        }

        boolean seedNotNull = true;
        if (worldBlock.getBlock() instanceof BlockCrops) {
            try {
                Item seed = (Item) Harvest._GET_SEED.invoke(worldBlock.getBlock());
                seedNotNull = seed != null && seed != Items.AIR;
            } catch (Exception e) {
                PamSimpleHarvest.logger.error("Failed to reflect BlockCrops: {}", e.getLocalizedMessage());
            }
        }
        
        return seedNotNull && foundSeed;

	}
	
	/**
	 * Return an {@link IReplantHandler} for a designated {@link CropType}
	 * @param type
	 * @return
	 */
	public static IReplantHandler getHandlerForType(CropType type) {
		return (world, pos, state, player, tileEntity) -> {
			BlockStack worldBlock = BlockStack.getStackFromPos(world, pos);
			BlockStack newBlock = Harvest.config.getCropMap().get(worldBlock).getFinalBlock();
			
			// deprecated, but Pam's Harvestcraft uses it...
			@SuppressWarnings("deprecation")
			List<ItemStack> drops = worldBlock.getBlock().getDrops(world, pos, state, 0);
			
			boolean cropDrop = true;
			if (type == CropType.CROP) {
				// If the CropType is a simple crop, a seed will need to be removed and only drop the item if it could be found
				cropDrop = removeSeed(worldBlock, drops);
			}
			
			if (!world.isRemote) {
				world.setBlockState(pos, newBlock.getState());
				for (ItemStack drop : drops) {
					if (cropDrop)
						dropBasedOnCropTypeAndConfig(type, drop, pos, player, world);					

				}
			}
		};
	}


}
