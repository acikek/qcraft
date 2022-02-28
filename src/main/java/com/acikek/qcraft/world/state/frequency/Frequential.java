package com.acikek.qcraft.world.state.frequency;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Frequential {

    public Optional<UUID> frequency;

    public Frequential(Optional<UUID> frequency) {
        this.frequency = frequency;
    }
}
