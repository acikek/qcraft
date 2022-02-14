package com.acikek.qcraft.items;

import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.blocks.qblock.QBlock;

public class QBlockEssence extends Essence {

    public QBlockEssence(Settings settings, Type essenceType) {
        super(settings, essenceType);
    }

    public QBlock getQBlock() {
        return switch(this.essenceType) {
            case OBSERVATION -> Blocks.OBSERVER_DEPENDENT_BLOCK;
            case SUPERPOSITION -> Blocks.QUANTUM_BLOCK;
            default -> null;
        };
    }
}
