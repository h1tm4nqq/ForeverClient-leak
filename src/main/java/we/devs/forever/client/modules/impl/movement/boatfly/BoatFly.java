package we.devs.forever.client.modules.impl.movement.boatfly;

import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.TravelEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.movement.boatfly.enums.Mode;
import we.devs.forever.client.modules.impl.movement.boatfly.enums.Strict;
import we.devs.forever.client.setting.Setting;

public class BoatFly extends Module {
    public static BoatFly INSTANCE;
    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Packet));
    private final Setting<Float> speed = (new Setting<>("Speed", 2.0f, 0.0f, 40.0f));
    private final Setting<Float> ySpeed = (new Setting<>("YSpeed", 1.0f, 0.0f, 10.0f));
    private final Setting<Float> glideSpeed = (new Setting<>("GlideSpeed", 1.0f, 0.0f, 10.0f));
    private final Setting<Float> timer = (new Setting<>("Timer", 1.0f, 0.0f, 5.0f));
    private final Setting<Strict> strict = (new Setting<>("Strict", Strict.None));
    private final Setting<Boolean> limit = (new Setting<>("Limit", false, v -> isPacketMode()));
    private final Setting<Boolean> remount = (new Setting<>("Remount", true));
    private final Setting<Boolean> cancel = (new Setting<>("Cancel", true));
    private final Setting<Boolean> cancelRotations = (new Setting<>("CancelRotations", true));
    private final Setting<Boolean> spoofPackets = (new Setting<>("SpoofPackets", false));
    public final Setting<Float> offset = (new Setting<>("Offset", 0.1f, 0.0f, 10.0f,
            v -> spoofPackets.getValue()));
    private final Setting<Boolean> onGroundPacket = (new Setting<>("OnGroundPacket", false));
    private final Setting<Boolean> phase = (new Setting<>("Phase", true, v -> isMotionMode()));
    private final Setting<Boolean> gravity = (new Setting<>("Gravity", true, v -> isNotMotionMode()));
    public final Setting<Float> renderScale = (new Setting<>("RenderScale", 1.0f, 0.01f, 1.0f));
    private final Setting<Boolean> stop = (new Setting<>("Stop", false));
    private final Setting<Integer> enableTicks = (new Setting<>("EnableTicks", 10, 1, 100,
            v -> stop.getValue()));
    private final Setting<Integer> waitTicks = (new Setting<>("WaitTicks", 10, 1, 100,
            v -> stop.getValue()));
    private final Setting<Boolean> stopUnloaded = (new Setting<>("StopUnloaded", true));
    private final Setting<Boolean> autoMount = (new Setting<>("AutoMount", true));
    private final Setting<Boolean> debug = (new Setting<>("Debug", true));
    private final Setting<Boolean> yLimit = (new Setting<>("yLimit", false));
    private final Setting<Float> height = (new Setting<>("Height", 127.0f, 0.0f, 256.0f,
            v -> yLimit.getValue()));
    private final ConcurrentSet<CPacketVehicleMove> packet = new ConcurrentSet<>();
    private int ticks = 0;
    private int wait = 0;
    private boolean isWait = false;
    private boolean isUnloaded = false;
    private boolean isSpoofPacket = false;

    public BoatFly() {
        super("BoatFly", "Improved BoatFly", Category.MOVEMENT);
        INSTANCE = this;
    }

    public BoatFly geyInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.player.world == null) {
            this.disable();
            return;
        }
        if ((this.autoMount.getValue())) {
            this.isBoat();
        }
    }

    @Override
    public void onDisable() {
        timerManager.setTimer(1.0f,11);
        this.packet.clear();
        this.isWait = false;
        if (mc.player == null) {
            return;
        }
        if ((this.phase.getValue()) && this.mode.getValue() == Mode.Motion) {
            if (mc.player.getRidingEntity() != null) {
                mc.player.getRidingEntity().noClip = false;
            }
            mc.player.noClip = false;
        }
        if (mc.player.getRidingEntity() != null) {
            mc.player.getRidingEntity().setNoGravity(false);
        }
        mc.player.setNoGravity(false);
    }

    @Override
    public String getDisplayInfo() {
        return ChatFormatting.WHITE + ((Mode) (this.mode.getValue())).name();
    }


    private float spoofPacket() {
        this.isSpoofPacket = !this.isSpoofPacket;
        return this.isSpoofPacket ? ((Float) this.offset.getValue()) : -((Float) this.offset.getValue());
    }

    private void sendPacket(CPacketVehicleMove cPacketVehicleMove) {
        this.packet.add(cPacketVehicleMove);
        mc.player.connection.sendPacket(cPacketVehicleMove);
    }

    private void setGround(Entity entity) {
        double d = entity.posY;
        BlockPos blockPos = new BlockPos(entity.posX, (double) ((int) entity.posY), entity.posZ);
        for (int i = 0; i < 255; ++i) {
            if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable() || mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                entity.posY = blockPos.getY() + 1;
                if ((this.debug.getValue())) {
                    Command.sendMessage("GroundY" + entity.posY);
                }
                this.sendPacket(new CPacketVehicleMove(entity));
                entity.posY = d;
                break;
            }
            blockPos = blockPos.add(0, -1, 0);
        }
    }

    private void isBoat() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityBoat) || !(mc.player.getDistance(entity) < 5.0f)) continue;
            mc.player.connection.sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND));
            break;
        }
    }

    @EventListener(priority = ListenerPriority.HIGHEST)
    private void onTravel(TravelEvent eventPlayerTravel) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.getRidingEntity() == null) {
            if ((this.autoMount.getValue())) {
                this.isBoat();
            }
            return;
        }
        if ((this.phase.getValue()) && this.mode.getValue() == Mode.Motion) {
            mc.player.getRidingEntity().noClip = true;
            mc.player.getRidingEntity().setNoGravity(true);
            mc.player.noClip = true;
        }
        if (!this.isUnloaded && isNotMotionMode()) {
            mc.player.getRidingEntity().setNoGravity(this.gravity.getValue() == false);
            mc.player.setNoGravity(this.gravity.getValue() == false);
        }
        if ((this.stop.getValue())) {
            if (this.ticks > (Integer) this.enableTicks.getValue() && !this.isWait) {
                this.ticks = 0;
                this.isWait = true;
                this.wait = (Integer) this.waitTicks.getValue();
            }
            if (this.wait > 0 && this.isWait) {
                --this.wait;
                return;
            }
            if (this.wait <= 0) {
                this.isWait = false;
            }
        }
        Entity entity = mc.player.getRidingEntity();
        if ((this.debug.getValue())) {
            Command.sendMessage("Y" + entity.posY);
            Command.sendMessage("Fall" + entity.fallDistance);
        }
        if ((!mc.world.isChunkGeneratedAt(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4) || entity.getPosition().getY() < 0) && (this.stopUnloaded.getValue())) {
            if ((this.debug.getValue())) {
                Command.sendMessage("Detected unloaded chunk!");
            }
            this.isUnloaded = true;
            return;
        }
        if (((Float) this.timer.getValue()) != 1.0f) {
            timerManager.setTimer(((Float) this.timer.getValue()),11);
        }
        entity.rotationYaw = mc.player.rotationYaw;
        double[] dArray = RotationUtil.Method1330(((Float) this.speed.getValue()));
        double d = entity.posX + dArray[0];
        double d2 = entity.posZ + dArray[1];
        double d3 = entity.posY;
        if ((!mc.world.isChunkGeneratedAt((int) d >> 4, (int) d2 >> 4) || entity.getPosition().getY() < 0) && (this.stopUnloaded.getValue())) {
            if ((this.debug.getValue())) {
                Command.sendMessage("Detected unloaded chunk!");
            }
            this.isUnloaded = true;
            return;
        }
        this.isUnloaded = false;
        entity.motionY = -(((Float) this.glideSpeed.getValue()) / 100.0f);
        if (this.mode.getValue() == Mode.Motion) {
            entity.motionX = dArray[0];
            entity.motionZ = dArray[1];
        }
        if (mc.player.movementInput.jump) {
            if (!(this.yLimit.getValue()) || entity.posY <= (double) ((Float) this.height.getValue())) {
                if (this.mode.getValue() == Mode.Motion) {
                    entity.motionY += (double) ((Float) this.ySpeed.getValue());
                } else {
                    d3 += (double) ((Float) this.ySpeed.getValue());
                }
            }
        } else if (mc.player.movementInput.sneak) {
            if (this.mode.getValue() == Mode.Motion) {
                entity.motionY += (double) (-((Float) this.ySpeed.getValue()));
            } else {
                d3 += (double) (-((Float) this.ySpeed.getValue()));
            }
        }
        if (mc.player.movementInput.moveStrafe == 0.0f && mc.player.movementInput.moveForward == 0.0f) {
            entity.motionX = 0.0;
            entity.motionZ = 0.0;
        }
        if ((this.onGroundPacket.getValue())) {
            this.setGround(entity);
        }
        if (this.mode.getValue() != Mode.Motion) {
            entity.setPosition(d, d3, d2);
        }
        if (this.mode.getValue() == Mode.Packet) {
            this.sendPacket(new CPacketVehicleMove(entity));
        }
        if (strict.getValue() == Strict.Invalid) {
            mc.player.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.CLONE, ItemStack.EMPTY, (short) 0));
        }
        if ((this.spoofPackets.getValue())) {
            Vec3d vec3d = entity.getPositionVector().add(0.0, (double) this.spoofPacket(), 0.0);
            EntityBoat entityBoat = new EntityBoat((World) mc.world, vec3d.x, vec3d.y, vec3d.z);
            entityBoat.rotationYaw = entity.rotationYaw;
            entityBoat.rotationPitch = entity.rotationPitch;
            this.sendPacket(new CPacketVehicleMove((Entity) entityBoat));
        }
        if ((this.remount.getValue())) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND));
        }
        eventPlayerTravel.cancel();
        ++this.ticks;
    }

    @EventListener(priority = ListenerPriority.HIGHEST)
    private void Method2863(PacketEvent.Receive eventNetworkPrePacketEvent) {
        if (eventNetworkPrePacketEvent.getPacket() instanceof SPacketDisconnect) {
            this.disable();
        }
        if (!mc.player.isRiding() || this.isUnloaded || this.isWait) {
            return;
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof SPacketMoveVehicle && mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof SPacketPlayerPosLook && mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof SPacketEntity && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof SPacketEntityAttach && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send eventNetworkPostPacketEvent) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if ((eventNetworkPostPacketEvent.getPacket() instanceof CPacketPlayer.Rotation && (this.cancelRotations.getValue()) || eventNetworkPostPacketEvent.getPacket() instanceof CPacketInput) && mc.player.isRiding()) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (this.isUnloaded && eventNetworkPostPacketEvent.getPacket() instanceof CPacketVehicleMove) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (!mc.player.isRiding() || this.isUnloaded || this.isWait) {
            return;
        }
        Entity entity = mc.player.getRidingEntity();
        if ((!mc.world.isChunkGeneratedAt(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4) || entity.getPosition().getY() < 0) && (this.stopUnloaded.getValue())) {
            return;
        }
        if (eventNetworkPostPacketEvent.getPacket() instanceof CPacketVehicleMove && (this.limit.getValue()) && this.mode.getValue() == Mode.Packet) {
            CPacketVehicleMove cPacketVehicleMove = (CPacketVehicleMove) eventNetworkPostPacketEvent.getPacket();
            if (this.packet.contains(cPacketVehicleMove)) {
                this.packet.remove(cPacketVehicleMove);
            } else {
                eventNetworkPostPacketEvent.cancel();
            }
        }
    }

    private boolean isNotMotionMode() {
        return this.mode.getValue() != Mode.Motion;
    }

    private boolean isMotionMode() {
        return this.mode.getValue() == Mode.Motion;
    }

    private boolean isPacketMode() {
        return this.mode.getValue() == Mode.Packet;
    }
}
