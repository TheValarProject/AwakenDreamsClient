package com.elementfx.tvp.ad.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStakeSeeds extends ItemSeeds
{
	private final Block crops;
	
	public ItemStakeSeeds(Block crops, Block soil)
	{
		super(crops, soil);
		this.crops = crops;
	}
	
	public Block getCrop() {
		return this.crops;
	}
}
