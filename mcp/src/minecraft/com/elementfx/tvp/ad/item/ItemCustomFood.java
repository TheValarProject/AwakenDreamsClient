package com.elementfx.tvp.ad.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemCustomFood extends ItemFood
{
	/** Number of ticks to run while 'EnumAction'ing until result. */
    private int itemUseDuration;

    public ItemCustomFood(int amount, int duration, float saturation, boolean isWolfFood) {
    	super(amount, saturation, isWolfFood);
    	this.itemUseDuration = duration;
    }

    public ItemCustomFood(int amount, float saturation, boolean isWolfFood)
    {
    	this(amount, 32, saturation, isWolfFood);
    }

    public ItemCustomFood(int amount, boolean isWolfFood)
    {
        this(amount, 0.6F, isWolfFood);
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return this.itemUseDuration;
    }
}
