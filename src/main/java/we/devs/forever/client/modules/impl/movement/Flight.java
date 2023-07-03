package we.devs.forever.client.modules.impl.movement;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Flight extends Module {
    private static Flight INSTANCE;

    static {
        Flight.INSTANCE = new Flight();
    }

    private final Fly flySwitch;
    private final List<CPacketPlayer> packets;
    private final TimerUtil delayTimerUtil;
    public Setting<Mode> mode;
    public Setting<Boolean> better;
    public Setting<Boolean> phase;
    public Setting<Format> format;
    public Setting<PacketMode> type;
    public Setting<Float> speed;
    public Setting<Boolean> noKick;
    public Setting<Boolean> noClip;
    public Setting<Boolean> groundSpoof;
    public Setting<Boolean> antiGround;
    public Setting<Integer> cooldown;
    public Setting<Boolean> ascend;
    private int teleportId;
    private int counter;
    private double moveSpeed;
    private double lastDist;
    private int level;

    public Flight() {
        super("Flight", "Makes you fly.", Category.MOVEMENT);
        this.flySwitch = new Fly();
        this.packets = new ArrayList<CPacketPlayer>();
        this.delayTimerUtil = new TimerUtil();
        this.mode = (Setting<Mode>) (new Setting<>("Mode", Mode.Packet));
        this.better = (Setting<Boolean>) (new Setting<>("Better", false, v -> this.mode.getValue() == Mode.Packet));
        this.phase = (Setting<Boolean>) (new Setting<>("Phase", false, v -> this.mode.getValue() == Mode.Packet && this.better.getValue()));
        this.format = (Setting<Format>) (new Setting<>("Format", Format.Damage, v -> this.mode.getValue() == Mode.Damage));
        this.type = (Setting<PacketMode>) (new Setting<>("Type", PacketMode.Y, v -> this.mode.getValue() == Mode.Packet));
        this.speed = (Setting<Float>) (new Setting<>("Speed", 0.1f, 0.0f, 10.0f, "The speed.", v -> this.mode.getValue() == Mode.Packet || this.mode.getValue() == Mode.Creative || this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Descend || this.mode.getValue() == Mode.Damage));
        this.noKick = (Setting<Boolean>) (new Setting<>("NoKick", false, v -> this.mode.getValue() == Mode.Packet || this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Damage));
        this.noClip = (Setting<Boolean>) (new Setting<>("NoClip", false, v -> this.mode.getValue() == Mode.Damage));
        this.groundSpoof = (Setting<Boolean>) (new Setting<>("GroundSpoof", false, v -> this.mode.getValue() == Mode.Spoof));
        this.antiGround = (Setting<Boolean>) (new Setting<>("AntiGround", true, v -> this.mode.getValue() == Mode.Spoof));
        this.cooldown = (Setting<Integer>) (new Setting<>("Cooldown", 1, v -> this.mode.getValue() == Mode.Descend));
        this.ascend = (Setting<Boolean>) (new Setting<>("Ascend", false, v -> this.mode.getValue() == Mode.Descend));
    }

    @EventListener
    public void onTickEvent(final TickEvent.ClientTickEvent event) {
        if (fullNullCheck() || this.mode.getValue() != Mode.Descend) {
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            if (!Flight.mc.player.isElytraFlying()) {
                if (this.counter < 1) {
                    this.counter += this.cooldown.getValue();
                    Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(Flight.mc.player.posX, Flight.mc.player.posY, Flight.mc.player.posZ, false));
                    Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(Flight.mc.player.posX, Flight.mc.player.posY - 0.03, Flight.mc.player.posZ, true));
                } else {
                    --this.counter;
                }
            }
        } else {
            Flight.mc.player.motionY = this.ascend.getValue() ? this.speed.getValue() : ((double) (-this.speed.getValue()));
        }
    }

    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        if (this.mode.getValue() == Mode.Packet) {
            this.teleportId = 0;
            this.packets.clear();
            final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, 0.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
            this.packets.add(bounds);
            Flight.mc.player.connection.sendPacket(bounds);
        }
        if (this.mode.getValue() == Mode.Creative) {
            Flight.mc.player.capabilities.isFlying = true;
            if (Flight.mc.player.capabilities.isCreativeMode) {
                return;
            }
            Flight.mc.player.capabilities.allowFlying = true;
        }
        if (this.mode.getValue() == Mode.Spoof) {
            this.flySwitch.enable();
        }
        if (this.mode.getValue() == Mode.Damage) {
            this.level = 0;
            if (this.format.getValue() == Format.Packet && Flight.mc.world != null) {
                this.teleportId = 0;
                this.packets.clear();
                final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, (Flight.mc.player.posY <= 10.0) ? 255.0 : 1.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
                this.packets.add(bounds);
                Flight.mc.player.connection.sendPacket(bounds);
            }
        }
    }

    @EventListener
    public void onUpdateWalkingPlayerPre(MotionEvent.Pre event) {
        if (this.mode.getValue() == Mode.Damage) {
            if (this.format.getValue() == Format.Damage) {
                if (event.getStage() == 0) {
                    Flight.mc.player.motionY = 0.0;
                    double motionY = 0.41999998688697815;
                    if (Flight.mc.player.onGround) {
                        if (Flight.mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                            motionY += (Objects.requireNonNull(Flight.mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
                        }
                        Flight.mc.player.motionY = motionY;
                        positionManager.setPlayerPosition(Flight.mc.player.posX, Flight.mc.player.motionY, Flight.mc.player.posZ);
                        this.moveSpeed *= 2.149;
                    }
                }
                if (Flight.mc.player.ticksExisted % 2 == 0) {
                    Flight.mc.player.setPosition(Flight.mc.player.posX, Flight.mc.player.posY + MathUtil.getRandom(1.2354235325235235E-14, 1.2354235325235233E-13), Flight.mc.player.posZ);
                }
                if (Flight.mc.gameSettings.keyBindJump.isKeyDown()) {
                    final EntityPlayerSP player = Flight.mc.player;
                    player.motionY += this.speed.getValue() / 2.0f;
                }
                if (Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    final EntityPlayerSP player2 = Flight.mc.player;
                    player2.motionY -= this.speed.getValue() / 2.0f;
                }
            }
            if (this.format.getValue() == Format.Normal) {
                Flight.mc.player.motionY = Flight.mc.gameSettings.keyBindJump.isKeyDown() ? this.speed.getValue() : (Flight.mc.gameSettings.keyBindSneak.isKeyDown() ? (-this.speed.getValue()) : 0.0);
                if (this.noKick.getValue() && Flight.mc.player.ticksExisted % 5 == 0) {
                    positionManager.setPlayerPosition(Flight.mc.player.posX, Flight.mc.player.posY - 0.03125, Flight.mc.player.posZ, true);
                }
                final double[] dir = EntityUtil.forward(this.speed.getValue());
                Flight.mc.player.motionX = dir[0];
                Flight.mc.player.motionZ = dir[1];
            }
            if (this.format.getValue() == Format.Packet) {
                if (this.teleportId <= 0) {
                    final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, (Flight.mc.player.posY <= 10.0) ? 255.0 : 1.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
                    this.packets.add(bounds);
                    Flight.mc.player.connection.sendPacket(bounds);
                    return;
                }
                Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                final double posY = -1.0E-8;
                if (!Flight.mc.gameSettings.keyBindJump.isKeyDown() && !Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    if (EntityUtil.isMoving()) {
                        for (double x = 0.0625; x < this.speed.getValue(); x += 0.262) {
                            final double[] dir2 = EntityUtil.forward(x);
                            Flight.mc.player.setVelocity(dir2[0], posY, dir2[1]);
                            this.move(dir2[0], posY, dir2[1]);
                        }
                    }
                } else if (Flight.mc.gameSettings.keyBindJump.isKeyDown()) {
                    for (int i = 0; i <= 3; ++i) {
                        Flight.mc.player.setVelocity(0.0, (Flight.mc.player.ticksExisted % 20 == 0) ? -0.03999999910593033 : ((double) (0.062f * i)), 0.0);
                        this.move(0.0, (Flight.mc.player.ticksExisted % 20 == 0) ? -0.03999999910593033 : ((double) (0.062f * i)), 0.0);
                    }
                } else if (Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    for (int i = 0; i <= 3; ++i) {
                        Flight.mc.player.setVelocity(0.0, posY - 0.0625 * i, 0.0);
                        this.move(0.0, posY - 0.0625 * i, 0.0);
                    }
                }
            }
            if (this.format.getValue() == Format.Slow) {
                final double posX = Flight.mc.player.posX;
                final double posY2 = Flight.mc.player.posY;
                final double posZ = Flight.mc.player.posZ;
                final boolean ground = Flight.mc.player.onGround;
                Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                if (!Flight.mc.gameSettings.keyBindJump.isKeyDown() && !Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    final double[] dir3 = EntityUtil.forward(0.0625);
                    Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(posX + dir3[0], posY2, posZ + dir3[1], ground));
                    Flight.mc.player.setPositionAndUpdate(posX + dir3[0], posY2, posZ + dir3[1]);
                } else if (Flight.mc.gameSettings.keyBindJump.isKeyDown()) {
                    Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY2 + 0.0625, posZ, ground));
                    Flight.mc.player.setPositionAndUpdate(posX, posY2 + 0.0625, posZ);
                } else if (Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY2 - 0.0625, posZ, ground));
                    Flight.mc.player.setPositionAndUpdate(posX, posY2 - 0.0625, posZ);
                }
                Flight.mc.player.connection.sendPacket(new CPacketPlayer.Position(posX + Flight.mc.player.motionX, (Flight.mc.player.posY <= 10.0) ? 255.0 : 1.0, posZ + Flight.mc.player.motionZ, ground));
            }
            if (this.format.getValue() == Format.Delay) {
                if (this.delayTimerUtil.passedMs(1000L)) {
                    this.delayTimerUtil.reset();
                }
                if (this.delayTimerUtil.passedMs(600L)) {
                    Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                    return;
                }
                if (this.teleportId <= 0) {
                    final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, (Flight.mc.player.posY <= 10.0) ? 255.0 : 1.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
                    this.packets.add(bounds);
                    Flight.mc.player.connection.sendPacket(bounds);
                    return;
                }
                Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                final double posY = -1.0E-8;
                if (!Flight.mc.gameSettings.keyBindJump.isKeyDown() && !Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    if (EntityUtil.isMoving()) {
                        final double[] dir4 = EntityUtil.forward(0.2);
                        Flight.mc.player.setVelocity(dir4[0], posY, dir4[1]);
                        this.move(dir4[0], posY, dir4[1]);
                    }
                } else if (Flight.mc.gameSettings.keyBindJump.isKeyDown()) {
                    Flight.mc.player.setVelocity(0.0, 0.06199999898672104, 0.0);
                    this.move(0.0, 0.06199999898672104, 0.0);
                } else if (Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    Flight.mc.player.setVelocity(0.0, 0.0625, 0.0);
                    this.move(0.0, 0.0625, 0.0);
                }
            }
            if (this.noClip.getValue()) {
                Flight.mc.player.noClip = true;
            }
        }
        if (event.getStage() == 0) {
            if (this.mode.getValue() == Mode.Creative) {
                Flight.mc.player.capabilities.setFlySpeed(this.speed.getValue());
                Flight.mc.player.capabilities.isFlying = true;
                if (Flight.mc.player.capabilities.isCreativeMode) {
                    return;
                }
                Flight.mc.player.capabilities.allowFlying = true;
            }
            if (this.mode.getValue() == Mode.Vanilla) {
                Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                Flight.mc.player.jumpMovementFactor = this.speed.getValue();
                if (this.noKick.getValue() && Flight.mc.player.ticksExisted % 4 == 0) {
                    Flight.mc.player.motionY = -0.03999999910593033;
                }
                final double[] dir = MathUtil.directionSpeed(this.speed.getValue());
                if (Flight.mc.player.movementInput.moveStrafe != 0.0f || Flight.mc.player.movementInput.moveForward != 0.0f) {
                    Flight.mc.player.motionX = dir[0];
                    Flight.mc.player.motionZ = dir[1];
                } else {
                    Flight.mc.player.motionX = 0.0;
                    Flight.mc.player.motionZ = 0.0;
                }
                if (Flight.mc.gameSettings.keyBindJump.isKeyDown()) {
                    final EntityPlayerSP player3 = Flight.mc.player;
                    double motionY;
                    if (this.noKick.getValue()) {
                        motionY = (Flight.mc.player.ticksExisted % 20 == 0) ? -0.03999999910593033 : this.speed.getValue();
                    } else {
                        final EntityPlayerSP player4 = Flight.mc.player;
                        motionY = (player4.motionY += this.speed.getValue());
                    }
                    player3.motionY = motionY;
                }
                if (Flight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    final EntityPlayerSP player5 = Flight.mc.player;
                    player5.motionY -= this.speed.getValue();
                }
            }
            if (this.mode.getValue() == Mode.Packet && !this.better.getValue()) {
                this.doNormalPacketFly();
            }
            if (this.mode.getValue() == Mode.Packet && this.better.getValue()) {
                this.doBetterPacketFly();
            }
        }
    }

    private void doNormalPacketFly() {
        if (this.teleportId <= 0) {
            final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, 0.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
            this.packets.add(bounds);
            Flight.mc.player.connection.sendPacket(bounds);
            return;
        }
        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
        if (Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, 0.0, -0.0625)).isEmpty()) {
            final double ySpeed = Flight.mc.gameSettings.keyBindJump.isKeyDown() ? (this.noKick.getValue() ? ((Flight.mc.player.ticksExisted % 20 == 0) ? -0.03999999910593033 : 0.06199999898672104) : 0.06199999898672104) : (Flight.mc.gameSettings.keyBindSneak.isKeyDown() ? -0.062 : (Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty() ? ((Flight.mc.player.ticksExisted % 4 == 0) ? (this.noKick.getValue() ? -0.04f : 0.0f) : 0.0) : 0.0));
            final double[] directionalSpeed = MathUtil.directionSpeed(this.speed.getValue());
            if (Flight.mc.gameSettings.keyBindJump.isKeyDown() || Flight.mc.gameSettings.keyBindSneak.isKeyDown() || Flight.mc.gameSettings.keyBindForward.isKeyDown() || Flight.mc.gameSettings.keyBindBack.isKeyDown() || Flight.mc.gameSettings.keyBindRight.isKeyDown() || Flight.mc.gameSettings.keyBindLeft.isKeyDown()) {
                if (directionalSpeed[0] != 0.0 || directionalSpeed[1] != 0.0) {
                    if (Flight.mc.player.movementInput.jump && (Flight.mc.player.moveStrafing != 0.0f || Flight.mc.player.moveForward != 0.0f)) {
                        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                        this.move(0.0, 0.0, 0.0);
                        for (int i = 0; i <= 3; ++i) {
                            Flight.mc.player.setVelocity(0.0, ySpeed * i, 0.0);
                            this.move(0.0, ySpeed * i, 0.0);
                        }
                    } else if (Flight.mc.player.movementInput.jump) {
                        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                        this.move(0.0, 0.0, 0.0);
                        for (int i = 0; i <= 3; ++i) {
                            Flight.mc.player.setVelocity(0.0, ySpeed * i, 0.0);
                            this.move(0.0, ySpeed * i, 0.0);
                        }
                    } else {
                        for (int i = 0; i <= 2; ++i) {
                            Flight.mc.player.setVelocity(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                            this.move(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                        }
                    }
                }
            } else if (this.noKick.getValue() && Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty()) {
                Flight.mc.player.setVelocity(0.0, (Flight.mc.player.ticksExisted % 2 == 0) ? 0.03999999910593033 : -0.03999999910593033, 0.0);
                this.move(0.0, (Flight.mc.player.ticksExisted % 2 == 0) ? 0.03999999910593033 : -0.03999999910593033, 0.0);
            }
        }
    }

    private void doBetterPacketFly() {
        if (this.teleportId <= 0) {
            final CPacketPlayer.Position bounds = new CPacketPlayer.Position(Flight.mc.player.posX, 10000.0, Flight.mc.player.posZ, Flight.mc.player.onGround);
            this.packets.add(bounds);
            Flight.mc.player.connection.sendPacket(bounds);
            return;
        }
        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
        if (Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, 0.0, -0.0625)).isEmpty()) {
            final double ySpeed = Flight.mc.gameSettings.keyBindJump.isKeyDown() ? (this.noKick.getValue() ? ((Flight.mc.player.ticksExisted % 20 == 0) ? -0.03999999910593033 : 0.06199999898672104) : 0.06199999898672104) : (Flight.mc.gameSettings.keyBindSneak.isKeyDown() ? -0.062 : (Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty() ? ((Flight.mc.player.ticksExisted % 4 == 0) ? (this.noKick.getValue() ? -0.04f : 0.0f) : 0.0) : 0.0));
            final double[] directionalSpeed = MathUtil.directionSpeed(this.speed.getValue());
            if (Flight.mc.gameSettings.keyBindJump.isKeyDown() || Flight.mc.gameSettings.keyBindSneak.isKeyDown() || Flight.mc.gameSettings.keyBindForward.isKeyDown() || Flight.mc.gameSettings.keyBindBack.isKeyDown() || Flight.mc.gameSettings.keyBindRight.isKeyDown() || Flight.mc.gameSettings.keyBindLeft.isKeyDown()) {
                if (directionalSpeed[0] != 0.0 || directionalSpeed[1] != 0.0) {
                    if (Flight.mc.player.movementInput.jump && (Flight.mc.player.moveStrafing != 0.0f || Flight.mc.player.moveForward != 0.0f)) {
                        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                        this.move(0.0, 0.0, 0.0);
                        for (int i = 0; i <= 3; ++i) {
                            Flight.mc.player.setVelocity(0.0, ySpeed * i, 0.0);
                            this.move(0.0, ySpeed * i, 0.0);
                        }
                    } else if (Flight.mc.player.movementInput.jump) {
                        Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                        this.move(0.0, 0.0, 0.0);
                        for (int i = 0; i <= 3; ++i) {
                            Flight.mc.player.setVelocity(0.0, ySpeed * i, 0.0);
                            this.move(0.0, ySpeed * i, 0.0);
                        }
                    } else {
                        for (int i = 0; i <= 2; ++i) {
                            Flight.mc.player.setVelocity(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                            this.move(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                        }
                    }
                }
            } else if (this.noKick.getValue() && Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty()) {
                Flight.mc.player.setVelocity(0.0, (Flight.mc.player.ticksExisted % 2 == 0) ? 0.03999999910593033 : -0.03999999910593033, 0.0);
                this.move(0.0, (Flight.mc.player.ticksExisted % 2 == 0) ? 0.03999999910593033 : -0.03999999910593033, 0.0);
            }
        }
    }

    public void onUpdate() {
        if (this.mode.getValue() == Mode.Spoof) {
            if (fullNullCheck()) {
                return;
            }
            if (!Flight.mc.player.capabilities.allowFlying) {
                this.flySwitch.disable();
                this.flySwitch.enable();
                Flight.mc.player.capabilities.isFlying = false;
            }
            Flight.mc.player.capabilities.setFlySpeed(0.05f * this.speed.getValue());
        }
    }

    public void onDisable() {
        if (this.mode.getValue() == Mode.Creative && Flight.mc.player != null) {
            Flight.mc.player.capabilities.isFlying = false;
            Flight.mc.player.capabilities.setFlySpeed(0.05f);
            if (Flight.mc.player.capabilities.isCreativeMode) {
                return;
            }
            Flight.mc.player.capabilities.allowFlying = false;
        }
        if (this.mode.getValue() == Mode.Spoof) {
            this.flySwitch.disable();
        }
        if (this.mode.getValue() == Mode.Damage) {
            timerManager.reset(10);
            Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
            this.lastDist = 0.0;
            if (this.noClip.getValue()) {
                Flight.mc.player.noClip = false;
            }
        }
    }

    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    public void onLogout() {
        if (this.isEnabled()) {
            this.disable();
        }
    }

    @EventListener
    public void onMove(final MoveEvent event) {
        if (event.getStage() == 0 && this.mode.getValue() == Mode.Damage && this.format.getValue() == Format.Damage) {
            double forward = Flight.mc.player.movementInput.moveForward;
            double strafe = Flight.mc.player.movementInput.moveStrafe;
            final float yaw = Flight.mc.player.rotationYaw;
            if (forward == 0.0 && strafe == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            }
            if (forward != 0.0 && strafe != 0.0) {
                forward *= Math.sin(0.7853981633974483);
                strafe *= Math.cos(0.7853981633974483);
            }
            if (this.level != 1 || (Flight.mc.player.moveForward == 0.0f && Flight.mc.player.moveStrafing == 0.0f)) {
                if (this.level == 2) {
                    ++this.level;
                } else if (this.level == 3) {
                    ++this.level;
                    final double difference = ((Flight.mc.player.ticksExisted % 2 == 0) ? -0.05 : 0.1);
                    this.moveSpeed = this.lastDist - difference;
                } else {
                    if (Flight.mc.world.getCollisionBoxes(Flight.mc.player, Flight.mc.player.getEntityBoundingBox().offset(0.0, Flight.mc.player.motionY, 0.0)).size() > 0 || Flight.mc.player.collidedVertically) {
                        this.level = 1;
                    }
                    this.moveSpeed = this.lastDist - this.lastDist / 159.0;
                }
            } else {
                this.level = 2;
                final double boost = Flight.mc.player.isPotionActive(MobEffects.SPEED) ? 1.86 : 2.05;
            }
            final double mx = -Math.sin(Math.toRadians(yaw));
            final double mz = Math.cos(Math.toRadians(yaw));
            event.setX(forward * this.moveSpeed * mx + strafe * this.moveSpeed * mz);
            event.setZ(forward * this.moveSpeed * mz - strafe * this.moveSpeed * mx);
        }
    }

    @EventListener
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getStage() == 0) {
            if (this.mode.getValue() == Mode.Packet) {
                if (fullNullCheck()) {
                    return;
                }
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.cancel();
                }
                if (event.getPacket() instanceof CPacketPlayer) {
                    final CPacketPlayer packet = event.getPacket();
                    if (this.packets.contains(packet)) {
                        this.packets.remove(packet);
                        return;
                    }
                    event.cancel();
                }
            }
            if (this.mode.getValue() == Mode.Spoof) {
                if (fullNullCheck()) {
                    return;
                }
                if (!this.groundSpoof.getValue() || !(event.getPacket() instanceof CPacketPlayer) || !Flight.mc.player.capabilities.isFlying) {
                    return;
                }
                final CPacketPlayer packet = event.getPacket();
                if (!packet.moving) {
                    return;
                }
                final AxisAlignedBB range = Flight.mc.player.getEntityBoundingBox().expand(0.0, -Flight.mc.player.posY, 0.0).contract(0.0, -Flight.mc.player.height, 0.0);
                final List<AxisAlignedBB> collisionBoxes = Flight.mc.player.world.getCollisionBoxes(Flight.mc.player, range);
                final AtomicReference<Double> newHeight = new AtomicReference<Double>(0.0);
                packet.y = newHeight.get();
                packet.onGround = true;
            }
            if (this.mode.getValue() == Mode.Damage && (this.format.getValue() == Format.Packet || this.format.getValue() == Format.Delay)) {
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.cancel();
                }
                if (event.getPacket() instanceof CPacketPlayer) {
                    final CPacketPlayer packet = event.getPacket();
                    if (this.packets.contains(packet)) {
                        this.packets.remove(packet);
                        return;
                    }
                    event.cancel();
                }
            }
        }
    }

    @EventListener
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getStage() == 0) {
            if (this.mode.getValue() == Mode.Packet) {
                if (fullNullCheck()) {
                    return;
                }
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    final SPacketPlayerPosLook packet = event.getPacket();
                    if (Flight.mc.player.isEntityAlive() && Flight.mc.world.isBlockLoaded(new BlockPos(Flight.mc.player.posX, Flight.mc.player.posY, Flight.mc.player.posZ)) && !(Flight.mc.currentScreen instanceof GuiDownloadTerrain)) {
                        if (this.teleportId <= 0) {
                            this.teleportId = packet.getTeleportId();
                        } else {
                            event.cancel();
                        }
                    }
                }
            }
            if (this.mode.getValue() == Mode.Spoof) {
                if (fullNullCheck()) {
                    return;
                }
                if (!this.antiGround.getValue() || !(event.getPacket() instanceof SPacketPlayerPosLook) || !Flight.mc.player.capabilities.isFlying) {
                    return;
                }
                final SPacketPlayerPosLook packet = event.getPacket();
                final double oldY = Flight.mc.player.posY;
                Flight.mc.player.setPosition(packet.x, packet.y, packet.z);
                final AxisAlignedBB range = Flight.mc.player.getEntityBoundingBox().expand(0.0, 256.0f - Flight.mc.player.height - Flight.mc.player.posY, 0.0).contract(0.0, Flight.mc.player.height, 0.0);
                final List<AxisAlignedBB> collisionBoxes = Flight.mc.player.world.getCollisionBoxes(Flight.mc.player, range);
                final AtomicReference<Double> newY = new AtomicReference<Double>(256.0);
                packet.y = Math.min(oldY, newY.get());
            }
            if (this.mode.getValue() == Mode.Damage && (this.format.getValue() == Format.Packet || this.format.getValue() == Format.Delay) && event.getPacket() instanceof SPacketPlayerPosLook) {
                final SPacketPlayerPosLook packet = event.getPacket();
                if (Flight.mc.player.isEntityAlive() && Flight.mc.world.isBlockLoaded(new BlockPos(Flight.mc.player.posX, Flight.mc.player.posY, Flight.mc.player.posZ)) && !(Flight.mc.currentScreen instanceof GuiDownloadTerrain)) {
                    if (this.teleportId <= 0) {
                        this.teleportId = packet.getTeleportId();
                    } else {
                        event.cancel();
                    }
                }
            }
        }
    }

    @EventListener
    public void onSettingChange(final ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this) && this.isEnabled() && !event.getSetting().equals(this.enabled)) {
            this.disable();
        }
    }

    @EventListener
    public void onPush(final PushEvent event) {
        if (event.getStage() == 1 && this.mode.getValue() == Mode.Packet && this.better.getValue() && this.phase.getValue()) {
            event.cancel();
        }
    }

    private void move(final double x, final double y, final double z) {
        final CPacketPlayer.Position pos = new CPacketPlayer.Position(Flight.mc.player.posX + x, Flight.mc.player.posY + y, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
        this.packets.add(pos);
        Flight.mc.player.connection.sendPacket(pos);
        final Object bounds = this.better.getValue() ? this.createBoundsPacket(x, y, z) : new CPacketPlayer.Position(Flight.mc.player.posX + x, 0.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
        this.packets.add((CPacketPlayer) bounds);
        Flight.mc.player.connection.sendPacket((Packet) bounds);
        ++this.teleportId;
        Flight.mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId - 1));
        Flight.mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId));
        Flight.mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId + 1));
    }

    private CPacketPlayer createBoundsPacket(final double x, final double y, final double z) {
        switch (this.type.getValue()) {
            case Up: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, 10000.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
            case Down: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, -10000.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
            case Zero: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, 0.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
            case Y: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, (Flight.mc.player.posY + y <= 10.0) ? 255.0 : 1.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
            case X: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x + 75.0, Flight.mc.player.posY + y, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
            case Z: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, Flight.mc.player.posY + y, Flight.mc.player.posZ + z + 75.0, Flight.mc.player.onGround);
            }
            case XZ: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x + 75.0, Flight.mc.player.posY + y, Flight.mc.player.posZ + z + 75.0, Flight.mc.player.onGround);
            }
            default: {
                return new CPacketPlayer.Position(Flight.mc.player.posX + x, 2000.0, Flight.mc.player.posZ + z, Flight.mc.player.onGround);
            }
        }
    }

    private enum PacketMode {
        Up,
        Down,
        Zero,
        Y,
        X,
        Z,
        XZ
    }

    public enum Format {
        Damage,
        Slow,
        Delay,
        Normal,
        Packet
    }

    public enum Mode {
        Creative,
        Vanilla,
        Packet,
        Spoof,
        Descend,
        Damage
    }

    private static class Fly {
        protected void enable() {
            Util.mc.addScheduledTask(() -> {
                if (Util.mc.player != null && Util.mc.player.capabilities != null) {
                    Util.mc.player.capabilities.allowFlying = true;
                    Util.mc.player.capabilities.isFlying = true;
                }
            });
        }

        protected void disable() {
            PlayerCapabilities gmCaps;
            PlayerCapabilities capabilities;
            Util.mc.addScheduledTask(() -> {
                if (Util.mc.player != null && Util.mc.player.capabilities != null) {
                }
            });
        }
    }
}
