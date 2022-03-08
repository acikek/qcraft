package com.acikek.qcraft.world.state;

import com.acikek.qcraft.world.state.frequency.Frequential;
import com.acikek.qcraft.world.state.location.QuantumComputerLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class QuantumComputerData extends LocationState<QuantumComputerLocation, QuantumComputerLocation.Pair> {

    public static final Keys keys = new Keys("qc");

    /**
     * Gets or creates {@link QuantumComputerData} state from the specified world.
     *
     * @param world The {@link ServerWorld} to get the state from.
     * @return The {@link QuantumComputerData} instance.
     */
    public static QuantumComputerData get(World world) {
        return ((ServerWorld) world).getPersistentStateManager().getOrCreate(QuantumComputerData::fromNbt, QuantumComputerData::new, keys.id);
    }

    public static QuantumComputerData fromNbt(NbtCompound nbt) {
        QuantumComputerData data = new QuantumComputerData();
        List<QuantumComputerLocation> locations = Frequential.parse(QuantumComputerLocation.LIST_CODEC, nbt, keys.data);
        if (!locations.isEmpty()) {
            data.fill(locations, QuantumComputerLocation.Pair::new);
        }
        return data;
    }

    /**
     * Constructs and adds a {@link QuantumComputerLocation} to this state's locations.
     *
     * @return The added location.
     */
    public QuantumComputerLocation add(BlockPos blockPos, ItemStack stack) {
        if (locations.has(blockPos)) {
            return null;
        }
        Optional<UUID> frequency = Frequential.getFrequency(stack);
        QuantumComputerLocation result = new QuantumComputerLocation(blockPos, frequency);
        locations.list.add(result);
        frequency.ifPresent(f -> frequencies.add(f, result, QuantumComputerLocation.Pair::new));
        markDirty();
        return result;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Frequential.encode(QuantumComputerLocation.LIST_CODEC, locations.list, nbt, keys.data);
        return nbt;
    }
}
