package we.devs.forever.client.modules.impl.movement.speed;


import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly;
import we.devs.forever.client.modules.impl.player.Freecam;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.mixin.mixins.accessor.ICPacketPlayer;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

public class Speed extends Module {
    public static Speed INSTANCE;

    public Speed() {
        super("Speed", "Allows you to move faster", Category.MOVEMENT);
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.Strafe, "Mode for Speed");
    public static Setting<Boolean> timer = new Setting<>("Timer", true, "Uses timer to speed up strafe");

    public static Setting<BaseSpeed> speed = new Setting<>("Speed", BaseSpeed.NewNCP, "Base speed when moving");

    // **************************** anticheat ****************************
    public static Setting<Boolean> autoStep = new Setting<>("AutoStep", false, "Automatically enables step");
    public static Setting<Boolean> strictJump = new Setting<>("StrictJump", false, "Use slightly higher and therefore slower jumps to bypass better", v -> mode.getValue().equals(Mode.StrafeStrict));

    public static Setting<Boolean> strictSprint = new Setting<>("StrictSprint", false, "Maintains sprint while moving", v -> mode.getValue().equals(Mode.StrafeStrict));
    final Setting<Boolean> boost = (new Setting<>("Boost", false));
    final Setting<Float> crystalFactor = (new Setting<>("ExplosionFactor", 1.0F, 0.1F, 10F, v -> boost.getValue()));
    final Setting<Float> bowFactor = (new Setting<>("VelocityFactor", 1.0F, 0.1F, 10F, v -> boost.getValue()));



    // **************************** stages ****************************
    final TimerUtil velocityTimer = new TimerUtil();
    double maxVelocity = 0;

    // strafe stage
    private int strafeStage = 4;

    // on-ground stage
    private int groundStage = 2;

    // **************************** speeds ****************************

    // the move speed for the current mode
    private double moveSpeed;
    private double distance;

    // boost speed
    private double boostSpeed;

    // speed accelerate tick
    private boolean accelerate;

    // **************************** ticks ****************************

    // strict tick clamp
    private int strictTicks;

    // ticks boosted
    private int boostTicks;

    // **************************** packets ****************************

    // packet manipulation
    private boolean offsetPackets;

    @Override
    public void onEnable() {
        super.onEnable();
        //if(autoStep.getValue()) Step.INSTANCE.setEnabled(true);
        // awesome
        strafeStage = 4;
        groundStage = 2;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        //if(autoStep.getValue()) Step.INSTANCE.setEnabled(false);

        // reset all vars
        resetProcess();
    }

    @Override
    public void onUpdate() {

        // our latest move speed
        distance = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SuppressWarnings("ConstantConditions")
    @EventListener
    public void onMotion(MoveEvent event) {
        if (!PlayerUtil.isPlayerMoving()) {
            if (strictSprint.getValue() && (mc.player.isSprinting() || ((IEntityPlayerSP) mc.player).getServerSprintState())) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            return;
        }


        // make sure the player is not in a liquid
        if (mc.player.isInLava() || mc.player.isInWater()) {
            resetProcess();
            return;
        }

        // make sure the player is not in a web
        if (mc.player.isInWeb) {
            resetProcess();
            return;
        }


        // make sure the player can have speed applied
        if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.fallDistance > 2) {
            resetProcess();
            return;
        }

        // incompatibilities
        if (PacketFly.INSTANCE.isEnabled() || Freecam.INSTANCE.isEnabled()) {
            return;
        }

        // pause if sneaking
        if (mc.player.isSneaking()) {
            return;
        }

        // cancel vanilla movement, we'll send our own movements

        // base move speed
        double baseSpeed = 0.2873;

        if (speed.getValue().equals(BaseSpeed.OldNCP)) {
            baseSpeed = 0.272;
        }

        // scale move speed if Speed or Slowness potion effect is active
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            baseSpeed *= 1 + (0.2 * (amplifier + 1));
        }

        if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
            double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
            baseSpeed /= 1 + (0.2 * (amplifier + 1));

        }

        // start sprinting
        if (strictSprint.getValue() && (!mc.player.isSprinting() || !((IEntityPlayerSP) mc.player).getServerSprintState())) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

        }

        switch (mode.getValue()) {

            /*
             * OnGround, the idea behind this is that you are simulating a fake jump by modifying packets instead
             * of actually jumping (i.e. Strafe), this allows you to gain lots of Speed on NCP servers without
             * actually jumping
             */
            case OnGround: {

                // only function when we are on the ground
                if (mc.player.onGround && PlayerUtil.isPlayerMoving()) {

                    // fake jump by offsetting packets
                    if (groundStage == 2) {

                        // offset our y-packets to simulate a jump
                        offsetPackets = true;

                        // acceleration jump factor
                        double acceleration = 2.149;

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;

                        // we can start speeding
                        groundStage = 3;
                    } else if (groundStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;

                        // we need to "jump" again now
                        groundStage = 2;
                    }

                    // we will not be able to jump
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, 0.21, 0)).size() > 0 || mc.player.collidedVertically) {
                        groundStage = 1;
                    }
                }

                // do not allow movements slower than base speed
                moveSpeed = Math.max(moveSpeed, baseSpeed);
                if (maxVelocity > 0 && boost.getValue() && !velocityTimer.passedMs(75) && !mc.player.collidedHorizontally) {
                    moveSpeed = Math.max(moveSpeed, maxVelocity);
                }

                // the current movement input values of the user
                float forward = mc.player.movementInput.moveForward;
                float strafe = mc.player.movementInput.moveStrafe;
                float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!PlayerUtil.isPlayerMoving()) {
                    event.setX(0);
                    event.setZ(0);
                } else if (forward != 0) {
                    if (strafe > 0) {
                        yaw += forward > 0 ? -45 : 45;
                    } else if (strafe < 0) {
                        yaw += forward > 0 ? 45 : -45;
                    }

                    strafe = 0;

                    if (forward > 0) {
                        forward = 1;
                    } else if (forward < 0) {
                        forward = -1;
                    }
                }

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw));
                double sin = -Math.sin(Math.toRadians(yaw));

                // update the movements
                event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));
                break;
            }

            /*
             * Incredibly similar to sprint jumping, bypasses lots of anticheats as the movement is similar
             * to sprint jumping. Max speed: ~29 kmh
             */
            case Strafe: {

                // only attempt to modify speed if we are inputting movement
                if (PlayerUtil.isPlayerMoving()) {

                    // use timer
                    if (timer.getValue()) {
                        timerManager.setTimer(1.088F, 11);
                    }

                    // start the motion
                    if (strafeStage == 1) {

                        // starting speed
                        moveSpeed = 1.35 * baseSpeed - 0.01;
                    }

                    // start jumping
                    else if (strafeStage == 2) {

                        // the jump height
                        double jumpSpeed = 0.3999999463558197;

                        // scale jump speed if Jump Boost potion effect is active

                        // not really too useful for Speed like the other potion effects
                        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                            double amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
                            jumpSpeed += (amplifier + 1) * 0.1;
                        }


                        // jump
                        mc.player.motionY = jumpSpeed;
                        event.setY(jumpSpeed);

                        // alternate acceleration ticks
                        double acceleration = 1.395;

                        // if can accelerate, increase speed
                        if (accelerate) {
                            acceleration = 1.6835;
                        }

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;
                    }

                    // start actually speeding when falling
                    else if (strafeStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;

                        // we've just slowed down and need to alternate acceleration
                        accelerate = !accelerate;
                    } else {
                        if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {

                            // reset strafe stage
                            strafeStage = PlayerUtil.isPlayerMoving() ? 1 : 0;
                        }

                        // collision speed
                        moveSpeed = distance - (distance / 159);
                    }

                    // do not allow movements slower than base speed
                    moveSpeed = Math.max(moveSpeed, baseSpeed);
                    if (maxVelocity > 0 && boost.getValue() && !velocityTimer.passedMs(75) && !mc.player.collidedHorizontally) {
                        moveSpeed = Math.max(moveSpeed, maxVelocity);
                    }
                    // the current movement input values of the user
                    float forward = mc.player.movementInput.moveForward;
                    float strafe = mc.player.movementInput.moveStrafe;
                    float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

                    // if we're not inputting any movements, then we shouldn't be adding any motion
                    if (!PlayerUtil.isPlayerMoving()) {
                        event.setX(0);
                        event.setZ(0);
                    } else if (forward != 0) {
                        if (strafe > 0) {
                            yaw += forward > 0 ? -45 : 45;
                        } else if (strafe < 0) {
                            yaw += forward > 0 ? 45 : -45;
                        }

                        strafe = 0;

                        if (forward > 0) {
                            forward = 1;
                        } else if (forward < 0) {
                            forward = -1;
                        }
                    }

                    // our facing values, according to movement not rotations
                    double cos = Math.cos(Math.toRadians(yaw));
                    double sin = -Math.sin(Math.toRadians(yaw));

                    // update the movements
                    event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                    event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));

                    // update
                    strafeStage++;
                }

                break;
            }

            /*
             * Mode: Strafe for NCP Updated
             * Max speed: ~26 or 27 kmh
             */
            case StrafeStrict: {

                // only attempt to modify speed if we are inputting movement
                if (PlayerUtil.isPlayerMoving()) {

                    // use timer
                    if (timer.getValue()) {
                        timerManager.setTimer(1.088F, 11);
                    }

                    // start the motion
                    if (strafeStage == 1) {

                        // starting speed
                        moveSpeed = 1.35 * baseSpeed - 0.01;
                    }

                    // start jumping
                    else if (strafeStage == 2) {

                        // the jump height
                        double jumpSpeed = 0.3999999463558197;

                        // jump slightly higher (i.e. slower, this uses vanilla jump height)
                        if (strictJump.getValue()) {
                            jumpSpeed = 0.41999998688697815;
                        }

                        // scale jump speed if Jump Boost potion effect is active

                        // not really too useful for Speed like the other potion effects
                        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                            double amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
                            jumpSpeed += (amplifier + 1) * 0.1;
                        }


                        // jump
                        mc.player.motionY = jumpSpeed;
                        event.setY(jumpSpeed);

                        // acceleration jump factor
                        double acceleration = 2.149;

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;
                    }

                    // start actually speeding when falling
                    else if (strafeStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;
                    } else {
                        if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {

                            // reset strafe stage
                            strafeStage = PlayerUtil.isPlayerMoving() ? 1 : 0;
                        }

                        // collision speed
                        moveSpeed = distance - (distance / 159);
                    }

                    // do not allow movements slower than base speed
                    moveSpeed = Math.max(moveSpeed, baseSpeed);

                    // base speeds
                    double baseStrictSpeed = 0.465;
                    double baseRestrictedSpeed = 0.44;

                    // scale move speed if Speed or Slowness potion effect is active
                    if (mc.player.isPotionActive(MobEffects.SPEED)) {
                        double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                        baseStrictSpeed *= 1 + (0.2 * (amplifier + 1));
                        baseRestrictedSpeed *= 1 + (0.2 * (amplifier + 1));
                    }

                    if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
                        double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
                        baseStrictSpeed /= 1 + (0.2 * (amplifier + 1));
                        baseRestrictedSpeed /= 1 + (0.2 * (amplifier + 1));
                    }


                    // clamp the value based on the number of ticks passed
                    moveSpeed = Math.min(moveSpeed, strictTicks > 25 ? baseStrictSpeed : baseRestrictedSpeed);

                    // update & reset our tick count
                    strictTicks++;
                    if (maxVelocity > 0 && boost.getValue() && !velocityTimer.passedMs(75) && !mc.player.collidedHorizontally) {
                        moveSpeed = Math.max(moveSpeed, maxVelocity);
                    }
                    // reset strict ticks every 50 ticks
                    if (strictTicks > 50) {
                        strictTicks = 0;
                    }

                    // the current movement input values of the user
                    float forward = mc.player.movementInput.moveForward;
                    float strafe = mc.player.movementInput.moveStrafe;
                    float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

                    // if we're not inputting any movements, then we shouldn't be adding any motion
                    if (!PlayerUtil.isPlayerMoving()) {
                        event.setX(0);
                        event.setZ(0);
                    } else if (forward != 0) {
                        if (strafe >= 1) {
                            yaw += (forward > 0 ? -45 : 45);
                            strafe = 0;
                        } else if (strafe <= -1) {
                            yaw += (forward > 0 ? 45 : -45);
                            strafe = 0;
                        }

                        if (forward > 0) {
                            forward = 1;
                        } else if (forward < 0) {
                            forward = -1;
                        }
                    }

                    // our facing values, according to movement not rotations
                    double cos = Math.cos(Math.toRadians(yaw));
                    double sin = -Math.sin(Math.toRadians(yaw));

                    // update the movements
                    event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                    event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));

                    // update
                    strafeStage++;
                }

                break;
            }

            /*
             * Maintains speed at 22.4 kmh on ground
             * Similar to Sprint
             */
            case StrafeGround: {

                // instant max speed
                moveSpeed = baseSpeed;

                // walking speed = 0.7692307692 * sprint speed
                if (!mc.player.isSprinting()) {
                    moveSpeed *= 0.7692307692;
                }

                // sneak scale = 0.3 * sprint speed
                else if (mc.player.isSneaking()) {
                    moveSpeed *= 0.3;
                }
                if (maxVelocity > 0 && boost.getValue() && !velocityTimer.passedMs(75) && !mc.player.collidedHorizontally) {
                    moveSpeed = Math.max(moveSpeed, maxVelocity);
                }

                // the current movement input values of the user
                float forward = mc.player.movementInput.moveForward;
                float strafe = mc.player.movementInput.moveStrafe;
                float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!PlayerUtil.isPlayerMoving()) {
                    event.setX(0);
                    event.setZ(0);
                }

                if (forward != 0) {
                    if (strafe > 0) {
                        yaw += ((forward > 0) ? -45 : 45);
                    } else if (strafe < 0) {
                        yaw += ((forward > 0) ? 45 : -45);
                    }

                    strafe = 0;
                    if (forward > 0) {
                        forward = 1;
                    } else if (forward < 0) {
                        forward = -1;
                    }
                }

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw));
                double sin = -Math.sin(Math.toRadians(yaw));

                // update the movements
                event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));
                break;
            }

            /*
             * Similar to Mode: Strafe with a lower jump height in order to reach higher speeds
             * Max speed: ~31 kmh
             */
            case StrafeLow:
                break;
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketEntityAction) {

            // slowdown movement
            if (((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.STOP_SPRINTING) || ((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.START_SNEAKING)) {

                // keep sprint
                if (strictSprint.getValue()) {
                    event.setCanceled(true);
                }
            }
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            if (((ICPacketPlayer) event.getPacket()).isMoving() && offsetPackets) {

                // offset packets
                ((ICPacketPlayer) event.getPacket()).setY(((CPacketPlayer) event.getPacket()).getY(0) + (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, 0.21, 0)).size() > 0 ? 2 : 4));
                offsetPackets = false;
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {

        // reset our process on a rubberband
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            resetProcess();
        }


        if (event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion velocity = event.getPacket();

            maxVelocity = Math.sqrt(velocity.getMotionX() * velocity.getMotionX() + velocity.getMotionZ() * velocity.getMotionZ());


            maxVelocity *= crystalFactor.getValue();

            velocityTimer.reset();
            event.cancel();
        } else if (event.getPacket() instanceof SPacketEntityVelocity) {
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

    /**
     * Resets the Speed process and sets all values back to defaults
     */
    public void resetProcess() {
        strafeStage = 4;
        groundStage = 2;
        moveSpeed = 0;
        distance = 0;
        strictTicks = 0;
        maxVelocity = 0;
        velocityTimer.reset();
        timerManager.reset(8);
        accelerate = false;
        offsetPackets = false;
    }

    public enum Mode {

        /**
         * Speed that automatically jumps to simulate BHops
         */
        Strafe,

        /**
         *
         */
        StrafeStrict,
        /**
         * Strafe with a lower jump height
         */
        StrafeLow,

        /**
         * Speeds your movement while on the ground
         */
        StrafeGround,
        /**
         * Speeds your movement while on the ground, spoofs jump state
         */
        OnGround
    }

    public enum BaseSpeed {

        /**
         * Base speed for NCP
         */
        NewNCP,

        /**
         * Base speed for old NCP
         */
        OldNCP
    }

    public enum Friction {

        /**
         * Factors in material friction but otherwise retains all functionality
         */
        Factor,

        /**
         * Ignores friction
         */
        Fast,

        /**
         * Stop all speed when experiencing friction
         */
        CutOFF
    }
}
