package net.minecraft.world.gen.structure;

import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class MapGenMineshaft extends MapGenStructure {
   private double field_82673_e = 0.004D;

   public MapGenMineshaft() {
   }

   public String func_143025_a() {
      return "Mineshaft";
   }

   public MapGenMineshaft(Map<String, String> p_i2034_1_) {
      for(Entry<String, String> entry : p_i2034_1_.entrySet()) {
         if(((String)entry.getKey()).equals("chance")) {
            this.field_82673_e = MathHelper.func_82712_a((String)entry.getValue(), this.field_82673_e);
         }
      }

   }

   protected boolean func_75047_a(int p_75047_1_, int p_75047_2_) {
      return this.field_75038_b.nextDouble() < this.field_82673_e && this.field_75038_b.nextInt(80) < Math.max(Math.abs(p_75047_1_), Math.abs(p_75047_2_));
   }

   protected StructureStart func_75049_b(int p_75049_1_, int p_75049_2_) {
      Biome biome = this.field_75039_c.func_180494_b(new BlockPos((p_75049_1_ << 4) + 8, 64, (p_75049_2_ << 4) + 8));
      MapGenMineshaft.Type mapgenmineshaft$type = biome instanceof BiomeMesa?MapGenMineshaft.Type.MESA:MapGenMineshaft.Type.NORMAL;
      return new StructureMineshaftStart(this.field_75039_c, this.field_75038_b, p_75049_1_, p_75049_2_, mapgenmineshaft$type);
   }

   public static enum Type {
      NORMAL,
      MESA;

      public static MapGenMineshaft.Type func_189910_a(int p_189910_0_) {
         return p_189910_0_ >= 0 && p_189910_0_ < values().length?values()[p_189910_0_]:NORMAL;
      }
   }
}
