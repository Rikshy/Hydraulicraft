package k4unl.minecraft.Hydraulicraft.tileEntities.storage;

import k4unl.minecraft.Hydraulicraft.tileEntities.TileHydraulicBaseNoPower;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.*;

import javax.annotation.Nullable;

/**
 * @author Koen Beckers (K-4U)
 */
public class TileFluidTank extends TileHydraulicBaseNoPower implements IFluidHandler, ITickable {

    private boolean hasUpdated = false;
    private FluidTank fluidTank;

    public TileFluidTank() {

        this.fluidTank = new FluidTank(16 * FluidContainerRegistry.BUCKET_VOLUME);
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {

        hasUpdated = true;
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {

        hasUpdated = true;
        return fluidTank.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {

        hasUpdated = true;
        return fluidTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {

        return from.equals(EnumFacing.DOWN) || from.equals(EnumFacing.UP);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {

        return from.equals(EnumFacing.DOWN) || from.equals(EnumFacing.UP);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {

        return new FluidTankInfo[]{fluidTank.getInfo()};
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {

        NBTTagCompound tagCompound = packet.getNbtCompound();
        readFromNBT(tagCompound);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {

        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound);
        return new SPacketUpdateTileEntity(getPos(), 4, tagCompound);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        compound = super.writeToNBT(compound);
        fluidTank.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        fluidTank = fluidTank.readFromNBT(compound);

    }

    @Override
    public void update() {

        if (hasUpdated && worldObj.getTotalWorldTime() % 20 == 0) {
            hasUpdated = false;
            markDirty();
            markBlockForUpdate();
        }
    }
}
