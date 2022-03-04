package com.acikek.qcraft.advancement;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.QBlock;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class QuantumObservationCriterion extends AbstractCriterion<QuantumObservationCriterion.Conditions> {

    public static Identifier ID = QCraft.id("quantum_observation");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        BlockPredicate face = BlockPredicate.fromJson(obj.get("face"));
        JsonPrimitive observationType = obj.getAsJsonPrimitive("observation_type");
        JsonPrimitive qBlockType = obj.getAsJsonPrimitive("qblock_type");
        JsonPrimitive entangled = obj.getAsJsonPrimitive("entangled");
        return new Conditions(
                playerPredicate, face,
                observationType != null ? Type.valueOf(observationType.getAsString().toUpperCase()) : null,
                qBlockType != null ? QBlock.Type.valueOf(qBlockType.getAsString().toUpperCase()) : null,
                entangled != null && entangled.getAsBoolean()
        );
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos, Type observationType, QBlock.Type qBlockType, boolean entangled) {
        trigger(player, (Conditions conditions) -> conditions.matches(player.getWorld(), pos, observationType, qBlockType, entangled));
    }

    public enum Type {
        PLAYER,
        AUTOMATED_OBSERVER
    }

    public static class Conditions extends AbstractCriterionConditions {

        public BlockPredicate face;
        public Type observationType;
        public QBlock.Type qBlockType;
        public boolean entangled;

        public Conditions(EntityPredicate.Extended playerPredicate, BlockPredicate face, Type observationType, QBlock.Type qBlockType, boolean entangled) {
            super(ID, playerPredicate);
            this.face = face;
            this.observationType = observationType;
            this.qBlockType = qBlockType;
            this.entangled = entangled;
        }

        public boolean matches(ServerWorld world, BlockPos pos, Type observationType, QBlock.Type qBlockType, boolean entangled) {
            return face.test(world, pos)
                    && (this.observationType == null || this.observationType == observationType)
                    && (this.qBlockType == null || this.qBlockType ==  qBlockType)
                    && this.entangled == entangled;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            if (face != null) {
                jsonObject.add("face", face.toJson());
            }
            if (observationType != null) {
                jsonObject.add("observation_type", new JsonPrimitive(observationType.toString().toLowerCase()));
            }
            if (qBlockType != null) {
                jsonObject.add("qblock_type", new JsonPrimitive(qBlockType.toString().toLowerCase()));
            }
            jsonObject.add("entangled", new JsonPrimitive(entangled));
            return jsonObject;
        }
    }
}
