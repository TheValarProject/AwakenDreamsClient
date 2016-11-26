package com.elementfx.tvp.ad.block;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCustomCrops extends BlockCrops
{
	public PropertyInteger age;
	private int stages;
	private Item seedItem, cropItem;
	protected final BlockStateContainer customBlockState;

    public BlockCustomCrops(int totalStages)
    {
    	this.stages = totalStages - 1;
    	this.age = PropertyInteger.create("age", 0, this.stages);
    	this.customBlockState = this.createBlockState();
    	this.setDefaultState(this.customBlockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(0)));
        this.setTickRandomly(true);
        this.setCreativeTab((CreativeTabs)null);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.disableStats();
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0D, 0D, 0D, 1D, 1D, 1D);
    }

    public int getMaxAge()
    {
        return this.stages;
    }

    protected int getBonemealAgeIncrease(World worldIn)
    {
        return MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);
    }

    protected static float getGrowthChance(Block blockIn, World worldIn, BlockPos pos)
    {
        float f = 1.0F;
        BlockPos blockpos = pos.down();

        for (int i = -1; i <= 1; ++i)
        {
            for (int j = -1; j <= 1; ++j)
            {
                float f1 = 0.0F;
                IBlockState iblockstate = worldIn.getBlockState(blockpos.add(i, 0, j));

                if (iblockstate.getBlock() == Blocks.FARMLAND)
                {
                    f1 = 1.0F;

                    if (((Integer)iblockstate.getValue(BlockFarmland.MOISTURE)).intValue() > 0)
                    {
                        f1 = 3.0F;
                    }
                }

                if (i != 0 || j != 0)
                {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        BlockPos blockpos1 = pos.north();
        BlockPos blockpos2 = pos.south();
        BlockPos blockpos3 = pos.west();
        BlockPos blockpos4 = pos.east();
        boolean flag = blockIn == worldIn.getBlockState(blockpos3).getBlock() || blockIn == worldIn.getBlockState(blockpos4).getBlock();
        boolean flag1 = blockIn == worldIn.getBlockState(blockpos1).getBlock() || blockIn == worldIn.getBlockState(blockpos2).getBlock();

        if (flag && flag1)
        {
            f /= 2.0F;
        }
        else
        {
            boolean flag2 = blockIn == worldIn.getBlockState(blockpos3.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.south()).getBlock() || blockIn == worldIn.getBlockState(blockpos3.south()).getBlock();

            if (flag2)
            {
                f /= 2.0F;
            }
        }

        return f;
    }

    public void setSeed(Item seed) {
    	this.seedItem = seed;
    }
    
    protected Item getSeed()
    {
        return this.seedItem;
    }
    
    public void setCrop(Item crop) {
    	this.cropItem = crop;
    }

    protected Item getCrop()
    {
        return this.cropItem;
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);

        if (!worldIn.isRemote)
        {
            int i = this.getAge(state);

            if (i >= this.getMaxAge())
            {
                int j = 3 + fortune;

                for (int k = 0; k < j; ++k)
                {
                    if (worldIn.rand.nextInt(2 * this.getMaxAge()) <= i)
                    {
                        spawnAsEntity(worldIn, pos, new ItemStack(this.getSeed()));
                    }
                }
            }
        }
    }
    
    protected PropertyInteger getAgeProperty()
    {
    	if(this.age == null) {
    		return super.getAgeProperty();
    	}
        return this.age;
    }
    
    public BlockStateContainer getBlockState()
    {
    	if(this.customBlockState == null) {
    		return super.getBlockState();
    	}
        return this.customBlockState;
    }
    
    protected BlockStateContainer createBlockState()
    {
    	if(age == null) {
    		return super.createBlockState();
    	}
        return new BlockStateContainer(this, new IProperty[] {age});
    }
}
