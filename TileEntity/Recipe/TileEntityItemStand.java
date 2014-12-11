/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Auxiliary.Interfaces.ItemOnRightClick;
import Reika.ChromatiCraft.Base.TileEntity.InventoriedChromaticBase;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Render.Particle.EntityCenterBlurFX;
import Reika.DragonAPI.Instantiable.InertItem;
import Reika.DragonAPI.Instantiable.Data.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityItemStand extends InventoriedChromaticBase implements ItemOnRightClick {

	private InertItem item;
	private Coordinate tile;
	private int clickTick = 0;

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (world.isRemote && tile != null) {
			TileEntityCastingTable te = (TileEntityCastingTable)tile.getTileEntity(world);
			if (te != null && te.getCraftingTick() > 0) {
				this.spawnCraftParticles(world, x, y, z);
			}
		}
		if (clickTick > 0)
			clickTick--;
	}

	private boolean recentClicked() {
		return clickTick > 0;
	}

	@SideOnly(Side.CLIENT)
	private void spawnCraftParticles(World world, int x, int y, int z) {
		if (rand.nextInt(32) == 0) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.375);
			double rz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.375);
			EntityFX fx = new EntityCenterBlurFX(world, rx, y, rz, 0, 0.1, 0);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	protected void onFirstTick(World world, int x, int y, int z) {
		this.updateItem();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack is, int side) {
		return false;
	}

	@Override
	public ItemStack onRightClickWith(ItemStack item, EntityPlayer ep) {
		int has = inv[0] != null ? inv[0].stackSize : 0;
		int sum = item != null ? has+item.stackSize : has;
		boolean all = this.recentClicked() && ReikaItemHelper.matchStacks(item, inv[0]) && item != null && sum <= item.getMaxStackSize();
		if (!all)
			this.dropSlot();
		ItemStack put = item != null ? (all ? ReikaItemHelper.getSizedItemStack(item, sum) : ReikaItemHelper.getSizedItemStack(item, 1)) : null;
		inv[0] = put;
		this.updateItem();
		if (item != null) {
			if (all)
				item = null;
			else
				item.stackSize--;
		}
		clickTick = 10;
		return item;
	}

	private void updateItem() {
		item = inv[0] != null ? new InertItem(worldObj, ReikaItemHelper.getSizedItemStack(inv[0], 1)) : null;
		if (worldObj != null) {
			TileEntity te = tile != null ? tile.getTileEntity(worldObj) : null;
			if (te instanceof TileEntityCastingTable) {
				((TileEntityCastingTable)te).markDirty();
			}
		}
	}

	public EntityItem getItem() {
		return item;
	}

	private void dropSlot() {
		if (inv[0] != null)
			ReikaItemHelper.dropItem(worldObj, xCoord+0.5, yCoord+1, zCoord+0.5, inv[0]);
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.STAND;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public void readFromNBT(NBTTagCompound NBT) {
		super.readFromNBT(NBT);

		this.updateItem();

		if (NBT.hasKey("table"))
			tile = Coordinate.readFromNBT("table", NBT);
	}

	@Override
	public void writeToNBT(NBTTagCompound NBT) {
		super.writeToNBT(NBT);

		if (tile != null)
			tile.writeToNBT("table", NBT);
	}

	public void setTable(TileEntityCastingTable te) {
		tile = te != null ? new Coordinate(te) : null;
	}

}
