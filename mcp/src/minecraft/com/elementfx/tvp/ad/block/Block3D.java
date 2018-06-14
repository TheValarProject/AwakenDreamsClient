package com.elementfx.tvp.ad.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Block3D extends Block
{
    private List<AxisAlignedBB> collisionBoxes;
    AxisAlignedBB boundingBox;

    public Block3D(Material materialIn)
    {
        this(materialIn, Arrays.asList(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)));
    }

    public Block3D(Material materialIn, List<AxisAlignedBB> collisionBoxes)
    {
        super(materialIn);
        this.collisionBoxes = collisionBoxes;
        double minX = 0.5, minY = 0.5, minZ = 0.5, maxX = 0.5, maxY = 0.5, maxZ = 0.5;

        for (AxisAlignedBB box : collisionBoxes)
        {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }

        boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
    {
        for (AxisAlignedBB box : collisionBoxes)
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
        }
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return this.boundingBox;
    }
}
