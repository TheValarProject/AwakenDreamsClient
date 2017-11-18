package com.elementfx.tvp.ad.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class RecipesCustom 
{

	public void addRecipes(CraftingManager manager) 
	{
		manager.addRecipe(new ItemStack(Items.PIPE, 1), new Object[] {"  S", "SS ", "S  ", 'S', Items.STICK});
		manager.addShapelessRecipe(new ItemStack(Items.THROWING_STONE, 4), new Object[] {Blocks.COBBLESTONE});
		manager.addShapelessRecipe(new ItemStack(Items.FLAMING_ARROW, 1), new Object[] {Items.ARROW, Items.FIRE_CHARGE});
	}
}
