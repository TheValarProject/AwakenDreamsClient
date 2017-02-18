package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeOcean extends Biome {
   public BiomeOcean(Biome.BiomeProperties p_i46700_1_) {
      super(p_i46700_1_);
      this.field_76762_K.clear();
   }

   public Biome.TempCategory func_150561_m() {
      return Biome.TempCategory.OCEAN;
   }

   public void func_180622_a(World p_180622_1_, Random p_180622_2_, ChunkPrimer p_180622_3_, int p_180622_4_, int p_180622_5_, double p_180622_6_) {
      super.func_180622_a(p_180622_1_, p_180622_2_, p_180622_3_, p_180622_4_, p_180622_5_, p_180622_6_);
   }
}
