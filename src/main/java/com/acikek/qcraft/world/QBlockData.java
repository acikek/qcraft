package com.acikek.qcraft.world;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.qblock.QBlock;
import com.acikek.qcraft.blocks.qblock.QBlockRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QBlockData extends PersistentState {

    public static Codec<List<QBlockLocation>> CODEC = Codec.list(QBlockLocation.CODEC);
    public static final String DATA = "qblocklocations";
    public static final String KEY = QCraft.ID + "_" + DATA;

    public List<QBlockLocation> locations = new ArrayList<>();

    public QBlockData() {
    }

    public static QBlockData get(World world) {
        return ((ServerWorld) world).getPersistentStateManager().getOrCreate(QBlockData::fromNbt, QBlockData::new, KEY);
    }

    public static QBlockData fromNbt(NbtCompound nbt) {
        QBlockData blockData = new QBlockData();
        List<QBlockLocation> locations = CODEC.parse(NbtOps.INSTANCE, nbt.getList(DATA, NbtElement.COMPOUND_TYPE))
                .result()
                .orElse(Collections.emptyList());
        blockData.locations.addAll(locations);
        return blockData;
    }

    public boolean addBlock(QBlock.Type type, BlockPos blockPos, ItemStack stack) {
        String[] faces = QBlock.getFaces(stack);
        if (faces == null || getBlock(blockPos).isPresent()) {
            return false;
        }
        locations.add(new QBlockLocation(type, blockPos, List.of(faces)));
        System.out.println(locations);
        markDirty();
        return true;
    }

    public Optional<QBlockLocation> getBlock(BlockPos blockPos) {
        return locations.stream()
                .filter(loc -> loc.pos.asLong() == blockPos.asLong())
                .findFirst();
    }

    public void removeBlock(BlockPos blockPos) {
        this.getBlock(blockPos).ifPresent(this::removeBlock);
    }

    public void removeBlock(QBlockLocation location) {
        if (locations.remove(location)) {
            markDirty();
        }
    }

    public boolean hasBlock(BlockPos blockPos) {
        return getBlock(blockPos).isPresent();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        CODEC.encodeStart(NbtOps.INSTANCE, locations)
                .result()
                .ifPresent(tag -> nbt.put(DATA, tag));
        return nbt;
    }

    public static class QBlockLocation {

        public static Codec<QBlockLocation> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        QBlock.Type.CODEC.fieldOf("type").forGetter(l -> l.type),
                        BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
                        Codec.list(Codec.STRING).fieldOf("faces").forGetter(l -> l.faces)
                )
                        .apply(instance, QBlockLocation::new)
        );

        public QBlock.Type type;
        public BlockPos pos;
        public List<String> faces;

        public QBlockLocation(QBlock.Type type, BlockPos pos, List<String> faces) {
            this.type = type;
            this.pos = pos;
            this.faces = faces;
        }

        public Block getFaceBlock(int index) {
            return Registry.BLOCK.get(Identifier.tryParse(faces.get(index)));
        }

        public Block getFaceBlock(QBlock.Face face) {
            return getFaceBlock(face.index);
        }

        public ItemStack getItemStack() {
            ItemStack stack = new ItemStack(this.type.resolveBlock());
            return QBlockRecipe.applyFaces(stack, faces);
        }
    }
}
