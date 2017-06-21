package com.elementfx.tvp.ad.inventory;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.item.crafting.ElvenCraftingManager;
import com.elementfx.tvp.ad.item.crafting.GoblinCraftingManager;
import com.elementfx.tvp.ad.item.crafting.GondorianCraftingManager;
import com.elementfx.tvp.ad.item.crafting.HobbitCraftingManager;
import com.elementfx.tvp.ad.item.crafting.HumanCraftingManager;
import com.elementfx.tvp.ad.item.crafting.IsengardCraftingManager;
import com.elementfx.tvp.ad.item.crafting.MordorCraftingManager;
import com.elementfx.tvp.ad.item.crafting.RohirrimCraftingManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class ContainerCustomWorkbench extends Container
{
	/** The crafting matrix inventory (3x3). */
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    private final World worldObj;

    /** Position of the workbench */
    private final BlockPos pos;
    
    private final int meta;

    public ContainerCustomWorkbench(InventoryPlayer playerInventory, World worldIn, BlockPos posIn, int metaIn)
    {
        this.worldObj = worldIn;
        this.pos = posIn;
        this.meta = metaIn;
        
        
        this.addSlotToContainer(new SlotCustomCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35, meta));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
    	ItemStack recipe;
        switch(meta)
        {
        case 0:
        	recipe = ElvenCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 1:
        	recipe = HumanCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 2:
        	recipe = GondorianCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 3:
        	recipe = RohirrimCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 4:
        	recipe = HobbitCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 5:
        	recipe = MordorCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 6:
        	recipe = IsengardCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        case 7:
        	recipe = GoblinCraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        	break;
        	
        default:
        	recipe = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        }
        
        this.craftResult.setInventorySlotContents(0, recipe);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.worldObj.getBlockState(this.pos).getBlock() != Blocks.CUSTOM_CRAFTING_TABLE ? false : playerIn.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack != null)
                {
                    playerIn.dropItem(itemstack, false);
                }
            }
        }
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

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 10 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false))
                {
                    return null;
                }
            }
            else if (index >= 37 && index < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false))
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

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
     * is null for the initial slot that was double-clicked.
     */
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
