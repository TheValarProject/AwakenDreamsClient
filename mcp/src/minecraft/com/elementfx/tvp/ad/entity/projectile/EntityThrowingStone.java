package com.elementfx.tvp.ad.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityThrowingStone extends EntityThrowable
{
    private int thrownDemage;

    public EntityThrowingStone(World worldIn)
    {
        super(worldIn);
    }

    public EntityThrowingStone(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
    }

    public EntityThrowingStone(World worldIn, EntityLivingBase throwerIn, int thrownDemageIn)
    {
        super(worldIn, throwerIn);
        this.thrownDemage = thrownDemageIn;
    }

    public EntityThrowingStone(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public static void func_189664_a(DataFixer p_189664_0_)
    {
        EntityThrowable.func_189661_a(p_189664_0_, "ThrownWeapon");
    }

    protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit != null)
        {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), thrownDemage);
        }

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }
}
