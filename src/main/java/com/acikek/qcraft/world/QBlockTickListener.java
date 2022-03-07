package com.acikek.qcraft.world;

import com.acikek.qcraft.item.Goggles;
import com.acikek.qcraft.world.state.QCraftData;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class QBlockTickListener implements ServerTickEvents.StartWorldTick {

    public QCraftData data;

    public static final double PITCH_THRESHOLD = 60.0;
    public static final double YAW_THRESHOLD = 80.0;

    // These methods took 4 days to write.
    // Massive thanks to sssubtlety, nHail, and dirib!

    public double getPitch(Vec3d between) {
        double pitch = Math.atan(between.y / Math.sqrt(between.x * between.x + between.z * between.z));
        return Math.toDegrees(pitch);
    }

    public double getYawDifference(PlayerEntity player, Vec3d between) {
        Vec3d rotated = between.rotateY((float) Math.toRadians(270));
        double yaw = Math.atan2(rotated.z, rotated.x);
        return 180.0 - Math.abs(Math.abs(player.getYaw() - Math.toDegrees(yaw)) - 180.0);
    }

    public double getPitchDifference(double playerPitch, double betweenPitch, double yawDiff) {
        if (yawDiff > 90.0) {
            betweenPitch = (playerPitch > 0.0 ? 180.0 : -180.0) - betweenPitch;
        }
        return Math.abs(playerPitch - betweenPitch);
    }

    public boolean isObserved(double pitchDiff, double yawDiff, double playerPitch) {
        boolean viewportIntersectsYAxis = playerPitch + PITCH_THRESHOLD > 90 || playerPitch -PITCH_THRESHOLD < -90;
        return pitchDiff < PITCH_THRESHOLD && (viewportIntersectsYAxis || yawDiff < YAW_THRESHOLD);
    }

    @Override
    public void onStartTick(ServerWorld world) {
        if (data == null) {
            data = QCraftData.get(world, true);
        }
        if (data.qBlockLocations.locations.isEmpty()) {
            return;
        }
        List<QBlockLocation> loadedLocations = data.getLoadedLocations(world);
        if (loadedLocations.isEmpty()) {
            return;
        }
        for (PlayerEntity player : world.getPlayers()) {
            if (Goggles.isWearingGoggles(player, Goggles.Type.ANTI_OBSERVATION)) {
                return;
            }
            List<QBlockLocation> localLocations = data.getLocalLocations(loadedLocations, player);
            if (localLocations.isEmpty()) {
                return;
            }
            Vec3d center = player.getPos().lerp(player.getEyePos(), 0.5);
            for (QBlockLocation location : localLocations) {
                Vec3d between = location.getBetween(player.getEyePos());
                double yawDiff = getYawDifference(player, between);
                double betweenPitch = getPitch(between);
                double pitchDiff = getPitchDifference(player.getPitch(), betweenPitch, yawDiff);
                if (isObserved(pitchDiff, yawDiff, player.getPitch())) {
                    if (!location.observed && data.getOtherNotObserved(location)) {
                        data.observe(location, world, player);
                    }
                }
                else if (location.observed && data.canBeUnobserved(location, center)) {
                    data.unobserve(location, world, true);
                }
            }
        }
    }
}
