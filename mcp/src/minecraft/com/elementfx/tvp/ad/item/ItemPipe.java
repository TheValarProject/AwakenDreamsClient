package com.elementfx.tvp.ad.item;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class ItemPipe extends Item
{
	public List<Item> smokableItems;
	
	private int soundCountdown = 0;
	
	public ItemPipe(boolean packedIn)
	{
		this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxDamage(127);
        
        this.smokableItems = new ArrayList<Item>();
        
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
        		this.decreaseStuffing(stack);
        		playerIn.addStat(StatList.getObjectUseStats(this));
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
    	if(this.isPacked(itemStackIn))
    	{
    		EntitySnowball headingHelper = new EntitySnowball(worldIn, playerIn);
    		headingHelper.setHeadingFromThrower(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 0.1F, 1.0F);

    		for(int j = 0; j < 10; j++)
   			{
   				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, playerIn.posX, playerIn.posY + 1.45, playerIn.posZ, headingHelper.motionX, headingHelper.motionY, headingHelper.motionZ, new int[0]);
   			}
  			
    		playerIn.setActiveHand(hand);
    		
    		worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
    		this.soundCountdown = (int) (5 + Math.random() * 11);
    		
    		if(itemStackIn.getItemDamage() + 1 > this.getMaxDamage())
        	{
    			ItemStack stuffing = this.getStuffing(itemStackIn);
    			stuffing.stackSize--;
        		playerIn.dropItem(stuffing, false, false);
        	}
    		
    		return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    	}
    	else
    	{
    		return new ActionResult(EnumActionResult.FAIL, itemStackIn);
    	}
    }
    
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
    	subItems.add(new ItemStack(itemIn));
    	for(Item stuffing : this.smokableItems) {
    		subItems.add(this.getStuffed(stuffing));
    	}
    }
    
    public boolean isPacked(ItemStack pipe)
    {
    	if(!(pipe != null && pipe.hasTagCompound() && pipe.getTagCompound().hasKey("Stuffing")))
    	{
    		return false;
    	}
    	ItemStack stuffing = ItemStack.loadItemStackFromNBT(pipe.getTagCompound().getCompoundTag("Stuffing"));
    	return stuffing != null && this.canSmoke(stuffing.getItem());
    }
    
    @Nullable
    public ItemStack getStuffing(ItemStack pipe)
    {
    	return this.isPacked(pipe) ? ItemStack.loadItemStackFromNBT(pipe.getTagCompound().getCompoundTag("Stuffing")) : null;
    }
    
    public ItemStack getStuffed(Item stuffing)
    {
    	return getStuffed(stuffing, 64);
    }
    
    public ItemStack getStuffed(Item stuffing, int amount)
    {
    	return stuffStack(new ItemStack(this), new ItemStack(stuffing, amount));
    }
    
    public ItemStack stuffStack(ItemStack pipeStack, ItemStack stuffingStack)
    {
		NBTTagCompound pipeTag = pipeStack.hasTagCompound() ? pipeStack.getTagCompound() : new NBTTagCompound();
		NBTTagCompound stuffingTag = stuffingStack.hasTagCompound() ? stuffingStack.getTagCompound() : new NBTTagCompound();
		stuffingStack.writeToNBT(stuffingTag);
		pipeTag.setTag("Stuffing", stuffingTag);
		pipeStack.setTagCompound(pipeTag);
    	return pipeStack;
    }
    
    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (this.isPacked(stack))
        {
        	if(playerIn.isCreative())
        	{
        		tooltip.add(TextFormatting.DARK_GREEN + I18n.translateToLocal("stuffing." + this.getStuffing(stack).getUnlocalizedName().substring(5)).replace(" %s ", " "));
        	}
        	else
        	{
        		tooltip.add(TextFormatting.DARK_GREEN + String.format(I18n.translateToLocal("stuffing." + this.getStuffing(stack).getUnlocalizedName().substring(5)), this.getStuffingAmount(stack)));
        	}
        }
        else {
        	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("stuffing.empty"));
        }
    }
    
    public ItemStack clearStuffing(ItemStack pipe)
    {
    	if(pipe.hasTagCompound())
    	{
    		pipe.getTagCompound().removeTag("Stuffing");
    	}
    	return pipe;
    }
    
    public String getModelName(ItemStack stack)
    {
        return transformName(super.getUnlocalizedName().substring(5)) + (this.isPacked(stack) ? "_" + this.getStuffing(stack).getUnlocalizedName().substring(5).replace('.', '_') : "");
    }
    
    public String transformName(String s)
    {
    	for(int i = 0; i < s.length(); i++)
    	{
    		if(Character.isUpperCase(s.charAt(i)))
    		{
    			s = s.substring(0, i) + "_" + Character.toLowerCase(s.charAt(i)) + s.substring(i + 1, s.length());
    		}
    	}
    	return s;
    }
    
    public void addSmokableItem(Item smokable)
    {
    	this.smokableItems.add(smokable);
    }
    
    public List<Item> getSmokableItems()
    {
    	return this.smokableItems;
    }
    
    public boolean canSmoke(Item item)
    {
    	return this.smokableItems.contains(item);
    }
    
    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	if(entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).getActiveItemStack() == stack)
    	{
    		if(this.soundCountdown <= 0)
    		{
    			EntitySnowball headingHelper = new EntitySnowball(worldIn, (EntityPlayer)entityIn);
        		headingHelper.setHeadingFromThrower(entityIn, entityIn.rotationPitch, entityIn.rotationYaw, 0.0F, 0.1F, 1.0F);

        		for(int j = 0; j < 10; j++)
       			{
       				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, entityIn.posX, entityIn.posY + 1.45, entityIn.posZ, headingHelper.motionX, headingHelper.motionY, headingHelper.motionZ, new int[0]);
       			}
        		
    			worldIn.playSound((EntityPlayer)null, entityIn.posX, entityIn.posY, entityIn.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.NEUTRAL, 0.5F, 1.0F);
    			this.soundCountdown = (int) (5 + Math.random() * 11);
    		}
    		else {
    			this.soundCountdown--;
    		}
    	}
    }
    
    public int getStuffingAmount(ItemStack pipe)
    {
    	if(!this.isPacked(pipe))
    	{
    		return 0;
    	}
    	
    	return this.getStuffing(pipe).stackSize;
    }
    
    public void setStuffingAmount(ItemStack pipe, int amount)
    {
    	if(!this.isPacked(pipe))
    	{
    		return;
    	}
    	
    	ItemStack stuffing = this.getStuffing(pipe);
    	stuffing.stackSize = amount;
    	
    	if(stuffing.stackSize <= 0)
    	{
    		// TODO Modify the NBT attribute directly for efficiency
    		this.clearStuffing(pipe);
    	}
    	else
    	{
    		this.stuffStack(pipe, stuffing);
    	}
    }
    
    public void decreaseStuffing(ItemStack pipe)
    {
    	if(!this.isPacked(pipe))
    	{
    		return;
    	}
    	
    	ItemStack stuffing = this.getStuffing(pipe);
    	stuffing.stackSize--;
    	
    	if(stuffing.stackSize <= 0)
    	{
    		// TODO Modify the NBT attribute directly for efficiency
    		this.clearStuffing(pipe);
    	}
    	else
    	{
    		this.stuffStack(pipe, stuffing);
    	}
    }
}