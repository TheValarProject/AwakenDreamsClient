package com.elementfx.tvp.ad.item;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

public enum EnumRingBase  implements IStringSerializable
{
    BRONZE("bronze", Items.BRONZE_INGOT, 0, 6133),
    GOLD("gold", Items.GOLD_INGOT, 1, 6115),
    SILVER("silver", Items.SILVER_INGOT, 2, 6124),
    MITHRIL("mithril", Items.MITHRIL_INGOT, 3, 6142);

    private static final EnumRingBase[] META_LOOKUP = new EnumRingBase[values().length];

    private final String name;
    private final Item baseItem;
    private final int meta;
    private final Integer id;

    private EnumRingBase(String unlocalizedName, Item baseItem, int meta)
    {
        this(unlocalizedName, baseItem, meta, null);
    }

    private EnumRingBase(String unlocalizedName, Item baseItem, int meta, int oldId)
    {
        this(unlocalizedName, baseItem, meta, Integer.valueOf(oldId));
    }

    private EnumRingBase(String unlocalizedName, Item baseItem, int meta, Integer oldId)
    {
        this.name = unlocalizedName;
        this.baseItem = baseItem;
        this.meta = meta;
        this.id = oldId;
    }

    public int getMetadata()
    {
        return this.meta;
    }

    @Nullable
    public Integer getIdOffset()
    {
        return this.id;
    }

    @Nullable
    public static EnumRingBase fromIdOffset(int idOffset)
    {
        switch (idOffset)
        {
            case 6133:
                return BRONZE;

            case 6115:
                return GOLD;

            case 6124:
                return SILVER;

            case 6142:
                return MITHRIL;

            default:
                return null;
        }
    }

    public static EnumRingBase fromMetadata(int meta)
    {
        return META_LOOKUP[meta & 3];
    }

    public Item getItem()
    {
        return this.baseItem;
    }

    public String getUnlocalizedName()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name;
    }

    static {
        for (EnumRingBase enumRingBase : values())
        {
            META_LOOKUP[enumRingBase.getMetadata()] = enumRingBase;
        }
    }
}
