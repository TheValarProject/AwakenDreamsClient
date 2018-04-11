package com.elementfx.tvp.ad.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemElvenWeapon extends ItemWeapon
{
	private int glowAmount;
	
    public ItemElvenWeapon(int maxUses, float damageVsEntity) {
    	this(maxUses, damageVsEntity, null);
    }
    
    public ItemElvenWeapon(int maxUses, float damageVsEntity, Item.ToolMaterial material) {
    	this(maxUses, damageVsEntity, -2.4F, 0, true, material);
    }

    public ItemElvenWeapon(int maxUses, float damageVsEntity, float attackSpeed, int enchantability, boolean renderFull3D, Item.ToolMaterial material) {
    	super(maxUses, damageVsEntity, attackSpeed, enchantability, renderFull3D, material);
    }
    
    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	List<EntityMob> list = worldIn.<EntityMob>getEntitiesWithinAABB(EntityMob.class, entityIn.getEntityBoundingBox().expand(20.0D, 20.0D, 20.0D));

    	if(!list.isEmpty())
    	{
    		double minDistSq = (int) list.get(0).getDistanceSqToEntity(entityIn);
    		for(EntityMob entity : list) {
    			minDistSq = Math.min(minDistSq, entity.getDistanceSqToEntity(entityIn));
    		}
    		// 20^2 = 864
        	this.glowAmount = 255 - (int) Math.min(minDistSq / 400.0 * 255, 255);
    	}
    	else
    	{
    		this.glowAmount = 0;
    	}
    }
    
    public boolean isGlowing()
    {
    	return this.glowAmount > 0;
    }

    public int getGlowAmount()
    {
    	return this.glowAmount;
    }
}
