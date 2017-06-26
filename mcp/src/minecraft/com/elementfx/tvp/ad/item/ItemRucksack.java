package com.elementfx.tvp.ad.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.World;

public class ItemRucksack extends Item
{
	public ItemRucksack() {
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxStackSize(1);
	}
	
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
		if(worldIn.isRemote)
		{
			return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
		}
    	playerIn.openRucksack(itemStackIn, hand);
        playerIn.addStat(StatList.getObjectUseStats(this));
    	return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    }
	
	public InventoryBasic getInventory(ItemStack rucksack)
	{
		if(!rucksack.hasTagCompound())
		{
			return new InventoryBasic("Rucksack", false, 9);
		}
		
		InventoryBasic inventory = new InventoryBasic("Rucksack", false, 9);

		NBTTagList nbttaglist = rucksack.getTagCompound().getTagList("Items", 10);
		
		if(nbttaglist == null)
		{
			return new InventoryBasic("Rucksack", false, 9);
		}

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < inventory.getSizeInventory())
			{
				inventory.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
			}
		}
		
		return inventory;
	}
	
	public ItemStack saveInventory(ItemStack rucksack, IInventory inventory)
	{
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inventory.getSizeInventory(); ++i)
		{
			if (inventory.getStackInSlot(i) != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				inventory.getStackInSlot(i).writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBTTagCompound compound = rucksack.hasTagCompound() ? rucksack.getTagCompound() : new NBTTagCompound();
		compound.setTag("Items", nbttaglist);
		
		rucksack.setTagCompound(compound);
		
		return rucksack;
	}
}
