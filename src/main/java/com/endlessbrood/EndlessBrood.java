package com.endlessbrood;

import com.endlessbrood.common.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = EndlessBrood.MODID, name = "Endless Brood", version = "1.0.0-rc2")
public final class EndlessBrood {
    public static final String MODID = "endlessbrood";
    public static ItemBroodEgg parasiteEgg;
    public static Item chitin;
    public static Item serum;
    public static Item scanner;
    public static Block nest;
    public static Block taintedSoil;
    public static Block ward;

    @Mod.Instance(MODID)
    public static EndlessBrood instance;

    @SidedProxy(clientSide = "com.endlessbrood.client.ClientProxy",
                serverSide = "com.endlessbrood.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BroodConfig.load(event.getSuggestedConfigurationFile());
        EntityRegistry.registerModEntity(EntityParasite.class, "parasite", 0, this, 64, 3, true);
        parasiteEgg = new ItemBroodEgg();
        GameRegistry.registerItem(parasiteEgg, "parasiteEgg");
        chitin = new Item().setUnlocalizedName("endlessbrood.chitin")
            .setTextureName("endlessbrood:chitin").setCreativeTab(CreativeTabs.tabMaterials);
        GameRegistry.registerItem(chitin, "chitin");
        serum = new ItemBroodSerum();
        GameRegistry.registerItem(serum, "serum");
        scanner = new ItemBroodScanner();
        GameRegistry.registerItem(scanner, "scanner");
        nest = new BlockBroodNest();
        GameRegistry.registerBlock(nest, "nest");
        taintedSoil = new BlockTaintedSoil();
        GameRegistry.registerBlock(taintedSoil, "taintedSoil");
        ward = new BlockBroodWard();
        GameRegistry.registerBlock(ward, "ward");
        GameRegistry.registerWorldGenerator(new BroodWorldGenerator(), 0);
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addShapelessRecipe(new ItemStack(serum, 2),
            Items.ghast_tear, Items.sugar, Items.glass_bottle, chitin);
        GameRegistry.addRecipe(new ItemStack(ward),
            "IRI", "RCR", "IRI", 'I', Items.iron_ingot, 'R', Items.redstone, 'C', chitin);
        GameRegistry.addRecipe(new ItemStack(scanner),
            " R ", "ICI", " G ", 'R', Items.redstone, 'I', Items.iron_ingot,
            'C', Items.compass, 'G', chitin);
        if (BroodConfig.spawnWeight > 0) EntityRegistry.addSpawn(EntityParasite.class,
            BroodConfig.spawnWeight, 1, 2, EnumCreatureType.monster,
            BiomeGenBase.plains, BiomeGenBase.forest, BiomeGenBase.taiga,
            BiomeGenBase.swampland, BiomeGenBase.extremeHills,
            BiomeGenBase.jungle, BiomeGenBase.savanna);
        MinecraftForge.EVENT_BUS.register(new InfectionEvents());
        MinecraftForge.EVENT_BUS.register(new BroodAdaptationEvents());
        MinecraftForge.EVENT_BUS.register(new BroodCombatEvents());
        MinecraftForge.EVENT_BUS.register(new BroodSerumEvents());
        FMLCommonHandler.instance().bus().register(new BroodOutbreakTicker());
        FMLCommonHandler.instance().bus().register(new EvolutionTicker());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new EvolutionCommand());
    }
}
