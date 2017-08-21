package com.elementfx.tvp.ad.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemRing extends Item
{
	public ItemRing() {
		this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxStackSize(1);
	}
	
	/**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
	public String getUnlocalizedName(ItemStack stack)
	{
		EnumRingBase base = EnumRingBase.fromMetadata(stack.getMetadata());
		EnumRingStone stone;
		if(stack.hasTagCompound())
		{
			stone = EnumRingStone.fromId(stack.getTagCompound().getInteger("Stone"));
		}
		else
		{
			stone = EnumRingStone.NONE;
		}
		return super.getUnlocalizedName() + "." + base.getUnlocalizedName() + (stone == EnumRingStone.NONE ? "" : "." + stone.getUnlocalizedName());
	}

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
    	for(EnumRingBase base : EnumRingBase.values()) {
    		for (EnumRingStone stone : EnumRingStone.values())
    		{
    			ItemStack is = new ItemStack(itemIn, 1, base.getMetadata());
    			NBTTagCompound tagCompound = is.hasTagCompound() ? is.getTagCompound() : new NBTTagCompound();
    			tagCompound.setInteger("Stone", stone.getId());
    			is.setTagCompound(tagCompound);
    			subItems.add(is);
    		}
    	}
    }
}
