package com.elementfx.tvp.ad.block;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCustomFlower extends BlockBush
{
    private AxisAlignedBB boundingBox;

    public BlockCustomFlower() {
    	this(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
    }
    
    public BlockCustomFlower(AxisAlignedBB bb)
    {
        this(Material.PLANTS, bb);
    }

    public BlockCustomFlower(Material materialIn, AxisAlignedBB bb)
    {
        super(materialIn);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.boundingBox = bb;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return this.boundingBox;
    }
    
    /** Helps create bounding boxes for blocks that use the cross block model */
    public static AxisAlignedBB generateCrossBoundingBox(int width, int height)
    {
    	return generateCrossBoundingBox(width, height, 32);
    }
    
    /** Helps create bounding boxes for blocks that use the cross block model */
    public static AxisAlignedBB generateCrossBoundingBox(int width, int height, int scale)
    {
    	// 0.9^2 + 0.9^2 = 1.62
    	double w2 = Math.sqrt(1.62) * width / (scale * 2 * Math.sqrt(2));
    	return new AxisAlignedBB(0.5 - w2, 0.0D, 0.5 - w2, 0.5 + w2, height / (double)scale, 0.5 + w2);
    }
}
