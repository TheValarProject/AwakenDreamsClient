package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldNameable;

public interface IInventory extends IWorldNameable {
   int func_70302_i_();

   @Nullable
   ItemStack func_70301_a(int var1);

   @Nullable
   ItemStack func_70298_a(int var1, int var2);

   @Nullable
   ItemStack func_70304_b(int var1);

   void func_70299_a(int var1, @Nullable ItemStack var2);

   int func_70297_j_();

   void func_70296_d();

   boolean func_70300_a(EntityPlayer var1);

   void func_174889_b(EntityPlayer var1);

   void func_174886_c(EntityPlayer var1);

   boolean func_94041_b(int var1, ItemStack var2);

   int func_174887_a_(int var1);

   void func_174885_b(int var1, int var2);

   int func_174890_g();

   void func_174888_l();
}
