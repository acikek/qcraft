package com.acikek.qcraft.world.state.location;

import com.acikek.qcraft.world.state.frequency.Frequential;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.BlockPos;

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

    public BlockPos pos;

    public QuantumComputerLocation(BlockPos pos, Optional<UUID> uuid) {
        super(uuid);
        this.pos = pos;
    }

    public static class Pair extends com.acikek.qcraft.world.state.frequency.Pair<QuantumComputerLocation> {

        public Pair(QuantumComputerLocation first) {
            super(first);
        }
    }
}
