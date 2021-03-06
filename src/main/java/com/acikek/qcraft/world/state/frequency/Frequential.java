package com.acikek.qcraft.world.state.frequency;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Frequential {

    public final BlockPos pos;
    public Optional<UUID> frequency;

    public Frequential(BlockPos pos, Optional<UUID> frequency) {
        this.pos = pos;
        this.frequency = frequency;
    }

    public static Optional<UUID> getFrequency(ItemStack stack) {
        NbtCompound stackNbt = stack.getOrCreateNbt();
        return stackNbt.containsUuid("frequency")
                ? Optional.of(stackNbt.getUuid("frequency"))
                : Optional.empty();
    }

    public static <T extends Frequential> List<T> parse(Codec<List<T>> codec, NbtCompound nbt, String data) {
        return codec.parse(NbtOps.INSTANCE, nbt.getList(data, NbtElement.COMPOUND_TYPE))
                .result()
                .orElse(Collections.emptyList());
    }

    public static <T extends Frequential> void encode(Codec<List<T>> codec, List<T> list, NbtCompound nbt, String data) {
        codec.encodeStart(NbtOps.INSTANCE, list)
                .result()
                .ifPresent(tag -> nbt.put(data, tag));
    }
}
