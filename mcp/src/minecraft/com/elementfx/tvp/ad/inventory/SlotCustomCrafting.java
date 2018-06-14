package com.elementfx.tvp.ad.inventory;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.item.crafting.DwarvenCraftingManager;
import com.elementfx.tvp.ad.item.crafting.ElvenCraftingManager;
import com.elementfx.tvp.ad.item.crafting.GoblinCraftingManager;
import com.elementfx.tvp.ad.item.crafting.GondorianCraftingManager;
import com.elementfx.tvp.ad.item.crafting.HobbitCraftingManager;
import com.elementfx.tvp.ad.item.crafting.HumanCraftingManager;
import com.elementfx.tvp.ad.item.crafting.IsengardCraftingManager;
import com.elementfx.tvp.ad.item.crafting.MordorCraftingManager;
import com.elementfx.tvp.ad.item.crafting.RohirrimCraftingManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class SlotCustomCrafting extends SlotCrafting
{
    /** The craft matrix inventory linked to this result slot. */
    private final InventoryCrafting craftMatrix;

    /** The player that is using the GUI where this slot resides. */
    private final EntityPlayer thePlayer;

    private final int meta;

    public SlotCustomCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition, int metaIn)
    {
        super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
        this.thePlayer = player;
        this.craftMatrix = craftingInventory;
        this.meta = metaIn;
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        this.onCrafting(stack);
        ItemStack[] aitemstack;

        switch (meta)
        {
            case 0:
                aitemstack = ElvenCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 1:
                aitemstack = HumanCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 2:
                aitemstack = GondorianCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 3:
                aitemstack = RohirrimCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 4:
                aitemstack = HobbitCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 5:
                aitemstack = MordorCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 6:
                aitemstack = IsengardCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 7:
                aitemstack = GoblinCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            case 8:
                aitemstack = DwarvenCraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
                break;

            default:
                aitemstack = CraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
        }

        for (int i = 0; i < aitemstack.length; ++i)
        {
            ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
            ItemStack itemstack1 = aitemstack[i];

            if (itemstack != null)
            {
                this.craftMatrix.decrStackSize(i, 1);
                itemstack = this.craftMatrix.getStackInSlot(i);
            }

            if (itemstack1 != null)
            {
                if (itemstack == null)
                {
                    this.craftMatrix.setInventorySlotContents(i, itemstack1);
                }
                else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
                {
                    itemstack1.stackSize += itemstack.stackSize;
                    this.craftMatrix.setInventorySlotContents(i, itemstack1);
                }
                else if (!this.thePlayer.inventory.addItemStackToInventory(itemstack1))
                {
                    this.thePlayer.dropItem(itemstack1, false);
                }
            }
        }
    }
}
