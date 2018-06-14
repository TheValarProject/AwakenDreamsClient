package com.elementfx.tvp.ad.item.crafting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class HobbitCraftingManager extends CustomCraftingManager
{
    /** The static instance of this class */
    private static final HobbitCraftingManager INSTANCE = new HobbitCraftingManager();
    private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();

    /**
     * Returns the static instance of this class
     */
    public static HobbitCraftingManager getInstance()
    {
        return INSTANCE;
    }

    protected HobbitCraftingManager()
    {
        this.addRecipe(new ItemStack(Items.HOBBIT_SWORD, 1), new Object[] {"I", "I", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
        this.addRecipe(new ItemStack(Items.HOBBIT_AXE, 1), new Object[] {"II", "SI", "S ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
        this.addRecipe(new ItemStack(Items.HOBBIT_DAGGER, 1), new Object[] {"W", "S", 'S', Items.STICK, 'W', Blocks.PLANKS});
        this.addRecipe(new ItemStack(Items.HOBBIT_HAMMER, 1), new Object[] {"I", "S", 'S', Items.STICK, 'I', Items.IRON_INGOT});
        this.addRecipe(new ItemStack(Items.SHIRRIFF_CLUB, 1), new Object[] {"X", "S", 'S', Items.STICK, 'X', Blocks.PLANKS});
        Collections.sort(this.getRecipeList(), new Comparator<IRecipe>()
        {
            public int compare(IRecipe p_compare_1_, IRecipe p_compare_2_)
            {
                return p_compare_1_ instanceof ShapelessRecipes && p_compare_2_ instanceof ShapedRecipes ? 1 : (p_compare_2_ instanceof ShapelessRecipes && p_compare_1_ instanceof ShapedRecipes ? -1 : (p_compare_2_.getRecipeSize() < p_compare_1_.getRecipeSize() ? -1 : (p_compare_2_.getRecipeSize() > p_compare_1_.getRecipeSize() ? 1 : 0)));
            }
        });
    }
}
