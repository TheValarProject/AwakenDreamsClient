package com.elementfx.tvp.ad.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityCustomArrow extends EntityTippedArrow
{    
    public EntityCustomArrow(World worldIn)
    {
        super(worldIn);
    }

    public EntityCustomArrow(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public EntityCustomArrow(World worldIn, EntityLivingBase shooter)
    {
        super(worldIn, shooter);
    }
    
    public EntityCustomArrow(World worldIn, EntityLivingBase shooter, boolean isFlamingIn) 
    {
		this(worldIn, shooter);
		if(isFlamingIn)
		{
			this.setFire(180);	
		}
	}
}
