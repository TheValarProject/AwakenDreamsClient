package com.elementfx.tvp.ad.client.gui.inventory;

import com.elementfx.tvp.ad.inventory.ContainerCustomWorkbench;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiCustomCrafting extends GuiContainer
{
	private static ResourceLocation CUSTOM_CRAFTING_TABLE_GUI_TEXTURES;
	private static String typeIdentifier;
	private static int fontColor;
    
	protected int xSize = 204;
    protected int ySize = 194;
    
    public GuiCustomCrafting(InventoryPlayer playerInv, World worldIn, String typeIdentifierIn, int fontColorIn, int metaIn)
    {
    	this(playerInv, worldIn, BlockPos.ORIGIN, typeIdentifierIn, fontColorIn, metaIn);
    }
    
    public GuiCustomCrafting(InventoryPlayer playerInv, World worldIn, BlockPos blockPosition, String typeIdentifierIn, int fontColorIn, int metaIn)
    {
    	super(new ContainerCustomWorkbench(playerInv, worldIn, blockPosition, metaIn));
    	this.typeIdentifier = typeIdentifierIn;
    	this.fontColor = fontColorIn;
    	CUSTOM_CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/" + typeIdentifier + "_crafting_table.png");
    		
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("container." + typeIdentifier + "Crafting", new Object[0]), 20, 6, fontColor);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 120, fontColor);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CUSTOM_CRAFTING_TABLE_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
