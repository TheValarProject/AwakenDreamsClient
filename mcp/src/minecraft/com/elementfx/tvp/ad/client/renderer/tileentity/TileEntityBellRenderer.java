package com.elementfx.tvp.ad.client.renderer.tileentity;

import com.elementfx.tvp.ad.client.model.ModelBell;
import com.elementfx.tvp.ad.tileentity.TileEntityBell;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TileEntityBellRenderer extends TileEntitySpecialRenderer<TileEntityBell>
{
    private static final ResourceLocation TEXTURE_BELL = new ResourceLocation("textures/blocks/bell.png");
    private final ModelBell modelBell = new ModelBell();

    public void renderTileEntityAt(TileEntityBell te, double x, double y, double z, float partialTicks, int destroyStage)
    {
    	if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
    	else
    	{
    		this.bindTexture(TEXTURE_BELL);
    	}
    	
    	GlStateManager.pushMatrix();
    	GlStateManager.translate((float)x + 0.5F, (float)y + 1F, (float)z + 0.5F);
    	GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
    	GlStateManager.rotate((float)EnumFacing.getHorizontal(te.getBlockMetadata() & 3).getHorizontalAngle() + 90, 0.0F, 1.0F, 0.0F);
    	GlStateManager.enableCull();
    	float f = (float)(Math.PI / 4 * (te.getAngle() + partialTicks * (te.getAngle() - te.getPrevAngle())));
    	float clapperOffset = (float) (Math.PI / 12 - 0.05);
    	if(Math.abs(f) > clapperOffset) {
    		this.modelBell.clapperRod.rotateAngleZ = (float) (f + Math.signum(f) * -1 * clapperOffset); 
    	}
    	else {
    		this.modelBell.clapperRod.rotateAngleZ = 0F;
    	}
    	this.modelBell.crown.rotateAngleZ = f;
    	this.modelBell.render((Entity)null, 0F, 0F, 0F, 0F, 0F, 0.075F);
    	GlStateManager.popMatrix();
    	
    	if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
