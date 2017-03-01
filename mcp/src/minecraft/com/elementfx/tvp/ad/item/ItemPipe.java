package com.elementfx.tvp.ad.item;


import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
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
	
	public ItemPipe(boolean packedIn)
	{
		this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxDamage(20);
        
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
    	if(itemStackIn.getItemDamage() + 1 >= this.getMaxDamage())
    	{
    		itemStackIn = this.clearStuffing(itemStackIn);
    	}
    	
    	if(this.isPacked(itemStackIn))
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
    	if(!(pipe.hasTagCompound() && pipe.getTagCompound().hasKey("Stuffing")))
    	{
    		return false;
    	}
    	ItemStack stuffing = ItemStack.loadItemStackFromNBT(pipe.getTagCompound().getCompoundTag("Stuffing"));
    	return stuffing != null && this.canSmoke(stuffing.getItem());
    }
    
    public ItemStack getStuffing(ItemStack pipe)
    {
    	return this.isPacked(pipe) ? ItemStack.loadItemStackFromNBT(pipe.getTagCompound().getCompoundTag("Stuffing")) : null;
    }
    
    public ItemStack getStuffed(Item stuffing)
    {
    	return getStuffed(stuffing, 1);
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
        	tooltip.add(TextFormatting.DARK_GREEN + I18n.translateToLocal(this.getUnlocalizedName() + "." + this.getStuffing(stack).getUnlocalizedName().substring(5)));
        }
        else {
        	tooltip.add(TextFormatting.GRAY + I18n.translateToLocal(this.getUnlocalizedName() + ".empty"));
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
        return super.getUnlocalizedName().substring(5).replace('.', '_') + (this.isPacked(stack) ? "_" + this.getStuffing(stack).getUnlocalizedName().substring(5).replace('.', '_') : "");
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
}