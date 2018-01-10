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
	private boolean glows;
	
    public ItemElvenWeapon(int maxUses, float damageVsEntity) {
    	this(maxUses, damageVsEntity, -2.4F, 0, true, null);
    }

    public ItemElvenWeapon(int maxUses, float damageVsEntity, float attackSpeed, int enchantability, boolean renderFull3D, Item repairItem) {
    	super(maxUses, damageVsEntity, attackSpeed, enchantability, renderFull3D, repairItem);
    }
    
    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	List<EntityMob> list = worldIn.<EntityMob>getEntitiesWithinAABB(EntityMob.class, entityIn.getEntityBoundingBox().expand(20.0D, 8.0D, 20.0D));
    	this.glows = !list.isEmpty();
    }
    
    public boolean isGlowing(ItemStack stack)
    {
    	return this.glows;
    }
}
