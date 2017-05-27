package com.elementfx.tvp.ad.tileentity;

import java.util.Set;

import com.elementfx.tvp.ad.block.BlockBell;
import com.elementfx.tvp.ad.block.BlockWaterWheel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityWaterWheel extends TileEntity implements ITickable
{
    public float spinDirection = 0;
    public float angle = 0;
	
	/**
     * Like the old updateEntity(), except more generic.
     */
	public void update()
	{
		this.spinDirection = spinDirection(this.worldObj, this.pos, this.worldObj.getBlockState(this.pos));
		this.angle += 0.075 * this.spinDirection;
		//System.out.println(this.pos + " - " + this.spinDirection);
	}
	
	private float spinDirection(World worldIn, BlockPos pos, IBlockState state) {
    	int positiveFlow = 0, negativeFlow = 0;
    	EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue(BlockWaterWheel.FACING).rotateY();
    	
    	// Handle flow beside
    	BlockPos centerPos = pos.offset(enumfacing);
    	for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL.facings())
        {
        	if(facing == enumfacing || facing == enumfacing.getOpposite())
        	{
        		continue;
        	}
        	
        	Block curState = this.worldObj.getBlockState(centerPos.offset(facing)).getBlock();
        	if(curState == Blocks.WATER || curState == Blocks.FLOWING_WATER) {
        		if(facing.getAxisDirection().getOffset() < 0) {
        			negativeFlow += 16;
        		}
        		else {
        			positiveFlow += 16;
        		}
        	}
        }
    	
    	// Handle flow under
    	
        BlockPos lowerCenterPos = centerPos.offset(EnumFacing.DOWN);
        
    	Axis flowAxis = enumfacing.getAxis() == Axis.X ? Axis.Z : Axis.X;
    	IBlockState[] states = new IBlockState[5];
    	
    	EnumFacing negFace = EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, flowAxis), posFace = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, flowAxis);
    	states[0] = worldIn.getBlockState(lowerCenterPos.offset(negFace, 2));
    	states[1] = worldIn.getBlockState(lowerCenterPos.offset(negFace));
    	states[2] = worldIn.getBlockState(lowerCenterPos);
    	states[3] = worldIn.getBlockState(lowerCenterPos.offset(posFace));
    	states[4] = worldIn.getBlockState(lowerCenterPos.offset(posFace, 2));
    	
    	/*System.out.println("----");
    	for(IBlockState debugState : states) {
    		if(debugState.getBlock() == Blocks.FLOWING_WATER || debugState.getBlock() == Blocks.WATER) {
    			System.out.println("debugState level: " + debugState.getValue(BlockLiquid.LEVEL));
    		}
    	}*/
    	
    	for(int i = 1; i <= 3; i++) {
    		if(states[i].getBlock() != Blocks.FLOWING_WATER && states[i].getBlock() != Blocks.WATER) {
    			continue;
    		}
    		if(states[i-1].getBlock() == Blocks.FLOWING_WATER || states[i-1].getBlock() == Blocks.WATER) {
    			int curLevel = states[i].getValue(BlockLiquid.LEVEL);
    			int sideLevel = states[i-1].getValue(BlockLiquid.LEVEL);
    			if(sideLevel > curLevel) {
    				positiveFlow += sideLevel - curLevel;
    			}
    			else {
    				negativeFlow += curLevel - sideLevel;
    			}
    		}
    		if(states[i+1].getBlock() == Blocks.FLOWING_WATER || states[i+1].getBlock() == Blocks.WATER) {
    			int curLevel = states[i].getValue(BlockLiquid.LEVEL);
    			int sideLevel = states[i+1].getValue(BlockLiquid.LEVEL);
    			if(sideLevel > curLevel) {
    				negativeFlow += sideLevel - curLevel;
    			}
    			else {
    				positiveFlow += curLevel - sideLevel;
    			}
    		}
        }
        
    	return Math.min(24, positiveFlow - negativeFlow) / 24F * (enumfacing == EnumFacing.SOUTH || enumfacing == EnumFacing.WEST ? -1 : 1);
    }
}
