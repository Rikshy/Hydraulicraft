package k4unl.minecraft.Hydraulicraft.multipart;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import k4unl.minecraft.Hydraulicraft.api.HydraulicBaseClassSupplier;
import k4unl.minecraft.Hydraulicraft.api.IBaseClass;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicMachine;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicTransporter;
import k4unl.minecraft.Hydraulicraft.api.PressureNetwork;
import k4unl.minecraft.Hydraulicraft.blocks.Blocks;
import k4unl.minecraft.Hydraulicraft.client.renderers.RendererPartValve;
import k4unl.minecraft.Hydraulicraft.lib.Functions;
import k4unl.minecraft.Hydraulicraft.lib.Log;
import k4unl.minecraft.Hydraulicraft.lib.config.Constants;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.lwjgl.opengl.GL11;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.EntityDigIconFX;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartValve extends TMultiPart implements TSlottedPart, JNormalOcclusion, IHollowConnect, IHydraulicTransporter {
	public static Cuboid6 boundingBox;
	public static Cuboid6[] boundingBoxes = new Cuboid6[6];
	public static Cuboid6 boundingBoxC;
    public static Cuboid6 boundingBoxNS;
    public static Cuboid6 boundingBoxEW;
    public static Cuboid6 boundingBoxUD;
    private static int expandBounds = -1;
    
    private PressureNetwork pNetwork;
    
    private IBaseClass baseHandler;
    private boolean needToCheckNeighbors;
    private boolean connectedSidesHaveChanged = true;
    private boolean hasCheckedSinceStartup;
    private boolean hasFoundNetwork = false;
    private ForgeDirection facing = ForgeDirection.NORTH;
    private boolean hasDirection = false;
    
    private int tier = 0;

    @SideOnly(Side.CLIENT)
    private static RendererPartValve renderer;
    
    @SideOnly(Side.CLIENT)
    private static Icon breakIcon;
    
    static {
    	float center = 0.5F;
        
        float width = 0.2F;
        float min = (center - width);
		float max = (center + width);
        boundingBoxNS = new Cuboid6(min, min, 0.0F, max, max, 1.0F);
        boundingBoxEW = new Cuboid6(0.0F, min, min, 1.0F, max, max);
        boundingBoxUD = new Cuboid6(min, 0.0F, min, max, 1.0F, max);
        boundingBoxC = new Cuboid6(min, min, min, max, max, max);
        boundingBox = boundingBoxNS;
        boundingBoxes[ForgeDirection.NORTH.ordinal()] = new Cuboid6(min, min, 0.0F, max, max, min);
        boundingBoxes[ForgeDirection.SOUTH.ordinal()] = new Cuboid6(min, min, max, max, max, 1.0F);
        
        boundingBoxes[ForgeDirection.EAST.ordinal()] = new Cuboid6(max, min, min, 1.0F, max, max);
        boundingBoxes[ForgeDirection.WEST.ordinal()] = new Cuboid6(0.0F, min, min, min, max, max);
        
        boundingBoxes[ForgeDirection.UP.ordinal()] = new Cuboid6(min, max, min, max, 1.0F, max);
        boundingBoxes[ForgeDirection.DOWN.ordinal()] = new Cuboid6(min, 0.0F, min, max, min, max);
    }
    
	@Override
	public String getType() {
		return "tile." + Names.partValve[0].unlocalized;
	}

	public void preparePlacement(int itemDamage) {
		tier = itemDamage;
	}
	
	@Override
	public void load(NBTTagCompound tagCompound){
		super.load(tagCompound);
		if(getHandler() != null)
			getHandler().readFromNBT(tagCompound);
		tier = tagCompound.getInteger("tier");
		facing = ForgeDirection.getOrientation(tagCompound.getInteger("facing"));
		hasDirection = tagCompound.getBoolean("hasDirection");
	}
	
	@Override
	public void save(NBTTagCompound tagCompound){
		super.save(tagCompound);
		getHandler().writeToNBT(tagCompound);
		tagCompound.setInteger("facing", getFacing().ordinal());
		tagCompound.setBoolean("hasDirection", hasDirection);
		tagCompound.setInteger("tier", tier);
	}
	
	@Override
    public void writeDesc(MCDataOutput packet){
		packet.writeInt(getTier());
		
		NBTTagCompound mainCompound = new NBTTagCompound();
		mainCompound.setInteger("facing", getFacing().ordinal());
		mainCompound.setBoolean("hasDirection", hasDirection);
		NBTTagCompound handlerCompound = new NBTTagCompound();
		if(connectedSidesHaveChanged && world() != null && !world().isRemote){
			connectedSidesHaveChanged = false;
			mainCompound.setBoolean("connectedSidesHaveChanged", true);
		}
		getHandler().writeToNBT(handlerCompound);
		mainCompound.setCompoundTag("handler", handlerCompound);
		
		packet.writeNBTTagCompound(mainCompound);
    }
	
    @Override
    public void readDesc(MCDataInput packet){
        tier = packet.readInt();
        
        NBTTagCompound mainCompound = packet.readNBTTagCompound();
        facing = ForgeDirection.getOrientation(mainCompound.getInteger("facing"));
        hasDirection = mainCompound.getBoolean("hasDirection");
		NBTTagCompound handlerCompound = mainCompound.getCompoundTag("handler");
		if(mainCompound.getBoolean("connectedSidesHaveChanged")){
			hasCheckedSinceStartup = false;
		}
        
        getHandler().readFromNBT(handlerCompound);
    }

	
	@Override
	public int getHollowSize() {
		return 6;
	}

	@Override
	public int getSlotMask() {
		return 0;
	}
	
	 @Override
    public Iterable<IndexedCuboid6> getSubParts() {
        Iterable<Cuboid6> boxList = getCollisionBoxes();
        LinkedList<IndexedCuboid6> partList = new LinkedList<IndexedCuboid6>();
        for (Cuboid6 c : boxList)
            partList.add(new IndexedCuboid6(0, c));
        return partList;
    }

    @Override
    public boolean occlusionTest(TMultiPart npart){
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes(){
    	LinkedList<Cuboid6> list = new LinkedList<Cuboid6>();
    	list.add(boundingBoxC);
    	list.add(boundingBoxes[getFacing().ordinal()]);
    	list.add(boundingBoxes[getFacing().getOpposite().ordinal()]);
    	return list;
    }
    
    public Iterable<Cuboid6> getBoundingBox(ForgeDirection dir){
    	return Arrays.asList(boundingBoxes[dir.ordinal()], boundingBoxes[dir.getOpposite().ordinal()]);
    }

    @Override
    public Iterable<Cuboid6> getCollisionBoxes(){
    	LinkedList<Cuboid6> list = new LinkedList<Cuboid6>();
    	list.add(boundingBoxC);
    	if(!getFacing().equals(ForgeDirection.UNKNOWN)){
	    	list.add(boundingBoxes[getFacing().ordinal()]);
	    	list.add(boundingBoxes[getFacing().getOpposite().ordinal()]);
    	}
    	return list;
    	/*
    	if(getFacing().equals(ForgeDirection.NORTH) || getFacing().equals(ForgeDirection.SOUTH)){
    		return Arrays.asList(boundingBoxNS);
    	}else if(getFacing().equals(ForgeDirection.EAST) || getFacing().equals(ForgeDirection.WEST)){
    		return Arrays.asList(boundingBoxEW);
    	}else if(getFacing().equals(ForgeDirection.UP) || getFacing().equals(ForgeDirection.DOWN)){
    		return Arrays.asList(boundingBoxUD);
    	}else{
    		return Arrays.asList(boundingBoxNS);
    	}*/
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 pos, float frame, int pass){
        if (pass == 0){
        	if(renderer == null){
        		renderer = new RendererPartValve();
        	}
        	GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);
			
            renderer.doRender(pos.x, pos.y, pos.z, frame, tier, facing, hasDirection);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    private boolean shouldConnectTo(TileEntity entity, ForgeDirection dir, Object caller){
    	int opposite = Functions.getIntDirFromDirection(dir.getOpposite());
    	if(entity instanceof TileMultipart){
    		List<TMultiPart> t = ((TileMultipart)entity).jPartList();
    		
    		/*
    		if(Multipart.hasPartHose((TileMultipart)entity)){
    			if(!((TileMultipart)entity).canAddPart(new NormallyOccludedPart(boundingBoxes[opposite]))) return false;
    		}*/
    		
    		for (TMultiPart p: t) {
    			if(p instanceof IHydraulicTransporter && caller.equals(this)){
    				((IHydraulicTransporter)p).checkConnectedSides(this);
    			}
				if(p instanceof IHydraulicMachine){
					return ((IHydraulicMachine)p).canConnectTo(dir.getOpposite());
				}
			}
    		return false;
    	}else{
    		if(entity instanceof IHydraulicMachine){
    			return ((IHydraulicMachine)entity).canConnectTo(dir.getOpposite());
    		}else{
    			return false;
    		}
    	}
    }

    public boolean isConnectedTo(ForgeDirection side){
    	int d = side.ordinal();
    	
    	if(world() != null && tile() != null){
	    	TileEntity te = world().getBlockTileEntity(x() + side.offsetX, y() + side.offsetY, z() + side.offsetZ);
	    	NormallyOccludedPart p = new NormallyOccludedPart(getBoundingBox(side));
	    	boolean canAddPart = tile().canAddPart(p);
	    	if(side.equals(getFacing()) || side.getOpposite().equals(getFacing())){
	    		canAddPart = true;
	    	}
	    	return canAddPart && shouldConnectTo(te, side, this);
    	}else{
    		return false;
    	}
    }
    
    public void checkConnectedSides(){
    	checkConnectedSides(this);
    }
    
    public void checkConnectedSides(Object caller){
    	if(hasDirection){
	    	if(isConnectedTo(getFacing()) || isConnectedTo(getFacing().getOpposite())){
	    		
	    	}else{
	    		NormallyOccludedPart p = new NormallyOccludedPart(getBoundingBox(ForgeDirection.NORTH));
		    	boolean canAddPart = tile().canAddPart(p);
		    	if(canAddPart){
		    		facing = ForgeDirection.NORTH;
		    	}
	    		hasDirection = false;
	    	}
    	}else{
    		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
    			if(isConnectedTo(dir)){
    				facing = dir;
    				hasDirection = true;
    				break;
    			}
    		}
    	}
    	/*
        connectedSides = new HashMap<ForgeDirection, TileEntity>();
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			int d = Functions.getIntDirFromDirection(dir);
			
            TileEntity te = world().getBlockTileEntity(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ);
            if(shouldConnectTo(te, dir, caller)) {
            	if(tile().canAddPart(new NormallyOccludedPart(boundingBox))){
            		connectedSides.put(dir, te);
            	}
            }
        }*/
		//connectedSidesHaveChanged = true;
		//getHandler().updateBlock();
    }
    
    @Override
	public boolean canConnectTo(ForgeDirection side) {
    	//Do some ray tracing here as well..
    	if(!hasDirection){
    		NormallyOccludedPart p = new NormallyOccludedPart(getBoundingBox(side));
    		if(tile().canAddPart(p)){
	    		return true;
    		}else{
    			return false;
    		}
    	}else if(getFacing().equals(side) || getFacing().equals(side.getOpposite())){
    		return true;
    	}else{
    		return false;
    	}
	}
    
    public ForgeDirection getFacing(){
    	return facing;
    }
    
    public void onNeighborChanged(){
        checkConnectedSides();
        if(!world().isRemote){
        	//getHandler().updateFluidOnNextTick();
        	/*float oldPressure = 0F;
            if(pNetwork != null){
            	oldPressure = pNetwork.getPressure();
            }*/
        	//getHandler().updateNetworkOnNextTick(oldPressure);
        }
    }
    
    public ItemStack getItem(){
        return new ItemStack(Multipart.itemPartValve, 1, tier);
    }
    
    @Override
    public void onPartChanged(TMultiPart part){
        checkConnectedSides();
        //getHandler().updateFluidOnNextTick();
        if(!world().isRemote){
	        float oldPressure = 0F;
	        if(pNetwork != null){
	        	oldPressure = pNetwork.getPressure();
	        	pNetwork.removeMachine(this);
	        }
			getHandler().updateNetworkOnNextTick(oldPressure);
        }
        
        
    }
    
    @Override
    public Iterable<ItemStack> getDrops() {
    
        LinkedList<ItemStack> items = new LinkedList<ItemStack>();
        items.add(getItem());
        return items;
    }
    
    @Override
    public ItemStack pickItem(MovingObjectPosition hit){
        return getItem();
    }

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void validate() {
		
	}

	@Override
	public void onPressureChanged(float old) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFluidLevelChanged(int old) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxStorage() {
		return FluidContainerRegistry.BUCKET_VOLUME * (2 * (getTier()+1));
	}

	public int getTier() {
		return tier;
	}
	
	@Override
	public void onBlockBreaks() {
	}

    @Override
    public float getMaxPressure(boolean isOil, ForgeDirection from){
        if(isOil) {
            switch(getTier()){
                case 0:
                    return Constants.MAX_MBAR_OIL_TIER_1;
                case 1:
                    return Constants.MAX_MBAR_OIL_TIER_2;
                case 2:
                    return Constants.MAX_MBAR_OIL_TIER_3;
            }
        } else {
            switch(getTier()){
                case 0:
                    return Constants.MAX_MBAR_WATER_TIER_1;
                case 1:
                    return Constants.MAX_MBAR_WATER_TIER_2;
                case 2:
                    return Constants.MAX_MBAR_WATER_TIER_3;
            }
        }
        return 0;
    }

	@Override
	public IBaseClass getHandler() {
		if(baseHandler == null) baseHandler = HydraulicBaseClassSupplier.getBaseClass(this);
        return baseHandler;
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		getHandler().onDataPacket(net, packet); 
	}

	@Override
	public Packet getDescriptionPacket() {
		return getHandler().getDescriptionPacket();
	}

	@Override
	public void readNBT(NBTTagCompound tagCompound) {
		//readConnectedSidesFromNBT(tagCompound);
	}

	@Override
	public void writeNBT(NBTTagCompound tagCompound) {
		//writeConnectedSidesToNBT(tagCompound);		
	}
	
    @Override
    public void update(){
    	if(getHandler() != null){
    		//This should never happen that this is null! :|
    		getHandler().updateEntity();
    	}else{
    		Log.error("PartValve does not have a handler!");
    	}
    	if(world() != null){
	    	if(world().getTotalWorldTime() % 10 == 0 && hasCheckedSinceStartup == false){
	    		checkConnectedSides();
	    		hasCheckedSinceStartup = true;
	    		//Hack hack hack
	    		//Temporary bug fix that we will forget about
	    	}
	    	//if(world().getTotalWorldTime() % 10 == 0 && pNetwork != null && !pNetwork.getMachines().contains(this.getHandler().getBlockLocation())){
	    		//Dum tie dum tie dum
	    		//If you see this, please step out of this if
	    		// *makes jedi hand motion* You never saw this!
	    		// TODO: figure out why the fuck this code is auto removing itself, without letting me know.
	    		// I Honestly believe it's because of FMP
	    		//getHandler().updateNetworkOnNextTick(pNetwork.getPressure());
	    		//pNetwork.addMachine(this, pNetwork.getPressure());
	    	//}
    	}
    	
        if(needToCheckNeighbors) {
            needToCheckNeighbors = false;
            
            if(!world().isRemote){
        		connectedSidesHaveChanged = true;
        		getHandler().updateBlock();
        	}
        }
    }

	@Override
	public void updateEntity() {
		// TODO Auto-generated method stub
		
	}
	
	

	@Override
	public PressureNetwork getNetwork(ForgeDirection side) {
		return pNetwork;
	}

	@Override
	public void setNetwork(ForgeDirection side, PressureNetwork toSet) {
		pNetwork = toSet;
	}

	@Override
	public void firstTick() {
				
	}

	@Override
	public float getPressure(ForgeDirection from) {
		if(world().isRemote){
			return getHandler().getPressure();
		}
		if(getNetwork(from) == null){
			Log.error("Valve at " + getHandler().getBlockLocation().printCoords() + " has no pressure network!");
			return 0;
		}
		return getNetwork(from).getPressure();
	}

	@Override
	public void setPressure(float newPressure, ForgeDirection side) {
		getNetwork(side).setPressure(newPressure);
	}

	@Override
	public void updateNetwork(float oldPressure) {
		PressureNetwork newNetwork = null;
		PressureNetwork foundNetwork = null;
		PressureNetwork endNetwork = null;
		//This block can merge networks!
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			if(!isConnectedTo(dir)){
				continue;
			}
			TileEntity ent = world().getBlockTileEntity(x() + dir.offsetX, y()+dir.offsetY, z()+ dir.offsetZ);
			if(ent == null) continue;
			if(!shouldConnectTo(ent, dir, this)) continue;
			foundNetwork = PressureNetwork.getNetworkInDir(world(), x(), y(), z(), dir);
			if(foundNetwork != null){
				if(endNetwork == null){
					endNetwork = foundNetwork;
				}else{
					newNetwork = foundNetwork;
				}
			}
			
			if(newNetwork != null && endNetwork != null){
				//Hmm.. More networks!? What's this!?
				//Log.info("Found an existing network (" + newNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
				endNetwork.mergeNetwork(newNetwork);
				newNetwork = null;
			}
			
		}
			
		if(endNetwork != null){
			pNetwork = endNetwork;
			pNetwork.addMachine(this, oldPressure, ForgeDirection.UP);
			//Log.info("Found an existing network (" + pNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
		}else{
			pNetwork = new PressureNetwork(this, oldPressure, ForgeDirection.UP);
			//Log.info("Created a new network (" + pNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
		}
		hasFoundNetwork = true;
	}
	
	@Override
	public void onRemoved(){
		if(!world().isRemote){
			if(pNetwork != null){
				pNetwork.removeMachine(this);
			}
		}
	}

	@Override
	public int getFluidInNetwork(ForgeDirection from) {
		if(world().isRemote){
			//TODO: Store this in a variable locally. Mostly important for pumps though.
			return 0;
		}else{
			return getNetwork(from).getFluidInNetwork();
		}
	}

	@Override
	public int getFluidCapacity(ForgeDirection from) {
		if(world().isRemote){
			//TODO: Store this in a variable locally. Mostly important for pumps though.
			return 0;
		}else{
			return getNetwork(from).getFluidCapacity();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void addDestroyEffects(MovingObjectPosition hit, EffectRenderer effectRenderer){
		addDestroyEffects(effectRenderer);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void addDestroyEffects(EffectRenderer effectRenderer) {
		if(breakIcon == null){
			breakIcon = Blocks.hydraulicPressureWall.getIcon(0, 0);
		}
        EntityDigIconFX.addBlockDestroyEffects(world(), Cuboid6.full.copy()
                .add(Vector3.fromTileEntity(tile())), new Icon[] { breakIcon,
                breakIcon, breakIcon, breakIcon, breakIcon, breakIcon },
                effectRenderer);
    }
	
	@SideOnly(Side.CLIENT)
    @Override
    public void addHitEffects(MovingObjectPosition hit,
            EffectRenderer effectRenderer) {
   
        EntityDigIconFX.addBlockHitEffects(world(),
                Cuboid6.full.copy().add(Vector3.fromTileEntity(tile())),
                hit.sideHit, breakIcon, effectRenderer);
    }
	
	@Override
	public float getStrength(MovingObjectPosition hit, EntityPlayer player){
		return 8F;
	}
	
}