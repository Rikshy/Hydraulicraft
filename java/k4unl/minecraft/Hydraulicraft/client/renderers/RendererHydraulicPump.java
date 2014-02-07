package k4unl.minecraft.Hydraulicraft.client.renderers;

import k4unl.minecraft.Hydraulicraft.TileEntities.generator.TileHydraulicPump;
import k4unl.minecraft.Hydraulicraft.blocks.Blocks;
import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;
import k4unl.minecraft.Hydraulicraft.lib.helperClasses.Vector3fMax;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class RendererHydraulicPump extends TileEntitySpecialRenderer {
	private static final ResourceLocation resLoc =
			new ResourceLocation(ModInfo.LID,"textures/model/hydraulicPump.png");
	

	private RenderBlocks renderer;
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {
		TileHydraulicPump t = (TileHydraulicPump)tileentity;
		//Get metadata for rotation:
		int rotation = 0;//t.getDir();
		int metadata = t.getBlockMetadata();
		
		renderer = new RenderBlocks(tileentity.worldObj);
		
		doRender(t, (float)x, (float)y, (float)z, f, rotation, metadata);
	}
	
	public void itemRender(float x, float y,
			float z, float f, int tier){
		
		renderer = new RenderBlocks();
		GL11.glPushMatrix();
		
		GL11.glTranslatef(x, y, z);
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(resLoc);
		
		GL11.glPushMatrix();
		//GL11.glDisable(GL11.GL_TEXTURE_2D); //Do not use textures
		GL11.glDisable(GL11.GL_LIGHTING); //Disregard lighting
		GL11.glColor3f(0.9F, 0.9F, 0.9F);
		//Do rendering
        
		float thickness = 0.06F;
		renderTieredBars(tier, thickness);
		
		renderInsidesWithoutLighting(thickness);
		
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING); 
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
	
	public void doRender(TileHydraulicPump t, float x, float y,
			float z, float f, int rotation, int metadata){
		GL11.glPushMatrix();
		
		
		
		GL11.glTranslatef(x, y, z);
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(resLoc);
		
		GL11.glPushMatrix();
		//GL11.glDisable(GL11.GL_TEXTURE_2D); //Do not use textures
		GL11.glDisable(GL11.GL_LIGHTING); //Disregard lighting
		//Do rendering
		GL11.glColor3f(0.9F, 0.9F, 0.9F);
		float thickness = 0.06F;
		renderTieredBars(t.getTier(), thickness);
		renderInsides(thickness, t);
		
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING); 
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
	
	private void renderInsidesWithoutLighting(float thickness){
		thickness -= 0.025F;
		GL11.glBegin(GL11.GL_QUADS);
		Vector3fMax insides = new Vector3fMax(thickness, thickness, thickness, 1.0F-thickness, 1.0F-thickness, 1.0F-thickness);
		RenderHelper.drawTexturedCube(insides);
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glBegin(GL11.GL_QUADS);
		Vector3fMax vector = new Vector3fMax(thickness + 0.1F, 0.0F, thickness+0.1F, 1.0F-thickness-0.1F, 1.01F-thickness, thickness+0.1F+0.2F);
		
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), 215F/256F, 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), 215F/256F, 0.39F);		
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), 189F/256F, 0.39F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), 189F/256F, 0.0F);
		
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void renderInsides(float thickness, TileHydraulicPump t){
		thickness -= 0.025F;
		GL11.glBegin(GL11.GL_QUADS);
		Vector3fMax insides = new Vector3fMax(thickness, thickness, thickness, 1.0F-thickness, 1.0F-thickness, 1.0F-thickness);	
		RenderHelper.drawTexturedCubeWithLight(insides, (TileEntity)t);
		
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glBegin(GL11.GL_QUADS);
		Vector3fMax vector = new Vector3fMax(thickness + 0.1F, 0.0F, thickness+0.1F, 1.0F-thickness-0.1F, 1.01F-thickness, thickness+0.1F+0.2F);
		
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), 215F/256F, 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), 215F/256F, 0.39F);		
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), 189F/256F, 0.39F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), 189F/256F, 0.0F);
		
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
		
	}
	
	private void renderTieredBars(int tier, float thickness){
		Vector3fMax ln = new Vector3fMax(thickness, 0.0F, 0.0F, 1.0F-thickness, thickness, thickness);
		Vector3fMax tn = new Vector3fMax(thickness, 1.0F-thickness, 0.0F, 1.0F-thickness, 1.0F, thickness);
		Vector3fMax ne = new Vector3fMax(1.0F-thickness, 0.0F, 0.0F, 1.0F, 1.0F, thickness);
		
		for(int i = 0; i<4; i++){
			drawTieredHorizontalCube(ln, tier, thickness);
			drawTieredHorizontalCube(tn, tier, thickness);
			drawTieredVerticalCube(ne, tier, thickness);
			
			GL11.glTranslatef(1.0F, 0.0F, 0.0F);
			GL11.glRotatef(90.0F, 0.0F, -1.0F, 0.0F);
		}
	}
	
	private void drawTieredHorizontalCube(Vector3fMax vector, int tier, float thickness){
		GL11.glBegin(GL11.GL_QUADS);
		//RenderHelper.drawColoredCube(vector);
		float tXb[] = {128.0F/256.0F, 148.0F/256.0F, 168.0F/256.0F};
		float tXe[] = {148.0F/256.0F, 168.0F/256.0F, 188.0F/256.0F};
		tXe[tier] = tXb[tier] + (thickness/2);
		
		//Top side:
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXe[tier], 1.0F);		
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXb[tier], 0.0F);
		
		//Bottom side:
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXe[tier], 1.0F);		
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXb[tier], 0.0F);

		//Draw west side:
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXe[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXb[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXe[tier], 0.0F);
		
		//Draw east side:
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXe[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXb[tier], 0.0F);
		
		//Draw north side
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXe[tier], 1.0F); 
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXb[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXe[tier], 0.0F);

		//Draw south side
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXe[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXb[tier], 0.0F);
		GL11.glEnd();
	}
	
	private void drawTieredVerticalCube(Vector3fMax vector, int tier, float thickness){
		GL11.glBegin(GL11.GL_QUADS);
		//RenderHelper.drawColoredCube(vector);
		float tXb[] = {128.0F/256.0F, 148.0F/256.0F, 168.0F/256.0F};
		float tXe[] = {148.0F/256.0F, 168.0F/256.0F, 188.0F/256.0F};
		tXe[tier] = tXb[tier] + (thickness/2);
		
		//Top side:
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXe[tier], (thickness/2));		
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXb[tier], (thickness/2));
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXb[tier], 0.0F);
		
		//Bottom side:
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXe[tier], (thickness/2));		
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXb[tier], (thickness/2));
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXb[tier], 0.0F);

		//Draw west side:
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXb[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXe[tier], 1.0F);
		
		//Draw east side:
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXe[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXb[tier], 0.0F);
		
		//Draw north side
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMin(), tXb[tier], 1.0F); 
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMin(), tXb[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMin(), tXe[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMin(), tXe[tier], 1.0F);

		//Draw south side
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMin(), vector.getZMax(), tXe[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMin(), vector.getZMax(), tXb[tier], 1.0F);
		RenderHelper.vertexWithTexture(vector.getXMax(), vector.getYMax(), vector.getZMax(), tXb[tier], 0.0F);
		RenderHelper.vertexWithTexture(vector.getXMin(), vector.getYMax(), vector.getZMax(), tXe[tier], 0.0F);
		GL11.glEnd();
	}

}