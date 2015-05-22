package k4unl.minecraft.Hydraulicraft.blocks.handlers;

import java.util.List;

import k4unl.minecraft.Hydraulicraft.blocks.ITooltipProvider;
import k4unl.minecraft.Hydraulicraft.blocks.consumers.misc.BlockHydraulicFluidPump;
import k4unl.minecraft.Hydraulicraft.lib.Localization;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HandlerHydraulicBlock extends ItemBlock {
	private Block blockToHandle;
	
	
	public HandlerHydraulicBlock(Block _blockToHandle) {
		super(_blockToHandle);
		blockToHandle = _blockToHandle;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4){
		if(itemstack != null){
			Item theItem  = itemstack.getItem();
			Block btH = ((HandlerHydraulicBlock)theItem).blockToHandle;
			if(btH instanceof BlockHydraulicFluidPump) {
                list.add(EnumChatFormatting.RED + Localization.getString(Localization.MAXPRESSURE_LOW));
                list.add(EnumChatFormatting.RESET + Localization.getString(Localization.DESC_HYDRAULICFLUIDPUMP));
            }else if(btH instanceof ITooltipProvider){
                //list.add(EnumChatFormatting.RESET + )
			}else{
				list.add(EnumChatFormatting.GREEN + Localization.getString(Localization.MAXPRESSURE_HIGH));
			}
		}
	}
}
