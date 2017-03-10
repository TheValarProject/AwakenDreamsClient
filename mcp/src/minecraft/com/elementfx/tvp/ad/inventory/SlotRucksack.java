package com.elementfx.tvp.ad.inventory;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.item.ItemRucksack;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class SlotRucksack extends Slot
{
	public SlotRucksack(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
		super(inventoryIn, index, xPosition, yPosition);
    }
	
	/**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(@Nullable ItemStack stack)
    {
        return stack == null ? true : !(stack.getItem() instanceof ItemRucksack);
    }
}
