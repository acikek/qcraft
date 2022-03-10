package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.advancement.Criteria;
import com.acikek.qcraft.block.BlockItemProvider;
import com.acikek.qcraft.block.QuantumOre;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.world.state.QBlockData;
import com.acikek.qcraft.world.state.QuantumComputerData;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import com.acikek.qcraft.world.state.location.QuantumComputerLocation;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class QuantumComputer extends Block implements BlockItemProvider, BlockEntityProvider, InventoryProvider {

    public static final BooleanProperty ENTANGLED = BooleanProperty.of("entangled");

    public static final Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.METAL).strength(5.0f, 10.0f);

    public QuantumComputer() {
        super(DEFAULT_SETTINGS);
        setDefaultState(getStateManager().getDefaultState().with(ENTANGLED, false));
    }

    public static class Result<T> {

        public Optional<T> value = Optional.empty();
        public Optional<Text> error = Optional.empty();

        public Result(T value) {
            this.value = Optional.of(value);
        }

        public Result(Text error) {
            this.error = Optional.of(error);
        }

        public static class Value {

            public int[] offsets;
            public int height;

            public Value(int[] offsets, int height) {
                this.offsets = offsets;
                this.height = height;
            }

            public int[] getDimensions() {
                return new int[] {
                        offsets[2] + offsets[3],
                        offsets[0] + offsets[1],
                        height
                };
            }
        }
    }

    public static int[] getPylonOffsets(World world, BlockPos pos) {
        int[] offsets = new int[4];
        QBlockData data = QBlockData.get(world, true);
        for (int offset = 0; offset < offsets.length; offset++) {
            QBlock.Face face = QBlock.Face.CARDINALS[offset];
            for (int value = 1; value <= 8; value++) {
                BlockPos newPos = pos.offset(face.direction, value);
                Optional<QBlockLocation> loc = data.locations.get(newPos);
                if (loc.isEmpty()) {
                    continue;
                }
                QBlockLocation location = loc.get();
                if (location.isPylonBase(face.getOpposite())) {
                    offsets[offset] = value;
                }
            }
        }
        return offsets;
    }

    public static int getPylonHeight(World world, BlockPos pos) {
        int height = 0;
        do {
            height++;
        }
        while (world.getBlockState(pos.add(0, height, 0)).isOf(Blocks.OBSIDIAN));
        return height;
    }

    public static Result<Result.Value> getPylons(World world, BlockPos pos) {
        int[] offsets = getPylonOffsets(world, pos);
        MutableText missing = null;
        boolean multiple = false;
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] == 0) {
                TranslatableText name = QBlock.Face.CARDINALS[i].text;
                if (missing == null) {
                    missing = name.copy();
                }
                else {
                    missing.append(", ").append(name);
                    multiple = true;
                }
            }
        }
        if (missing != null) {
            String error = "error.qcraft.pylon_missing." + (multiple ? "multiple" : "single");
            return new Result<>(new TranslatableText(error).append(": ").append(missing));
        }
        int height = 0;
        for (int i = 0; i < offsets.length; i++) {
            QBlock.Face face = QBlock.Face.CARDINALS[i];
            int pylonHeight = getPylonHeight(world, pos.offset(face.direction, offsets[i]));
            if (height == 0) {
                height = pylonHeight;
            }
            else if (height != pylonHeight) {
                return new Result<>(new TranslatableText("error.qcraft.pylon_misaligned", face.text, pylonHeight, height));
            }
        }
        return new Result<>(new Result.Value(offsets, height));
    }

    public static Result<Boolean> validateConnection(Result.Value here, Result.Value other) {
        for (int i = 0; i < here.offsets.length; i++) {
            if (here.offsets[i] != other.offsets[i]) {
                return new Result<>(new TranslatableText("error.qcraft.pylon_distances", QBlock.Face.CARDINALS[i].text, here.offsets[i], other.offsets[i]));
            }
        }
        if (here.height != other.height) {
            return new Result<>(new TranslatableText("error.qcraft.pylon_heights", here.height, other.height));
        }
        return new Result<>(true);
    }

    public static Iterable<BlockPos> collectPositions(BlockPos pos, Result.Value result) {
        BlockPos corner1 = pos.add(result.offsets[2] - 1, 0, -result.offsets[0] + 1);
        BlockPos corner2 = pos.add(-result.offsets[3] + 1, result.height - 1, result.offsets[1] - 1);
        return BlockPos.iterate(corner1, corner2);
    }

    public static List<BlockState> collectStates(World world,Iterable<BlockPos> positions) {
        List<BlockState> result = new ArrayList<>();
        for (BlockPos pos : positions) {
            result.add(world.getBlockState(pos));
        }
        return result;
    }

    public static void setStates(World world, Iterable<BlockPos> positions, List<BlockState> states) {
        Iterator<BlockPos> iter = positions.iterator();
        for (BlockState state : states) {
            if (iter.hasNext()) {
                BlockPos pos = iter.next();
                if (!world.getBlockState(pos).isOf(com.acikek.qcraft.block.Blocks.QUANTUM_COMPUTER)) {
                    world.setBlockState(pos, state);
                }
            }
        }
    }

    public static void playEffects(World world, BlockPos pos) {
        QuantumOre.spawnParticles(world, pos);
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 3.0f, 1.0f, false);
    }

    public static void teleport(World world, BlockPos pos, PlayerEntity player) {
        QuantumComputerData data = QuantumComputerData.get(world);
        data.locations.get(pos).ifPresent(location -> data.frequencies.ifPresent(location, pair -> {
            QuantumComputerLocation other = pair.getOther(location);
            if (other != null) {
                Result<Result.Value> result = getPylons(world, location.pos);
                if (result.error.isPresent()) {
                    player.sendMessage(result.error.get(), false);
                    return;
                }
                Result<Result.Value> otherResult = getPylons(world, other.pos);
                if (otherResult.error.isPresent()) {
                    player.sendMessage(otherResult.error.get(), false);
                    return;
                }
                Result.Value pylons = result.value.get();
                Result.Value otherPylons = otherResult.value.get();
                Result<Boolean> valid = validateConnection(pylons, otherPylons);
                if (valid.error.isPresent()) {
                    player.sendMessage(valid.error.get(), false);
                    return;
                }
                Iterable<BlockPos> positions = collectPositions(location.pos, pylons);
                Iterable<BlockPos> otherPositions = collectPositions(other.pos, otherPylons);
                List<BlockState> states = collectStates(world, positions);
                List<BlockState> otherStates = collectStates(world, otherPositions);
                setStates(world, positions, otherStates);
                setStates(world, otherPositions, states);
                Criteria.QUANTUM_TELEPORTATION.trigger((ServerPlayerEntity) player, pylons.getDimensions());
            }
        }));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        /*if (hand == Hand.MAIN_HAND) {
            if (!world.isClient()) {
                teleport(world, pos, player);
            }
            playEffects(world, pos);
        }
        return super.onUse(state, world, pos, player, hand, hit);*/
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.SUCCESS;
    }

    public void remove(World world, BlockPos pos) {
        if (!world.isClient()) {
            QuantumComputerData data = QuantumComputerData.get(world);
            data.locations.get(pos).ifPresent(location -> data.remove(location, false));
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        remove(world, pos);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        remove(world, pos);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ENTANGLED);
    }

    @Override
    public BiFunction<Block, Item.Settings, BlockItem> getBlockItem() {
        return QuantumComputerItem::new;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumComputerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) blockEntity : null;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return ((QuantumComputerBlockEntity) world.getBlockEntity(pos));
    }
}
