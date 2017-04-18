package com.elementfx.tvp.ad.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockCustomOre extends BlockOre
{
	public BlockCustomOre()
	{
	}
	
	@Nullable

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return this == Blocks.JADE_ORE ? Items.JADE : (this == Blocks.AMBER_ORE ? Items.AMBER : (this == Blocks.TANZANITE_ORE ? Items.TANZANITE : (this == Blocks.AMETHYST_ORE ? Items.AMETHYST : (this == Blocks.ONYX_ORE ? Items.ONYX : (this == Blocks.MOONSTONE_ORE ? Items.MOONSTONE : (this == Blocks.CRYSTAL_ORE ? Items.CRYSTAL : (this == Blocks.SALT_ORE ? Items.SALT : Item.getItemFromBlock(this))))))));
    }
	
	/**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);

        if (this.getItemDropped(state, worldIn.rand, fortune) != Item.getItemFromBlock(this))
        {
            int i = 0;

            if (this == Blocks.JADE_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.AMBER_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.TANZANITE_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.AMETHYST_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.ONYX_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.MOONSTONE_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 3, 7);
            }
            else if (this == Blocks.CRYSTAL_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);
            }
            else if (this == Blocks.SALT_ORE)
            {
                i = MathHelper.getRandomIntegerInRange(worldIn.rand, 0, 2);
            }

            this.dropXpOnBlockBreak(worldIn, pos, i);
        }
    }
}
