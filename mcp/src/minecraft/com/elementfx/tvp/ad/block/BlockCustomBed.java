package com.elementfx.tvp.ad.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import com.elementfx.tvp.ad.item.ItemCustomBed;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCustomBed extends BlockBed
{
    private Item inventoryItem = null;
    /** Whether this bed is placed below another one */
    public static final PropertyBool BUNK = PropertyBool.create("bunk");
    public static final AxisAlignedBB BOTTOM_BUNK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public BlockCustomBed()
    {
        super();
        this.setHardness(0.2F);
        this.setSoundType(SoundType.CLOTH);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BUNK, Boolean.valueOf(false)));
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState stateIn, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        IBlockState state = this.getActualState(stateIn, worldIn, pos);
        boolean flag = heldItem != null;

        if (flag && heldItem.getItem() instanceof ItemCustomBed && !((Boolean)stateIn.getValue(BUNK)).booleanValue())
        {
            ItemCustomBed bed = (ItemCustomBed) heldItem.getItem();

            if (bed.onItemUse(heldItem, playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS)
            {
                return true;
            }
        }

        return super.onBlockActivated(worldIn, pos, stateIn, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    // The item cannot be set in the constructor because the members of Items aren't initialized yet
    public void setItem(Item i)
    {
        this.inventoryItem = i;
    }

    public Item getItem(Item i)
    {
        return this.inventoryItem;
    }

    @Nullable

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return state.getValue(PART) == BlockBed.EnumPartType.HEAD ? null : this.inventoryItem;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(this.inventoryItem);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        state = this.getActualState(state, source, pos);

        if (((Boolean)state.getValue(BUNK)).booleanValue())
        {
            return BOTTOM_BUNK_AABB;
        }

        return BED_AABB;
    }

    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        IBlockState iblockstateup = worldIn.getBlockState(pos.up());
        Block blockup = iblockstateup.getBlock();

        if (!(blockup instanceof BlockCustomBed && block instanceof BlockCustomBed))
        {
            return false;
        }

        Boolean isSameDirection = iblockstate.getValue(FACING) == iblockstateup.getValue(FACING);
        Boolean isOppositeDirection = iblockstate.getValue(FACING) == iblockstateup.getValue(FACING).getOpposite();
        Boolean isSamePart = iblockstate.getValue(PART) == iblockstateup.getValue(PART);
        return (isSameDirection && isSamePart) || (isOppositeDirection && !isSamePart);
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(BUNK, Boolean.valueOf(this.canConnectTo(worldIn, pos)));
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {BUNK, FACING, PART, OCCUPIED});
    }
}
