package net.minecraft.entity.monster;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;

public enum ZombieType {
   NORMAL("Zombie", false),
   VILLAGER_FARMER("Zombie", true),
   VILLAGER_LIBRARIAN("Zombie", true),
   VILLAGER_PRIEST("Zombie", true),
   VILLAGER_SMITH("Zombie", true),
   VILLAGER_BUTCHER("Zombie", true),
   HUSK("Husk", false);

   private boolean field_190163_h;
   private final TextComponentTranslation field_190164_i;

   private ZombieType(String p_i47152_3_, boolean p_i47152_4_) {
      this.field_190163_h = p_i47152_4_;
      this.field_190164_i = new TextComponentTranslation("entity." + p_i47152_3_ + ".name", new Object[0]);
   }

   public int func_190150_a() {
      return this.ordinal();
   }

   public boolean func_190154_b() {
      return this.field_190163_h;
   }

   public int func_190148_c() {
      return this.field_190163_h?this.func_190150_a() - 1:0;
   }

   public static ZombieType func_190146_a(int p_190146_0_) {
      return values()[p_190146_0_];
   }

   public static ZombieType func_190144_b(int p_190144_0_) {
      return p_190144_0_ >= 0 && p_190144_0_ < 5?func_190146_a(p_190144_0_ + 1):VILLAGER_FARMER;
   }

   public TextComponentTranslation func_190145_d() {
      return this.field_190164_i;
   }

   public boolean func_190155_e() {
      return this != HUSK;
   }

   public SoundEvent func_190153_f() {
      switch(this) {
      case HUSK:
         return SoundEvents.field_190022_cI;
      case VILLAGER_FARMER:
      case VILLAGER_LIBRARIAN:
      case VILLAGER_PRIEST:
      case VILLAGER_SMITH:
      case VILLAGER_BUTCHER:
         return SoundEvents.field_187940_hn;
      default:
         return SoundEvents.field_187899_gZ;
      }
   }

   public SoundEvent func_190152_g() {
      switch(this) {
      case HUSK:
         return SoundEvents.field_190024_cK;
      case VILLAGER_FARMER:
      case VILLAGER_LIBRARIAN:
      case VILLAGER_PRIEST:
      case VILLAGER_SMITH:
      case VILLAGER_BUTCHER:
         return SoundEvents.field_187944_hr;
      default:
         return SoundEvents.field_187934_hh;
      }
   }

   public SoundEvent func_190151_h() {
      switch(this) {
      case HUSK:
         return SoundEvents.field_190023_cJ;
      case VILLAGER_FARMER:
      case VILLAGER_LIBRARIAN:
      case VILLAGER_PRIEST:
      case VILLAGER_SMITH:
      case VILLAGER_BUTCHER:
         return SoundEvents.field_187943_hq;
      default:
         return SoundEvents.field_187930_hd;
      }
   }

   public SoundEvent func_190149_i() {
      switch(this) {
      case HUSK:
         return SoundEvents.field_190025_cL;
      case VILLAGER_FARMER:
      case VILLAGER_LIBRARIAN:
      case VILLAGER_PRIEST:
      case VILLAGER_SMITH:
      case VILLAGER_BUTCHER:
         return SoundEvents.field_187946_ht;
      default:
         return SoundEvents.field_187939_hm;
      }
   }
}
