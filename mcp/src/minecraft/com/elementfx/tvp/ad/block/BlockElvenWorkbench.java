package com.elementfx.tvp.ad.block;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.inventory.ContainerElvenWorkbench;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class BlockElvenWorkbench extends BlockWorkbench
{
	public BlockElvenWorkbench()
    {
        super();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            playerIn.displayGui(new BlockElvenWorkbench.InterfaceElvenCraftingTable(worldIn, pos));
            return true;
        }
    }
    
    public boolean isOpaqueCube(IBlockState state) 
    {
    	return false;
    }
    
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
    
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    public static class InterfaceElvenCraftingTable extends InterfaceCraftingTable
    {
        private final World world;
        private final BlockPos position;

        public InterfaceElvenCraftingTable(World worldIn, BlockPos pos)
        {
        	super(worldIn, pos);
            this.world = worldIn;
            this.position = pos;
        }

        public ITextComponent getDisplayName()
        {
            return new TextComponentTranslation(Blocks.ELVEN_CRAFTING_TABLE.getUnlocalizedName() + ".name", new Object[0]);
        }
        
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
        {
            return new ContainerElvenWorkbench(playerInventory, this.world, this.position);
        }

        public String getGuiID()
        {
            return "minecraft:elven_crafting_table";
        }
    }
}
