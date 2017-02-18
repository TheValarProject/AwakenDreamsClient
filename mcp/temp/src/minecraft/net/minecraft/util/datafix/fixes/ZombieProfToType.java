package net.minecraft.util.datafix.fixes;

import java.util.Random;
import net.minecraft.entity.monster.ZombieType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class ZombieProfToType implements IFixableData {
   private static final Random field_190049_a = new Random();

   public int func_188216_a() {
      return 502;
   }

   public NBTTagCompound func_188217_a(NBTTagCompound p_188217_1_) {
      if("Zombie".equals(p_188217_1_.func_74779_i("id")) && p_188217_1_.func_74767_n("IsVillager")) {
         if(!p_188217_1_.func_150297_b("ZombieType", 99)) {
            ZombieType zombietype = null;
            if(p_188217_1_.func_150297_b("VillagerProfession", 99)) {
               try {
                  zombietype = ZombieType.func_190146_a(p_188217_1_.func_74762_e("VillagerProfession") + 1);
               } catch (RuntimeException var4) {
                  ;
               }
            }

            if(zombietype == null) {
               zombietype = ZombieType.func_190146_a(field_190049_a.nextInt(5) + 1);
            }

            p_188217_1_.func_74768_a("ZombieType", zombietype.func_190150_a());
         }

         p_188217_1_.func_82580_o("IsVillager");
      }

      return p_188217_1_;
   }
}
