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

public class ContainerRucksack extends Container
{
    private final IInventory rucksackInventory;

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    public ContainerRucksack(IInventory playerInventory, IInventory rucksackInventoryIn, int readOnlySlot)
    {
        this.rucksackInventory = rucksackInventoryIn;

        // Rucksack slots
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotRucksack(rucksackInventoryIn, j + i * 3, 106 + j * 18, 17 + i * 18));
            }
        }

        // Player armor slots
        for (int k = 0; k < 4; ++k)
        {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }
                public boolean isItemValid(@Nullable ItemStack stack)
                {
                    if (stack == null)
                    {
                        return false;
                    }
                    else
                    {
                        EntityEquipmentSlot entityequipmentslot1 = EntityLiving.getSlotForItemStack(stack);
                        return entityequipmentslot1 == entityequipmentslot;
                    }
                }
                @Nullable
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        // Player left hand
        if (readOnlySlot == 40)
        {
            this.addSlotToContainer(new SlotReadOnly(playerInventory, 40, 77, 62)
            {
                public boolean isItemValid(@Nullable ItemStack stack)
                {
                    return super.isItemValid(stack);
                }
                @Nullable
                public String getSlotTexture()
                {
                    return "minecraft:items/empty_armor_slot_shield";
                }
            });
        }
        else
        {
            this.addSlotToContainer(new Slot(playerInventory, 40, 77, 62)
            {
                public boolean isItemValid(@Nullable ItemStack stack)
                {
                    return super.isItemValid(stack);
                }
                @Nullable
                public String getSlotTexture()
                {
                    return "minecraft:items/empty_armor_slot_shield";
                }
            });
        }

        // Player inventory slots
        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        // Player hotbar slots
        for (int l = 0; l < 9; ++l)
        {
            if (readOnlySlot == l)
            {
                this.addSlotToContainer(new SlotReadOnly(playerInventory, l, 8 + l * 18, 142));
            }
            else
            {
                this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 142));
            }
        }
    }

    public IInventory getRucksackInventory()
    {
        return this.rucksackInventory;
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.rucksackInventory.isUseableByPlayer(playerIn);
    }

    @Nullable

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 9, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }
}
