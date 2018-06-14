package com.elementfx.tvp.ad.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemStakeSeeds extends ItemSeeds
{
    private final Block crops;

    /** BlockID of the block the seeds can be planted on. */
    private final Block soil;

    public ItemStakeSeeds(Block crops, Block soil)
    {
        super(crops, soil);
        this.crops = crops;
        this.soil = soil;
    }

    public Block getCrop()
    {
        return this.crops;
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (playerIn.canPlayerEdit(pos, facing, stack) && worldIn.getBlockState(pos).getBlock() == Blocks.STAKES)
        {
            worldIn.setBlockState(pos, this.crops.getDefaultState());
            --stack.stackSize;
            return EnumActionResult.SUCCESS;
        }
        else if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && worldIn.getBlockState(pos).getBlock() == this.soil && worldIn.getBlockState(pos.up()).getBlock() == Blocks.STAKES)
        {
            worldIn.setBlockState(pos.up(), this.crops.getDefaultState());
            --stack.stackSize;
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
}
