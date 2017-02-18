package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerSkeletonType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.util.ResourceLocation;

public class RenderSkeleton extends RenderBiped<EntitySkeleton> {
   private static final ResourceLocation field_110862_k = new ResourceLocation("textures/entity/skeleton/skeleton.png");
   private static final ResourceLocation field_110861_l = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
   private static final ResourceLocation field_190084_m = new ResourceLocation("textures/entity/skeleton/stray.png");

   public RenderSkeleton(RenderManager p_i46143_1_) {
      super(p_i46143_1_, new ModelSkeleton(), 0.5F);
      this.func_177094_a(new LayerHeldItem(this));
      this.func_177094_a(new LayerBipedArmor(this) {
         protected void func_177177_a() {
            this.field_177189_c = new ModelSkeleton(0.5F, true);
            this.field_177186_d = new ModelSkeleton(1.0F, true);
         }
      });
      this.func_177094_a(new LayerSkeletonType(this));
   }

   protected void func_77041_b(EntitySkeleton p_77041_1_, float p_77041_2_) {
      if(p_77041_1_.func_189771_df() == SkeletonType.WITHER) {
         GlStateManager.func_179152_a(1.2F, 1.2F, 1.2F);
      }

   }

   public void func_82422_c() {
      GlStateManager.func_179109_b(0.09375F, 0.1875F, 0.0F);
   }

   protected ResourceLocation func_110775_a(EntitySkeleton p_110775_1_) {
      SkeletonType skeletontype = p_110775_1_.func_189771_df();
      return skeletontype == SkeletonType.WITHER?field_110861_l:(skeletontype == SkeletonType.STRAY?field_190084_m:field_110862_k);
   }
}
