package com.elementfx.tvp.ad.item;

import java.util.TreeMap;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

public enum EnumRingStone  implements IStringSerializable
{
	NONE(0, null, false),
	ONXY(1, Items.ONYX),
	TANZANITE(2, Items.TANZANITE),
	JADE(3, Items.JADE),
	MOONSTONE(4, Items.MOONSTONE),
	AMETHYST(5, Items.AMETHYST),
	RUBY(6, Items.RUBY),
	QUARTZ(7, Items.QUARTZ),
	AMBER(8, Items.AMBER),
	STONE_OF_DARKNESS(67, Items.STONE_OF_DARKNESS),
	STONE_OF_EARTH(68, Items.STONE_OF_EARTH),
	STONE_OF_FIRE(69, Items.STONE_OF_FIRE),
	STONE_OF_GREED(70, Items.STONE_OF_GREED),
	STONE_OF_HOPE(71, Items.STONE_OF_HOPE),
	STONE_OF_NATURE(72, Items.STONE_OF_NATURE),
	STONE_OF_SEA(73, Items.STONE_OF_SEA),
	STONE_OF_SKY(74, Items.STONE_OF_SKY),
	STONE_OF_SUNLIGHT(75, Items.STONE_OF_SUNLIGHT),
	STONE_OF_WEALTH(76, Items.STONE_OF_WEALTH),
	STONE_OF_WIND(77, Items.STONE_OF_WIND);
	
	private static final TreeMap<Integer, EnumRingStone> ID_LOOKUP = new TreeMap<Integer, EnumRingStone>();
	
	private final int id;
	private final Item baseItem;
	private final boolean isOld;

	private EnumRingStone(int id, Item baseItem)
	{
		this(id, baseItem, true);
	}
	
	private EnumRingStone(int id, Item baseItem, boolean isOld)
	{
		this.id = id;
		this.baseItem = baseItem;
		this.isOld = isOld;
	}
	
	public int getId() {
		return this.id;
	}
	
	@Nullable
	public Integer getIdOffset()
	{
		return this.isOld ? Integer.valueOf(this.id) : null;
	}
	
	public static EnumRingStone fromId(int id) {
		return ID_LOOKUP.get(Integer.valueOf(id));
	}
	
	@Nullable
	public Item getItem()
	{
		return this.baseItem;
	}
	
	public String getUnlocalizedName()
	{
		return this.baseItem == null ? "" : this.baseItem.getUnlocalizedName().substring(5);
	}
	
	public String getName()
    {
		return this.baseItem == null ? "" : this.baseItem.getUnlocalizedName().substring(5);
    }
	
	static {
        for (EnumRingStone enumRingStone : values())
        {
            ID_LOOKUP.put(Integer.valueOf(enumRingStone.getId()), enumRingStone);
        }
    }
}
