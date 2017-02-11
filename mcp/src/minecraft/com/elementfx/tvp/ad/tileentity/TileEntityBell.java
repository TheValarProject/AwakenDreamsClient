package com.elementfx.tvp.ad.tileentity;

import com.elementfx.tvp.ad.block.BlockBell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

public class TileEntityBell extends TileEntity implements ITickable
{
	/** The current index angle (between -14 and 14 inclusive) */
    public int angleIndex;
    /** 0 if not moving, 1 if moving in the positive direction, -1 if moving in the negative direction */
    public float angleDirection;
    /** true if the bell should continue moving once it has reach the bottom of its motion */
    public boolean continueMoving;
    /** Angle lookup table
     * Values generated from the formula sin((i-14)*pi/28)
     * (see update for usage details)
     */
    final public float[] angleLookup = { -1, -.99371f, -.97493f, -.94388f, -.90097f, -.84672f, -.78183f, -.70711f, -.62349f, -.53203f, -.43388f, -.33028f, -.22252f, -.11196f, 0f, -.11196f, -.22252f, -.33028f, -.43388f, -.53203f, -.62349f, -.70711f, -.78183f, -.84672f, -.90097f, -.94388f, -.97493f, -.99371f, -1 };
    
    /** The direction of the bell (determined when placed) */
    public EnumFacing direction;
    
    public void ring()
    {
    	this.continueMoving = true;
    	// If the bell is not moving yet
    	if(this.angleDirection == 0)
    	{
    		this.angleDirection = 1;
    	}
    }
    
    public float getAngle()
    {
    	return this.angleLookup[this.angleIndex];
    }
	
	/**
     * Like the old updateEntity(), except more generic.
     */
	public void update()
	{
		// Bell is moving
		if(this.angleDirection != 0)
		{
			
			this.angleIndex += this.angleDirection;
			
			// If bell is at the bottom of its swing
			if(this.angleIndex == 0)
			{
				if(this.continueMoving)
				{
					this.continueMoving = false;
				}
				else
				{
					// Stop
					this.angleDirection = 0;
				}
			}
			
			// If bell is at the top of its swing
			if(this.angleIndex <= -14 || this.angleIndex >= 14)
			{
				// Reverse the direction
				this.angleDirection *= -1;
				
				// Play sound
				BlockBell.playSound(this.worldObj, this.pos);
			}
		}
	}
}
