package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.block.BlockItemProvider;
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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.*;
import java.util.function.BiFunction;

public class QuantumComputer extends Block implements BlockItemProvider, BlockEntityProvider {

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

    public static Result<Pair<int[], Integer>> getPylons(World world, BlockPos pos) {
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
        return new Result<>(new Pair<>(offsets, height));
    }

    public static Result<Boolean> validateConnection(Pair<int[], Integer> left, Pair<int[], Integer> right) {
        if (!Objects.equals(left.getRight(), right.getRight())) {
            return new Result<>(new TranslatableText("error.qcraft.pylon_heights", left.getRight(), right.getRight()));
        }
        for (int i = 0; i < left.getLeft().length; i++) {
            int here = left.getLeft()[i];
            int other = right.getLeft()[i];
            if (here != other) {
                return new Result<>(new TranslatableText("error.qcraft.pylon_distances", QBlock.Face.CARDINALS[i].text, here, other));
            }
        }
        return new Result<>(true);
    }

    public static Iterable<BlockPos> collectPositions(BlockPos pos, Pair<int[], Integer> result) {
        int[] offsets = result.getLeft();
        int height = result.getRight();
        BlockPos corner1 = pos.add(offsets[2] - 1, 0, -offsets[0] + 1);
        BlockPos corner2 = pos.add(-offsets[3] + 1, height - 1, offsets[1] - 1);
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

    public static void teleport(World world, BlockPos pos, PlayerEntity player) {
        QuantumComputerData data = QuantumComputerData.get(world);
        data.locations.get(pos).ifPresent(location -> data.frequencies.ifPresent(location, pair -> {
            QuantumComputerLocation other = pair.getOther(location);
            if (other != null) {
                Result<Pair<int[], Integer>> pylons = getPylons(world, location.pos);
                if (pylons.error.isPresent()) {
                    player.sendMessage(pylons.error.get(), false);
                    return;
                }
                Result<Pair<int[], Integer>> otherPylons = getPylons(world, other.pos);
                if (otherPylons.error.isPresent()) {
                    player.sendMessage(otherPylons.error.get(), false);
                    return;
                }
                Result<Boolean> valid = validateConnection(pylons.value.get(), otherPylons.value.get());
                if (valid.error.isPresent()) {
                    player.sendMessage(valid.error.get(), false);
                    return;
                }
                Iterable<BlockPos> positions = collectPositions(location.pos, pylons.value.get());
                Iterable<BlockPos> otherPositions = collectPositions(other.pos, otherPylons.value.get());
                List<BlockState> states = collectStates(world, positions);
                List<BlockState> otherStates = collectStates(world, otherPositions);
                setStates(world, positions, otherStates);
                setStates(world, otherPositions, states);
            }
        }));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && !world.isClient()) {
            teleport(world, pos, player);
        }
        return super.onUse(state, world, pos, player, hand, hit);
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
}
