package itzglecni.customhitboxes.mixin;

import itzglecni.customhitboxes.config.ConfigManager;
import itzglecni.customhitboxes.config.ModConfig;
import itzglecni.customhitboxes.tracker.HitTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityRenderManagerMixin {

    @Inject(method = "drawHitbox", at = @At("HEAD"), cancellable = true)
    private void onDrawHitbox(Entity entity, float tickDelta, boolean localServerEntity, CallbackInfo ci) {
        ModConfig config = ConfigManager.getConfig();

        if (!config.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity localPlayer = client.player;

        if (!config.selfHitbox && entity == localPlayer) {
            ci.cancel();
            return;
        }

        if (!config.renderProjectiles && entity instanceof ProjectileEntity) {
            ci.cancel();
            return;
        }

        // When enabled, render hitboxes only for players and projectiles.
        if (config.renderEntities && !(entity instanceof PlayerEntity) && !(entity instanceof ProjectileEntity)) {
            ci.cancel();
            return;
        }

        if (config.noFireworkHitbox && entity instanceof FireworkRocketEntity) {
            ci.cancel();
            return;
        }

        if (config.noStuckArrows && entity instanceof PersistentProjectileEntity ppe) {
            if (ppe.getVelocity().lengthSquared() == 0) {
                ci.cancel();
                return;
            }
        }

        if (config.renderOnlyWhenElytraEquipped && entity instanceof PlayerEntity targetPlayer && targetPlayer != localPlayer) {
            if (!targetPlayer.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                ci.cancel();
                return;
            }
        }

        // Confirm a hit only after the target is actually in hurt state.
        HitTracker.confirmDamageState(entity);

        int resolvedColor = config.rainbowMain? getRainbowColor() : config.hitboxMainColor;
        
        if (entity instanceof ProjectileEntity) {
            resolvedColor = config.rainbowProjectiles? getRainbowColor() : config.projectilesColor;
        }
        
        if (config.hoverColorHitbox && localPlayer != null && entity instanceof PlayerEntity) {
            HitResult crosshairTarget = client.crosshairTarget;
            if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
                if (entityHitResult.getEntity() == entity) {
                    if (localPlayer.squaredDistanceTo(entity) <= 9.0) {
                        resolvedColor = config.rainbowHover ? getRainbowColor() : config.hoverColor;
                    }
                }
            }
        }

        if (config.hitColorHitbox && entity instanceof PlayerEntity && HitTracker.wasRecentlyHit(entity.getId())) {
            resolvedColor = config.rainbowHit ? getRainbowColor() : config.hitColor;
        }

        float lineWidth = entity instanceof PlayerEntity ? config.hitboxWidth : config.nonPlayerHitboxWidth;
        int argbColor = withFullAlpha(resolvedColor);
        Box box = getLerpedBox(entity, tickDelta);

        GizmoDrawing.box(box, DrawStyle.stroked(argbColor, lineWidth));

        if (config.lookingDirection) {
            int dirColor = config.rainbowLooking? getRainbowColor() : config.lookingDirectionColor;
            int dirArgb = withFullAlpha(dirColor);
            Vec3d lookVec = entity.getRotationVec(tickDelta);
            Vec3d basePos = entity.getLerpedPos(tickDelta);
            float eyeHeight = entity.getStandingEyeHeight();
            float length = 2.0f;
            Vec3d start = basePos.add(0.0, eyeHeight, 0.0);
            Vec3d end = start.add(lookVec.multiply(length));

            if (!config.removeDirectionArrow) {
                GizmoDrawing.arrow(start, end, dirArgb, lineWidth);
            } else {
                GizmoDrawing.line(start, end, dirArgb, lineWidth);
            }
        }

        if (config.foreheadLine && entity instanceof LivingEntity) {
            int foreColor = config.rainbowForehead? getRainbowColor() : config.foreheadLineColor;
            int foreArgb = withFullAlpha(foreColor);
            float eyeHeight = entity.getStandingEyeHeight();
            Vec3d basePos = entity.getLerpedPos(tickDelta);
            double y = basePos.y + eyeHeight;
            Box foreheadBox = new Box(box.minX, y - 0.01f, box.minZ, box.maxX, y + 0.01f, box.maxZ);
            GizmoDrawing.box(foreheadBox, DrawStyle.stroked(foreArgb, lineWidth));
        }

        ci.cancel();
    }

    private static Box getLerpedBox(Entity entity, float tickDelta) {
        Box base = entity.getBoundingBox();
        Vec3d lerpedPos = entity.getLerpedPos(tickDelta);
        Vec3d posDelta = lerpedPos.subtract(entity.getX(), entity.getY(), entity.getZ());
        return base.offset(posDelta.x, posDelta.y, posDelta.z);
    }

    private static int withFullAlpha(int rgb) {
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    private static int getRainbowColor() {
        float hue = (System.currentTimeMillis() % 2000L) / 2000f;
        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }
}