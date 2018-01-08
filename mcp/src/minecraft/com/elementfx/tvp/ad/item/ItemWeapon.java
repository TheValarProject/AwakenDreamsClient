package com.elementfx.tvp.ad.item;

import java.util.List;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWeapon extends Item
{
    private final float attackDamage, attackSpeed;
    private final int enchantability;
    private final boolean isFull3d;
    private final Item repairItem;
    private final boolean detectsOrcs;
    private boolean glows;
    
    public ItemWeapon(int maxUses, float damageVsEntity) {
    	this(maxUses, damageVsEntity, false);
    }
    
    public ItemWeapon(int maxUses, float damageVsEntity, boolean detectIn) {
    	this(maxUses, damageVsEntity, -2.4F, 0, true, null, detectIn);
    }

    public ItemWeapon(int maxUses, float damageVsEntity, float attackSpeed, int enchantability, boolean renderFull3D, Item repairItem, boolean detectIn) {
    	this.setCreativeTab(CreativeTabs.COMBAT);
    	this.setMaxStackSize(1);
        this.setMaxDamage(maxUses);
        this.attackDamage = damageVsEntity; // TODO add ability to map special damage value for specific entities
        this.attackSpeed = attackSpeed;
        this.enchantability = enchantability;
        this.isFull3d = renderFull3D;
        this.repairItem = repairItem;
        this.detectsOrcs = detectIn;
        this.glows = false;
    }
    
    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	if(detectsOrcs)
    	{
            BlockPos pos = entityIn.getPosition();
            List<EntityMob> list = worldIn.<EntityMob>getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)pos.getX() - 20.0D, (double)pos.getY() - 20.0D, (double)pos.getZ() - 20.0D, (double)pos.getX() + 20.0D, (double)pos.getY() + 20.0D, (double)pos.getZ() + 20.0D));

            if(list.isEmpty())
            {
            	this.glows = false;
            }
            else
            {
            	this.glows = true;
            }
    	}
    }
    
    public boolean hasEffect(ItemStack stack)
    {
        return glows;
    }
    
    /**
     * Returns the amount of damage this item will deal. One heart of damage is equal to 2 damage points.
     */
    public float getDamageVsEntity()
    {
        return this.attackDamage;
    }
    
    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        Block block = state.getBlock();

        if (block == Blocks.WEB)
        {
            return 15.0F;
        }
        else
        {
            Material material = state.getMaterial();
            return material != Material.PLANTS && material != Material.VINE && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
        }
    }
    
    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        stack.damageItem(1, attacker);
        return true;
    }
    
    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        if ((double)state.getBlockHardness(worldIn, pos) != 0.0D)
        {
            stack.damageItem(2, entityLiving);
        }

        return true;
    }
    
    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    public boolean isFull3D()
    {
        return true;
    }
    
    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(IBlockState blockIn)
    {
        return blockIn.getBlock() == Blocks.WEB;
    }
    
    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return this.enchantability;
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        return this.repairItem == repair.getItem() ? true : super.getIsRepairable(toRepair, repair);
    }
    
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
        }

        return multimap;
    }
}
