package we.devs.forever.client.modules.impl.combat.autocrystalold;

import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.entity.EntityWorldEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.CrystalUtils;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.combat.PredictPlayer;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.math.CalcUtil;
import we.devs.forever.api.util.math.MathUtil;
import we.devs.forever.api.util.math.path.BasePath;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.AutoPiston;
import we.devs.forever.client.modules.impl.combat.PistonPush;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.*;
import we.devs.forever.client.modules.impl.combat.autocrystalold.listeners.*;
import we.devs.forever.client.modules.impl.combat.autocrystalold.threads.CalcThread;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.AutoCrystalQueue;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.CRenderUtil;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.Combatutil;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.PosInfo;
import we.devs.forever.client.modules.impl.combat.burrow.Burrow;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoCrystal extends Module implements AutoCrystalQueue {
    public static AutoCrystal INSTANCE = new AutoCrystal();

    //Settings
    //Settings
    public Setting<Settings> setting = (new Setting<>("Settings", Settings.Place));
    //Place settings
    public Setting<Boolean> place = (new Setting<>("Place", true, "Place.", v -> setting.getValue() == Settings.Place));
    public Setting<Float> placeSpeed = (new Setting<>("PlaceSpeed", 19F, 0f, 20F, "Speed of place crystals.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<DirectionMode> directionMode = (new Setting<>("Interact", DirectionMode.Vanilla, "Place interact.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Boolean> strictDirection = (new Setting<>("StrictDirection", false, "StrictDirection.", v -> setting.getValue() == Settings.Place && place.getValue()));
    //public Setting<Boolean> check = (new Setting<>("CheckBreakRange", false, "Pos confirm if can break.", v -> setting.getValue() == Settings.Place && place.getValue()));
    //public Setting<Safety> safetyPlace = (new Setting<>("SafetyPlace", Safety.Normal, "Normal(Deprecated): place if SelfPosDamage <= SelfDamage \nSuperSafe: if SelfPosDamage <= SelfDamage and find pos to min damage from pos.\nOverride:if SelfPosDamage <= SelfDamage and find pos to min damage from pos or if target can pop place crystal,\nbut it can pop/kill you.", v -> setting.getValue() == Settings.Place));
    public Setting<Float> placeMinDamage = (new Setting<>("PlaceMinDamage", 4F, 0.1F, 36.0F, "Min damage for place.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Float> maxSelfPlace = (new Setting<>("MaxSelfPlaceDamage", 8.0F, 1.0F, 36.0F, "MaxSelfDamage.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Float> placeRange = (new Setting<>("PlaceRange", 5.0F, 0.0F, 6.0F, "Range for place.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Float> placeWallRange = (new Setting<>("PlaceWallRange", 3.0F, 0.0F, 6.0F, "Range for place throws wall.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Boolean> place13 = (new Setting<>("Place 1.13", false, "1.13 placements.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Boolean> sequential = (new Setting<>("Sequential", false, "Place in dead crystals.", v -> setting.getValue() == Settings.Place && place.getValue()));
    public Setting<Integer> seqTime = (new Setting<>("SeqTime", 100,0,300, "Place in dead crystals.", v -> setting.getValue() == Settings.Place && place.getValue() && sequential.getValue()));
    public Setting<Boolean> liquids = (new Setting<>("Liquids", false, "Place in liquids.", v -> setting.getValue() == Settings.Place && place.getValue()));//Break settings


    public Setting<Boolean> breakk = (new Setting<>("Break", true, "Break.", v -> setting.getValue() == Settings.Break));
    public Setting<BreakMode> breakMode = (new Setting<>("BreakMode", BreakMode.Always, "Always: break crystal always.\nCrystal: break crystal only when you hold crystal in hand.\nCalc: break crystal if you hold crystal or 2 times more than minBreakDamage\nor can be pop/kill target break crystal.", v -> setting.getValue() == Settings.Break));
    //   public Setting<Safety> safetyBreak = (new Setting<>("SafetyBreak", Safety.Normal, "Normal(Deprecated): break if SelfCrystalDamage <= SelfDamage.\nSuperSafe: if SelfCrystalDamage <= SelfDamage and find crystal to min damage from crystal.\nOverride:if SelfCrystalDamage <= SelfDamage and find crystal to min damage from crystal,\nbut it can pop/kill you.", v -> setting.getValue() == Settings.Break));
    public Setting<Boolean> antiWeakness = (new Setting<>("AntiWeakness", true, "Switch to sword for break when you have weakness.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> breakSpeed = (new Setting<>("BreakSpeed", 20F, 0F, 20F, "Delay for break.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> attackDelay = new Setting<>("AttackDelay", 1F, 0F, 5F, "Speed to explode crystals using old delays", v -> setting.getValue() == Settings.Break && breakk.getValue());

    public Setting<Integer> crystalTicks = (new Setting<>("Crystal Ticks", 0, 0, 20, "Wait crystals ticks for break.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> breakMinDamage = (new Setting<>("BreakMinDamage", 4F, 0.1F, 36.0F, "Min damage for break.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> maxSelfbreak = (new Setting<>("MaxSelfBreakDamage", 8.0F, 1.0F, 36.0F, "MaxSelfDamage.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> breakRange = (new Setting<>("Range", 5.0F, 0.0F, 6.0F, "Range for break crystals.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Float> breakWallRange = (new Setting<>("WallRange", 4.5F, 0.0F, 6.0F, "Range for break throws wall.", v -> setting.getValue() == Settings.Break && breakk.getValue()));
    public Setting<Inhibit> inhibit = new Setting<>("inhibit", Inhibit.Normal, "Prevents excessive attacks on crystals", v -> setting.getValue() == Settings.Break && breakk.getValue());
    public Setting<Float> inhibitFactor = new Setting<>("InhibitFactor", 1F, 0F, 5F, "Time to wait after inhibiting", v -> setting.getValue() == Settings.Break && breakk.getValue() && inhibit.getValue().equals(Inhibit.Strict));
    public Setting<Double> breakSwitchDelay = new Setting<>("BreakSwitchDelay", 0., 0., 10.0, "Time to wait after switching", v -> setting.getValue() == Settings.Break && breakk.getValue() && inhibit.getValue().equals(Inhibit.Strict));
    public Setting<Boolean> await = new Setting<>("Await", true, "Runs delays on packet time", v -> setting.getValue() == Settings.Break && breakk.getValue());
    public Setting<Float> yieldProtection = new Setting<>("YieldProtection", 0F, 0F, 5F, "Inhibit factor", v -> setting.getValue() == Settings.Break && breakk.getValue() && await.getValue());
    public Setting<Boolean> cancelcrystal = new Setting<>("CancelCrystal", false, "Delete crystal from world, speed up AutoCrystal (but can desync you).", v -> setting.getValue() == Settings.Break && breakk.getValue());
    public Setting<Boolean> setDead = (new Setting<>("SetDead", true, "Kill(on client side) crystal, speed up AutoCrystal.", v -> setting.getValue() == Settings.Break && breakk.getValue()));//Rotate Setting
    public Setting<Boolean> packet = (new Setting<>("Packet", true, "", v -> setting.getValue() == Settings.Break && breakk.getValue()));//Rotate Setting
    public Setting<Rotate> rotateMode = (new Setting<>("Rotate", Rotate.Off, "Rotate mode for AutoCrystal.", v -> setting.getValue() == Settings.Rotate));
    public Setting<YawStep> yawStepMode = (new Setting<>("YawStep", YawStep.None, "Limits yaw rotations", v -> setting.getValue() == Settings.Rotate && rotateMode.getValue() != Rotate.Off));
    public Setting<Float> yawStepThreshold = (new Setting<>("YawStepThreshold", 1F, 0F, 180F, "Max angle to rotate in one tick", v -> setting.getValue() == Settings.Rotate && rotateMode.getValue() != Rotate.Off && yawStepMode.getValue() != YawStep.None));
    //  public Setting<Boolean> randomizeSp = (new Setting<>("Randomize", true, "Randomise rotates.", v -> setting.getValue() == Settings.Rotate && rotateMode.getValue() != Rotate.Off));
    //Predict setting
    public Setting<Boolean> targetPredict = (new Setting<>("TargetPredict", true, "Extrapolate target in ticks.", v -> setting.getValue() == Settings.Predict));
    public Setting<Integer> targetPredictTicks = (new Setting<>("ExtrapolationTicks", 2, 1, 30, "ExtrapolationTicks.", v -> setting.getValue() == Settings.Predict && targetPredict.getValue()));

    public Setting<Boolean> selfPredict = (new Setting<>("SelfPredict", true, "Extrapolate self in ticks.", v -> setting.getValue() == Settings.Predict));
    public Setting<Integer> selfPredictTicks = (new Setting<>("ExtrapolationTicksSelf", 2, 1, 30, v -> setting.getValue() == Settings.Predict && selfPredict.getValue()));

    //FacePlace Setting
    public Setting<Boolean> holdFacePlace = (new Setting<>("HoldFacePlace", false, "FacePlace when pressed primary mouse button.", v -> setting.getValue() == Settings.FacePlace));
    public Setting<Float> facePlaceSpeed = (new Setting<>("FacePlaceDelay", 15F, 0F, 20F, "Delay when you facePlace target.", v -> setting.getValue() == Settings.FacePlace));
    public Setting<Float> facePlaceDamage = (new Setting<>("FacePlaceDamage", 8F, 0.1F, 36.0F, "FacePlace when target health <= your value.", v -> setting.getValue() == Settings.FacePlace));
    public Setting<Integer> armorScale = (new Setting<>("ArmorFucker", 25, 0, 100, "FacePlace when target armor <= your value.", v -> setting.getValue() == Settings.FacePlace));
    //Switch Setting
    public Setting<Swing> placeSwing = (new Setting<>("PlaceSwing", Swing.Mainhand, "Swing for place (for server all modes except None identical).", v -> setting.getValue() == Settings.Switch));
    public Setting<Swing> breakSwing = (new Setting<>("BreakSwing", Swing.Mainhand, "Swing for break (for server all modes except None identical).", v -> setting.getValue() == Settings.Switch));
    public Setting<AutoSwitch> crysSwap = (new Setting<>("CrystalSwitch", AutoSwitch.None, "Modes for switch to crystal.", v -> setting.getValue() == Settings.Switch));
    public Setting<Boolean> switchBack = new Setting<>("SwitchBack", false, v -> setting.getValue() == Settings.Switch && crysSwap.getValue() == AutoSwitch.Normal);
    public Setting<AutoSwitch> antiWeaknessSwap = (new Setting<>("AntiWeaknessSwitch", AutoSwitch.Silent, "Modes for switch to sword.", v -> setting.getValue() == Settings.Switch));
    public Setting<AutoSwitch> obySwap = (new Setting<>("ObsidianSwitch", AutoSwitch.Silent, "Modes for switch to obsidian.", v -> setting.getValue() == Settings.Switch));
    public Setting<Integer> switchCooldown = (new Setting<>("SwitchCooldown", 0, 0, 500, "Not implement.", v -> setting.getValue() == Settings.Switch && (antiWeaknessSwap.getValue() != AutoSwitch.None || crysSwap.getValue() != AutoSwitch.None)));

    //Render settings
    public Setting<InfoMode> infomode = (new Setting<>("Display", InfoMode.Target, v -> setting.getValue() == Settings.Render));
    public Setting<Boolean> render = (new Setting<>("Render", true, v -> setting.getValue() == Settings.Render));
    public Setting<Mode> renderMode = (new Setting<>("Mode", Mode.Static, v -> setting.getValue() == Settings.Render && render.getValue()));
    public final Setting<Boolean> fadeFactor = (new Setting<>("Fade", true, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Fade && render.getValue()));
    public final Setting<Boolean> scaleFactor = (new Setting<>("Shrink", false, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Fade && render.getValue()));
    public final Setting<Boolean> slabFactor = (new Setting<>("Slab", false, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Fade && render.getValue()));
    public final Setting<Float> duration = (new Setting<>("Duration", 1500.0f, 0.0f, 5000.0f, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Fade && render.getValue()));
    public final Setting<Integer> max = (new Setting<>("MaxPositions", 15, 1, 30, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Fade && render.getValue()));
    public final Setting<Float> slabHeight = (new Setting<>("SlabHeight", 1.0f, 0.1f, 1.0f, v -> setting.getValue() == Settings.Render && (renderMode.getValue() == Mode.Static || renderMode.getValue() == Mode.Glide) && render.getValue()));
    public final Setting<Float> moveSpeed = (new Setting<>("Speed", 900.0f, 0.0f, 1500.0f, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Glide && render.getValue()));
    public final Setting<Float> accel = (new Setting<>("Deceleration", 0.8f, 0.0f, 1.0f, v -> setting.getValue() == Settings.Render && renderMode.getValue() == Mode.Glide && render.getValue()));
    // public Setting<Boolean> colorS ync = (new Setting<>("CSync", true, v -> setting.getValue() == Settings.Render && render.getValue()));
    private final Setting<RenderMode> mode = (new Setting<>("BoxMode", RenderMode.Fill, v -> setting.getValue() == Settings.Render && render.getValue()));
    private final Setting<Color> fillColor = (new Setting<>("Color", new Color(10, 93, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> setting.getValue() == Settings.Render && render.getValue() && (mode.getValue() == RenderMode.Fill || mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Wireframe)));
    private final Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(0, 214, 252, 255), ColorPickerButton.Mode.Normal, 100, v -> setting.getValue() == Settings.Render && render.getValue() && (mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline)));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> setting.getValue() == Settings.Render && render.getValue() && (mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline || mode.getValue() == RenderMode.Wireframe)));

    public Setting<Boolean> text = (new Setting<>("Text", false, v -> setting.getValue() == Settings.Render && render.getValue()));
    //Misc settings

    public Setting<Boolean> multiTask = (new Setting<>("MultiTask", true, "", v -> setting.getValue() == Settings.Misc));
    public Setting<Float> range = (new Setting<>("TargetRange", 9.5F, 0.0F, 16.0F, v -> setting.getValue() == Settings.Misc));
    public Setting<Float> lethalMult = (new Setting<>("LethalMultiper", 0.0F, 0.0F, 6.0F, "Multiper for calc damage (recommend set to 1.0 -1.5).", v -> setting.getValue() == Settings.Misc));
    public Setting<Integer> stopHealth = (new Setting<>("StopHealth", 0, 0, 20, "Stop AutoCrystal when self health <= your value.", v -> setting.getValue() == Settings.Misc));
    public Setting<Boolean> test = (new Setting<>("Test", true, "Stop AutoCrystal when Burrow is burrowing.", v -> setting.getValue() == Settings.Misc));

    //Stops
    public Setting<Boolean> stopBurrow = (new Setting<>("BurrowStop", true, "Stop AutoCrystal when Burrow is burrowing.", v -> setting.getValue() == Settings.Stops));
    public Setting<Boolean> stopSurround = (new Setting<>("SurroundStop", true, "Stop AutoCrystal when Surround is Surrounding.", v -> setting.getValue() == Settings.Stops));
    public Setting<Boolean> stopAutoTrap = (new Setting<>("AutoTrapStop", true, "Stop AutoCrystal when AutoTrap is traping.", v -> setting.getValue() == Settings.Stops));
    public Setting<Boolean> stopPistonPush = (new Setting<>("PistonPushStop", true, "Stop AutoCrystal when PistonPush is pushing.", v -> setting.getValue() == Settings.Stops));
    public Setting<Boolean> stopAutoPiston = (new Setting<>("AutoPistonStop", true, "Stop AutoCrystal when AutoPiston is attacking.", v -> setting.getValue() == Settings.Stops));

    //oby
    public Setting<Boolean> autoOby = new Setting<>("AutoObsidian", false, "Place automatically obsidian for place crystal", v -> setting.getValue() == Settings.AutoObsidian);
    public Setting<Integer> helpingBlocks = new Setting<>("HelpingBlocks", 3, 0, 5, "Count of blocks help place main obsidian.", v -> autoOby.getValue() && setting.getValue() == Settings.AutoObsidian);
    public Setting<Boolean> rotateOby = new Setting<>("RotateToObsidian", false, v -> autoOby.getValue() && setting.getValue() == Settings.AutoObsidian);
    public Setting<Boolean> placeAfterCrystal = new Setting<>("InstantCrystal", false, "Place Instantlly crystal on placed obsidian", v -> autoOby.getValue() && setting.getValue() == Settings.AutoObsidian);
    public Setting<Integer> delayOby = new Setting<>("Delay", 0, 0, 300, v -> autoOby.getValue() && setting.getValue() == Settings.AutoObsidian);

    //public Setting<Float> minDamageOby = new Setting<>("MinDamage",5F,0F,6F, v -> autoOby.getValue());

    //     public Setting<Boolean> autoDisable = (new Setting<>("AutoDisable", true, v -> setting.getValue() == Settings.Misc));
    //Dev
    //  public Setting<EnumFacing> modeFacing = (new Setting<>("Test", EnumFacing.UP, v -> setting.getValue() == Settings.Dev));
    public float pitch = 0.0f, yaw = 0.0f;
    public Deque<PosInfo<Entity>> breakPosInfoQueue = new ArrayDeque<>();
    public Deque<PosInfo<BlockPos>> placePosInfoQueue = new ArrayDeque<>();
    public final Set<Entity> inhibitEntities = new HashSet<>();
    public final SwitchUtil switchUtil = new SwitchUtil(obySwap);
    public final SwitchUtil antiWeaknessSwitch = new SwitchUtil(antiWeaknessSwap);
    public final SwitchUtil crysSwitchUtil = new SwitchUtil(crysSwap);
    public final ConcurrentHashMap<BlockPos, Long> placeLocations = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, Long> breakLocations = new ConcurrentHashMap<>();
    public final TimerUtil
            breakTimer = new TimerUtil(),
            renderTimer = new TimerUtil(),
            placeTimer = new TimerUtil(),
            switchTimer = new TimerUtil(),
            rotationTimer = new TimerUtil(),
            obyTimer = new TimerUtil(),
            clearTimer = new TimerUtil(),
            seqTimer = new TimerUtil(),
            inhibitTimer = new TimerUtil();
    public static EntityPlayer currentTarget;
    public EntityPlayer self;
    public final TreeMap<Long, Integer> spawnedCrystals = new TreeMap<>();
    public final Set<Integer> deadCrystals = new HashSet<>();
    public List<PredictPlayer> targets = new LinkedList<>();
    public static boolean sprintState = false;
    public static EntityEnderCrystal renderCrystal = null;
    public Entity lastHitEntity;
    public volatile PosInfo<Entity> breakPosInfo;
    public volatile PosInfo<BlockPos> placePosInfo;
    public volatile boolean explodeClearance;
    public volatile boolean placeClearance;
    public float placeTime = 0;
    int rotateticks;
    public double renderDamage = 0.0D;
    public boolean offhand, mainhand;
    public BlockPos renderPos = null, oldPos = null;
    public Random random = new Random();
    public Vec3d rotationVector;
    public RayTraceResult postResult;
    public EnumFacing facing = EnumFacing.UP;
    public ListenerOnUpdateWalkingPlayer listenerOnUpdateWalkingPlayer = new ListenerOnUpdateWalkingPlayer(this);
    double breakMaxDamage = 0.5D;
    Entity crystal = null;
    public float selfHealth = 0;
    public CalcThread calcThread;
    boolean placeOby = false;
    public int counterPlace = 0;
    public int counterBreak = 0;
    List<BasePath> paths = new ArrayList<>();
    public CalcUtil calcUtil = new CalcUtil();
    public volatile long lastAttackTime;
    public volatile long[] attackTimes = new long[10];
 public   SeqHelper seqHelper = new SeqHelper(this);

    public AutoCrystal() {
        super("AutoCrystal", "Based AutoCrystal.", Category.COMBAT);
        getModuleListeners().add(new ListenerPacket(this));
        getModuleListeners().add(new ListenerPacketReceive(this));
        getModuleListeners().add(listenerOnUpdateWalkingPlayer);
        addModuleListeners(new ListenerOnUpdateWalkingPLayerPost(this));

        INSTANCE = this;
    }

    public static AutoCrystal getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoad() throws Throwable {
        placeLocations.clear();
        breakLocations.clear();
        breakTimer.reset();
        placeTimer.reset();
        renderTimer.reset();
        switchTimer.reset();
        currentTarget = null;
        deadCrystals.clear();
        renderPos = null;
        oldPos = null;
        offhand = false;
        calcThread = new CalcThread(this);
        calcThread.getThread().setDaemon(true);
        calcThread.getThread().setPriority(5);
        calcThread.getThread().start();
    }

    @Override
    public void onUnload() throws Throwable {
        calcThread.getThread().interrupt();
    }

    @Override
    public void onEnable() {
        calcThread.start();
    }

    @Override
    public void onDisable() {
        calcThread.getThread().interrupt();
    }

    @Override
    public void onToggle() {
        placeLocations.clear();
        breakLocations.clear();
        breakTimer.reset();
        placeTimer.reset();
        renderTimer.reset();
        deadCrystals.clear();
        switchTimer.reset();
        explodeClearance = false;
        placeClearance = false;
        rotateticks = 0;
        currentTarget = null;
        if (autoOby.getValue()) {
            paths.clear();
            obyTimer.reset();
            placeOby = false;
        }

        renderPos = null;
        oldPos = null;
        offhand = false;

    }

    @EventListener
    public void onTick(MotionEvent.Pre e) {
        if ((Burrow.burrow.isActive() && stopBurrow.getValue())) return;
        if ((AutoPiston.autoPiston.isEnabled() && stopAutoPiston.getValue())) return;
        if ((PistonPush.pistonPush.isEnabled() && stopPistonPush.getValue())) return;
        placeLocations.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1000) {
                placeLocations.remove(pos);
            }
        });
        breakLocations.forEach((id, time) -> {
            if (System.currentTimeMillis() - time > 5000) {
                breakLocations.remove(id);
            }
        });
        if (clearTimer.passedMs(1000)) {
            // Command.sendMessage(String.format("Place: %s Break: %s", counterPlace, counterBreak));
            counterPlace = 0;
            counterBreak = 0;
            clearTimer.reset();

        }
        if (renderTimer.passedMs(1000)) {
            renderTimer.reset();
            renderPos = null;
        }
//        if((Burrow.burrow.isActive() && stopBurrow.getValue())) return;
//        if((AutoPiston.autoPiston.isEnabled() && stopAutoPiston.getValue())) return;
//        if((PistonPush.pistonPush.isEnabled() && stopPistonPush.getValue())) return;
       doCa();

        if (rotateMode.getValue() != Rotate.Off && !rotationTimer.passedMs(100)) {
            if (yawStepMode.getValue() != YawStep.None) {
                float packetYaw = ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                float diff = MathUtil.wrapDegrees(yaw - packetYaw);
                if (Math.abs(diff) > yawStepThreshold.getValue()) {
                    yaw = packetYaw + (diff * ((yawStepThreshold.getValue()) / Math.abs(diff)));
                }
            }
            //   if (randomize.getValue()) {
            yaw = yaw + (random.nextInt(4) - 2) / 100f;
            //  }
            rotationManager.doRotation(RotationType.Legit, yaw, pitch, 15);

        }
    }

    public void doCa() {
        if (true) {
            if (breakPosInfo != null &&  (attackDelay.getValue() <= attackDelay.getMin() || inhibitFactor.getValue() > inhibitFactor.getMin() && inhibit.getValue().equals(Inhibit.Strict))) {
                PosInfo<Entity> breakPosInfo = this.breakPosInfo;
                long explodeDelay = (long) (500L - breakSpeed.getValue() * 25L);

                // prevent attacks faster than our ping would allow
                if (await.getValue()) {
                    explodeDelay = (long) (getAverageWaitTime() + (50 * yieldProtection.getValue()));
                }
                boolean delayed = breakTimer.passedMs(explodeDelay) && switchTimer.passedMs(breakSwitchDelay.getValue() * 25);
                if (explodeClearance || delayed) {
//                    System.out.println(3);
                    listenerOnUpdateWalkingPlayer.handleBreakRotation(breakPosInfo.getValue().posX, breakPosInfo.getValue().posY, breakPosInfo.getValue().posZ);
                    onBreak(breakPosInfo.getValue());
                    explodeClearance = false;
                }
            }
            if (placePosInfo != null) {
                if (!place.getValue()) return;
                if (cantPlace()) return;
                if (!(mainhand || offhand)) return;
                if (placeTimer.passedMs(500 - placeSpeed.getValue() * 25L) || placeClearance) {
                    facing = listenerOnUpdateWalkingPlayer.handlePlaceRotation(placePosInfo.getValue());
                    onPlace(placePosInfo.getValue());
                    placeClearance = false;
                    if (placePosInfo.isByQueue()) {
//                        System.out.println(4);
                        placePosInfoQueue.clear();
                    }
                }
            }

        } else {
            rotateticks--;
        }
    }



    public PosInfo<Entity> calcBreak(boolean rots) {
        if (cantBreak()) return null; //50
        List<Entity> crystals = new LinkedList<>(mc.world.loadedEntityList)
                .stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(this::isValidCrystalTarget)
                .collect(Collectors.toCollection(LinkedList::new));
        float maxDamage = 0;
        float selfMaxDamage = Float.MAX_VALUE;
        PredictPlayer predictPlayer1 = null;
        Entity pos1 = null;
        boolean facePlace = false;
        for (PredictPlayer predictPlayer : targets) {
            for (Entity entity : crystals) {
                float damage = CrystalUtils.calculateDamage(entity, predictPlayer.getTarget());
                boolean isPopable = EntityUtil.getHealth(predictPlayer.getOldPlayer()) <= damage;
                boolean ispassed =
                        (DamageUtil.isArmorLow(predictPlayer.getOldPlayer(), armorScale.getValue())
                                || Combatutil.shouldHoldFacePlace(holdFacePlace.getValue()
                                || EntityUtil.getHealth(predictPlayer.getOldPlayer()) <= facePlaceDamage.getValue())
                        ) && damage > 2F;

                if (damage >= breakMinDamage.getValue() || ispassed || isPopable) {
                    float selfdamage = CrystalUtils.calculateDamage(entity, self);
                    if (selfdamage <= maxSelfbreak.getValue()
                            && (stopHealth.getValue() == 0 || selfdamage + stopHealth.getValue() <= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                            && (damage >= maxDamage)) {
                        facePlace = ispassed;
                        maxDamage = damage;
                        selfMaxDamage = selfdamage;
                        predictPlayer1 = predictPlayer;
                        pos1 = entity;
                    }
                }
            }
        }

        renderCrystal = (EntityEnderCrystal) crystal;
        if (predictPlayer1 == null || pos1 == null) return null;

        PosInfo<Entity> temp = new PosInfo<>(pos1, predictPlayer1, maxDamage, selfMaxDamage);
        temp.setFacePlace(facePlace);
        
        return temp;
    }

    public boolean onBreak(Entity crystal) {
        if (cantBreak()) return false; //50
        if (crystal == null) return false;

        if (breakMode.getValue().equals(BreakMode.Calc)) {
            if (!(2 * breakMinDamage.getValue() >= breakMaxDamage || EntityUtil.getHealth(breakPosInfo.getOldPlayer()) <= breakMaxDamage || (mainhand || offhand))) {
                return false;
            }
        }

        lastHitEntity = crystal;
        if (antiWeakness.getValue() && Combatutil.shouldAntiWeakness()) {
            antiWeaknessSwitch.switchTo(Items.DIAMOND_SWORD);
            interactionManager.attackEntity(crystal, packet.getValue(), breakSwing.getValue());
            antiWeaknessSwitch.switchBack();
        } else {
            interactionManager.attackEntity(crystal, packet.getValue(), breakSwing.getValue());
        }
        seqHelper.setExpecting(null);

        placeLocations.forEach((blockPos, aLong) -> {
            if (crystal.getDistanceSq(blockPos) <= 36) {
                placeLocations.remove(blockPos);
            }
        });
//        addAttackTime();
        if (sequential.getValue()) deadCrystals.add(crystal.getEntityId());
//                    for (Entity entity : mc.world.loadedEntityList) {
//                        if (entity instanceof EntityEnderCrystal && entity.getDistanceSq(lastHitEntity.posX, lastHitEntity.posY, lastHitEntity.posZ) <= 36) {
//                            breakLocations.put(entity.getEntityId(), System.currentTimeMillis());
//                        }
//                    }

        counterBreak++;
        breakTimer.reset();
        return true;

    }

    public PosInfo<BlockPos> calcPlace() {
        List<BlockPos> poss = BlockUtil.getSphere(placeRange.getValue(), true)
                .parallelStream()
                .filter(pos -> canPlaceCrystal(pos, true))
                .collect(Collectors.toCollection(LinkedList::new));
        float maxDamage = 0;
        float selfMaxDamage = Float.MAX_VALUE;
        PredictPlayer predictPlayer1 = null;
        BlockPos pos1 = null;
        boolean facePlace = false;
        for (PredictPlayer predictPlayer : targets) {
            if (predictPlayer.getTarget() == null) continue;
            for (BlockPos pos : poss) {
                float damage = CrystalUtils.calculateDamage(pos, predictPlayer.getTarget());
                boolean isPopable = EntityUtil.getHealth(predictPlayer.getOldPlayer()) <= damage;
                boolean ispassed =
                        (DamageUtil.isArmorLow(predictPlayer.getOldPlayer(), armorScale.getValue())
                                || Combatutil.shouldHoldFacePlace(holdFacePlace.getValue()
                                || EntityUtil.getHealth(predictPlayer.getOldPlayer()) <= facePlaceDamage.getValue())
                        ) && damage > 2F;

                if (damage >= placeMinDamage.getValue() || ispassed || isPopable) {
                    float selfdamage = CrystalUtils.calculateDamage(pos, self);
                    if (selfdamage <= maxSelfPlace.getValue()
                            && (stopHealth.getValue() == 0 || selfdamage + stopHealth.getValue() <= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                            && (damage >= maxDamage)) {
                        facePlace = ispassed;
                        maxDamage = damage;
                        selfMaxDamage = selfdamage;
                        predictPlayer1 = predictPlayer;
                        pos1 = pos;
                    }
                }
            }
        }

        if (predictPlayer1 == null || pos1 == null) return null;
        // if (breakPosInfo != null) if (breakPosInfo.getDamage() >= maxDamage) return null;

        PosInfo<BlockPos> temp = new PosInfo<>(pos1, predictPlayer1, maxDamage, selfMaxDamage);
        temp.setFacePlace(facePlace);
        return temp;
    }


    public boolean onPlace(BlockPos placePos) {
        if (placePos != null && facing != null && postResult != null) {
            renderPos = placePos;
            if(seqHelper.isBlockingPlacement())return false;
            if (test.getValue() && placeLocations.containsKey(placePos) && seqHelper.isBlockingPlacement()) {
                return false;
            }
            if (mc.world.getBlockState(placePos.up()).getBlock() == Blocks.FIRE) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, placePos.up(), EnumFacing.DOWN));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, placePos.up(), EnumFacing.DOWN));
                return false;
            }

//                if (timer.passedMs(2000)) {
            if ((!offhand || mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)) {
                crysSwitchUtil.switchTo(Items.END_CRYSTAL);
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, facing, mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float) postResult.hitVec.x, (float) postResult.hitVec.y, (float) postResult.hitVec.z));
                crysSwitchUtil.switchBack();
            } else
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, facing, mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float) postResult.hitVec.x, (float) postResult.hitVec.y, (float) postResult.hitVec.z));
            seqHelper.setExpecting(placePos);
            EntityUtil.swing(placeSwing.getValue());
            counterPlace++;
            placeLocations.put(placePos, System.currentTimeMillis());
            placeClearance = false;
            placeTimer.reset();
//                }
            if(placePosInfo != null) AutoCrystal.currentTarget = this.placePosInfo.getOldPlayer();
            if (AutoCrystal.currentTarget != null) targetManager.addTarget(AutoCrystal.currentTarget);
            return true;
        }
        if (renderTimer.passedMs(1000)) {
            renderTimer.reset();
            renderPos = null;
        }

        return false;
    }

    private boolean isValidCrystalTarget(Entity crystal) {
        if (mc.player.getPositionEyes(1F).squareDistanceTo(crystal.getPositionVector()) >=
                (CrystalUtils.isNotVisible(crystal, crystal.getEyeHeight())
                        ? breakWallRange.getValue() * breakWallRange.getValue()
                        : breakRange.getValue() * breakRange.getValue()))
            return false;
         if (crystal.ticksExisted < crystalTicks.getValue())
            return false;

        return !inhibitEntities.contains(crystal) || inhibit.getValue().equals(Inhibit.None);
        //+  if (inhibitEntities.contains(inhibitEntities)) return false;

        //if (CrystalUtils.calculateDamage(crystal, mc.player) + stopHealth.getValue() >= mc.player.getHealth() + mc.player.getAbsorptionAmount()) return false;
    }

    public boolean cantBreak() {
        if (breakMode.getValue().equals(BreakMode.Always) || breakMode.getValue().equals(BreakMode.Calc)) return false;

        if ((PlayerUtil.isEating() && !offhand) && !multiTask.getValue()) {
            return false;
        }
        return !(mainhand || offhand);
    }

    public boolean cantPlace() {
        return (PlayerUtil.isEating() || offhand) && !multiTask.getValue();
    }


    public boolean canPlaceCrystal(BlockPos blockPos, boolean checkbedrock) {
        if (checkbedrock)
            if (!(mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                    || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) return false;

        BlockPos boost = blockPos.add(0, 1, 0);

        if (!(mc.world.getBlockState(boost).getBlock() == Blocks.AIR)) {
            if (!((mc.world.getBlockState(boost).getBlock() == Blocks.FIRE) || (mc.world.getBlockState(boost).getBlock() instanceof BlockLiquid && liquids.getValue()))) {
                return false;
            }
        }
        BlockPos boost2 = blockPos.add(0, 2, 0);

        if (!place13.getValue()) {
            if (!(mc.world.getBlockState(boost2).getBlock() == Blocks.AIR)) {
                if (!(mc.world.getBlockState(boost).getBlock() instanceof BlockLiquid && liquids.getValue())) {
                    return false;
                }
            }
        }

//        if (check.getValue() && !CrystalUtils.rayTraceBreak(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5)) {
//            if (mc.player.getPositionEyes(1F).squareDistanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5)) > breakWallRange.getValue() * breakWallRange.getValue()) {
//                return false;
//            }
//        }

        if (placeWallRange.getValue() < placeRange.getValue()) {
            if (!CrystalUtils.rayTracePlace(blockPos)) {
                if (strictDirection.getValue()) {
                    Vec3d eyesPos = mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0);
                    boolean inRange = false;
                    if (directionMode.getValue() == DirectionMode.Vanilla) {
                        for (EnumFacing facing : EnumFacing.values()) {
                            Vec3d cVector = new Vec3d(blockPos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                                    blockPos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                                    blockPos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5);
                            if (eyesPos.squareDistanceTo(cVector) <= placeWallRange.getValue() * placeWallRange.getValue()) {
                                inRange = true;
                                break;
                            }
                        }
                    } else {
                        double increment = 0.45D;
                        double start = 0.05D;
                        double end = 0.95D;

                        loop:
                        for (double xS = start; xS <= end; xS += increment) {
                            for (double yS = start; yS <= end; yS += increment) {
                                for (double zS = start; zS <= end; zS += increment) {
                                    Vec3d posVec = (new Vec3d(blockPos)).add(xS, yS, zS);

                                    double distToPosVec = eyesPos.squareDistanceTo(posVec);

                                    if (distToPosVec <= placeWallRange.getValue() * placeWallRange.getValue()) {
                                        inRange = true;
                                        break loop;
                                    }
                                }
                            }
                        }
                    }
                    if (!inRange) return false;
                } else {
                    if ((double) blockPos.getY() > mc.player.posY + (double) mc.player.getEyeHeight()) {
                        if (mc.player.getDistanceSq(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5) > placeWallRange.getValue() * placeWallRange.getValue()) {
                            return false;
                        }
                    } else if (mc.player.getDistanceSq(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5) > placeWallRange.getValue() * placeWallRange.getValue()) {
                        return false;
                    }
                }
            }
        } else if (strictDirection.getValue()) {
            Vec3d eyesPos = mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0);
            boolean inRange = false;
            if (directionMode.getValue() == DirectionMode.Vanilla) {
                for (EnumFacing facing : EnumFacing.values()) {
                    Vec3d cVector = new Vec3d(blockPos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                            blockPos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                            blockPos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5);
                    if (eyesPos.squareDistanceTo(cVector) <= placeRange.getValue() * placeRange.getValue()) {
                        inRange = true;
                        break;
                    }
                }
            } else {
                double increment = 0.45D;
                double start = 0.05D;
                double end = 0.95D;

                loop:
                for (double xS = start; xS <= end; xS += increment) {
                    for (double yS = start; yS <= end; yS += increment) {
                        for (double zS = start; zS <= end; zS += increment) {
                            Vec3d posVec = (new Vec3d(blockPos)).add(xS, yS, zS);

                            double distToPosVec = eyesPos.squareDistanceTo(posVec);

                            if (distToPosVec <= placeRange.getValue() * placeRange.getValue()) {
                                inRange = true;
                                break loop;
                            }
                        }
                    }
                }
            }
            if (!inRange) return false;
        }

        return mc.world.getEntitiesWithinAABB(Entity.class,
                        new AxisAlignedBB(boost.getX() - .0001, boost.getY(), boost.getZ() - .0001,
                                boost2.getX() + 1.0001, boost2.getY() + 1, boost2.getZ() + 1.0001))
                .stream()
                .filter(Objects::nonNull)
                .filter(entity -> !entity.isDead)
                .filter(entity -> !(entity instanceof EntityXPOrb))
                .filter(entity -> !deadCrystals.contains(entity.getEntityId()))
                .filter(entity -> !(entity instanceof EntityEnderCrystal && entity.ticksExisted < 20)).count() == 0;
    }

    @EventListener
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        // crystal being removed from world
        if (event.getEntity() instanceof EntityEnderCrystal) {
            // remove crystal from our attacked crystals list
            breakLocations.remove(event.getEntity().getEntityId());


            // check if it is a crystal we have attacked
//            if (attackedCrystals.containsKey(event.getEntity().getEntityId())) {
//
//                // remove crystal from our attacked crystals list
//                lastConfirmTime =  System.currentTimeMillis() - attackedCrystals.remove(event.getEntity().getEntityId());
//
//                // recently broke a crystal
//                lastCrystalCount++;
//            }

//            // check if it is a crystal we have sent a packet for
//            if (breakPackets.contains(event.getEntity().getEntityId())) {
//
//                // clear
//                breakPackets.clear();
//            }

            inhibitEntities.remove(event.getEntity());
            deadCrystals.remove(event.getEntity().getEntityId());
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (render.getValue())
            CRenderUtil.renderCa(renderPos,
                    new float[]{lineWidth.getValue(), range.getValue(), slabHeight.getValue(), moveSpeed.getValue() * accel.getValue(), (float) renderDamage, duration.getValue(), max.getValue()},
                    fillColor.getColor(),
                    outLineColor.getColor(),
                    new boolean[]{mainhand || offhand, text.getValue(), slabFactor.getValue(), fadeFactor.getValue(), scaleFactor.getValue()},
                    renderMode.getValue(),
                    mode.getValue());
//        if (autoOby.getValue()) {
//            PlacePosInfo pos1 = calcObyPos();
//            PathUtil.findPath(pos1.getPlacePos(), 3F, 6F).forEach(pos -> {
//                RenderUtil.drawText(pos.getPos(), pos1.getDamage());
//                BlockRenderUtil.drawBlock(pos.getPos(), ColorUtil.changeAlpha(Color.GREEN, 50), Color.BLACK, 1F, RenderMode.FillOutline);
//            });
//        }
    }


    @Override
    public String getDisplayInfo() {
        if (placePosInfo == null) return null;
        if (placePosInfo.getTarget() == null) return null;
        renderDamage = placePosInfo.getDamage();
        switch (infomode.getValue()) {
            case Target:
                return placePosInfo.getOldPlayer().getName();
            case Damage:
                return ((Math.floor(renderDamage) == renderDamage) ? Integer.valueOf((int) renderDamage) : String.format("%.1f", renderDamage)) + "";
            case Both:
                return placePosInfo.getOldPlayer().getName() + ", " + ((Math.floor(renderDamage) == renderDamage) ? Integer.valueOf((int) renderDamage) : String.format("%.1f", renderDamage));
            case Debug:
                long time = getAverageWaitTime();
                return String.format("%s, %s ms ", counterPlace, time);
            default:
                return null;
        }

    }

    public void addAttackTime() {
        if (lastAttackTime <= 0) {
            lastAttackTime = System.currentTimeMillis();
        }
        if (attackTimes.length - 1 >= 0) {
            System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
        }
        attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;
        lastAttackTime = System.currentTimeMillis();
    }

    private long getAverageWaitTime() {

        // average value
        float average = getAverage(attackTimes);

        if (average > 500) {
            average = 0;
        }

        // 10 slots
        return (long) average;
    }

    private float getAverage(long[] in) {

        // average value
        float avg = 0;

        for (long time : in) {
            avg += time;
        }

        // average time
        return avg / 10F;
    }

    @Override
    public void addPlacePos(BlockPos pos) {
        placePosInfoQueue.addFirst(new PosInfo<>(pos, null, 0F, 0F, true));
    }

    @Override
    public void addBreakEntity(Entity entity) {
        breakPosInfoQueue.addFirst(new PosInfo<>(entity, null, 0F, 0F, true));
    }

//               if (placeOby && info != null && autoOby.getValue()) {
//        if (paths.size() != 1) {
//            paths.stream().filter(path -> path != paths.get(paths.size() - 1)).forEach(path -> placeLocations.put(path.getPos(), System.currentTimeMillis()));
//            for (BasePath path : paths) {
//                if (path.isPlaced()) continue;
//                if (obyTimer.passedMs(delayOby.getValue())) {
//                    switchUtil.switchTo(Blocks.OBSIDIAN);
////                            BlockUtil.placeBlockNotRetarded(path.getPos(), EnumHand.MAIN_HAND, rotateOby.getValue(), false);
//                    interactionManager.placeBlock(path.getPos(), rotateOby.getValue() ? RotationType.Legit : RotationType.Off, true, strictDirection.getValue(), true, false);
//                    switchUtil.switchBack();
//                    path.setPlaced(true);
//                    obyTimer.reset();
//                } else break;
//            }
//            placeOby = !paths.stream().allMatch(BasePath::isPlaced);
//            if (!placeOby && placeAfterCrystal.getValue()) {
//                doCrystal();
//            }
//        } else {
//            if (obyTimer.passedMs(delayOby.getValue())) {
//                switchUtil.switchTo(Blocks.OBSIDIAN);
//                interactionManager.placeBlock(paths.get(0).getPos(), rotateOby.getValue() ? RotationType.Legit : RotationType.Off, true, strictDirection.getValue(), true, false);
//                switchUtil.switchBack();
//                paths.get(0).setPlaced(true);
//                placeOby = false;
//                obyTimer.reset();
//            }
//        }
//    }

//            if (autoOby.getValue() && !placeOby && placePos == null) {
//        info = calcObyPos();
//        if (info == null) return;
//        paths = PathUtil.findPath(info.getPlacePos(), placeWallRange.getValue(), placeRange.getValue(), helpingBlocks.getValue());
//        placeOby = true;
//    }

}