/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Processing;

import net.minecraft.block.Block;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import Reika.ChromatiCraft.Base.TileEntity.InventoriedRelayPowered;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Interfaces.Registry.OreType;
import Reika.DragonAPI.Interfaces.Registry.OreType.OreRarity;
import Reika.DragonAPI.Interfaces.TileEntity.XPProducer;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.GeoStrata.Registry.RockTypes;
import Reika.RotaryCraft.Auxiliary.RecipeManagers.ExtractorModOres;

public class TileEntityCrystalFurnace extends InventoriedRelayPowered implements XPProducer {

	private static final ElementTagCompound smelt = new ElementTagCompound();

	public static final int MULTIPLY = 2;
	public static final int SMELT_TIME = 200;

	public int smeltTimer;

	private float xp;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);
		if (this.canSmelt()) {
			smeltTimer += this.getSmeltSpeed();
			if (smeltTimer >= SMELT_TIME) {
				this.smelt();
				smeltTimer = 0;
			}
		}
		else {
			smeltTimer = 0;
		}
	}

	private int getSmeltSpeed() {
		return 1+energy.getTotalEnergy()/12000;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack is, int side) {
		return slot == 1;
	}

	@Override
	public int getSizeInventory() {
		return 3;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return slot == 0 && FurnaceRecipes.smelting().getSmeltingResult(is) != null;
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.FURNACE;
	}

	private boolean canSmelt() {
		if (inv[0] == null)
			return false;
		if (!energy.containsAtLeast(smelt))
			return false;
		ItemStack out = FurnaceRecipes.smelting().getSmeltingResult(inv[0]);
		if (out == null)
			return false;
		out = out.copy();
		out.stackSize *= this.getMultiplyRate(inv[0], out);
		if (inv[1] == null)
			return true;
		if (!ReikaItemHelper.matchStacks(out, inv[1]) || !ItemStack.areItemStackTagsEqual(out, inv[1]))
			return false;
		if (out.stackSize+inv[1].stackSize > Math.min(this.getInventoryStackLimit(), out.getMaxStackSize()))
			return false;
		return true;
	}

	private void smelt() {
		ItemStack is = FurnaceRecipes.smelting().getSmeltingResult(inv[0]).copy();
		ElementTagCompound tag = getSmeltingCost(inv[0], is);
		is.stackSize *= this.getMultiplyRate(inv[0], is);
		ReikaInventoryHelper.addOrSetStack(is, inv, 1);
		xp += FurnaceRecipes.smelting().func_151398_b(inv[0])*6;
		ReikaInventoryHelper.decrStack(0, inv);
		this.drainEnergy(tag);
	}

	public static ElementTagCompound getSmeltingCost(ItemStack in, ItemStack out) {
		ElementTagCompound tag = smelt.copy();
		if (ReikaBlockHelper.isOre(in))
			tag.scale(1.5F);
		else if (ModList.ROTARYCRAFT.isLoaded() && ExtractorModOres.isOreFlake(in)) {
			tag.scale(2F);
		}
		else if (in.getItem() instanceof ItemFood) {
			tag.scale(0.75F);
		}
		return tag.scale((float)Math.pow(getMultiplyRate(in, out)/2F, 2));
	}

	public static int getMultiplyRate(ItemStack in, ItemStack out) {
		int[] ids = OreDictionary.getOreIDs(in);
		if (in.getDisplayName() != null && in.getDisplayName().toLowerCase().contains("cobblestone"))
			return 1;
		else if (ModList.GEOSTRATA.isLoaded() && RockTypes.getTypeFromID(Block.getBlockFromItem(in.getItem())) != null) {
			//ReikaItemHelper.matchStacks(out, RockTypes.getTypeFromID(Block.getBlockFromItem(in.getItem())).getItem(RockShapes.SMOOTH)))
			return 1;
		}
		else if (ReikaBlockHelper.isOre(in)) {
			int ret = 4;
			OreType ore = ReikaOreHelper.getEntryByOreDict(in);
			if (ore == null)
				ore = ModOreList.getModOreFromOre(in);
			if (ore != null) {
				if (ore.getRarity() == OreRarity.RARE) {
					ret = 8;
				}
			}
			return ret;
		}
		for (int i = 0; i < ids.length; i++) {
			String name = OreDictionary.getOreName(ids[i]);
			if (name.startsWith("dust")) //exploits
				return 1;
			else if (name.equalsIgnoreCase("cobblestone")) //exploits
				return 1;
		}
		return MULTIPLY;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public void clearXP() {
		xp = 0;
	}

	@Override
	public float getXP() {
		return xp;
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		smeltTimer = NBT.getInteger("time");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("time", smeltTimer);
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT) {
		super.readFromNBT(NBT);

		xp = NBT.getFloat("xp");
	}

	@Override
	public void writeToNBT(NBTTagCompound NBT) {
		super.writeToNBT(NBT);

		NBT.setFloat("xp", xp);
	}

	static {
		smelt.addTag(CrystalElement.ORANGE, 200);
		smelt.addTag(CrystalElement.YELLOW, 40);
		smelt.addTag(CrystalElement.PURPLE, 100);
	}

	@Override
	protected ElementTagCompound getRequiredEnergy() {
		return smelt.copy();
	}

	@Override
	public int getMaxStorage(CrystalElement e) {
		return 120000;
	}

	public static ElementTagCompound smeltTags() {
		return smelt.copy();
	}

	public int getCookProgressScaled(int a) {
		return smeltTimer * a / SMELT_TIME;
	}

	@Override
	public boolean isAcceptingColor(CrystalElement e) {
		return smelt.contains(e);
	}

	@Override
	protected boolean canReceiveFrom(CrystalElement e, ForgeDirection dir) {
		return true;
	}

}
