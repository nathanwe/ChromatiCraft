/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft;

import java.util.HashMap;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import Reika.ChromatiCraft.Auxiliary.ChromaRenderList;
import Reika.ChromatiCraft.Base.ChromaRenderBase;
import Reika.ChromatiCraft.Models.ColorizableSlimeModel;
import Reika.ChromatiCraft.Registry.ChromaItems;
import Reika.ChromatiCraft.Registry.ChromaOptions;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Render.ChromaItemRenderer;
import Reika.ChromatiCraft.Render.EnderCrystalRenderer;
import Reika.ChromatiCraft.Render.ISBRH.CrystalRenderer;
import Reika.ChromatiCraft.Render.ISBRH.DecoPlantRenderer;
import Reika.ChromatiCraft.Render.ISBRH.FiberRenderer;
import Reika.ChromatiCraft.Render.ISBRH.LampRenderer;
import Reika.ChromatiCraft.Render.ISBRH.PowerTreeRenderer;
import Reika.ChromatiCraft.Render.ISBRH.RuneRenderer;
import Reika.ChromatiCraft.Render.ISBRH.TankBlockRenderer;
import Reika.ChromatiCraft.Render.ISBRH.TieredOreRenderer;
import Reika.ChromatiCraft.Render.ISBRH.TieredPlantRenderer;
import Reika.ChromatiCraft.Render.TESR.CrystalPlantRenderer;
import Reika.ChromatiCraft.TileEntity.Plants.TileEntityCrystalPlant;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.KeybindHandler;
import Reika.DragonAPI.Instantiable.IO.SoundLoader;
import Reika.DragonAPI.Instantiable.Rendering.ForcedTextureArmorModel;
import Reika.DragonAPI.Instantiable.Rendering.ItemSpriteSheetRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ChromaClient extends ChromaCommon {

	public static final ItemSpriteSheetRenderer[] items = {
		new ItemSpriteSheetRenderer(ChromatiCraft.instance, ChromatiCraft.class, "Textures/Items/items.png"),
		new ItemSpriteSheetRenderer(ChromatiCraft.instance, ChromatiCraft.class, "Textures/Items/items2.png"),
	};

	//public static final ItemMachineRenderer machineItems = new ItemMachineRenderer();

	private static final HashMap<ChromaItems, ForcedTextureArmorModel> armorTextures = new HashMap();
	private static final HashMap<ChromaItems, String> armorAssets = new HashMap();

	private static final ChromaItemRenderer placer = new ChromaItemRenderer();

	private static final CrystalRenderer crystal = new CrystalRenderer();
	private static final RuneRenderer rune = new RuneRenderer();
	private static final TankBlockRenderer tank = new TankBlockRenderer();
	private static final PowerTreeRenderer tree = new PowerTreeRenderer();
	private static final LampRenderer lamp = new LampRenderer();
	private static FiberRenderer fiber;

	private static final TieredOreRenderer ore = new TieredOreRenderer();
	public static final TieredPlantRenderer plant = new TieredPlantRenderer();

	public static final DecoPlantRenderer plant2 = new DecoPlantRenderer();

	private static final EnderCrystalRenderer csr = new EnderCrystalRenderer();

	public static KeyBinding key_ability;

	@Override
	public void registerSounds() {
		new SoundLoader(ChromaSounds.soundList).register();
	}

	@Override
	public void registerRenderers() {
		if (DragonOptions.NORENDERS.getState()) {
			ChromatiCraft.logger.log("Disabling all machine renders for FPS and lag profiling.");
		}
		else {
			this.loadModels();
		}

		//RenderingRegistry.registerEntityRenderingHandler(EntityRailGunShot.class, new RenderRailGunShot());

		this.registerSpriteSheets();
		this.registerBlockSheets();

		RenderSlime slimeRenderer = (RenderSlime)RenderManager.instance.entityRenderMap.get(EntitySlime.class);
		slimeRenderer.scaleAmount = new ColorizableSlimeModel(0);
		ChromatiCraft.logger.log("Overriding Slime Renderer Edge Model.");
	}

	@Override
	public void registerKeys() {
		if (ChromaOptions.KEYBINDABILITY.getState()) {
			key_ability = new KeyBinding("Use Ability", -98, "ChromatiCraft"); //Middle mouse
			//ClientRegistry.registerKeyBinding(key_ability);
			KeybindHandler.instance.addKeybind(key_ability);
		}
	}

	@Override
	public void addArmorRenders() {
		/*
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/bedrock_1.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/bedrock_2.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/steel_1.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/steel_2.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/IOGoggles.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/NVGoggles.png");
		ReikaTextureHelper.forceArmorTexturePath("/Reika/ChromatiCraft/Textures/Misc/NVHelmet.png");*/

		//addArmorTexture(ChromatiItems.JETPACK, "/Reika/ChromatiCraft/Textures/Misc/jet.png");
	}

	private static void addArmorTexture(ChromaItems item, String tex) {
		ChromatiCraft.logger.log("Adding armor texture for "+item+": "+tex);
		armorTextures.put(item, new ForcedTextureArmorModel(ChromatiCraft.class, tex, item.getArmorType()));
		String[] s = tex.split("/");
		String file = s[s.length-1];
		String defaultTex = "Chromaticraft:textures/models/armor/"+file;
		//ReikaJavaLibrary.pConsole(defaultTex);
		armorAssets.put(item, defaultTex);
	}

	public static ForcedTextureArmorModel getArmorRenderer(ChromaItems item) {
		return armorTextures.get(item);
	}

	public static String getArmorTextureAsset(ChromaItems item) {
		return armorAssets.get(item);
	}

	public void loadModels() {
		for (int i = 0; i < ChromaTiles.TEList.length; i++) {
			ChromaTiles m = ChromaTiles.TEList[i];
			if (m.hasRender()) {
				ChromaRenderBase render = (ChromaRenderBase)ChromaRenderList.instantiateRenderer(m);
				//int[] renderLists = render.createLists();
				//GLListData.addListData(m, renderLists);
				ClientRegistry.bindTileEntitySpecialRenderer(m.getTEClass(), render);
			}
		}
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrystalPlant.class, new CrystalPlantRenderer());

		MinecraftForgeClient.registerItemRenderer(ChromaItems.PLACER.getItemInstance(), placer);
		MinecraftForgeClient.registerItemRenderer(ChromaItems.RIFT.getItemInstance(), placer);

		crystalRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(crystalRender, crystal);

		runeRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(runeRender, rune);

		tankRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(tankRender, tank);

		treeRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(treeRender, tree);

		lampRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(lampRender, lamp);

		fiberRender = RenderingRegistry.getNextAvailableRenderId();
		fiber = new FiberRenderer(fiberRender);
		RenderingRegistry.registerBlockHandler(fiberRender, fiber);

		oreRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(oreRender, ore);
		plantRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(plantRender, plant);

		plantRender2 = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(plantRender2, plant2);

		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGuardianStone.class, new GuardianStoneRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrystalPlant.class, new CrystalPlantRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAccelerator.class, new AcceleratorRenderer());

		//MinecraftForgeClient.registerItemRenderer(ChromaBlocks.GUARDIAN.getItem(), teibr);
		//MinecraftForgeClient.registerItemRenderer(ChromaBlocks.ACCELERATOR.getItem(), teibr);

		MinecraftForgeClient.registerItemRenderer(ChromaItems.ENDERCRYSTAL.getItemInstance(), csr);
	}


	private void registerBlockSheets() {
		//RenderingRegistry.registerBlockHandler(BlockSheetTexRenderID, block);
	}

	private void registerSpriteSheets() {
		for (int i = 0; i < ChromaItems.itemList.length; i++) {
			ChromaItems c = ChromaItems.itemList[i];
			if (!c.isPlacer() && c != ChromaItems.POTION)
				MinecraftForgeClient.registerItemRenderer(ChromaItems.itemList[i].getItemInstance(), items[ChromaItems.itemList[i].getTextureSheet()]);
		}
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

}
