package com.elementfx.tvp.ad.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RecipesOrcWeapons 
{
	public void addRecipes(CustomCraftingManager manager)
	{
		manager.addRecipe(new ItemStack(Items.ORC_SPEAR, 1), new Object[] {"  I", " S ", "S  ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
    	manager.addRecipe(new ItemStack(Items.ORC_DAGGER, 1), new Object[] {"I", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
    	manager.addRecipe(new ItemStack(Items.ORC_HALBARD, 1), new Object[] {"IS ", "IS ", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
    	manager.addRecipe(new ItemStack(Items.ORC_SWORD_1, 1), new Object[] {"I  ", " I ", "  S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
    	manager.addRecipe(new ItemStack(Items.ORC_SWORD_2, 1), new Object[] {"I", "I", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
    	manager.addRecipe(new ItemStack(Items.ORC_SWORD_3, 1), new Object[] {"C", "C", "S", 'S', Items.STICK, 'C', Blocks.COBBLESTONE});
    	manager.addRecipe(new ItemStack(Items.ORC_CAPTAIN_SWORD, 1), new Object[] {"G", "G", "S", 'S', Items.STICK, 'G', Items.GOLD_INGOT});    	
	}

}
