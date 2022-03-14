package com.acikek.qcraft.block;

import com.acikek.qcraft.QCraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

import java.util.Arrays;
import java.util.Random;

public class QuantumOre extends RedstoneOreBlock {

    public static final BooleanProperty LIT = RedstoneOreBlock.LIT;

    public static final Settings QUANTUM_ORE_SETTINGS = FabricBlockSettings
            .of(Material.STONE)
            .requiresTool()
            .strength(3.0f, 5.0f)
            .luminance((state) -> state.get(LIT) ? 9 : 0);

    public static final Settings DEEPSLATE_QUANTUM_ORE_SETTINGS = FabricBlockSettings
            .copyOf(QUANTUM_ORE_SETTINGS)
            .strength(4.5f, 5.0f)
            .sounds(BlockSoundGroup.DEEPSLATE);

    public static ConfiguredFeature<?, ?> QUANTUM_ORE_CONFIGURED_FEATURE;
    public static ConfiguredFeature<?, ?> DEEPSLATE_QUANTUM_ORE_CONFIGURED_FEATURE;
    public static PlacedFeature QUANTUM_ORE_PLACED_FEATURE;
    public static PlacedFeature DEEPSLATE_QUANTUM_ORE_PLACED_FEATURE;

    public static void createFeatures() {

        QUANTUM_ORE_CONFIGURED_FEATURE = new ConfiguredFeature<>(
                Feature.ORE,
                new OreFeatureConfig(
                        OreConfiguredFeatures.STONE_ORE_REPLACEABLES,
                        Blocks.QUANTUM_ORE.getDefaultState(),
                        8
                )
        );
        DEEPSLATE_QUANTUM_ORE_CONFIGURED_FEATURE = new ConfiguredFeature<>(
                Feature.ORE,
                new OreFeatureConfig(
                        OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
                        Blocks.DEEPSLATE_QUANTUM_ORE.getDefaultState(),
                        8
                )
        );
        QUANTUM_ORE_PLACED_FEATURE = new PlacedFeature(
                RegistryEntry.of(QUANTUM_ORE_CONFIGURED_FEATURE),
                Arrays.asList(
                        CountPlacementModifier.of(8),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(15))
                )
        );
        DEEPSLATE_QUANTUM_ORE_PLACED_FEATURE = new PlacedFeature(
                RegistryEntry.of(DEEPSLATE_QUANTUM_ORE_CONFIGURED_FEATURE),
                Arrays.asList(
                    CountPlacementModifier.of(8),
                    SquarePlacementModifier.of(),
                    HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-32), YOffset.aboveBottom(32))
                )
        );
    }

    // RGB(30, 199, 106)
    public static final int CONDENSED_GREEN = 2017130;

    public static final DustParticleEffect DUST_PARTICLE = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(CONDENSED_GREEN)), 1.0f);

    public QuantumOre(Settings settings) {
        super(settings);
    }

    public static void spawnParticles(World world, BlockPos pos) {
        Random random = world.random;
        for (Direction direction : Direction.values()) {
            BlockPos blockPos = pos.offset(direction);
            if (!world.getBlockState(blockPos).isOpaqueFullCube(world, blockPos)) {
                Direction.Axis axis = direction.getAxis();
                double e = axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getOffsetX() : (double) random.nextFloat();
                double f = axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getOffsetY() : (double) random.nextFloat();
                double g = axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getOffsetZ() : (double) random.nextFloat();
                world.addParticle(DUST_PARTICLE, (double) pos.getX() + e, (double) pos.getY() + f, (double) pos.getZ() + g, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void light(BlockState state, World world, BlockPos pos) {
        spawnParticles(world, pos);
        if (!state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, true), 3);
        }
    }

    // Override these methods with the exact same code since RedstoneOreBlock uses private static implementations of
    // spawnParticles and light

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            spawnParticles(world, pos);
        } else {
            light(state, world, pos);
        }
        ItemStack itemStack = player.getStackInHand(hand);
        return itemStack.getItem() instanceof BlockItem && (new ItemPlacementContext(player, hand, itemStack, hit)).canPlace() ? ActionResult.PASS : ActionResult.SUCCESS;
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        light(state, world, pos);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        light(state, world, pos);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            spawnParticles(world, pos);
        }
    }

    public static void registerFeature(String name, ConfiguredFeature<?, ?> configuredFeature, PlacedFeature placedFeature) {
        Identifier id = QCraft.id(name);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, configuredFeature);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature);
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(Registry.PLACED_FEATURE_KEY, id)
        );
    }

    public static void registerFeatures() {
        registerFeature("ore_quantum", QUANTUM_ORE_CONFIGURED_FEATURE, QUANTUM_ORE_PLACED_FEATURE);
        registerFeature("ore_quantum_lower", DEEPSLATE_QUANTUM_ORE_CONFIGURED_FEATURE, DEEPSLATE_QUANTUM_ORE_PLACED_FEATURE);
    }
}
