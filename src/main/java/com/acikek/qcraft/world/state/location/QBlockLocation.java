package com.acikek.qcraft.world.state.location;

import com.acikek.qcraft.block.qblock.InertQBlock;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.world.state.QBlockData;
import com.acikek.qcraft.world.state.frequency.Frequential;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class QBlockLocation extends Frequential {

    public static final Codec<QBlockLocation> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
                    DynamicSerializableUuid.CODEC.optionalFieldOf("frequency").forGetter(l -> l.frequency),
                    QBlock.Type.CODEC.fieldOf("type").forGetter(l -> l.type),
                    Codec.list(BlockState.CODEC).fieldOf("faces").forGetter(l -> l.faces),
                    Codec.BOOL.fieldOf("observed").forGetter(l -> l.observed)
            )
            .apply(instance, QBlockLocation::new)
    );

    public static final Codec<List<QBlockLocation>> LIST_CODEC = Codec.list(CODEC);

    public QBlock.Type type;
    public List<BlockState> faces = new ArrayList<>();
    public boolean observed;

    public QBlock.Face observedFace = null;

    /**
     * Constructs a {@link QBlockLocation}.<br>
     * To add a location to a {@link QBlockData} instance, see {@link QBlockData#add(QBlock.Type, BlockPos, ItemStack)}.
     *
     * @param type      The qBlock's type. This determines its observation behavior.
     * @param pos       The block position of the location.
     * @param faces     The block face IDs. To read these from an item, see {@link QBlockItem#getFaces(ItemStack)}.
     * @param observed  Whether this location is currently observed.
     * @param frequency The String UUID of the location's entanglement frequency.
     */
    public QBlockLocation(BlockPos pos, Optional<UUID> frequency, QBlock.Type type, List<BlockState> faces, boolean observed) {
        super(pos, frequency);
        this.type = type;
        this.faces.addAll(faces);
        this.observed = observed;
    }

    /**
     * @return The block at the specified face.
     */
    public BlockState getFaceState(QBlock.Face face) {
        return faces.get(face.index);
    }

    /**
     * @return The stack, based on block type, with the faces applied to its NBT.
     * @see QBlockItem#applyFaces(ItemStack, List)
     */
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(type.resolveBlock());
        QBlockItem.applyFaces(stack, faces);
        frequency.ifPresent(f -> stack.getOrCreateNbt().putUuid("frequency", f));
        return stack;
    }

    /**
     * @return The vector between the block position and a player's eye position.
     */
    public Vec3d getBetween(Vec3d eyePos) {
        return eyePos.subtract(Vec3d.ofCenter(pos));
    }

    /**
     * @return The closest axis to the player.
     * @see QBlockLocation#getBetween(Vec3d)
     */
    public QBlock.Axis getClosestAxis(Vec3d dists) {
        double absX = Math.abs(dists.x);
        double absY = Math.abs(dists.y);
        double absZ = Math.abs(dists.z);
        if (absX > absY && absX > absZ) {
            return QBlock.Axis.X;
        }
        else if (absY > absX && absY > absZ) {
            return QBlock.Axis.Y;
        }
        else {
            return QBlock.Axis.Z;
        }
    }

    /**
     * @return The closest block face to the player.
     * @see QBlockLocation#getBetween(Vec3d)
     */
    public QBlock.Face getClosestFace(Vec3d dists) {
        return switch (getClosestAxis(dists)) {
            case X -> dists.x > 0 ? QBlock.Face.EAST : QBlock.Face.WEST;
            case Y -> dists.y > 0 ? QBlock.Face.UP : QBlock.Face.DOWN;
            case Z -> dists.z > 0 ? QBlock.Face.SOUTH : QBlock.Face.NORTH;
        };
    }

    public QBlock.Face pickFace(PlayerEntity player, World world) {
        Vec3d dists = getBetween(player.getEyePos());
        return type.pickFace(this, dists, world.random);
    }

    /**
     * @return Whether the specified block state is impossible given the valid block faces.
     */
    public boolean isStateImpossible(BlockState state) {
        if (state.getBlock() instanceof InertQBlock) {
            return false;
        }
        for (BlockState face : faces) {
            if (face.getBlock() == state.getBlock()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPylonBase(QBlock.Face face) {
        if (type != QBlock.Type.OBSERVER_DEPENDENT) {
            return false;
        }
        for (int i = 0; i < faces.size(); i++) {
            BlockState state = faces.get(i);
            if ((face.index == i && state.getBlock() != Blocks.GOLD_BLOCK) || (face.index != i && state.getBlock() != Blocks.OBSIDIAN)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "qBlock (" + type + ", " + pos.toShortString() + ")";
    }

    /**
     * Represents a pair of entangled qBlocks.
     */
    public static class Pair extends com.acikek.qcraft.world.state.frequency.Pair<QBlockLocation> {

        public static final Codec<Pair> CODEC = getCodec(QBlockLocation.CODEC, Pair::new);

        public Pair(QBlockLocation first) {
            super(first);
        }

        public Pair(Optional<QBlockLocation> left, Optional<QBlockLocation> right) {
            super(left, right);
        }

        public boolean getOtherObserved(QBlockLocation location) {
            QBlockLocation other = getOther(location);
            if (other != null) {
                return other.observed;
            }
            return false;
        }
    }
}
