package k4unl.minecraft.Hydraulicraft.api;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

public interface IHarvesterTrolley {

	/**
	 * Gets the name for this trolley. Used as an ID
	 * @return
	 */
	String getName();
	
	/**
	 * Whether or not this trolley can harvest the plant at this location.
	 * Use this to detect whether the metadata is sufficient for your plant.
	 * The Y co�rdinate is the bottom. It links to the location where the crop is located
	 * If you want something like sugar cane, just change the Y co�rdinate untill you reach the top 
	 * of the sugar cane.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	boolean canHarvest(IBlockAccess world, int x, int y, int z);
	
	/**
	 * Whether or not this trolley can plant a seed at this location.
	 * Not all seeds require the same soil. 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param seed
	 * @return
	 */
	boolean canPlant(IBlockAccess world, int x, int y, int z, ItemStack seed);
	
	/**
	 * Which seeds this trolley can handle. 
	 * @return
	 */
	ArrayList<ItemStack> getHandlingSeeds();
	
	/**
	 * What block gets planted into the soil from this seed?
	 * @param seed
	 * @return
	 */
	Block getBlockForSeed(ItemStack seed);

	/**
	 * Pass a reference to the trolley texture in here. Look at the original trolley textures to see how the map is layed out.
	 * @return
	 */
	ResourceLocation getTexture();
	

}
