package com.elementfx.tvp.ad.item.crafting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class RohirrimCraftingManager extends CustomCraftingManager
{
    /** The static instance of this class */
    private static final RohirrimCraftingManager INSTANCE = new RohirrimCraftingManager();
    private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();

    /**
     * Returns the static instance of this class
     */
    public static RohirrimCraftingManager getInstance()
    {
        return INSTANCE;
    }

    protected RohirrimCraftingManager()
    {
        this.addRecipe(new ItemStack(Items.ROHIRRIM_AXE, 1), new Object[] {"II", "SI", "S ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
        this.addRecipe(new ItemStack(Items.ROHIRRIM_SPEAR, 1), new Object[] {"  I", " S ", "S  ", 'S', Items.STICK, 'I', Items.IRON_INGOT});
        Collections.sort(this.getRecipeList(), new Comparator<IRecipe>()
        {
            public int compare(IRecipe p_compare_1_, IRecipe p_compare_2_)
            {
                return p_compare_1_ instanceof ShapelessRecipes && p_compare_2_ instanceof ShapedRecipes ? 1 : (p_compare_2_ instanceof ShapelessRecipes && p_compare_1_ instanceof ShapedRecipes ? -1 : (p_compare_2_.getRecipeSize() < p_compare_1_.getRecipeSize() ? -1 : (p_compare_2_.getRecipeSize() > p_compare_1_.getRecipeSize() ? 1 : 0)));
            }
        });
    }
}
