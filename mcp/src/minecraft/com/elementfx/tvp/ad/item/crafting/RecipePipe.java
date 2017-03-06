package com.elementfx.tvp.ad.item.crafting;

import com.elementfx.tvp.ad.item.ItemPipe;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipePipe implements IRecipe
{
    public RecipePipe()
    {
    }

    @Nullable
    public ItemStack getRecipeOutput()
    {
        return null;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
    	ItemStack pipeStack = null;
    	int pipeIndex = 0;
        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
        	ItemStack itemstack = inv.getStackInSlot(i);

        	if (itemstack != null && itemstack.getItem() instanceof ItemPipe)
        	{
        		pipeStack = itemstack;
        		pipeIndex = i;
        		break;
        	}
        }
        ItemPipe pipe = (ItemPipe) pipeStack.getItem();
        
        int stuffingAmount = pipe.getStuffingAmount(pipeStack);
    	
        ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i)
        {
        	if(i == pipeIndex)
        	{
        		continue;
        	}
        	
            ItemStack itemstack = inv.getStackInSlot(i);

            if (itemstack != null)
            {
            	int stackSize = itemstack.stackSize;
            	
            	if(stackSize + stuffingAmount >= itemstack.getMaxStackSize())
            	{;
            		itemstack.stackSize = stackSize - (itemstack.getMaxStackSize() - stuffingAmount) + 1;
            	}
            	else if(itemstack.getItem().hasContainerItem())
            	{
            		aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
            	}
            	else
            	{
            		itemstack.stackSize = 0;
            	}
            	
            	stuffingAmount = Math.min(stuffingAmount + stackSize, itemstack.getMaxStackSize());
            }
        }

        return aitemstack;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        // Look for an instance of ItemPipe
        ItemStack pipeStack = null;
        int pipeIndex = 0;
        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
        	ItemStack itemstack = inv.getStackInSlot(i);

        	if (itemstack != null && itemstack.getItem() instanceof ItemPipe)
        	{
        		if(pipeStack != null)
        		{
        			// More than one pipe found
        			return false;
        		}
        		pipeStack = itemstack;
        		pipeIndex = i;
        	}
        }
        if(pipeStack == null)
        {
        	// No pipe found
        	return false;
        }
        ItemPipe pipe = (ItemPipe) pipeStack.getItem();
        
        // Look for something to stuff the pipe with
        Item stuffing = null;
        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
        	if(i == pipeIndex)
        	{
        		continue;
        	}
        	
        	ItemStack itemstack = inv.getStackInSlot(i);

        	if (itemstack != null)
        	{
        		if(stuffing == null)
        		{
        			stuffing = itemstack.getItem();
        			if(!pipe.canSmoke(stuffing))
        			{
        				// Unsmokable item found
        				return false;
        			}
        		}
        		else if(stuffing != itemstack.getItem())
        		{
        			// More than one type of stuffing found
        			return false;
        		}
        	}
        }
        if(stuffing == null)
        {
        	// No smokable item found
        	return false;
        }
        
        if(pipe.getStuffing(pipeStack) != null && pipe.getStuffing(pipeStack).getItem() != stuffing)
        {
        	// Smokable item is different from item already in pipe
        	return false;
        }

        return true;
    }

    @Nullable

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
    	ItemStack pipeStack = null;
        int pipeIndex = 0;
        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
        	ItemStack itemstack = inv.getStackInSlot(i);

        	if (itemstack != null && itemstack.getItem() instanceof ItemPipe)
        	{
        		pipeStack = itemstack;
        		pipeIndex = i;
        		break;
        	}
        }
        ItemPipe pipe = (ItemPipe) pipeStack.getItem();
        pipeStack = pipeStack.copy();
        
        ItemStack stuffing = pipe.getStuffing(pipeStack);
        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
        	if(i == pipeIndex)
        	{
        		continue;
        	}
        	
        	ItemStack itemstack = inv.getStackInSlot(i);

        	if (itemstack != null)
        	{
        		if(stuffing == null)
        		{
        			stuffing = itemstack.copy();
        		}
        		else
        		{
        			stuffing.stackSize = Math.min(stuffing.stackSize + itemstack.stackSize, stuffing.getMaxStackSize());
        		}
        	}
        }
        
        pipe.stuffStack(pipeStack, stuffing);
        
        return pipeStack;
    }

    /**
     * Returns the size of the recipe area
     */
    public int getRecipeSize()
    {
        return 2;
    }
}
