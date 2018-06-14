package com.elementfx.tvp.ad.item;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class ItemCustomArmor extends ItemArmor
{
    private Properties properties;

    public ItemCustomArmor(EntityEquipmentSlot armorType, Properties properties)
    {
        super(ItemArmor.ArmorMaterial.IRON, 2, armorType);
        this.properties = properties;
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return this.properties.getEnchantability();
    }

    /**
     * Return whether the specified armor ItemStack has a color.
     */
    public boolean hasColor(ItemStack stack)
    {
        return false;
    }

    /**
     * Return the color for the specified armor ItemStack.
     */
    public int getColor(ItemStack stack)
    {
        return 16777215;
    }

    /**
     * Remove the color from the specified armor ItemStack.
     */
    public void removeColor(ItemStack stack) {}

    /**
     * Sets the color of the specified armor ItemStack
     */
    public void setColor(ItemStack stack, int color)
    {
        throw new UnsupportedOperationException("Can\'t dye non-leather!");
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        return this.properties.getRepairItem() == repair.getItem() ? true : false;
    }

    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == this.armorType)
        {
            this.properties.applyAttributeModifiers(equipmentSlot.getIndex(), multimap);
        }

        return multimap;
    }

    public SoundEvent getSoundEvent()
    {
        return this.properties.getSound();
    }

    public String getTextureName()
    {
        return this.properties.getTextureName();
    }

    public static class Properties
    {
        private String textureName;
        private double maxDamage, defensePoints, toughness;
        private int enchantability;
        private SoundEvent sound;
        private Item repairItem;
        private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
        private static final int[] ARMOR_CONVERSION = new int[] { 4, 7, 8, 5 };
        private static final int[] MAX_DAMAGE_ARRAY = new int[] { 13, 15, 16, 11 };

        public Properties(String textureName, double maxDamage, double defensePoints)
        {
            this(textureName, maxDamage, defensePoints, 0);
        }

        public Properties(String textureName, double maxDamage, double defensePoints, double toughness)
        {
            this(textureName, maxDamage, defensePoints, toughness, 0);
        }

        public Properties(String textureName, double maxDamage, double defensePoints, double toughness, int enchantability)
        {
            this(textureName, maxDamage, defensePoints, toughness, enchantability, null);
        }

        public Properties(String textureName, double maxDamage, double defensePoints, double toughness, int enchantability, Item repairItem)
        {
            this(textureName, maxDamage, defensePoints, toughness, enchantability, repairItem, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        }

        public Properties(String textureName, double maxDamage, double defensePoints, double toughness, int enchantability, Item repairItem, SoundEvent sound)
        {
            this.setTextureName(textureName);
            this.setMaxDamage(maxDamage);
            this.setDefensePoints(defensePoints);
            this.setToughness(toughness);
            this.setEnchantability(enchantability);
            this.setRepairItem(repairItem);
            this.setSound(sound);
        }

        public Properties(Properties other)
        {
            this.setTextureName(other.getTextureName());
            this.setMaxDamage(other.getMaxDamage());
            this.setDefensePoints(other.getDefensePoints());
            this.setToughness(other.getToughness());
            this.setEnchantability(other.getEnchantability());
            this.setRepairItem(other.getRepairItem());
            this.setSound(other.getSound());
        }

        // Accessors

        public String getTextureName()
        {
            return this.textureName;
        }

        public int getMaxDamage()
        {
            return (int)Math.round(this.maxDamage);
        }

        public double getDefensePoints()
        {
            return this.defensePoints;
        }

        public double getToughness()
        {
            return this.toughness;
        }

        public int getEnchantability()
        {
            return this.enchantability;
        }

        public Item getRepairItem()
        {
            return this.repairItem;
        }

        public SoundEvent getSound()
        {
            return this.sound;
        }

        // Mutators

        public ItemCustomArmor.Properties setTextureName(String textureName)
        {
            this.textureName = textureName;
            return this;
        }

        public ItemCustomArmor.Properties setMaxDamage(double maxDamage)
        {
            this.maxDamage = maxDamage;
            return this;
        }

        public ItemCustomArmor.Properties modifyMaxDamage(double a, double b)
        {
            this.setMaxDamage(a * this.getMaxDamage() + b);
            return this;
        }

        public ItemCustomArmor.Properties setDefensePoints(double defensePoints)
        {
            this.defensePoints = defensePoints;
            return this;
        }

        public ItemCustomArmor.Properties modifyDefensePoints(double a, double b)
        {
            this.setDefensePoints(a * this.getDefensePoints() + b);
            return this;
        }

        public ItemCustomArmor.Properties setToughness(double toughness)
        {
            this.toughness = toughness;
            return this;
        }

        public ItemCustomArmor.Properties modifyToughness(float a, float b)
        {
            this.setToughness(a * this.getToughness() + b);
            return this;
        }

        public ItemCustomArmor.Properties setEnchantability(int enchantability)
        {
            this.enchantability = enchantability;
            return this;
        }

        public ItemCustomArmor.Properties modifyEnchantability(double a, double b)
        {
            this.setEnchantability((int)Math.round(a * this.getEnchantability() + b));
            return this;
        }

        public ItemCustomArmor.Properties setRepairItem(Item repairItem)
        {
            this.repairItem = repairItem;
            return this;
        }

        public ItemCustomArmor.Properties setSound(SoundEvent sound)
        {
            this.sound = sound;
            return this;
        }

        // Special functions

        public void applyAttributeModifiers(int armorIndex, Multimap<String, AttributeModifier> multimap)
        {
            multimap.removeAll(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName());
            multimap.removeAll(SharedMonsterAttributes.field_189429_h.getAttributeUnlocalizedName());
            multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[armorIndex], "Armor modifier", this.defensePoints, 0));
            multimap.put(SharedMonsterAttributes.field_189429_h.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[armorIndex], "Armor toughness", this.toughness, 0));
        }

        public ItemCustomArmor.Properties adjustFor(EntityEquipmentSlot armorSlot)
        {
            return this.adjustFor(armorSlot.getIndex());
        }

        public ItemCustomArmor.Properties adjustFor(int armorIndex)
        {
            modifyMaxDamage(MAX_DAMAGE_ARRAY[armorIndex], 0);
            modifyDefensePoints(ARMOR_CONVERSION[armorIndex], 0);
            modifyToughness(ARMOR_CONVERSION[armorIndex], 0);
            return this;
        }
    }
}
