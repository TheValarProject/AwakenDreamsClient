package com.elementfx.tvp.ad.client.renderer.tileentity;

import com.elementfx.tvp.ad.tileentity.TileEntityBell;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.ResourceLocation;

public class TileEntityBellRenderer extends TileEntitySpecialRenderer<TileEntityBell>
{
	// TODO these:
	/** The texture for the book above the enchantment table. */
    //private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("textures/entity/enchanting_table_book.png");
    //private final ModelBook modelBook = new ModelBook();

    public void renderTileEntityAt(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage)
    {
    	GlStateManager.pushMatrix();
    	// TODO render stuff
    	GlStateManager.popMatrix();
    }
}
