package com.elementfx.tvp.ad.block;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.BlockBed;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
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

    public BlockCustomBed()
    {
        super();
        this.setHardness(0.2F);
        this.setSoundType(SoundType.CLOTH);
    }
    
    // The item cannot be set in the constructor because the members of Items aren't initialized yet
    public void setItem(Item i) {
    	this.inventoryItem = i;
    }
    
    public Item getItem(Item i) {
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
}
