package com.elementfx.tvp.ad.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class RecipesCustom 
{

	public void addRecipes(CraftingManager manager) 
	{
		manager.addRecipe(new ItemStack(Items.PIPE, 1), new Object[] {"  S", "SS ", "S  ", 'S', Items.STICK});
	}
}
