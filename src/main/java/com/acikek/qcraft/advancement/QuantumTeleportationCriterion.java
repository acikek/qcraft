package com.acikek.qcraft.advancement;

import com.acikek.qcraft.QCraft;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class QuantumTeleportationCriterion extends AbstractCriterion<QuantumTeleportationCriterion.Conditions> {

    public static Identifier ID = QCraft.id("quantum_teleportation");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        JsonPrimitive width = obj.getAsJsonPrimitive("width");
        JsonPrimitive length = obj.getAsJsonPrimitive("length");
        JsonPrimitive height = obj.getAsJsonPrimitive("height");
        return new Conditions(playerPredicate, defaultInt(width), defaultInt(length), defaultInt(height));
    }

    public int defaultInt(JsonPrimitive value) {
        return value != null ? value.getAsInt() : 0;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public void trigger(ServerPlayerEntity player, int[] dimensions) {
        trigger(player, dimensions[0], dimensions[1], dimensions[2]);
    }

    public void trigger(ServerPlayerEntity player, int width, int length, int height) {
        trigger(player, (QuantumTeleportationCriterion.Conditions conditions) -> conditions.matches(width, length, height));
    }

    public static class Conditions extends AbstractCriterionConditions {

        int width;
        int length;
        int height;

        public Conditions(EntityPredicate.Extended playerPredicate, int width, int length, int height) {
            super(ID, playerPredicate);
            this.width = width;
            this.length = length;
            this.height = height;
        }

        public boolean matches(int width, int length, int height) {
            return width >= this.width
                    && length >= this.length
                    && height >= this.height;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject obj = super.toJson(predicateSerializer);
            obj.add("width", new JsonPrimitive(width));
            obj.add("length", new JsonPrimitive(length));
            obj.add("height", new JsonPrimitive(height));
            return obj;
        }
    }
}
