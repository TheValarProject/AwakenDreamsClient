package com.elementfx.tvp.ad.inventory;

import javax.annotation.Nullable;

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

public class SlotReadOnly extends Slot
{
	public SlotReadOnly(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
		super(inventoryIn, index, xPosition, yPosition);
    }
	
	/**
     * Return whether this slot's stack can be taken from this slot.
     */
    public boolean canTakeStack(EntityPlayer playerIn)
    {
        return false;
    }

    /**
     * Actualy only call when we want to render the white square effect over the slots. Return always True, except for
     * the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
     */
    public boolean canBeHovered()
    {
        return false;
    }
}
