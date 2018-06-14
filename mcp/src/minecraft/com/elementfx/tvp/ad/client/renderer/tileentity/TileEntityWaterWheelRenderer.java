package com.elementfx.tvp.ad.client.renderer.tileentity;

import com.elementfx.tvp.ad.client.model.ModelWaterWheel;
import com.elementfx.tvp.ad.tileentity.TileEntityWaterWheel;
import com.elementfx.tvp.ad.util.ADResourceLocation;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TileEntityWaterWheelRenderer extends TileEntitySpecialRenderer<TileEntityWaterWheel>
{
    private static final ResourceLocation TEXTURE_WATER_WHEEL = new ADResourceLocation("textures/entity/water_wheel.png");
    private final ModelWaterWheel modelWaterWheel = new ModelWaterWheel();

    public void renderTileEntityAt(TileEntityWaterWheel te, double x, double y, double z, float partialTicks, int destroyStage)
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
            this.bindTexture(TEXTURE_WATER_WHEEL);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y + 1F, (float)z + 0.5F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate((float)EnumFacing.getHorizontal(te.getBlockMetadata() & 3).getHorizontalAngle() + 90, 0.0F, 1.0F, 0.0F);
        GlStateManager.enableCull();
        this.modelWaterWheel.Axel.rotateAngleZ = te.angle;
        this.modelWaterWheel.render((Entity)null, 0F, 0F, 0F, 0F, 0F, 0.05F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
