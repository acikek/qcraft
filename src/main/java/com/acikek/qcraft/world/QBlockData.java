package com.acikek.qcraft.world;

import com.acikek.qcraft.QCraft;
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
        System.out.println();
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

    public static String[] getFaces(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("faces");
        if (nbt == null) {
            return null;
        }
        String[] faces = new String[6];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = nbt.getString(QBlockRecipe.Face.values()[i].name());
        }
        return faces;
    }

    public boolean addBlock(BlockPos blockPos, World world, ItemStack stack) {
        String[] faces = getFaces(stack);
        if (faces == null || getBlock(blockPos, world).isPresent()) {
            return false;
        }
        locations.add(new QBlockLocation(blockPos, world.getRegistryKey().getValue(), List.of(faces)));
        markDirty();
        return true;
    }

    public Optional<QBlockLocation> getBlock(BlockPos blockPos, World world) {
        return locations.stream()
                .filter(loc -> loc.world.equals(world.getRegistryKey().getValue()))
                .filter(loc -> loc.pos.asLong() == blockPos.asLong())
                .findFirst();
    }

    public boolean removeBlock(BlockPos blockPos, World world) {
        Optional<QBlockLocation> block = getBlock(blockPos, world);
        if (block.isEmpty()) {
            return false;
        }
        locations.remove(block.get());
        markDirty();
        return true;
    }

    public boolean hasBlock(BlockPos blockPos, World world) {
        return getBlock(blockPos, world).isPresent();
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
                    BlockPos.CODEC.fieldOf("position").forGetter(l -> l.pos),
                    Identifier.CODEC.fieldOf("world").forGetter(l -> l.world),
                    Codec.list(Codec.STRING).fieldOf("faces").forGetter(l -> l.faces)
            )
                    .apply(instance, QBlockLocation::new)
        );

        public BlockPos pos;
        public Identifier world;
        public List<String> faces;

        public QBlockLocation(BlockPos pos, Identifier world, List<String> faces) {
            this.pos = pos;
            this.world = world;
            this.faces = faces;
        }

        public Block getFace(int index) {
            return Registry.BLOCK.get(Identifier.tryParse(faces.get(index)));
        }

        public Block getFace(QBlockRecipe.Face face) {
            return getFace(face.index);
        }
    }
}
