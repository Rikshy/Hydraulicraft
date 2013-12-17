package pet.minecraft.Hydraulicraft.TileEntities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pet.minecraft.Hydraulicraft.baseClasses.entities.TileConsumer;
import pet.minecraft.Hydraulicraft.fluids.Fluids;
import pet.minecraft.Hydraulicraft.lib.Log;
import pet.minecraft.Hydraulicraft.lib.config.Constants;
import pet.minecraft.Hydraulicraft.lib.config.Names;

public class TileHydraulicMixer extends TileConsumer implements
		ISidedInventory, IFluidHandler {

	private ItemStack inputInventory;
	//private ItemStack outputInventory;
	
	private boolean isWorking = false;
	private int maxTicks = 500;
	private int ticksDone = 0;
	private float requiredPressure = 5.0F;
	
	private FluidTank inputTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 16);
	private FluidTank outputTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 8);
	
	
	public TileHydraulicMixer(){
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		
		NBTTagCompound inventoryCompound = tagCompound.getCompoundTag("inputInventory");
		inputInventory = ItemStack.loadItemStackFromNBT(inventoryCompound);
		
		inventoryCompound = tagCompound.getCompoundTag("outputInventory");
//		outputInventory = ItemStack.loadItemStackFromNBT(inventoryCompound);
		
		inputTank.readFromNBT(tagCompound.getCompoundTag("inputTank"));
		outputTank.readFromNBT(tagCompound.getCompoundTag("outputTank"));
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		
		if(inputInventory != null){
			NBTTagCompound inventoryCompound = new NBTTagCompound();
			inputInventory.writeToNBT(inventoryCompound);
			tagCompound.setCompoundTag("inputInventory", inventoryCompound);
		}
		/*if(outputInventory != null){
			NBTTagCompound inventoryCompound = new NBTTagCompound();
			outputInventory.writeToNBT(inventoryCompound);
			tagCompound.setCompoundTag("outputInventory", inventoryCompound);
		}*/
		
		NBTTagCompound tankCompound = new NBTTagCompound();
		inputTank.writeToNBT(tankCompound);
		tagCompound.setCompoundTag("inputTank", tankCompound);
		
		tankCompound = new NBTTagCompound();
		outputTank.writeToNBT(tankCompound);
		tagCompound.setCompoundTag("outputTank", tankCompound);
	}

	/*!
	 * Checks if the outputslot is free, if there's enough pressure in the system
	 * and if the item is smeltable
	 */
	private boolean canRun(){
		if(inputInventory == null || (getPressure() < requiredPressure)){
			return false;
		}else{
			if(outputTank.getFluidAmount() + Constants.OIL_FOR_ONE_SEED < outputTank.getCapacity()){
				if(inputInventory.itemID == Item.seeds.itemID){
					if(inputTank.getFluid().isFluidEqual(new FluidStack(FluidRegistry.WATER.getID(),0)) && inputTank.getFluidAmount() > Constants.WATER_FOR_ONE_SEED){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public float workFunction(boolean simulate) {
		if(canRun() || isWorking){
			if(!simulate){
				doConvert();
			}
			//The higher the pressure
			//The higher the speed!
			//But also the more it uses..
			return 5F + (getPressure() * 0.00005F);
		}else{
			return 0F;
		}
	}
	
	public void doConvert(){
		if(isWorking){
			ticksDone = ticksDone + 1 + (int)((getPressure()/100) * 0.00005F);
			Log.info(ticksDone+ "");
			if(ticksDone >= maxTicks){
				if(outputTank.getFluidAmount() <= 0){
					outputTank.setFluid(new FluidStack(Fluids.fluidOil, Constants.OIL_FOR_ONE_SEED));
				}else{
					outputTank.getFluid().amount+=Constants.OIL_FOR_ONE_SEED;
				}
				isWorking = false;
			}
		}else{
			if(canRun()){
				inputInventory.stackSize--;
				if(inputInventory.stackSize <= 0){
					inputInventory = null;
				}
				
				inputTank.drain(Constants.WATER_FOR_ONE_SEED, true);
				
				ticksDone = 0;
				isWorking = true;
			}
		}
	}

	@Override
	public int getMaxBar() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int i) {
		switch(i){
		case 0:
			return inputInventory;
		default:
			return null;
			
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack inventory = getStackInSlot(i);
		
		ItemStack ret = null;
		if(inventory.stackSize < j){
			ret = inventory;
			inventory = null;
			
		}else{
			ret = inventory.splitStack(j);
			if(inventory.stackSize == 0){
				inventory = null;
			}
		}
		
		return ret;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		ItemStack stack = getStackInSlot(i);
		if(stack != null){
			setInventorySlotContents(i, null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		if(i == 0){
			inputInventory = itemStack;
		}else{
			//Err...
			
		}
	}

	@Override
	public String getInvName() {
		// TODO Localization
		return Names.blockHydraulicMixer.localized;
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO Localization
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return ((worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this) && 
				player.getDistanceSq(xCoord, yCoord, zCoord) < 64);
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0){
			if(itemStack.itemID == Item.seeds.itemID){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[] {1, 0};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		if(i == 0 && itemStack.itemID == Item.seeds.itemID){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return false;
	}

	@Override
	public int getStorage() {
		// TODO Auto-generated method stub
		return FluidContainerRegistry.BUCKET_VOLUME * 6;
	}


	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		int filled = inputTank.fill(resource, doFill); 
		if(doFill && filled > 10){
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		FluidStack drained = outputTank.drain(maxDrain, doDrain); 
		if(doDrain && drained.amount > 0){
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		return drained;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(fluid.equals(FluidRegistry.WATER)){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(fluid.equals(Fluids.fluidOil)){
			return true;			
		}else{
			return false;
		}
		
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] tankInfo = {new FluidTankInfo(inputTank), new FluidTankInfo(outputTank)};
		return tankInfo;
	}


}
