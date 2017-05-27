package com.elementfx.tvp.ad.block;

import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.tileentity.TileEntityBell;
import com.elementfx.tvp.ad.tileentity.TileEntityWaterWheel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWaterWheel extends BlockContainer
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
    public BlockWaterWheel()
    {
        super(Material.WOOD, MapColor.WOOD);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        // TODO
        this.setHardness(2.5F);
        this.setResistance(10.0F);
        this.setTickRandomly(true);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }
    
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }
    
    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityWaterWheel();
    }
    
    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
    	EnumFacing enumfacing = placer.getHorizontalFacing().rotateY();
    	worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    	return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, enumfacing);
    }
    
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        this.func_189540_a(state, worldIn, pos, state.getBlock());
    }
    
    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn)
    {
        return 10;
    }
    
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    	this.func_189540_a(state, worldIn, pos, state.getBlock());
    	worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }
    
    public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_)
    {
        if (!this.canBlockStay(p_189540_2_, p_189540_3_, p_189540_1_))
        {
            p_189540_2_.destroyBlock(p_189540_3_, true);
        }
    }
    
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state)
    {
    	EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue(FACING).rotateY();
        BlockPos centerPos = pos.offset(enumfacing);
        
        Block curBlock = worldIn.getBlockState(centerPos).getBlock();
        if(curBlock != Blocks.AIR && curBlock != Blocks.WATER && curBlock != Blocks.FLOWING_WATER)
        {
        	return false;
        }
        
        
        for (EnumFacing facing : EnumFacing.values())
        {
        	if(facing == enumfacing || facing == enumfacing.getOpposite())
        	{
        		continue;
        	}
        	curBlock = worldIn.getBlockState(centerPos.offset(facing)).getBlock();
        	if(curBlock != Blocks.AIR && curBlock != Blocks.WATER && curBlock != Blocks.FLOWING_WATER)
            {
            	return false;
            }
        	if(facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
        		curBlock = worldIn.getBlockState(centerPos.offset(facing).offset(EnumFacing.UP)).getBlock();
        		if(curBlock != Blocks.AIR && curBlock != Blocks.WATER && curBlock != Blocks.FLOWING_WATER)
                {
                	return false;
                }
        		curBlock = worldIn.getBlockState(centerPos.offset(facing).offset(EnumFacing.DOWN)).getBlock();
        		if(curBlock != Blocks.AIR && curBlock != Blocks.WATER && curBlock != Blocks.FLOWING_WATER)
                {
                	return false;
                }
        	}
        }
        
        return true;
    }
    
    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
    }
    
    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }
    
    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }
    
    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }
    
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
    	System.out.println("getWeakPower " + ((TileEntityWaterWheel)blockAccess.getTileEntity(pos)).spinDirection * 15);
        return (int)Math.min(15, (Math.abs(((TileEntityWaterWheel)blockAccess.getTileEntity(pos)).spinDirection) * 23));
    }
    
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
    	//System.out.println("getStrongPower");
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }
}
