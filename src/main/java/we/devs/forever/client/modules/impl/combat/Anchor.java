
package we.devs.forever.client.modules.impl.combat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.impl.player.holeManager.AirHoleFinder;
import we.devs.forever.api.manager.impl.player.holeManager.Hole;
import we.devs.forever.api.manager.impl.player.holeManager.HoleManager;
import we.devs.forever.api.manager.impl.player.holeManager.SimpleHoleManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.MovementUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly;
import we.devs.forever.client.modules.impl.movement.LongJump;
import we.devs.forever.client.modules.impl.movement.fastfall.FastFall;
import we.devs.forever.client.modules.impl.movement.speed.Speed;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.mixin.ducks.IChunk;

import java.util.Comparator;

public class Anchor
        extends Module {
    public static Anchor INSTANCE;
    private static final FastFall REVERSE_STEP = FastFall.INSTANCE;
    private static final PacketFly PACKET_FLY = PacketFly.getInstance();
    private static final LongJump LONGJUMP = LongJump.INSTANCE;
    private static final Speed SPEED = Speed.INSTANCE;
    //    private static final SettingCache<SpeedMode, Setting<SpeedMode>, Speed> SPEED_MODE = Caches.getSetting<>(Speed.class, Setting.class, "Mode", SpeedMode.Instant);
    private final Setting<InputMode> inputMode = (new Setting<>("Input-Mode", InputMode.Keys));
    private final Setting<Float> pitch = (new Setting<Float>("Pitch", 90.0f, -90.0f, 90.0f));
    private final Setting<Integer> delay = (new Setting<Integer>("Delay", 400, 0, 5000));
    private final Setting<Integer> yBlocks = (new Setting<Integer>("Y-Down", 1, 1, 6));
    private final Setting<Integer> pullRange = (new Setting<Integer>("PullRange", 1, 0, 3));
    private final Setting<Mode> yMode = (new Setting<Mode>("Y-Mode", Mode.Off));
    private final Setting<Double> y = (new Setting<Double>("Y-Speed", 0.0, -10.0, 10.0));
    private final Setting<Mode> xzMode = (new Setting<Mode>("XZ-Mode", Mode.Constant));
    private final Setting<Double> xz = (new Setting<Double>("XZ-Speed", 0.2, 0.0, 10.0));
    private final Setting<Double> yOffset = (new Setting<Double>("Y-Offset", 1.0, 0.0, 1.0));
    private final Setting<Integer> lagTime = (new Setting<Integer>("Lag-Time", 1000, 0, 10000));
    private final Setting<Boolean> sneaking = (new Setting<>("Sneaking", false));
    private final Setting<Boolean> withSpeed = (new Setting<>("UseWithSpeed", false));
    private final Setting<Boolean> withSpeedInstant = (new Setting<>("UseWithSpeedInstant", true));
    private final Setting<Boolean> withStep = (new Setting<>("UseWithStep", false));
    private final Setting<Boolean> withRStep = (new Setting<>("UseWithReverseStep", true));
    private final Setting<Boolean> movingTowardsCheck = (new Setting<>("MovingTowardsCheck", false));
    private final Setting<Boolean> movingTowardsWithoutKeys = (new Setting<>("MovingTowardsWithoutKeys", false));
    private final Setting<Boolean> holeCheck = (new Setting<>("HoleCheck", true));
    private final Setting<Boolean> oldCheck = (new Setting<>("OldHoleCheck", false));
    private final Setting<Boolean> filterByY = (new Setting<>("FilterByY", true));
    private final HoleManager holeManager = new SimpleHoleManager();
    private final AirHoleFinder holeFinder = new AirHoleFinder(this.holeManager);
    private final TimerUtil timer = new TimerUtil();
    public static boolean pulling = false;

    public Anchor() {
        super("Anchor", "", Category.COMBAT);
        INSTANCE = this;
//        SimpleData data = new SimpleData(this, "Makes you stop over holes.");
//        data.register(this.withSpeed, "Whether this module should be active while Speed is on.");
//        data.register(this.withStep, "Whether this module should be active while Step is on.");
//        data.register(this.pitch, "This module is only active while you are looking down further than this value.");
//        data.register(this.delay, "When to start using this module again after you've been in a hole.");
//        data.register(this.yMode, "-Factor will multiply your vertical speed with the Y-Speed setting.\n-Constant will set your vertical speed to the Y-Speed setting.\n-Add will add the Y-Speed setting to your vertical speed.\n-Off won't change your speed in the vertical direction.");
//        data.register(this.y, "Speed in the vertical direction, configurable by Y-Mode.");
//        data.register(this.xzMode, "-Factor will multiply your horizontal speed with the XZ-Speed setting.\n-Constant will set your horizontal speed to the XZ-Speed setting.\n-Add will add the XZ-Speed setting to your horizontal speed.\n-Off won't change your speed in the horizontal direction.");
//        data.register(this.xz, "Speed in the horizontal direction, configurable by XZ-Mode.");
//        data.register(this.yOffset, "Offset to the bottom of the hole when calculating distance.");
//        data.register(this.withSpeedInstant, "Exception to UseWithSpeed for Speed Mode - Instant.");
//        data.register(this.withRStep, "Whether to use this module together with ReverseStep.");
//        data.register(this.inputMode, "-Always: module is always active.\n-NoKeys: module is only active while you are not pressing any movement keys.\n-Keys: module is only active while you are pressing movement keys.");
//        data.register(this.movingTowardsCheck, "Checks if you are moving towards the hole.");
//        data.register(this.yblocks, "How deep the holes can be");
//        data.register(this.pullRange, "How many blocks away you can be from hole");
//        this.setData(data);
    }

    @EventListener
    public void onMove(MoveEvent event) {
        if (Anchor.mc.player.isSpectator() || !ncpManager.passed(this.lagTime.getValue()) || !this.sneaking.getValue().booleanValue() && Anchor.mc.player.isSneaking()) {
            pulling = false;
            return;
        }
        this.holeManager.reset();
        BlockPos pos = Anchor.mc.player.getPosition();
        this.holeFinder.setChunk((IChunk) Anchor.mc.world.getChunk(pos));
        this.holeFinder.setMaxX(pos.getX() + this.pullRange.getValue());
        this.holeFinder.setMinX(pos.getX() - this.pullRange.getValue());
        this.holeFinder.setMaxY(pos.getY());
        this.holeFinder.setMinY(pos.getY() - this.yBlocks.getValue());
        this.holeFinder.setMaxZ(pos.getZ() + this.pullRange.getValue());
        this.holeFinder.setMinZ(pos.getZ() - this.pullRange.getValue());
        this.holeFinder.calcHoles();
        Hole hole = this.holeManager.getHoles().values().stream().filter(h -> !this.filterByY.getValue() || (double) h.getY() < Anchor.mc.player.posY).filter(h -> this.movingTowardsCheck.getValue() == false || this.movingTowardsWithoutKeys.getValue() == false && MovementUtil.noMovementKeys() || this.isMovingTowards((Hole) h, (MoveEvent) event)).min(Comparator.comparingDouble(this::getDistance)).orElse(null);
        if (hole == null) {
            pulling = false;
            return;
        }
        if (this.oldCheck.getValue() && Math.ceil(Anchor.mc.player.posY) == (double) hole.getY() || this.holeCheck.getValue() && this.isInHole()) {
            pulling = false;
            this.timer.reset();
            return;
        }
        if (/*this.withSpeed.getValue() == false && SPEED.isEnabled() && (this.withSpeedInstant.getValue() == false || SPEED_MODE.getValue() != SpeedMode.Instant) || this.withStep.getValue() == false && STEP.isEnabled() || this.withRStep.getValue() == false && REVERSE_STEP.isEnabled() || PACKET_FLY.isEnabled() || BLOCK_LAG.isEnabled() || LONGJUMP.isEnabled() ||*/
                this.inputMode.getValue() == InputMode.Keys && MovementUtil.noMovementKeys() || this.inputMode.getValue() == InputMode.NoKeys && !MovementUtil.noMovementKeys() || !this.timer.passedMs(this.delay.getValue().intValue()) || Anchor.mc.player.rotationPitch < this.pitch.getValue().floatValue() || this.holeCheck.getValue().booleanValue() && this.isInHole()) {
            pulling = false;
            return;
        }
        double x = (double) hole.getX() + (double) (hole.getMaxX() - hole.getX()) / 2.0 - Anchor.mc.player.posX;
        double z = (double) hole.getZ() + (double) (hole.getMaxZ() - hole.getZ()) / 2.0 - Anchor.mc.player.posZ;
        double distance = Math.sqrt(x * x + z * z);
        event.setY(this.modify(this.yMode.getValue(), event.getY(), this.y.getValue()));
        if (distance == 0.0) {
            event.setX(0.0);
            event.setY(0.0);
            pulling = false;
            return;
        }
        pulling = true;
        double pull_factor = this.xz.getValue() / distance;
        event.setX(this.modify(this.xzMode.getValue(), event.getX(), x * pull_factor));
        event.setZ(this.modify(this.xzMode.getValue(), event.getZ(), z * pull_factor));
    }

    private boolean isInHole() {
        return this.holeManager.getHoles().values().stream().anyMatch(h -> h.contains(mc.player.posX, mc.player.posY, mc.player.posZ));
    }

    private double modify(Mode mode2, double value, double setting) {
        switch (mode2) {
            case Factor: {
                return value * setting;
            }
            case Constant: {
                return setting;
            }
            case Add: {
                return value + setting;
            }
        }
        return value;
    }

    private double getDistance(Hole hole) {
        double holeX = (double) hole.getX() + (double) (hole.getMaxX() - hole.getX()) / 2.0;
        double holeY = (double) hole.getY() + this.yOffset.getValue();
        double holeZ = (double) hole.getZ() + (double) (hole.getMaxZ() - hole.getZ()) / 2.0;
        return mc.player.getDistanceSq(holeX, holeY, holeZ);
    }

    private boolean isMovingTowards(Hole hole, MoveEvent event) {
        double nextDistance;
        double holeZ;
        double holeY;
        double holeX = (double) hole.getX() + (double) (hole.getMaxX() - hole.getX()) / 2.0;
        double distance = mc.player.getDistanceSq(holeX, holeY = hole.getY(), holeZ = (double) hole.getZ() + (double) (hole.getMaxZ() - hole.getZ()) / 2.0);
        return distance >= mc.player.getPositionVector().add(new Vec3d(event.getX(), event.getY(), event.getZ()).normalize().scale(Math.sqrt(distance))).squareDistanceTo(holeX, holeY, holeZ);
    }

    public static enum InputMode {
        Always,
        NoKeys,
        Keys;

    }

    public static enum Mode {
        Factor,
        Constant,
        Add,
        Off;

    }
}