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

public class MordorCraftingManager extends CustomCraftingManager
{
    /** The static instance of this class */
    private static final MordorCraftingManager INSTANCE = new MordorCraftingManager();
    private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();

    /**
     * Returns the static instance of this class
     */
    public static MordorCraftingManager getInstance()
    {
        return INSTANCE;
    }

    protected MordorCraftingManager()
    {
        this.addRecipe(new ItemStack(Items.MORANNON_DAGGER, 1), new Object[] {"G", "S", 'S', Items.STICK, 'G', Items.GOLD_INGOT});
        this.addRecipe(new ItemStack(Items.DOL_GULDUR_SPIKED_MACE, 1), new Object[] {" I ", "IPI", " S ", 'S', Items.STICK, 'I', Items.IRON_INGOT, 'P', Blocks.PLANKS});
        this.addRecipe(new ItemStack(Items.MORGUL_SWORD, 1), new Object[] {"D", "D", "S", 'S', Items.STICK, 'D', Items.DIAMOND});
        (new RecipesOrcWeapons()).addRecipes(this);
        Collections.sort(this.getRecipeList(), new Comparator<IRecipe>()
        {
            public int compare(IRecipe p_compare_1_, IRecipe p_compare_2_)
            {
                return p_compare_1_ instanceof ShapelessRecipes && p_compare_2_ instanceof ShapedRecipes ? 1 : (p_compare_2_ instanceof ShapelessRecipes && p_compare_1_ instanceof ShapedRecipes ? -1 : (p_compare_2_.getRecipeSize() < p_compare_1_.getRecipeSize() ? -1 : (p_compare_2_.getRecipeSize() > p_compare_1_.getRecipeSize() ? 1 : 0)));
            }
        });
    }
}
