package com.elementfx.tvp.ad.block;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.tileentity.TileEntityBell;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBell extends BlockContainer
{
    protected BlockBell()
    {
        super(Material.IRON);
        this.setLightOpacity(0);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }
    
    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBell();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	this.ring(worldIn, pos);
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }
    
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        this.ring(worldIn, pos);
        super.onBlockClicked(worldIn, pos, playerIn);
    }
    
    public void ring(World worldIn, BlockPos pos)
    {
    	if (worldIn.isRemote)
        {
            return;
        }
        else
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityBell)
            {
            	TileEntityBell tileentitybell = (TileEntityBell)tileentity;
                tileentitybell.ring();
            }
        }
    }
    
    public static void playSound(World worldIn, BlockPos pos)
    {
    	// TODO add soundevent
    	worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 3.0F, 1.0F);
    }
}
