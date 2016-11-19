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
}
