package com.acikek.qcraft.world.state.location;

import com.acikek.qcraft.block.Blocks;
import com.acikek.qcraft.world.state.frequency.Frequential;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class QuantumComputerLocation extends Frequential {

    public static final Codec<QuantumComputerLocation> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
                    DynamicSerializableUuid.CODEC.optionalFieldOf("frequency").forGetter(l -> l.frequency)
            )
            .apply(instance, QuantumComputerLocation::new)
    );

    public static final Codec<List<QuantumComputerLocation>> LIST_CODEC = Codec.list(CODEC);

    public QuantumComputerLocation(BlockPos pos, Optional<UUID> frequency) {
        super(pos, frequency);
    }

    /**
     * @return The stack with frequency applied, if any.
     */
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(Blocks.QUANTUM_COMPUTER);
        frequency.ifPresent(f -> stack.getOrCreateNbt().putUuid("frequency", f));
        return stack;
    }

    public static class Pair extends com.acikek.qcraft.world.state.frequency.Pair<QuantumComputerLocation> {

        public Pair(QuantumComputerLocation first) {
            super(first);
        }
    }
}
