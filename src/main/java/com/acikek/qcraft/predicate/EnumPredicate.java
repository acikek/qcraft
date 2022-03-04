package com.acikek.qcraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.function.Function;

public class EnumPredicate<T extends Enum<T>> {

    public T value;

    public EnumPredicate(T value) {
        this.value = value;
    }

    public boolean test(T value) {
        if (this.value == null) {
            return true;
        }
        return this.value == value;
    }

    public static <T extends Enum<T>> EnumPredicate<T> fromJson(JsonElement json, Function<String, T> e) {
        if (json == null || json.isJsonNull()) {
            return new EnumPredicate<>(null);
        }
        String value = json.getAsString().toUpperCase();
        return new EnumPredicate<>(e.apply(value));
    }

    public JsonElement toJson() {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.toString().toLowerCase());
    }
}
