package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemShears extends Item
{
    public ItemShears()
    {
        this.setMaxStackSize(1);
        this.setMaxDamage(238);
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        stack.damageItem(1, entityLiving);
        Block block = state.getBlock();
        // Begin Awaken Dreams code
        return state.getMaterial() != Material.LEAVES && block instanceof BlockWeb && block != Blocks.TALLGRASS && block != Blocks.VINE && block != Blocks.TRIPWIRE && block != Blocks.WOOL ? super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving) : true;
        // End Awaken Dreams code
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(IBlockState blockIn)
    {
        Block block = blockIn.getBlock();
        // Begin Awaken Dreams code
        return block instanceof BlockWeb || block == Blocks.REDSTONE_WIRE || block == Blocks.TRIPWIRE;
        // End Awaken Dreams code
    }

    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        Block block = state.getBlock();
        // Begin Awaken Dreams code
        return block instanceof BlockWeb && state.getMaterial() != Material.LEAVES ? (block == Blocks.WOOL ? 5.0F : super.getStrVsBlock(stack, state)) : 15.0F;
        // End Awaken Dreams code
    }
}
