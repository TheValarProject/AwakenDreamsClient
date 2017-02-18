package net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockContainer extends Block implements ITileEntityProvider {
   protected BlockContainer(Material p_i45386_1_) {
      this(p_i45386_1_, p_i45386_1_.func_151565_r());
   }

   protected BlockContainer(Material p_i46402_1_, MapColor p_i46402_2_) {
      super(p_i46402_1_, p_i46402_2_);
      this.field_149758_A = true;
   }

   protected boolean func_181086_a(World p_181086_1_, BlockPos p_181086_2_, EnumFacing p_181086_3_) {
      return p_181086_1_.func_180495_p(p_181086_2_.func_177972_a(p_181086_3_)).func_185904_a() == Material.field_151570_A;
   }

   protected boolean func_181087_e(World p_181087_1_, BlockPos p_181087_2_) {
      return this.func_181086_a(p_181087_1_, p_181087_2_, EnumFacing.NORTH) || this.func_181086_a(p_181087_1_, p_181087_2_, EnumFacing.SOUTH) || this.func_181086_a(p_181087_1_, p_181087_2_, EnumFacing.WEST) || this.func_181086_a(p_181087_1_, p_181087_2_, EnumFacing.EAST);
   }

   public EnumBlockRenderType func_149645_b(IBlockState p_149645_1_) {
      return EnumBlockRenderType.INVISIBLE;
   }

   public void func_180663_b(World p_180663_1_, BlockPos p_180663_2_, IBlockState p_180663_3_) {
      super.func_180663_b(p_180663_1_, p_180663_2_, p_180663_3_);
      p_180663_1_.func_175713_t(p_180663_2_);
   }

   public boolean func_189539_a(IBlockState p_189539_1_, World p_189539_2_, BlockPos p_189539_3_, int p_189539_4_, int p_189539_5_) {
      super.func_189539_a(p_189539_1_, p_189539_2_, p_189539_3_, p_189539_4_, p_189539_5_);
      TileEntity tileentity = p_189539_2_.func_175625_s(p_189539_3_);
      return tileentity == null?false:tileentity.func_145842_c(p_189539_4_, p_189539_5_);
   }
}
