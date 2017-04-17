package com.elementfx.tvp.ad.tileentity;

import com.elementfx.tvp.ad.block.BlockBell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

public class TileEntityWaterWheel extends TileEntity implements ITickable
{
	/** The direction of the bell (determined when placed) */
    public EnumFacing direction;
	
	/**
     * Like the old updateEntity(), except more generic.
     */
    // TODO
	public void update()
	{
		/*this.prevAngleIndex = this.angleIndex;
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
		}*/
	}
}
