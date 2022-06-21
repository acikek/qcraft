package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.advancement.Criteria;
import com.acikek.qcraft.block.BlockItemProvider;
import com.acikek.qcraft.block.QuantumOre;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.sound.Sounds;
import com.acikek.qcraft.world.state.QBlockData;
import com.acikek.qcraft.world.state.QuantumComputerData;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import com.acikek.qcraft.world.state.location.QuantumComputerLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuantumComputer extends Block implements BlockItemProvider, BlockEntityProvider, InventoryProvider {

    public static final BlockSoundGroup SOUND_GROUP = new BlockSoundGroup(
            1.0f,
            1.0f,
            Sounds.QUANTUM_COMPUTER_BREAK,
            SoundEvents.BLOCK_METAL_STEP,
            Sounds.QUANTUM_COMPUTER_PLACE,
            SoundEvents.BLOCK_METAL_HIT,
            SoundEvents.BLOCK_METAL_FALL
    );

    public static final Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.METAL)
            .strength(5.0f, 10.0f)
            .sounds(SOUND_GROUP);

    public QuantumComputer() {
        super(DEFAULT_SETTINGS);
    }

    public static class Result<T> {

        public enum Error {

            MISSING_FREQUENCY("missing_frequency"),
            MISSING_PYLONS("missing_pylons"),
            MISALIGNED("misaligned"),
            PYLON_DISTANCES("pylon_distances"),
            PYLON_HEIGHTS("pylon_heights"),
            MISSING_COUNTERPART("missing_counterpart"),
            ERRORED_COUNTERPART("errored_counterpart");

            public static final Codec<Error> CODEC = Codec.STRING.comapFlatMap(Error::validate, error -> error.id);

            public static DataResult<Error> validate(String id) {
                return switch (id) {
                    case "missing_frequency" -> DataResult.success(MISSING_FREQUENCY);
                    case "missing_pylons" -> DataResult.success(MISSING_PYLONS);
                    case "misaligned" -> DataResult.success(MISALIGNED);
                    case "pylon_distances" -> DataResult.success(PYLON_DISTANCES);
                    case "pylon_heights" -> DataResult.success(PYLON_HEIGHTS);
                    case "missing_counterpart" -> DataResult.success(MISSING_COUNTERPART);
                    case "errored_counterpart" -> DataResult.success(ERRORED_COUNTERPART);
                    default -> DataResult.error("Not a valid Quantum Computer Error: " + id);
                };
            }

            public String id;

            Error(String id) {
                this.id = id;
            }

            public Text getText() {
                return Text.translatable("gui." + QCraft.ID + ".error." + id);
            }
        }

        public Optional<T> value = Optional.empty();
        public Optional<Error> error = Optional.empty();

        public Result(T value) {
            this.value = Optional.of(value);
        }

        public Result(Error error) {
            this.error = Optional.of(error);
        }

        public Result(Optional<T> value, Optional<Error> error) {
            this.value = value;
            this.error = error;
        }

        public static final Codec<Result<Teleportation>> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.optionalField("value", Teleportation.CODEC).forGetter(r -> r.value),
                        Codec.optionalField("error", Error.CODEC).forGetter(r -> r.error)
                )
                        .apply(instance, Result::new)
        );

        public static class Value {

            public static final Codec<Value> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.list(Codec.INT).fieldOf("offsets").forGetter(v -> v.offsets),
                            Codec.INT.fieldOf("height").forGetter(v -> v.height)
                    )
                            .apply(instance, Value::new)
            );

            public List<Integer> offsets;
            public int height;

            public Value(List<Integer> offsets, int height) {
                this.offsets = offsets;
                this.height = height;
            }

            public int[] getDimensions() {
                return new int[] {
                        offsets.get(2) + offsets.get(3),
                        offsets.get(0) + offsets.get(1),
                        height
                };
            }
        }

        public static class Connection {

            public Value here;
            public Value other;

            public Connection(Value here, Value other) {
                this.here = here;
                this.other = other;
            }
        }

        public static class Teleportation extends Connection {

            public static final Codec<Teleportation> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Value.CODEC.fieldOf("here").forGetter(t -> t.here),
                            Value.CODEC.fieldOf("other").forGetter(t -> t.other),
                            BlockPos.CODEC.fieldOf("start").forGetter(t -> t.start),
                            BlockPos.CODEC.fieldOf("end").forGetter(t -> t.end)
                    )
                            .apply(instance, Teleportation::new)
            );

            public BlockPos start;
            public BlockPos end;

            public Teleportation(Value here, Value other, BlockPos start, BlockPos end) {
                super(here, other);
                this.start = start;
                this.end = end;
            }

            public void toBoth(Consumer<BlockPos> consumer) {
                consumer.accept(start);
                consumer.accept(end);
            }

            public static Result<Teleportation> fromConnection(Result<Connection> result, BlockPos start, BlockPos end) {
                if (result.error.isPresent()) {
                    return new Result<>(result.error.get());
                }
                else if (result.value.isPresent()) {
                    Connection connection = result.value.get();
                    return new Result<>(new Teleportation(connection.here, connection.other, start, end));
                }
                return null;
            }
        }
    }

    public static List<Integer> getPylonOffsets(World world, BlockPos pos) {
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
        return Arrays.stream(offsets).boxed().collect(Collectors.toList());
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
        List<Integer> offsets = getPylonOffsets(world, pos);
        for (int offset : offsets) {
            if (offset == 0) {
                return new Result<>(Result.Error.MISSING_PYLONS);
            }
        }
        int height = 0;
        for (int i = 0; i < offsets.size(); i++) {
            QBlock.Face face = QBlock.Face.CARDINALS[i];
            int pylonHeight = getPylonHeight(world, pos.offset(face.direction, offsets.get(i)));
            if (height == 0) {
                height = pylonHeight;
            }
            else if (height != pylonHeight) {
                return new Result<>(Result.Error.MISALIGNED);
            }
        }
        return new Result<>(new Result.Value(offsets, height));
    }

    public static Result<Result.Connection> validateConnection(Result.Value here, Result.Value other) {
        for (int i = 0; i < here.offsets.size(); i++) {
            if (!Objects.equals(here.offsets.get(i), other.offsets.get(i))) {
                return new Result<>(Result.Error.PYLON_DISTANCES);
            }
        }
        if (here.height != other.height) {
            return new Result<>(Result.Error.PYLON_HEIGHTS);
        }
        return new Result<>(new Result.Connection(here, other));
    }

    public static Box collectPositions(BlockPos pos, Result.Value result) {
        BlockPos corner1 = pos.add(result.offsets.get(2) - 1, 0, -result.offsets.get(0) + 1);
        BlockPos corner2 = pos.add(-result.offsets.get(3) + 1, result.height - 1, result.offsets.get(1) - 1);
        return new Box(corner1, corner2);
    }

    public static void setStates(World world, Box positions, List<BlockState> states) {
        Iterator<BlockPos> iter = BlockPos.stream(positions).iterator();
        for (BlockState state : states) {
            if (iter.hasNext()) {
                BlockPos pos = iter.next();
                if (!world.getBlockState(pos).isOf(com.acikek.qcraft.block.Blocks.QUANTUM_COMPUTER)) {
                    world.setBlockState(pos, state);
                }
            }
        }
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), sound, SoundCategory.BLOCKS, 3.0f, 1.0f, false);
    }

    public static void playEffects(World world, BlockPos pos) {
        QuantumOre.spawnParticles(world, pos);
        playSound(world, pos, net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT);
    }

    public static Result<Result.Teleportation> getConnection(World world, BlockPos pos) {
        QuantumComputerData data = QuantumComputerData.get(world);
        AtomicReference<Result<Result.Teleportation>> connection = new AtomicReference<>();
        data.locations.get(pos).ifPresent(location -> {
            if (location.frequency.isEmpty()) {
                connection.set(new Result<>(Result.Error.MISSING_FREQUENCY));
                return;
            }
            data.frequencies.ifPresent(location, pair -> {
                QuantumComputerLocation other = pair.getOther(location);
                if (other == null) {
                    connection.set(new Result<>(Result.Error.MISSING_COUNTERPART));
                    return;
                }
                Result<Result.Value> result = getPylons(world, location.pos);
                if (result.error.isPresent()) {
                    connection.set(new Result<>(result.error.get()));
                    return;
                }
                Result<Result.Value> otherResult = getPylons(world, other.pos);
                if (otherResult.error.isPresent()) {
                    connection.set(new Result<>(Result.Error.ERRORED_COUNTERPART));
                    return;
                }
                if (result.value.isEmpty() || otherResult.value.isEmpty()) {
                    return;
                }
                connection.set(Result.Teleportation.fromConnection(
                        validateConnection(result.value.get(), otherResult.value.get()),
                        location.pos,
                        other.pos
                ));
            });
        });
        return connection.get();
    }

    public static void teleport(World world, PlayerEntity player, Result.Teleportation teleportation) {
        Box positions = collectPositions(teleportation.start, teleportation.here);
        Box otherPositions = collectPositions(teleportation.end, teleportation.other);
        List<BlockState> states = world.getStatesInBox(positions).toList();
        List<BlockState> otherStates = world.getStatesInBox(otherPositions).toList();
        setStates(world, positions, otherStates);
        setStates(world, otherPositions, states);
        teleportation.toBoth(pos -> playEffects(world, pos));
        Criteria.QUANTUM_TELEPORTATION.trigger((ServerPlayerEntity) player, teleportation.here.getDimensions());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND) {
            if (!world.isClient() && world.getBlockEntity(pos) instanceof QuantumComputerBlockEntity blockEntity) {
                blockEntity.result = getConnection(world, pos);
            }
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        }
        return ActionResult.SUCCESS;
    }

    public void remove(World world, BlockPos pos) {
        if (!world.isClient()) {
            QuantumComputerData data = QuantumComputerData.get(world);
            data.locations.get(pos).ifPresent(location -> data.remove(location, true));
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> result = new ArrayList<>();
        if (!builder.getWorld().isClient()) {
            QuantumComputerLocation location = QuantumComputerData.get(builder.getWorld()).locations.removed;
            if (location != null) {
                result.add(location.getItemStack());
            }
        }
        return result;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (world.getBlockEntity(pos) instanceof QuantumComputerBlockEntity blockEntity) {
            dropStack(world, pos, blockEntity.getStack(0));
        }
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
        return blockEntity instanceof NamedScreenHandlerFactory named ? named : null;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return ((QuantumComputerBlockEntity) world.getBlockEntity(pos));
    }
}
