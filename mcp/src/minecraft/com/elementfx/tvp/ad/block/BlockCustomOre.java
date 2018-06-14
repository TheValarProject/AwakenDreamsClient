package com.elementfx.tvp.ad.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockCustomOre extends BlockOre
{
    private int minExp;
    private int maxExp;
    private int dropAmount = 1;
    private Item dropItem;

    public BlockCustomOre(int minExpIn, int maxExpIn)
    {
        this.minExp = minExpIn;
        this.maxExp = maxExpIn;
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public BlockCustomOre()
    {
        this(3, 7);
    }

    public void setItemDrop(Item drop)
    {
        this.dropItem = drop;
    }

    public Block setQuantityDropped(int quantity)
    {
        this.dropAmount = Math.max(0, quantity);
        return this;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return this.dropAmount;
    }

    @Nullable

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return this.dropItem != null ? this.dropItem : Item.getItemFromBlock(this);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);

        if (this.getItemDropped(state, worldIn.rand, fortune) != Item.getItemFromBlock(this))
        {
            int i = MathHelper.getRandomIntegerInRange(worldIn.rand, minExp, maxExp);
            this.dropXpOnBlockBreak(worldIn, pos, i);
        }
    }
}
