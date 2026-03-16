package itzglecni.customhitboxes.tracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HitTracker {
    private static final long ATTACK_CONFIRMATION_WINDOW_MS = 900;
    private static final long HIT_HIGHLIGHT_DURATION_MS = 1000;

    private static int lastAttackedEntityId = -1;
    private static long lastAttackTimestamp = 0L;

    private static int lastConfirmedHitEntityId = -1;
    private static long lastConfirmedHitTimestamp = 0L;

    public static void recordAttackAttempt(int entityId) {
        lastAttackedEntityId = entityId;
        lastAttackTimestamp = System.currentTimeMillis();
    }

    public static void confirmDamageState(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        long now = System.currentTimeMillis();
        if (entity.getId() != lastAttackedEntityId) {
            return;
        }

        if ((now - lastAttackTimestamp) > ATTACK_CONFIRMATION_WINDOW_MS) {
            return;
        }

        if (living.hurtTime > 0) {
            lastConfirmedHitEntityId = entity.getId();
            lastConfirmedHitTimestamp = now;
        }
    }

    public static boolean wasRecentlyHit(int entityId) {
        if (entityId == lastConfirmedHitEntityId) {
            return (System.currentTimeMillis() - lastConfirmedHitTimestamp) <= HIT_HIGHLIGHT_DURATION_MS;
        }
        return false;
    }
}