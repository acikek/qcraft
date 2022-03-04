package com.acikek.qcraft.advancement;

import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.minecraft.advancement.criterion.Criterion;

import java.util.ArrayList;
import java.util.List;

public class Criteria {

    public static QuantumObservationCriterion QUANTUM_OBSERVATION = new QuantumObservationCriterion();

    public static List<Criterion<?>> CRITERIA = new ArrayList<>();

    static {
        CRITERIA.add(QUANTUM_OBSERVATION);
    }

    public static void registerAll() {
        for (Criterion<?> criterion : CRITERIA) {
            CriterionRegistry.register(criterion);
        }
    }
}
