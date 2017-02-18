package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRecipe {
   boolean func_77569_a(InventoryCrafting var1, World var2);

   @Nullable
   ItemStack func_77572_b(InventoryCrafting var1);

   int func_77570_a();

   @Nullable
   ItemStack func_77571_b();

   ItemStack[] func_179532_b(InventoryCrafting var1);
}
