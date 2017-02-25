package com.elementfx.tvp.ad.item;


import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class ItemPipe extends Item
{
	private boolean isPacked;
	
	public ItemPipe(boolean packedIn)
	{
		this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxDamage(20);
        this.isPacked = packedIn;
        
        this.addPropertyOverride(new ResourceLocation("smoking"), new IItemPropertyGetter()
        {
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
	}

	/**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
    	EntityPlayer playerIn = (EntityPlayer)entityLiving;
        if (!worldIn.isRemote)
        {	
        	if(!playerIn.capabilities.isCreativeMode)
        	{
        		stack.damageItem(1, playerIn);
        	}
        }
    }
    
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 120; 
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.SMOKE;      
    }

    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
    	if(itemStackIn.getItemDamage() == 19)
    	{
    		itemStackIn = new ItemStack(Items.PIPE, 1);
    	}
    	if(!playerIn.capabilities.isCreativeMode && !isPacked)
    	{
    		return new ActionResult(EnumActionResult.FAIL, itemStackIn);
    	}
    	else
    	{
    		EntitySnowball headingHelper = new EntitySnowball(worldIn, playerIn);
    		headingHelper.setHeadingFromThrower(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 0.1F, 1.0F);

    		for(int j = 0; j < 10; j++)
   			{
   				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, playerIn.posX, playerIn.posY + 1.45, playerIn.posZ, headingHelper.motionX, headingHelper.motionY, headingHelper.motionZ, new int[0]);
   			}
  			
    		playerIn.setActiveHand(hand);
    		return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    	}
    }
}