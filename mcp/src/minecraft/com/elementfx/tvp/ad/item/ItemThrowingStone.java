package com.elementfx.tvp.ad.item;

import com.elementfx.tvp.ad.entity.projectile.EntityThrowingStone;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemThrowingStone extends Item
{
	private int thrownDemage;
	public ItemThrowingStone(int thrownDemage)
	{
		this.maxStackSize = 64;
		this.setCreativeTab(CreativeTabs.COMBAT);
		this.thrownDemage = thrownDemage;
	}
	
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (!playerIn.capabilities.isCreativeMode)
        {
        	--itemStackIn.stackSize;
        }
        
        playerIn.swingArm(hand);
        worldIn.playSound(playerIn, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
       
        if(!worldIn.isRemote)
        {
        	EntityThrowingStone thrownStone = new EntityThrowingStone(worldIn, playerIn, thrownDemage);
        	thrownStone.setHeadingFromThrower(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
        	worldIn.spawnEntityInWorld(thrownStone);
        }
        
        return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    }
}
