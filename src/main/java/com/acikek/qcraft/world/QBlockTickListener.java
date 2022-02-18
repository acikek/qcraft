package com.acikek.qcraft.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QBlockTickListener implements ServerTickEvents.StartWorldTick {

    public QBlockData data;

    @Override
    public void onStartTick(ServerWorld world) {
        if (data == null) {
            data = QBlockData.get(world);
        }
        if (data.locations.isEmpty()) {
            return;
        }
        List<QBlockData.QBlockLocation> loadedLocations = data.getLoadedLocations(world);
        if (loadedLocations.isEmpty()) {
            return;
        }
        for (PlayerEntity player : world.getPlayers()) {
            List<QBlockData.QBlockLocation> localLocations = data.getLocalLocations(loadedLocations, player);
            if (localLocations.isEmpty()) {
                return;
            }
            for (QBlockData.QBlockLocation location : localLocations) {
                Vec3d between = location.getBetween(player.getEyePos());
                double pitchDiff = location.getPitchDifference(player, between);
                double yawDiff = location.getYawDifference(player, between);
                if (!location.observed) {
                    if (pitchDiff < 60.0 && (yawDiff < 75.0 || yawDiff > 130.0)) {
                        data.observe(location, world, player);
                    }
                }
                else if ((yawDiff > 75.0 && yawDiff < 130.0) || pitchDiff > 60.0) {
                    data.unobserve(location, world);
                }
            }
        }
    }
}
