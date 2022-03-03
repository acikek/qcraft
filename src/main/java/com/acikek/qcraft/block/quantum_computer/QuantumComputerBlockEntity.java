package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.block.Blocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class QuantumComputerBlockEntity extends BlockEntity {

    public static BlockEntityType<QuantumComputerBlockEntity> QUANTUM_COMPUTER_BLOCK_ENTITY;

    public UUID frequency;

    public QuantumComputerBlockEntity(BlockPos pos, BlockState state) {
        super(QUANTUM_COMPUTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.containsUuid("frequency")) {
            frequency = nbt.getUuid("frequency");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (frequency != null) {
            nbt.putUuid("frequency", frequency);
        }
        super.writeNbt(nbt);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public static void register() {
        QUANTUM_COMPUTER_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                "qcraft:quantum_computer_block_entity",
                FabricBlockEntityTypeBuilder
                        .create(QuantumComputerBlockEntity::new, Blocks.QUANTUM_COMPUTER)
                        .build(null)
        );
    }
}
