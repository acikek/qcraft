package com.acikek.qcraft.sound;

import com.acikek.qcraft.QCraft;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class Sounds {

    public static SoundEvent QUANTUM_COMPUTER_PLACE = create("block.quantum_computer.place");
    public static SoundEvent QUANTUM_COMPUTER_BREAK = create("block.quantum_computer.break");

    public static SoundEvent[] SOUNDS = {
            QUANTUM_COMPUTER_PLACE,
            QUANTUM_COMPUTER_BREAK
    };

    public static SoundEvent create(String id) {
        return new SoundEvent(QCraft.id(id));
    }

    public static void registerAll() {
        for (SoundEvent sound : SOUNDS) {
            Registry.register(Registry.SOUND_EVENT, sound.getId(), sound);
        }
    }
}
