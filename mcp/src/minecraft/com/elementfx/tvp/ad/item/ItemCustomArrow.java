package com.elementfx.tvp.ad.item;

import com.elementfx.tvp.ad.entity.projectile.EntityCustomArrow;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCustomArrow extends ItemArrow
{
    /**Hold  whether the arrow is flaming or not*/
    public boolean isFlaming;

    public ItemCustomArrow()
    {
        this(false);
    }

    public ItemCustomArrow(boolean flaming)
    {
        super();
        this.isFlaming = flaming;
    }

    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter)
    {
        EntityCustomArrow entityarrow = new EntityCustomArrow(worldIn, shooter, isFlaming);
        return entityarrow;
    }
}
