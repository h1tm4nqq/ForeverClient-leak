package we.devs.forever.client.modules.impl.movement;

import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class LongJump extends Module {
    public static LongJump INSTANCE;
    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Bypass));
    public Setting<Float> speed = (new Setting<>("Speed", 4.5f, 0.5f, 20.0f));
    public Setting<Float> modifier = (new Setting<>("Modifier", 5.0F, 0.1F, 10.0F));
    public Setting<Float> glide = (new Setting<>("Glide", 1.0F, 0.1F, 10.0F));
    public Setting<Boolean> shortJump = (new Setting<>("ShortJump", false));
    public Setting<GroundCheck> groundCheck = (new Setting<>("GroundCheck", GroundCheck.Normal));
    public Setting<Boolean> autoDisable = (new Setting<>("AutoDisable", false));

    private enum Mode {
        Normal,
        Bypass
    }

    private enum GroundCheck {
        None,
        Normal,
        EdgeJump
    }
    private final TimerUtil timer = new TimerUtil();
    private boolean timerStatus;
    private boolean walkingStatus;
    private int onGroundTracker = 0;
    private double walkingState;
    private double totalWalkingState;
    private int bypassState;
    private int state;
    private double currentSpeed;
    private boolean groundTracker;

    public LongJump() {
        super("LongJump", "", Category.MOVEMENT);
        INSTANCE = this;
    }

    @EventListener
    public void onUpdateWalkingPlayer(MotionEvent.Pre event) {
        if (groundTracker) {
            if (groundCheck.getValue() == GroundCheck.Normal) {
                if (mc.player.onGround) {
                    groundTracker = false;
                }
            } else if (groundCheck.getValue() == GroundCheck.EdgeJump) {
                if (mc.player.onGround && !mc.player.isSneaking() && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, 0.0, 0.0).shrink(0.001)).isEmpty()) {
                    groundTracker = false;
                }
            }
        } else {
            if (mode.getValue() == Mode.Normal) {
                walkingState = mc.player.posX - mc.player.prevPosX;
                double difZ = mc.player.posZ - mc.player.prevPosZ;
                totalWalkingState = Math.sqrt(walkingState * walkingState + difZ * difZ);
            } else {
                double difX = mc.player.posX - mc.player.prevPosX;
                double difZ = mc.player.posZ - mc.player.prevPosZ;
                totalWalkingState = Math.sqrt(difX * difX + difZ * difZ);
                if (!walkingStatus) return;
                mc.player.motionY = 0.005;
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (autoDisable.getValue()) {
                toggle();
            }
        }
    }

    @EventListener
    public void onMove(MoveEvent event) {
        if (groundTracker) return;
        if (mc.player != mc.getRenderViewEntity()) return;
        switch (mode.getValue()) {
            case Normal: {
                if (mc.player.moveStrafing <= 0.0f && mc.player.moveForward <= 0.0f) {
                    state = 1;
                }
                if (roundDecimalUp((mc.player.posY - (double)((int) mc.player.posY)), 3) == 0.943) {
                    mc.player.motionY -= 0.0157 * glide.getValue();
                    event.setY(event.getY()-0.0157 * glide.getValue());
                }
                if (state == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f)) {
                    state = 2;
                    currentSpeed = speed.getValue() * getBaseSpeed() - 0.01;
                } else if (state == 2) {
                    mc.player.motionY = 0.0848*modifier.getValue();
                    event.setY(0.0848*modifier.getValue());
                    state = 3;
                    currentSpeed *= 2.149802;
                } else if (state == 3) {
                    state = 4;
                    walkingState = 0.66 * totalWalkingState;
                    currentSpeed = totalWalkingState - walkingState;
                } else {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
                        state = 1;
                    }
                    currentSpeed = totalWalkingState - totalWalkingState / 159.0;
                }
                currentSpeed = Math.max(currentSpeed,getBaseSpeed());
                float moveForward = mc.player.movementInput.moveForward;
                float moveStrafe = mc.player.movementInput.moveStrafe;
                float rotationYaw = mc.player.rotationYaw;
                if (moveForward == 0.0f && moveStrafe == 0.0f) {
                    event.setX(0.0);
                    event.setZ(0.0);
                } else {
                    if (moveForward != 0.0f) {
                        if (moveStrafe >= 1.0f) {
                            rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                            moveStrafe = 0.0f;
                        }
                        else {
                            if (moveStrafe <= -1.0f) {
                                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                                moveStrafe = 0.0f;
                            }
                        }
                        if (moveForward > 0.0f) {
                            moveForward = 1.0f;
                        } else if (moveForward < 0.0f) {
                            moveForward = -1.0f;
                        }
                    }
                }
                double cos = Math.cos(Math.toRadians(rotationYaw + 90.0f));
                double sin = Math.sin(Math.toRadians(rotationYaw + 90.0f));
                event.setX(moveForward * currentSpeed * cos + moveStrafe * currentSpeed * sin);
                event.setZ(moveForward * currentSpeed * sin - moveStrafe * currentSpeed * cos);
                return;
            }
            case Bypass: {
                if (timerStatus) {
                    if (mc.player.onGround) {
                        timer.reset();
                    }
                    if (roundDecimalUp((mc.player.posY - (double) ((int) mc.player.posY)), 3) == 0.410) {
                        mc.player.motionY = 0.0;
                    }
                    if (mc.player.moveStrafing <= 0.0f && mc.player.moveForward <= 0.0f) {
                        bypassState = 1;
                    }
                    if (roundDecimalUp(mc.player.posY - (double) ((int) mc.player.posY), 3) == 0.943) {
                        mc.player.motionY = 0.0;
                    }
                    if (bypassState == 1) {
                        if (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) {
                            bypassState = 2;
                            currentSpeed = speed.getValue() * getBaseSpeed() - 0.01;
                        }
                    } else if (bypassState == 2) {
                        bypassState = 3;
                        if (!shortJump.getValue()) {
                            mc.player.motionY = 0.424;
                        }
                        event.setY(0.424);
                        currentSpeed *= 2.149802;
                    } else if (bypassState == 3) {
                        bypassState = 4;
                        double speed = 0.66 * (totalWalkingState - getBaseSpeed());
                        currentSpeed = totalWalkingState - speed;
                    } else {
                        if (!(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() <= 0 && !mc.player.collidedVertically)) {
                            bypassState = 1;
                        }
                        currentSpeed = totalWalkingState - totalWalkingState / 159.0;
                    }
                    currentSpeed = Math.max(currentSpeed, getBaseSpeed());
                    float moveForward = mc.player.movementInput.moveForward;
                    float moveStrafe = mc.player.movementInput.moveStrafe;
                    float rotationYaw = mc.player.rotationYaw;
                    if (moveForward == 0.0f || moveStrafe == 0.0f) {
                        event.setX(0.0);
                        event.setZ(0.0);
                    } else {
                        if (moveStrafe >= 1.0f) {
                            rotationYaw += (float) (moveForward > 0.0f ? -45 : 45);
                            moveStrafe = 0.0f;
                        } else {
                            if (moveStrafe <= -1.0f) {
                                rotationYaw += (float) (moveForward > 0.0f ? 45 : -45);
                                moveStrafe = 0.0f;
                            }
                        }
                        if (moveForward > 0.0f) {
                            moveForward = 1.0f;
                        } else {
                            if (moveForward < 0.0f) {
                                moveForward = -1.0f;
                            }
                        }
                    }
                    double cos = Math.cos(Math.toRadians(rotationYaw + 90.0f));
                    double sin = Math.sin(Math.toRadians(rotationYaw + 90.0f));
                    event.setX((double) moveForward * currentSpeed * cos + (double) moveStrafe * currentSpeed * sin);
                    event.setZ((double) moveForward * currentSpeed * sin - (double) moveStrafe * currentSpeed * cos);
                    if (moveForward == 0.0f && moveStrafe == 0.0f) {
                        event.setX(0.0);
                        event.setZ(0.0);
                    }
                }
            }
            if (mc.player.onGround) {
                onGroundTracker++;
            } else {
                if (!mc.player.onGround && onGroundTracker != 0) {
                    onGroundTracker--;
                }
            }
            if (shortJump.getValue()) {
                if (timer.passedMs(35L)) {
                    walkingStatus = true;
                }
                if ((timer.passedMs(2490L))) {
                    walkingStatus = false;
                    timerStatus = false;
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                }
                if (!timer.passedMs(2820L)) return;
                timerStatus = true;
                mc.player.motionX *= 0.0;
                mc.player.motionZ *= 0.0;
                timer.reset();
            } else {
                if (timer.passedMs(480L)) {
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                    timerStatus = false;
                }
                if (timer.passedMs(780L)) {
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                    timerStatus = true;
                    timer.reset();
                }
            }
        }
    }

    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            currentSpeed = getBaseSpeed();
            mc.player.onGround = true;
        }
        groundTracker = groundCheck.getValue() != GroundCheck.None;
        walkingStatus = false;
        timerStatus = true;
        totalWalkingState = 0.0;
        state = 1;
    }

    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            if (mode.getValue() == Mode.Bypass) {
                mc.player.onGround = false;
                mc.player.capabilities.isFlying = false;
            }
        }
    }

    public static double getBaseSpeed() {
        double baseSpeed = 0.2873;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            int speedAmplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + 0.2D * (double) (speedAmplifier + 1);
        }
        return baseSpeed;
    }

    public double roundDecimalUp(double d, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}