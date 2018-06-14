package com.elementfx.tvp.ad.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockCustomPlanks extends Block
{
    public static final PropertyEnum<BlockCustomPlanks.EnumType> VARIANT = PropertyEnum.<BlockCustomPlanks.EnumType>create("variant", BlockCustomPlanks.EnumType.class);

    public BlockCustomPlanks()
    {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockCustomPlanks.EnumType.RIVENDELL));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return ((BlockCustomPlanks.EnumType)state.getValue(VARIANT)).getMetadata();
    }

    /*
    * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
    */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockCustomPlanks.EnumType blockplanks$enumtype : BlockCustomPlanks.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blockplanks$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, BlockCustomPlanks.EnumType.byMetadata(meta));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return ((BlockCustomPlanks.EnumType)state.getValue(VARIANT)).getMapColor();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((BlockCustomPlanks.EnumType)state.getValue(VARIANT)).getMetadata();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {VARIANT});
    }

    public static enum EnumType implements IStringSerializable
    {
        RIVENDELL(0, "rivendell", MapColor.BROWN);

        private static final BlockCustomPlanks.EnumType[] META_LOOKUP = new BlockCustomPlanks.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        private final MapColor mapColor;

        private EnumType(int metaIn, String nameIn, MapColor mapColorIn)
        {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }

        private EnumType(int metaIn, String nameIn, String unlocalizedNameIn, MapColor mapColorIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
            this.unlocalizedName = unlocalizedNameIn;
            this.mapColor = mapColorIn;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        public MapColor getMapColor()
        {
            return this.mapColor;
        }

        public String toString()
        {
            return this.name;
        }

        public static BlockCustomPlanks.EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName()
        {
            return this.name;
        }

        public String getUnlocalizedName()
        {
            return this.unlocalizedName;
        }

        static {
            for (BlockCustomPlanks.EnumType blockcustomplanks$enumtype : values())
            {
                META_LOOKUP[blockcustomplanks$enumtype.getMetadata()] = blockcustomplanks$enumtype;
            }
        }
    }
}
