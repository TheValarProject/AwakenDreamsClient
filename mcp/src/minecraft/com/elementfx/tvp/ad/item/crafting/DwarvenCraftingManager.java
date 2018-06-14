package com.elementfx.tvp.ad.item.crafting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

public class DwarvenCraftingManager extends CustomCraftingManager
{
    /** The static instance of this class */
    private static final DwarvenCraftingManager INSTANCE = new DwarvenCraftingManager();
    private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();

    /**
     * Returns the static instance of this class
     */
    public static DwarvenCraftingManager getInstance()
    {
        return INSTANCE;
    }

    protected DwarvenCraftingManager()
    {
        this.addRecipe(new ItemStack(Items.DWARVEN_LORD_KNIFE, 1), new Object[] {"I", "S", 'S', Items.STICK, 'I', Items.MITHRIL_INGOT});
        this.addRecipe(new ItemStack(Items.GOLDEN_WAR_HAMMER_OF_EREBOR, 1), new Object[] {"IGI", "ISI", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'G', Items.GOLD_INGOT});
        this.addRecipe(new ItemStack(Items.DWARVEN_AXE_MORIA, 1), new Object[] {"IGI", " SI", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'G', Items.GOLD_INGOT});
        Collections.sort(this.getRecipeList(), new Comparator<IRecipe>()
        {
            public int compare(IRecipe p_compare_1_, IRecipe p_compare_2_)
            {
                return p_compare_1_ instanceof ShapelessRecipes && p_compare_2_ instanceof ShapedRecipes ? 1 : (p_compare_2_ instanceof ShapelessRecipes && p_compare_1_ instanceof ShapedRecipes ? -1 : (p_compare_2_.getRecipeSize() < p_compare_1_.getRecipeSize() ? -1 : (p_compare_2_.getRecipeSize() > p_compare_1_.getRecipeSize() ? 1 : 0)));
            }
        });
    }
}
