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
		
		manager.addRecipe(new ItemStack(Items.KNIFE, 1), new Object[] {"I", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
		manager.addRecipe(new ItemStack(Items.BATTLE_PICKAXE, 1), new Object[] {"III", " I ", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
		manager.addRecipe(new ItemStack(Items.PIKE_CLUB, 1), new Object[] {"I", "I", "I", 'I', Items.IRON_INGOT});
		manager.addRecipe(new ItemStack(Items.SHIRRIFF_CLUB, 1), new Object[] {"S", "S", "S", 'S', Items.STICK});
		manager.addRecipe(new ItemStack(Items.TWO_HANDED_SWORD, 1), new Object[] {"  I", " I ", "S  ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
		manager.addRecipe(new ItemStack(Items.IRON_MACE, 1), new Object[] {"IXI", " I ", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'X', Items.SILVER_INGOT});
		manager.addRecipe(new ItemStack(Items.BULL_HEAD_MACE, 1), new Object[] {"IDI", " I ", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'D', Items.DIAMOND});
		manager.addRecipe(new ItemStack(Items.HARADRIM_SNAKE_DAGGER, 1), new Object[] {"G", "S", 'S', Items.STICK, 'G', Items.GOLD_INGOT});
		manager.addRecipe(new ItemStack(Items.CORSAIR_EKET, 1), new Object[] {"I", "G", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'G', Items.GOLD_INGOT});
	}
}
