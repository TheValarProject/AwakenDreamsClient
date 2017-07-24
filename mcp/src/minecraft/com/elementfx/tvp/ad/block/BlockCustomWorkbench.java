package com.elementfx.tvp.ad.block;

import java.util.List;

import javax.annotation.Nullable;

import com.elementfx.tvp.ad.inventory.ContainerCustomWorkbench;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.BlockWorkbench.InterfaceCraftingTable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class BlockCustomWorkbench extends Block
{
	public static final PropertyEnum<BlockCustomWorkbench.EnumType> TYPE = PropertyEnum.<BlockCustomWorkbench.EnumType>create("type", BlockCustomWorkbench.EnumType.class);

    public BlockCustomWorkbench()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockCustomWorkbench.EnumType.HUMAN));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return ((BlockCustomWorkbench.EnumType)state.getValue(TYPE)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockCustomWorkbench.EnumType blockcustomworkbench$enumtype : BlockCustomWorkbench.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blockcustomworkbench$enumtype.getMetadata()));
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return MapColor.WOOD;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, BlockCustomWorkbench.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((BlockCustomWorkbench.EnumType)state.getValue(TYPE)).getMetadata();
    }
    
    public static String getName(IBlockState state)
    {
    	return ((BlockCustomWorkbench.EnumType)state.getValue(TYPE)).getName();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {TYPE});
    }
    
    public boolean isOpaqueCube(IBlockState state) 
    {
    	return false;
    }
    
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
    
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    public static String getUnlocalizedName(IBlockState state)
    {
        return ((BlockCustomWorkbench.EnumType)state.getValue(TYPE)).getUnlocalizedName();
    }
    
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            playerIn.displayGui(new BlockCustomWorkbench.InterfaceCustomCraftingTable(worldIn, pos));
            playerIn.addStat(StatList.CRAFTING_TABLE_INTERACTION);
            return true;
        }
    }
    
    public static class InterfaceCustomCraftingTable extends InterfaceCraftingTable
    {
        private final World world;
        private final BlockPos position;

        public InterfaceCustomCraftingTable(World worldIn, BlockPos pos)
        {
        	super(worldIn, pos);
            this.world = worldIn;
            this.position = pos;
        }

        public ITextComponent getDisplayName()
        {
        	return new TextComponentTranslation("tile.customWorkbench." + BlockCustomWorkbench.getUnlocalizedName(world.getBlockState(position)) + ".name");
        }
        
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
        {
        	return new ContainerCustomWorkbench(playerInventory, this.world, this.position, world.getBlockState(position).getBlock().getMetaFromState(world.getBlockState(position)));
        }

        public String getGuiID()
        { 	
        	return "minecraft:" + BlockCustomWorkbench.getName(world.getBlockState(position));   
        }
    }


    public static enum EnumType implements IStringSerializable
    {
    	ELVEN(0, "elven_crafting_table", "elven"),
    	HUMAN(1, "human_crafting_table", "human"),
    	GONDORIAN(2, "gondorian_crafting_table", "gondorian"),
    	ROHIRRIM(3, "rohirrim_crafting_table", "rohirrim"),
    	HOBBIT(4, "hobbit_crafting_table", "hobbit"),
    	MORDOR(5, "mordor_crafting_table", "mordor"),
    	ISENGARD(6, "isengard_crafting_table", "isengard"),
    	GOBLIN(7, "goblin_crafting_table", "goblin"),
    	DWARVEN(8, "dwarven_crafting_table", "dwarven");

        private static final BlockCustomWorkbench.EnumType[] META_LOOKUP = new BlockCustomWorkbench.EnumType[values().length];
        private final int metadata;
        private final String name;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName)
        {
            this.metadata = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata()
        {
            return this.metadata;
        }

        public String toString()
        {
            return this.name;
        }

        public static BlockCustomWorkbench.EnumType byMetadata(int meta)
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
            for (BlockCustomWorkbench.EnumType blockcustomworkbench$enumtype : values())
            {
                META_LOOKUP[blockcustomworkbench$enumtype.getMetadata()] = blockcustomworkbench$enumtype;
            }
        }
    }
}