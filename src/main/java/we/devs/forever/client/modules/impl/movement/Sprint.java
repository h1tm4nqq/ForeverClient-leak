package we.devs.forever.client.modules.impl.movement;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.Anchor;
import we.devs.forever.client.modules.impl.movement.speed.Speed;
import we.devs.forever.client.modules.impl.player.Freecam;
import we.devs.forever.client.setting.Setting;


public
class Sprint extends Module {

    private static Sprint INSTANCE = new Sprint();
    public static Setting<Mode> mode = (new Setting<>("Mode", Mode.Rage));
    public static Setting<Boolean> noWaterInstant = (new Setting<>("NoLiquidInstant", false, v -> mode.getValue() == Mode.Instant));
    public static Setting<Boolean> disableOnSneak = (new Setting<>("DisableOnSneak", false, v -> mode.getValue() == Mode.Instant));
    public Setting<Boolean> stopInAir = (new Setting<>("StopInAir", false, v -> mode.getValue() == Mode.Instant));
    public static Setting<Float> speed = (new Setting<>("Speed", 1F, 0.1F, 2F, v -> mode.getValue() == Mode.Instant));
    //    public Setting<Boolean> lag = (new Setting<>("lag", false, v -> mode.getValue() == Mode.Instant));
//    public Setting<Float> amount = (new Setting<>("Amount", 1F, 0.1F, 2F, v -> mode.getValue() == Mode.Instant && lag.getValue()));
    final Setting<Boolean> boost = (new Setting<>("Boost", false, v -> mode.getValue() == Mode.Instant));
    final Setting<Float> crystalFactor = (new Setting<>("ExplosionFactor", 1.0F, 0.1F, 10F, v -> boost.getValue() && mode.getValue() == Mode.Instant));
    final Setting<Float> bowFactor = (new Setting<>("VelocityFactor", 1.0F, 0.1F, 10F, v -> boost.getValue() && mode.getValue() == Mode.Instant));
    final TimerUtil velocityTimer = new TimerUtil();
    final TimerUtil bypassTime = new TimerUtil();
    double maxVelocity = 0;

    public Sprint() {
        super("Sprint", "Modifies sprinting", Category.MOVEMENT);
        setInstance();
    }

    public static Sprint getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Sprint();
        }
        return INSTANCE;
    }


    private void setInstance() {
        INSTANCE = this;
    }

    @EventListener(priority = ListenerPriority.LOW)
    public void onSprint(MoveEvent event) {
        if (mode.getValue() == Mode.Instant) {

            if (stopInAir.getValue() && (mc.player.motionY > 0.0 || !mc.player.onGround || mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.fallDistance > 2.0f)) {
                return;
            }
            if (mc.player.collidedHorizontally) return;

            if (Anchor.pulling
                    || Speed.INSTANCE.isEnabled()
                    || Freecam.INSTANCE.isEnabled()
                    || mc.player.isCreative()) return;

            if (disableOnSneak.getValue() && mc.player.isSneaking()) return;


            if (!noWaterInstant.getValue() || (!mc.player.isInWater() && !mc.player.isInLava())) {
//                if (!bypassTime.passedMs(Step.bypassTime.getValue()) && Step.bypass.getValue() && Step.INSTANCE.isEnabled()) {
//                    strafe(event, Step.speed.getValue());
//                } else {
                    strafe(event, speed.getValue());
//                }
            }
        }
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case Rage:
                if ((mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) && !(mc.player.isSneaking() || mc.player.collidedHorizontally || mc.player.getFoodStats().getFoodLevel() <= 6f)) {
                    mc.player.setSprinting(true);
                }
                break;
            case Legit:
                if (mc.gameSettings.keyBindForward.isKeyDown() && !(mc.player.isSneaking() || mc.player.isHandActive() || mc.player.collidedHorizontally || mc.player.getFoodStats().getFoodLevel() <= 6f) && mc.currentScreen == null) {
                    mc.player.setSprinting(true);
                }
                break;
        }
    }

    @Override
    public void onDisable() {
        if (!nullCheck()) {
            mc.player.setSprinting(false);
        }
    }

    public void strafe(MoveEvent event, float speed1) {
        if (Anchor.pulling) return;

        double speed = PlayerUtil.getSpeed(true, speed1);

        if (boost.getValue() && velocityTimer.passedMs(75) && maxVelocity > 0) {
            speed = Math.max(speed, maxVelocity);
        }

        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.prevRotationYaw
                + (mc.player.rotationYaw - mc.player.prevRotationYaw)
                * mc.getRenderPartialTicks();

        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
            } else if (moveStrafe < 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            } else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }
        double v = moveForward * speed * -Math.sin(Math.toRadians(rotationYaw))
                + moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        double v1 = moveForward * speed * Math.cos(Math.toRadians(rotationYaw))
                - moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
        event.setX(v);
        event.setZ(v1);

    }


    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {

        // reset our process on a rubberband
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            velocityTimer.reset();
            maxVelocity = 0;
        }


        if (event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion velocity = event.getPacket();

            maxVelocity = Math.sqrt(velocity.getMotionX() * velocity.getMotionX() + velocity.getMotionZ() * velocity.getMotionZ());


            maxVelocity *= crystalFactor.getValue();

            velocityTimer.reset();
            event.cancel();
        }
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = event.getPacket();

            if (nullCheck() || velocity.getEntityID() != mc.player.entityId) {
                event.cancel();
                return;
            }


            maxVelocity = Math.sqrt(velocity.getMotionX() * velocity.getMotionX() + velocity.getMotionZ() * velocity.getMotionZ()) / 8000.0;


            maxVelocity *= bowFactor.getValue();

            velocityTimer.reset();
            event.cancel();
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    public
    enum Mode {
        Legit,
        Rage,
        Instant
    }
}
