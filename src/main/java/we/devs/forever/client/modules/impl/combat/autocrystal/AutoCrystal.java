package we.devs.forever.client.modules.impl.combat.autocrystal;

import com.mojang.realmsclient.util.Pair;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.CrystalUtils;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.combat.RaytraceUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.math.CalcUtil;
import we.devs.forever.api.util.math.MathUtil;
import we.devs.forever.api.util.math.path.BasePath;
import we.devs.forever.api.util.math.path.PathUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.Page;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.Mode;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.Settings;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.CRenderUtil;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.mixin.mixins.accessor.ICPacketUseEntity;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    public AutoCrystal() {
        super("AutoCrystal", "Places and explodes crystals", Category.COMBAT);
        INSTANCE = this;
        addModuleListeners(new ListenerEntityWorldEvent(this));
        addModuleListeners(new ListenerMotionPre(this));
        addModuleListeners(new ListenerPacketSend(this));
        addModuleListeners(new ListenerPacketRecieve(this));
        calcThread = new CalcThread(this,1);
    }


    public Setting<Page> pageSetting = new Setting<>("Page", Page.AntiCheat);

    // **************************** anticheat settings ****************************

    public Setting<Boolean> multitask = new Setting<>("Multitask", true, "Explodes only if we are not preforming any actions with our hands", v -> pageSetting.getValue().equals(Page.AntiCheat));
    public Setting<Boolean> whileMining = new Setting<>("WhileMining", true, "Explodes only if we are not mining", v -> pageSetting.getValue().equals(Page.AntiCheat));

    public Setting<Swing> swing = new Setting<>("Swing", Swing.Mainhand, "Swings the players hand when attacking and placing", v -> pageSetting.getValue().equals(Page.AntiCheat));

    public Setting<Interact> interact = new Setting<>("Interact", Interact.Vanilla, "Interaction with blocks and crystals", v -> pageSetting.getValue().equals(Page.AntiCheat));

    public Setting<Boolean> rotate = new Setting<>("Rotation", true, "Rotate to the current process", v -> pageSetting.getValue().equals(Page.AntiCheat));
    public Setting<YawStep> yawStep = new Setting<>("YawStep", YawStep.None, "Limits yaw rotations", v -> rotate.getValue() && pageSetting.getValue().equals(Page.AntiCheat));
    public Setting<Float> yawStepThreshold = new Setting<>("YawStepThreshold", 180.0F, 1.0F, 180.0F, "Max angle to rotate in one tick", v -> rotate.getValue() && !yawStep.getValue().equals(YawStep.None) && pageSetting.getValue().equals(Page.AntiCheat));
    public Setting<Boolean> raytrace = new Setting<>("Raytrace", false, "Restricts placements through walls", v -> pageSetting.getValue().equals(Page.AntiCheat));

    public Setting<Float> offset = new Setting<>("Offset", 2.0F, 1.0F, 2.0F, "Crystal placement offset", v -> pageSetting.getValue().equals(Page.AntiCheat));
    public Setting<Integer> threadDelay = new Setting<>("CalcUpdate",1,0,50, "Delay of thread calc updating in ms.\n Can make fps higher and reduce CPU load", v -> pageSetting.getValue().equals(Page.AntiCheat));

    // **************************** explode settings ****************************

    public Setting<Boolean> explode = new Setting<>("Break", true, "Breaks crystals", v -> pageSetting.getValue().equals(Page.Break));

    public Setting<Float> explodeSpeed = new Setting<>("BreakSpeed", 20.0F, 1.0F, 20.0F, "Speed to break crystals", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> attackDelay = new Setting<>("AttackDelay", 0.0F, 0.0F, 5.0F, "Speed to break crystals using old delays", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> explodeRange = new Setting<>("BreakRange", 5.0F, 1.0F, 6.0F, "Range to break crystals", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> explodeWallRange = new Setting<>("BreakWallRange", 3.5F, 1.0F, 6.0F, "Range to break crystals through walls", v -> explode.getValue() && !raytrace.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Boolean> rangeEye = new Setting<>("RangeEye", false, "Calculates ranges to the entity's eye", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> ticksExisted = new Setting<>("TicksExisted", 0.0F, 0.0F, 5.0F, "Minimum age of the crystal", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> explodeSwitchDelay = new Setting<>("SwitchDelay", 0.0F, 0.0F, 10.0F, "Delay to pause after switching items", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));
    public Setting<Inhibit> inhibit = new Setting<>("Inhibit", Inhibit.Semi, "Prevents excessive attacks on crystals", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> inhibitFactor = new Setting<>("inhibitFactor", 1.0F, 0.0F, 5.0F, "Time to wait after inhibiting", v -> explode.getValue() && inhibit.getValue().equals(Inhibit.Full) && pageSetting.getValue().equals(Page.Break));

    public Setting<Boolean> await = new Setting<>("Await", true, "Runs delays on packet time", v -> explode.getValue() && pageSetting.getValue().equals(Page.Break));

    public Setting<Float> yieldProtection = new Setting<>("YieldProtection", 2.0F, 0.0F, 5.0F, "Inhibit factor", v -> explode.getValue() && await.getValue() && !inhibit.getValue().equals(Inhibit.None) && pageSetting.getValue().equals(Page.Break));

    // **************************** place settings ****************************

    public Setting<Boolean> place = new Setting<>("Place", true, "Places crystals", v -> pageSetting.getValue().equals(Page.Place));

    public Setting<Placements> placements = new Setting<>("Placements", Placements.Native, "Placement calculations for current version", v -> place.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<Sequential> sequential = new Setting<>("Sequential", Sequential.Normal, "Timing for placements", v -> place.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<Float> placeSpeed = new Setting<>("PlaceSpeed", 20.0F, 1.0F, 20.0F, "Speed to place crystals", v -> place.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<Float> placeRange = new Setting<>("PlaceRange", 5.0F, 1.0F, 6.0F, "Range to place crystals", v -> place.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<Float> placeWallRange = new Setting<>("PlaceWallRange", 3.5F, 1.0F, 6.0F, "Range to place crystals through walls", v -> place.getValue() && !raytrace.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<AutoSwitch> autoSwitch = new Setting<>("Switch", AutoSwitch.None, "Switching to crystals before placement", v -> place.getValue() && pageSetting.getValue().equals(Page.Place));

    public Setting<AutoSwitch> antiWeakness = new Setting<>("AntiWeakness", AutoSwitch.None, "Switches to a tool when attacking crystals to bypass the weakness effect", v -> explode.getValue() && pageSetting.getValue().equals(Page.Place));


    // **************************** damage settings ****************************

    public Setting<Float> damage = new Setting<>("Damage", 4.0F, 2.0F, 10.0F, "Minimum damage done by an action", v -> pageSetting.getValue().equals(Page.Damage));

    public Setting<Float> lethalMultiplier = new Setting<>("LethalMultiplier", 1.0F, 0.0F, 5.0F, "Will override damages if we can kill the target in this many crystals", v -> pageSetting.getValue().equals(Page.Damage));

    public Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true, "Attempts to break enemy armor with crystals", v -> pageSetting.getValue().equals(Page.Damage));

    public Setting<Float> armorScale = new Setting<>("ArmorScale", 5.0F, 0.0F, 40.0F, "Will override damages if we can break the target's armor", v -> armorBreaker.getValue() && pageSetting.getValue().equals(Page.Damage));

    public Setting<Safety> safety = new Setting<>("Safety", Safety.None, "Safety check for processes", v -> pageSetting.getValue().equals(Page.Damage));

    public Setting<Float> safetyBalance = new Setting<>("SafetyBalance", 1.1F, 0.1F, 3.0F, "Multiplier for actions considered unsafe", v -> safety.getValue().equals(Safety.Balance) && pageSetting.getValue().equals(Page.Damage));

    public Setting<Boolean> blockDestruction = new Setting<>("BlockDestruction", false, "Ignores terrain that can be exploded when calculating damages", v -> pageSetting.getValue().equals(Page.Damage));
// **************************** Predict settings ****************************

    public Setting<Boolean> targetPredict = (new Setting<>("TargetPredict", true, "Extrapolate target in ticks.", v -> pageSetting.getValue().equals(Page.Predict)));
    public Setting<Integer> targetPredictTicks = (new Setting<>("ExtrapolationTicks", 2, 1, 30, "ExtrapolationTicks.", v -> pageSetting.getValue().equals(Page.Predict) && targetPredict.getValue()));

    public Setting<Boolean> selfPredict = (new Setting<>("SelfPredict", true, "Extrapolate self in ticks.", v -> pageSetting.getValue().equals(Page.Predict)));
    public Setting<Integer> selfPredictTicks = (new Setting<>("ExtrapolationTicksSelf", 2, 1, 30, v -> pageSetting.getValue().equals(Page.Predict) && selfPredict.getValue()));

    // **************************** target settings ****************************
    public Setting<Boolean> targetPlayers = new Setting<>("TargetPlayers", true, "Target players", v -> pageSetting.getValue().equals(Page.Targets));

    public Setting<Boolean> targetPassives = new Setting<>("TargetPassives", false, "Target passives", v -> pageSetting.getValue().equals(Page.Targets));

    public Setting<Boolean> targetNeutrals = new Setting<>("TargetNeutrals", false, "Target neutrals", v -> pageSetting.getValue().equals(Page.Targets));

    public Setting<Boolean> targetHostiles = new Setting<>("TargetHostiles", false, "Target hostiles", v -> pageSetting.getValue().equals(Page.Targets));
    public Setting<Boolean> targetArrows = new Setting<>("TargetArrows", false, "Target Arrows", v -> pageSetting.getValue().equals(Page.Targets));
    public Setting<Boolean> targetPearls = new Setting<>("TargetPearls", false, "Target Pearls", v -> pageSetting.getValue().equals(Page.Targets));

    public Setting<Float> targetRange = new Setting<>("TargetRange", 10.0F, 0.1F, 15.0F, "Range to consider an entity as a target", v -> pageSetting.getValue().equals(Page.Targets));
    // **************************** AutoObsidian settings ****************************
    public Setting<Boolean> autoOby = new Setting<>("AutoObsidian", false, "Place automatically obsidian for place crystal", v -> pageSetting.getValue() == Page.AutoObsidian);
    public Setting<Integer> helpingBlocks = new Setting<>("HelpingBlocks", 3, 0, 5, "Count of blocks help place main obsidian.", v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian);
    public Setting<Integer> delay = (new Setting<>("Delay", 4, 0, 30,  v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Integer> bpt = (new Setting<>("BPT", 4, 0, 30,  v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<RotationType> rotateoby = (new Setting<>("Rotate", RotationType.Off,  v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Swing> swingoby = (new Setting<>("Swing", Swing.Mainhand, v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Boolean> packet = (new Setting<>("PacketPlace", false, v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Boolean> strict = (new Setting<>("Strict", false, v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Boolean> noLag = (new Setting<>("NoLag", true, "Boost speed place.",  v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian));
    public Setting<Float> obyRange = new Setting<>("ObyRange", 6F, 0F, 5F, "", v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian);
    public Setting<Float> obyWallRange = new Setting<>("ObyWallRange", 3F, 0F, 5F, "", v -> autoOby.getValue() && pageSetting.getValue() == Page.AutoObsidian);

    // **************************** render settings ****************************


    public Setting<Boolean> render = (new Setting<>("Render", true, v -> pageSetting.getValue().equals(Page.Render)));
    public Setting<Mode> renderMode = (new Setting<>("Mode", Mode.Fade, v -> render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Boolean> fadeFactor = (new Setting<>("Fade", true, v -> renderMode.getValue() == Mode.Fade && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Boolean> scaleFactor = (new Setting<>("Shrink", false, v -> renderMode.getValue() == Mode.Fade && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Boolean> slabFactor = (new Setting<>("Slab", false, v -> renderMode.getValue() == Mode.Fade && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Float> duration = (new Setting<>("Duration", 1500.0f, 0.0f, 5000.0f, v -> renderMode.getValue() == Mode.Fade && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Integer> max = (new Setting<>("MaxPositions", 15, 1, 30, v -> renderMode.getValue() == Mode.Fade && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Float> slabHeight = (new Setting<>("SlabHeight", 1.0f, 0.1f, 1.0f, v -> (renderMode.getValue() == Mode.Static || renderMode.getValue() == Mode.Glide) && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Float> moveSpeed = (new Setting<>("Speed", 900.0f, 0.0f, 1500.0f, v -> renderMode.getValue() == Mode.Glide && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    public final Setting<Float> accel = (new Setting<>("Deceleration", 0.8f, 0.0f, 1.0f, v -> renderMode.getValue() == Mode.Glide && render.getValue() && pageSetting.getValue().equals(Page.Render)));
    // public Setting<Boolean> colorS ync = (new Setting<>("CSync", true && render.getValue()));
    private final Setting<RenderMode> mode = (new Setting<>("BoxMode", RenderMode.Fill, v -> render.getValue() && pageSetting.getValue().equals(Page.Render)));
    private final Setting<Color> fillColor = (new Setting<>("Color", new Color(10, 93, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> render.getValue() && (mode.getValue() == RenderMode.Fill || mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Wireframe) && pageSetting.getValue().equals(Page.Render)));
    private final Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(0, 214, 252, 255), ColorPickerButton.Mode.Normal, 100, v -> render.getValue() && (mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline) && pageSetting.getValue().equals(Page.Render)));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> render.getValue() && (mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline || mode.getValue() == RenderMode.Wireframe) && pageSetting.getValue().equals(Page.Render)));

    public Setting<Boolean> text = (new Setting<>("Text", false, v -> render.getValue() && pageSetting.getValue().equals(Page.Render)));


    // **************************** rotations ****************************

    // vector that holds the angle we are looking at
    Pair<Vec3d, YawStep> angleVector;

    // rotation angels
    Pair<Float, Float> rotateAngles;

    // ticks to pause the process
    public volatile int  rotateTicks =0;

    // **************************** explode ****************************

    // explode timers
    final TimerUtil explodeTimer = new TimerUtil();
    final TimerUtil switchTimer = new TimerUtil();
    boolean explodeClearance;

    // explosion
    volatile DamageHolder<EntityEnderCrystal> explosion;


    // map of all attacked crystals
    final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();
    final TreeMap<Long, Integer> spawnedCrystals = new TreeMap<>();

    // crystals that need to be ignored
    final List<EntityEnderCrystal> inhibitCrystals = new ArrayList<>();
    final List<Integer> deadCrystals = new ArrayList<>();

    // queue
    final Set<EntityEnderCrystal> queuedCrystals = new HashSet<>();

    // **************************** place ****************************
    SwitchUtil switchUtil = new SwitchUtil(autoSwitch);
    SwitchUtil switchUtilAnti = new SwitchUtil(antiWeakness);
    // place timers
    final TimerUtil placeTimer = new TimerUtil();
    boolean placeClearance;

    // switch timers
    final TimerUtil autoSwitchTimer = new TimerUtil();

    // placement
    volatile DamageHolder<BlockPos> placement;
    volatile DamageHolder<List<BasePath>> odsidianPlace;
    // map of all placed crystals
    final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();

    // **************************** debug ****************************

    // desync timer
    final TimerUtil desyncTimer = new TimerUtil();

    // attack flag
    long lastAttackTime;
    long lastConfirmTime;
    final long[] attackTimes = new long[10];

    // cps
    long lastCrystalCount;
    final TimerUtil crystalTimer = new TimerUtil();
    final long[] crystalCounts = new long[10];

    // **************************** packets ****************************

    // packets
    final List<BlockPos> placementPackets = new ArrayList<>();
    final List<Integer> explosionPackets = new ArrayList<>();
    CalcThread calcThread;
    CalcUtil calcUtil = new CalcUtil();
    int bpt1 = 0;
    @Override
    public void onThread() {
        if (fullNullCheck()) return;

        if (!strict.getValue() && odsidianPlace != null) {
              doPlaceAutoObsidian();
        }

    }

    @Override
    public void onUpdate() {

        // 2b2t
        //       if (interact.getValue().equals(Interact.Strict) && inhibit.getValue().equals(Inhibit.Full)) {
//
//            // if we are desynced, attempt to resync
//            if (isDesynced()) {
//
//                // don't spam resync packets
//                if (desyncTimer.passedS(5)) {
//
//                    // resync??
//                    mc.playerController.windowClick(0, mc.player.inventory.currentItem + 36, 0, ClickType.PICKUP, mc.player);
//                    mc.playerController.windowClick(0, mc.player.inventory.currentItem + 36, 0, ClickType.PICKUP, mc.player);
//
//                    // restart timer
//                    desyncTimer.reset();
//                    sendMessage("Re-synced!");
//
//                }
//            }
//       }

        // we are cleared to process our calculations
        if (rotateTicks <= 0) {

            // place on thread for more consistency
            if (attackDelay.getValue() <= attackDelay.getMin() || inhibitFactor.getValue() > inhibitFactor.getMin() && inhibit.getValue().equals(Inhibit.Full)) {
                DamageHolder<EntityEnderCrystal> explosion = this.explosion;
                // we found crystals to explode
                if (explosion != null) {

                    // calculate if we have passed delays
                    // place delay based on place speeds
                    long explodeDelay = (long) ((explodeSpeed.getMax() - explodeSpeed.getValue()) * 50);

                    // prevent attacks faster than our ping would allow
                    if (await.getValue()) {
                        explodeDelay = (long) (getAverageWaitTime() + (50 * yieldProtection.getValue()));
                    }

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = explodeTimer.passedMs(explodeDelay) && switchTimer.passedMs(switchDelay);

                    // check if we have passed the explode time
                    if (explodeClearance || delayed) {

                        // check attack flag
                        // face the crystal
                        angleVector = Pair.of(explosion.getDamageSource().getPositionVector(), YawStep.Full);

                        // attack crystal
                        if (attackCrystal(explosion.getDamageSource())) {

                            // add it to our list of attacked crystals
                            attackedCrystals.put(explosion.getDamageSource().getEntityId(), System.currentTimeMillis());

                            // clamp
                            if (lastAttackTime <= 0) {
                                lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (attackTimes.length - 1 >= 0) {
                                System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
                            }

                            // add to attack times
                            attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;

                            // mark attack flag
                            lastAttackTime = System.currentTimeMillis();

                            // clear
                            explodeClearance = false;
                            explodeTimer.reset();
                        }
                    }
                }
            }


            // we found a placement
            DamageHolder<BlockPos> placement= this.placement;
            if (placement != null) {

                // calculate if we have passed delays
                // place delay based on place speeds
                long placeDelay = (long) ((placeSpeed.getMax() - placeSpeed.getValue()) * 50);

                // we have waited the proper time ???
                boolean delayed = placeSpeed.getValue() >= placeSpeed.getMax() || placeTimer.passedMs(placeDelay);

                // check if we have passed the place time
                if (placeClearance || delayed) {

                    // face the placement
                    angleVector = Pair.of(new Vec3d(placement.getDamageSource()).add(0.5, 0.5, 0.5), YawStep.None);

                    // place the crystal
                    if (placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());  // place on the client thread

                        // clear
                        placeClearance = false;
                        placeTimer.reset();
                    }
                }
            }
        } else {
            rotateTicks--;
        }
    }

    @Override
    public void onAltRender3D(float ticks) {

        // render our current placement
        if (render.getValue() && placement != null) {

            // only render if we are holding crystals

            if (render.getValue())
                CRenderUtil.renderCa(placement.getDamageSource(),
                        new float[]{lineWidth.getValue(), targetRange.getValue(), slabHeight.getValue(), moveSpeed.getValue() * accel.getValue(), (float) placement.getTargetDamage(), duration.getValue(), max.getValue()},
                        fillColor.getColor(),
                        outLineColor.getColor(),
                        new boolean[]{isHoldingCrystal(), text.getValue(), slabFactor.getValue(), fadeFactor.getValue(), scaleFactor.getValue()},
                        renderMode.getValue(),
                        mode.getValue());
        }
    }



    public void doPlaceAutoObsidian() {
        for (BasePath basePath : odsidianPlace.getDamageSource()) {
            if (!BlockUtil.isPosEmpty(basePath.getPos())) continue;
            if (!BlockUtil.canPlace(basePath.getPos())) continue;
            if (!placeTimer.passedMs(delay.getValue())) break;
            if (bpt1 >= bpt.getValue()) break;
            if (switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANVIL)) {
                placeManager.place(basePath.getPos(), rotateoby.getValue(), Swing.Mainhand, packet.getValue(), strict.getValue(), noLag.getValue(), true);
                bpt1++;
                switchUtil.switchBack();
                placeTimer.reset();
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        if (placement != null) {
            return placement.target.getName()
                    + ", "
                    + getAverageCrystalsPerSecond();
        }
        return null;
    }

    @Override
    public void onEnable() {
        calcThread.start();
        // cleared on enable
        explodeClearance = false;
        placeClearance = false;
    }

    @Override
    public void onDisable() {
        calcThread.getThread().interrupt();
        // clear lists and reset variables
        explosion = null;
        placement = null;
        angleVector = null;
        rotateAngles = null;
        rotateTicks = 0;
        bpt1= 0;
        // sequentialTicks = 0;
        // explodeTimer.reset();
        // placeTimer.reset();
        // attackedCrystals.clear();
        inhibitCrystals.clear();
        deadCrystals.clear();
        // placedCrystals.clear();
        spawnedCrystals.clear();
    }
    @Override
    public void onTick() {
        bpt1 = 0;
    }



    @Override
    public void onLoad() throws Throwable {
        if (isEnabled()) calcThread.start();
        calcThread.setDelay(threadDelay.getValue());
    }

    @Override
    public void onUnload() throws Throwable {
        calcThread.getThread().interrupt();
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (explosion != null || placement != null) && isHoldingCrystal();
    }


    /**
     * Finds the best explode-able crystal for this tick
     *
     * @return The best explode-able crystal
     */
    public DamageHolder<EntityEnderCrystal> getCrystal() {

        /*
         * Map of valid crystals
         * Sorted by natural ordering of keys
         * Using tree map allows time complexity of O(logN)
         */
        TreeMap<Double, DamageHolder<EntityEnderCrystal>> validCrystals = new TreeMap<>();
        EntityPlayer self = selfPredict.getValue() ? MathUtil.predictPlayer(mc.player, selfPredictTicks.getValue()).getTarget() : mc.player;
        // iterate all crystals in the world
        for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {
            // make sure the entity actually exists
            if (crystal == null || crystal.isDead) {
                continue;
            }

            // check if the entity is a crystal
            if (!(crystal instanceof EntityEnderCrystal)) {
                continue;
            }

            // time elapsed since we placed this crystal (if we did place it)
            long elapsedTime = System.currentTimeMillis() - placedCrystals.getOrDefault(new BlockPos(crystal.getPositionVector()).down(), System.currentTimeMillis());

            // make sure the crystal has existed in the world for a certain number of ticks before it's a viable target
            if ((crystal.ticksExisted < ticksExisted.getValue() && (elapsedTime / 50F) < ticksExisted.getValue()) && !inhibit.getValue().equals(Inhibit.None)) {
                continue;
            }

            // make sure the crystal isn't already being exploded, prevent unnecessary attacks
            if (inhibitCrystals.contains(crystal) && !inhibit.getValue().equals(Inhibit.None)) {
                continue;
            }

            // distance to crystal
            double crystalRange = self.getDistance(crystal.posX, rangeEye.getValue() ? crystal.posY + crystal.getEyeHeight() : crystal.posY, crystal.posZ);

            // check if the entity is in range
            if (crystalRange > explodeRange.getValue()) {
                continue;
            }

            // check if crystal is behind a wall
            boolean isNotVisible = RaytraceUtil.isNotVisible(crystal, crystal.getEyeHeight());

            // check if entity can be attacked through wall
            if (isNotVisible) {
                if (crystalRange > explodeWallRange.getValue() || raytrace.getValue()) {
                    continue;
                }
            }

            // local damage done by the crystal
            double localDamage = CrystalUtils.calculateDamage(crystal, self);

            // search all targets
            for (Entity entity1 : new ArrayList<>(mc.world.loadedEntityList)) {
                Entity entity = entity1;
                // make sure the entity actually exists
                if (entity == null
                        || entity.equals(self)
                        || PlayerUtil.isDead(entity)
                        || friendManager.isFriend(entity.getName())) {

                    continue;
                }

                // ignore crystals, they can't be targets
                if (entity instanceof EntityEnderCrystal) {
                    continue;
                }

                // don't attack our riding entity
                if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
                    continue;
                }

                // verify that the entity is a target
                if (entity instanceof EntityPlayer && !targetPlayers.getValue()
                        || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue()
                        || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue()
                        || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()
                        || entity instanceof EntityArrow && !targetArrows.getValue()
                        || entity instanceof EntityEnderPearl && !targetPearls.getValue()) {
                    continue;
                }
                if (entity instanceof EntityPlayer && targetPredict.getValue()) {
                    entity = MathUtil.predictPlayer((EntityPlayer) entity, targetPredictTicks.getValue()).getTarget();
                }
                // distance to target
                double entityRange = self.getDistance(entity);

                // check if the target is in range
                if (entityRange > targetRange.getValue()) {
                    continue;
                }

                // target damage done by the crystal
                double targetDamage = CrystalUtils.calculateDamage(crystal, entity);

                // check the safety of the crystal
                double safetyIndex = 1;

                // check if we can take damage
                if (DamageUtil.canTakeDamage()) {

                    // local health
                    double health = PlayerUtil.getHealth();

                    // incredibly unsafe
                    if (localDamage + 0.5 > health) {
                        safetyIndex = -9999;
                    }

                    // unsafe -> if local damage is greater than target damage
                    else if (safety.getValue().equals(Safety.Stable)) {

                        // target damage and local damage scaled
                        double efficiency = targetDamage - localDamage;

                        // too small, we'll be fine :>
                        if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                            efficiency = 0;
                        }

                        safetyIndex = efficiency;
                    }

                    // unsafe -> if local damage is greater than balanced target damage
                    else if (safety.getValue().equals(Safety.Balance)) {

                        // balanced target damage
                        double balance = targetDamage * safetyBalance.getValue();

                        // balanced damage, should be proportionate to local damage
                        safetyIndex = balance - localDamage;
                    }
                }

                // crystal is unsafe
                if (safetyIndex < 0) {
                    continue;
                }

                // add to map
                validCrystals.put(targetDamage, new DamageHolder<>((EntityEnderCrystal) crystal, entity, targetDamage, localDamage));
            }
        }

        // make sure we actually have some valid crystals
        if (!validCrystals.isEmpty()) {

            // best crystal in the map, in a TreeMap this is the last entry
            DamageHolder<EntityEnderCrystal> bestCrystal = validCrystals.lastEntry().getValue();


            // no crystal under 1.5 damage is worth exploding
            if (bestCrystal.getTargetDamage() > 1.5) {

                // lethality of the crystal
                boolean lethal = false;

                // target health
                double health = PlayerUtil.getHealth(bestCrystal.getTarget());

                // can kill the target very quickly
                if (health <= 2) {
                    lethal = true;
                }

                // attempt to break armor; considered lethal
                if (armorBreaker.getValue()) {
                    if (bestCrystal.getTarget() instanceof EntityPlayer) {

                        // check durability for each piece of armor
                        for (ItemStack armor : bestCrystal.getTarget().getArmorInventoryList()) {
                            if (armor != null && !armor.getItem().equals(Items.AIR)) {

                                // durability of the armor
                                float armorDurability = ((armor.getMaxDamage() - armor.getItemDamage()) / (float) armor.getMaxDamage()) * 100;

                                // find lowest durability
                                if (armorDurability < armorScale.getValue()) {
                                    lethal = true; // check if armor damage is significant
                                    break;
                                }
                            }
                        }
                    }
                }

                // lethality factor of the crystal
                double lethality = bestCrystal.getTargetDamage() * lethalMultiplier.getValue();

                // will kill the target
                if (health - lethality < 0.5) {
                    lethal = true;
                }

                // check if the damage meets our requirements
                if (lethal || bestCrystal.getTargetDamage() > damage.getValue()) {

                    // mark it as our current explosion
                    return bestCrystal;
                }
            }
        }

        // we were not able to find any explode-able crystals
        return null;
    }

    /**
     * Gets the best placement for this tick
     *
     * @return The best placement for this tick
     */
    public DamageHolder<BlockPos> getPlacement() {

        // find place-able positions
        if (place.getValue()) {

            /*
             * Map of valid placements
             * Sorted by natural ordering of keys
             * Using tree map allows time complexity of O(logN)
             */
            EntityPlayer self = selfPredict.getValue() ? MathUtil.predictPlayer(mc.player, selfPredictTicks.getValue()).getTarget() : mc.player;
            TreeMap<Double, DamageHolder<BlockPos>> validPlacements = new TreeMap<>();

            // check all positions in range
            for (BlockPos position : BlockUtil.getBlocksInArea(self, new AxisAlignedBB(
                    -placeRange.getValue(), -placeRange.getValue(), -placeRange.getValue(), placeRange.getValue(), placeRange.getValue(), placeRange.getValue() // area in range of blocks
            ))) {

                // check if a crystal can be placed at this position
                if (!canPlaceCrystal(position, true)) {
                    continue;
                }
                // distance to placement
                double placementRange = BlockUtil.getDistanceToCenter(self, position); //#TODO ПЕРЕПИСАТЬ ЭТУ СХЕМУ

                // check if the placement is within range
                if (placementRange > placeRange.getValue() || placementRange > explodeRange.getValue()) {
                    continue;
                }

                // if the visibility for the expected crystal position is visible, then NCP won't flag us for placing at normal ranges
                boolean isNotVisible = RaytraceUtil.isNotVisible(position, 2.70000004768372);

                // check if placement can be placed on through a wall
                if (isNotVisible) {
                    if (placementRange > placeWallRange.getValue() || placementRange > explodeWallRange.getValue() || raytrace.getValue()) {
                        continue;
                    }
                }

                // local damage done by the placement
                double localDamage = CrystalUtils.calculateDamage(position, self);

                // search all targets
                for (Entity entity1 : new ArrayList<>(mc.world.loadedEntityList)) {
                    Entity entity = entity1;
                    // make sure the entity actually exists
                    if (entity == null
                            || entity.equals(self)
                            || PlayerUtil.isDead(entity)
                            || friendManager.isFriend(entity.getName())) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
                    if (entity instanceof EntityEnderCrystal) {
                        continue;
                    }

                    // don't attack our riding entity
                    if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
                        continue;
                    }

                    // verify that the entity is a target
                    if (entity instanceof EntityPlayer && !targetPlayers.getValue()
                            || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue()
                            || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue()
                            || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()
                            || entity instanceof EntityArrow && !targetArrows.getValue()
                            || entity instanceof EntityEnderPearl && !targetPearls.getValue()
                    ) {
                        continue;
                    }

                    if (entity instanceof EntityPlayer && targetPredict.getValue()) {
                        entity = MathUtil.predictPlayer((EntityPlayer) entity, targetPredictTicks.getValue()).getTarget();
                    }
                    // distance to target
                    double entityRange = self.getDistance(entity);

                    // check if the target is in range
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the placement
                    double targetDamage = CrystalUtils.calculateDamage(position, entity);

                    // check the safety of the placement
                    double safetyIndex = 1;

                    // check if we can take damage
                    if (DamageUtil.canTakeDamage()) {

                        // local health
                        double health = PlayerUtil.getHealth();

                        // incredibly unsafe
                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }

                        // unsafe -> if local damage is greater than target damage
                        else if (safety.getValue().equals(Safety.Stable)) {

                            // target damage and local damage scaled
                            double efficiency = targetDamage - localDamage;

                            // too small, we'll be fine :>
                            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                                efficiency = 0;
                            }

                            safetyIndex = efficiency;
                        }

                        // unsafe -> if local damage is greater than balanced target damage
                        else if (safety.getValue().equals(Safety.Balance)) {

                            // balanced target damage
                            double balance = targetDamage * safetyBalance.getValue();

                            // balanced damage, should be proportionate to local damage
                            safetyIndex = balance - localDamage;
                        }
                    }

                    // placement is unsafe
                    if (safetyIndex < 0) {
                        continue;
                    }

                    // add to map
                    validPlacements.put(targetDamage, new DamageHolder<>(position, entity, targetDamage, localDamage));
                }
            }

            // make sure we actually have some valid placements
            if (!validPlacements.isEmpty()) {

                // best placement in the map, in a TreeMap this is the last entry
                DamageHolder<BlockPos> bestPlacement = validPlacements.lastEntry().getValue();

                // no placement under 1.5 damage is worth placing
                if (bestPlacement.getTargetDamage() > 1.5) {

                    // lethality of the placement
                    boolean lethal = false;

                    // target health
                    double health = PlayerUtil.getHealth(bestPlacement.getTarget());

                    // can kill the target very quickly
                    if (health <= 2) {
                        lethal = true;
                    }

                    // attempt to break armor; considered lethal
                    if (armorBreaker.getValue()) {
                        if (bestPlacement.getTarget() instanceof EntityPlayer) {

                            // check durability for each piece of armor
                            for (ItemStack armor : bestPlacement.getTarget().getArmorInventoryList()) {
                                if (armor != null && !armor.getItem().equals(Items.AIR)) {

                                    // durability of the armor
                                    float armorDurability = ((armor.getMaxDamage() - armor.getItemDamage()) / (float) armor.getMaxDamage()) * 100;

                                    // find lowest durability
                                    if (armorDurability < armorScale.getValue()) {
                                        lethal = true;  // check if armor damage is significant
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // lethality factor of the placement
                    double lethality = bestPlacement.getTargetDamage() * lethalMultiplier.getValue();

                    // will kill the target
                    if (health - lethality < 0.5) {
                        lethal = true;
                    }

                    // check if the damage meets our requirements
                    if (lethal || bestPlacement.getTargetDamage() > damage.getValue()) {

                        // mark it as our current placement
                        return bestPlacement;
                    }
                }
            }
        }

        // we were not able to find any placements
        return null;
    }


    public DamageHolder<List<BasePath>> getObyPlacement() {

        // find place-able positions
        if (autoOby.getValue()) {
            /*
             * Map of valid placements
             * Sorted by natural ordering of keys
             * Using tree map allows time complexity of O(logN)
             */
            EntityPlayer self = selfPredict.getValue() ? MathUtil.predictPlayer(mc.player, selfPredictTicks.getValue()).getTarget() : mc.player;
            TreeMap<Double, DamageHolder<BlockPos>> validPlacements = new TreeMap<>();

            // check all positions in range
            for (BlockPos position : BlockUtil.getBlocksInArea(self, new AxisAlignedBB(
                    -obyRange.getValue(), -obyRange.getValue(), -obyRange.getValue(), obyRange.getValue(), obyRange.getValue(), obyRange.getValue() // area in range of blocks
            ))) {

                // check if a crystal can be placed at this position
                if (!canPlaceCrystal(position, false)) {
                    continue;
                }
                // distance to placement
                double placementRange = BlockUtil.getDistanceToCenter(self, position); //#TODO ПЕРЕПИСАТЬ ЭТУ СХЕМУ

                // check if the placement is within range
                if (placementRange > obyRange.getValue()) {
                    continue;
                }

                // if the visibility for the expected crystal position is visible, then NCP won't flag us for placing at normal ranges
                boolean isNotVisible = RaytraceUtil.isNotVisible(position, 2.70000004768372);

                // check if placement can be placed on through a wall
                if (isNotVisible) {
                    if (placementRange > obyRange.getValue() || placementRange > obyWallRange.getValue() || raytrace.getValue()) {
                        continue;
                    }
                }
                calcUtil.clearAllStates();
                // local damage done by the placement
                calcUtil.addBlockState(position.down(), Blocks.OBSIDIAN);
                double localDamage = calcUtil.calculateDamage(position, self);


                // search all targets
                for (Entity entity1 : new ArrayList<>(mc.world.loadedEntityList)) {
                    Entity entity = entity1;
                    // make sure the entity actually exists
                    if (entity == null
                            || entity.equals(self)
                            || PlayerUtil.isDead(entity)
                            || friendManager.isFriend(entity.getName())) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
                    if (entity instanceof EntityEnderCrystal) {
                        continue;
                    }

                    // don't attack our riding entity
                    if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
                        continue;
                    }

                    // verify that the entity is a target
                    if (entity instanceof EntityPlayer && !targetPlayers.getValue()
                            || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue()
                            || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue()
                            || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()
                            || entity instanceof EntityArrow && !targetArrows.getValue()
                            || entity instanceof EntityEnderPearl && !targetPearls.getValue()
                    ) {
                        continue;
                    }

                    if (entity instanceof EntityPlayer && targetPredict.getValue()) {
                        entity = MathUtil.predictPlayer((EntityPlayer) entity, targetPredictTicks.getValue()).getTarget();
                    }
                    // distance to target
                    double entityRange = self.getDistance(entity);

                    // check if the target is in range
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the placement
                    double targetDamage = calcUtil.calculateDamage(position, entity);

                    // check the safety of the placement
                    double safetyIndex = 1;

                    // check if we can take damage
                    if (DamageUtil.canTakeDamage()) {

                        // local health
                        double health = PlayerUtil.getHealth();

                        // incredibly unsafe
                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }

                        // unsafe -> if local damage is greater than target damage
                        else if (safety.getValue().equals(Safety.Stable)) {

                            // target damage and local damage scaled
                            double efficiency = targetDamage - localDamage;

                            // too small, we'll be fine :>
                            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                                efficiency = 0;
                            }

                            safetyIndex = efficiency;
                        }

                        // unsafe -> if local damage is greater than balanced target damage
                        else if (safety.getValue().equals(Safety.Balance)) {

                            // balanced target damage
                            double balance = targetDamage * safetyBalance.getValue();

                            // balanced damage, should be proportionate to local damage
                            safetyIndex = balance - localDamage;
                        }
                    }

                    // placement is unsafe
                    if (safetyIndex < 0) {
                        continue;
                    }

                    // add to map
                    validPlacements.put(targetDamage, new DamageHolder<>(position, entity, targetDamage, localDamage));
                }
            }

            // make sure we actually have some valid placements
            if (!validPlacements.isEmpty()) {

                // best placement in the map, in a TreeMap this is the last entry
                DamageHolder<BlockPos> bestPlacement = validPlacements.lastEntry().getValue();

                // no placement under 1.5 damage is worth placing
                if (bestPlacement.getTargetDamage() > 1.5) {

                    // lethality of the placement
                    boolean lethal = false;

                    // target health
                    double health = PlayerUtil.getHealth(bestPlacement.getTarget());

                    // can kill the target very quickly
                    if (health <= 2) {
                        lethal = true;
                    }

                    // attempt to break armor; considered lethal
                    if (armorBreaker.getValue()) {
                        if (bestPlacement.getTarget() instanceof EntityPlayer) {

                            // check durability for each piece of armor
                            for (ItemStack armor : bestPlacement.getTarget().getArmorInventoryList()) {
                                if (armor != null && !armor.getItem().equals(Items.AIR)) {

                                    // durability of the armor
                                    float armorDurability = ((armor.getMaxDamage() - armor.getItemDamage()) / (float) armor.getMaxDamage()) * 100;

                                    // find lowest durability
                                    if (armorDurability < armorScale.getValue()) {
                                        lethal = true;  // check if armor damage is significant
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // lethality factor of the placement
                    double lethality = bestPlacement.getTargetDamage() * lethalMultiplier.getValue();

                    // will kill the target
                    if (health - lethality < 0.5) {
                        lethal = true;
                    }

                    // check if the damage meets our requirements
                    if (lethal || bestPlacement.getTargetDamage() > damage.getValue()) {
                        if(placement != null &&  bestPlacement.getTargetDamage() > placement.getTargetDamage() + 2) {
                            return null;
                        }
                        // mark it as our current placement
                        return new DamageHolder<>(PathUtil.findPath(bestPlacement.getDamageSource(), obyWallRange.getValue(), obyRange.getValue(), helpingBlocks.getValue()), bestPlacement.target, bestPlacement.targetDamage, bestPlacement.localDamage);
                    }
                }
            }
        }

        // we were not able to find any placements
        return null;
    }


    /**
     * Attacks a given endcrystal
     *
     * @param in The given endcrystal
     * @return Whether or not the attack was successful
     */
    public boolean attackCrystal(EntityEnderCrystal in) {
        return attackCrystal(in.getEntityId());
    }

    /**
     * Attacks a given endcrystal
     *
     * @param in The entity id of the given endcrystal
     * @return Whether or not the attack was successful
     */
    @SuppressWarnings("all")
    public boolean attackCrystal(int in) {

        // strength and weakness effects on the player
        PotionEffect weaknessEffect = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
        PotionEffect strengthEffect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;

        // must be not doing anything
        if ((PlayerUtil.isEating() && !offhand) && !multitask.getValue()) {
            return false;
        }

        // must be not mining
//        if ((PlayerUtil.isMining() && !offhand) && !whileMining.getValue()) {
//            return false;
//        }

        // mark previous slot
        int previousSlot = -1;


        // antiweakness switches to a tool slot to bypass the weakness effect


        // player sprint state
        boolean sprintState = false;

        // on strict anticheat configs, you need to stop sprinting before attacking (keeping consistent with vanilla behavior)
        if (interact.getValue().equals(Interact.Strict)) {

            // update sprint state
            sprintState = mc.player.isSprinting() || ((IEntityPlayerSP) mc.player).getServerSprintState();

            // stop sprinting when attacking an entity
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.player.setSprinting(false);
            }
        }

        // packet for attacking the given endcrystal
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setAction(Action.ATTACK);
        ((ICPacketUseEntity) attackPacket).setID(in);

        // verify that we cannot break the crystal due to weakness
        if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {

            // check if we are holding a tool
            if (!InventoryUtil.isHolding(ItemSword.class) && !InventoryUtil.isHolding(ItemPickaxe.class)) {

                // previous held item
                switchUtilAnti.switchTo(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE);
            }
        }
        // send attack packet
        mc.player.connection.sendPacket(attackPacket);
        switchUtilAnti.switchBack();

        // count packets
        explosionPackets.add(in);

        // swing the player's arm
        EntityUtil.swing(swing.getValue());

        // swing with packets
        //  mc.player.connection.sendPacket(new CPacketAnimation(!offhand || weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier()) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));

        // reset sprint state
        if (sprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            mc.player.setSprinting(true);
        }


        // ignore
        if (sequential.getValue().equals(Sequential.Normal)) {
            deadCrystals.add(in);
        }

        // clear queue
        queuedCrystals.clear();
        lastCrystalCount++;
        // attack was successful
        return true;
    }

    /**
     * Places a crystal at a given position
     *
     * @param in The position to place the crystal on
     * @return Whether or not the placement was successful
     */
    public boolean placeCrystal(BlockPos in) {

        // make sure the position actually exits
        if (in == null) {
            return false;
        }
        // pause switch to account for actions
        if (PlayerUtil.isEating() || PlayerUtil.isMending()) {
            autoSwitchTimer.reset();
        }


        if (!isHoldingCrystal()) return false;


        // normal switch requires pausing


        // make sure we are holding a crystal before trying to place


        // directions of placement
        double facingX = 0;
        double facingY = 0;
        double facingZ = 0;

        // assume the face is visible
        EnumFacing facingDirection = EnumFacing.UP;

        // the angles to the last interaction
        Pair<Float, Float> vectorAngles = calculateAngles(angleVector.first());

        // vector from the angles
        Vec3d placeVector = RotationUtil.getVectorForRotation(new float[]{vectorAngles.first(), vectorAngles.second()});

        // interact vector
        RayTraceResult interactVector = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).add(placeVector.x * placeRange.getValue(), placeVector.y * placeRange.getValue(), placeVector.z * placeRange.getValue()), false, false, true);

        // make sure the direction we are facing is consistent with our rotations
        switch (interact.getValue()) {
            case None:
                facingDirection = EnumFacing.DOWN;
                facingX = 0.5;
                facingY = 0.5;
                facingZ = 0.5;
                break;
            case Vanilla:

                // find the direction to place against
                RayTraceResult laxResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), new Vec3d(in).add(0.5, 0.5, 0.5));

                if (laxResult != null && laxResult.typeOfHit.equals(Type.BLOCK)) {
                    facingDirection = laxResult.sideHit;

                    // if we're at world height, we can still place a crystal if we interact with the bottom of the block, this doesn't work on strict servers
                    if (in.getY() >= (mc.world.getActualHeight() - 1)) {
                        facingDirection = EnumFacing.DOWN;
                    }
                }

                // find rotations based on the placement
                if (interactVector != null && interactVector.hitVec != null) {
                    facingX = interactVector.hitVec.x - in.getX();
                    facingY = interactVector.hitVec.y - in.getY();
                    facingZ = interactVector.hitVec.z - in.getZ();
                }

                break;
            case Strict:

                // if the place position is likely out of sight
                if (in.getY() > mc.player.posY + mc.player.getEyeHeight()) {

                    // our nearest visible face
                    Pair<Double, EnumFacing> closestDirection = Pair.of(Double.MAX_VALUE, EnumFacing.UP);

                    // iterate through all points on the block
                    for (float x = 0; x <= 1; x += 0.05) {
                        for (float y = 0; y <= 1; y += 0.05) {
                            for (float z = 0; z <= 1; z += 0.05) {

                                // find the vector to raytrace to
                                Vec3d traceVector = new Vec3d(in).add(x, y, z);

                                // distance to face
                                double directionDistance = mc.player.getDistance(traceVector.x, traceVector.y, traceVector.z);

                                // if the face is the closest to the player and trace distance is reasonably close, then we have found a new ideal visible side to place against
                                if (directionDistance < closestDirection.first()) {

                                    // check visibility, raytrace to the current point
                                    RayTraceResult strictResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), traceVector, false, true, false);

                                    // if our raytrace is a block, check distances
                                    if (strictResult != null && strictResult.typeOfHit.equals(Type.BLOCK)) {
                                        closestDirection = Pair.of(directionDistance, strictResult.sideHit);
                                    }
                                }
                            }
                        }
                    }

                    facingDirection = closestDirection.second();
                }

                // find rotations based on the placement
                if (interactVector != null && interactVector.hitVec != null) {
                    facingX = interactVector.hitVec.x - in.getX();
                    facingY = interactVector.hitVec.y - in.getY();
                    facingZ = interactVector.hitVec.z - in.getZ();
                }

                break;
        }

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;
        // switches to a crystal before attempting to place
        if (!isHoldingCrystal()) {
            if (autoSwitch.getValue().equals(AutoSwitch.Normal)) {

                // wait for switch pause
                if (autoSwitchTimer.passedMs(500)) {
                    switchUtil.switchTo(Items.END_CRYSTAL);
                    // switch
                }
            } else {
                switchUtil.switchTo(Items.END_CRYSTAL);

            }
        }
        // place the crystal
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(in, facingDirection, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float) facingX, (float) facingY, (float) facingZ));
        switchUtil.switchBack();
        EntityUtil.swing(swing.getValue());
        // count packets
        placementPackets.add(in);


        // placement was successful
        return true;
    }

    /**
     * Checks if the player is facing a certain vector
     *
     * @return Whether the player is facing a certain vector
     */
    public Pair<Float, Float> calculateAngles(Vec3d to) {

        // find the yaw and pitch to the vector
        float yaw = (float) (Math.toDegrees(Math.atan2(to.subtract(mc.player.getPositionEyes(1)).z, to.subtract(mc.player.getPositionEyes(1)).x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(to.subtract(mc.player.getPositionEyes(1)).y, Math.hypot(to.subtract(mc.player.getPositionEyes(1)).x, to.subtract(mc.player.getPositionEyes(1)).z)));

        // wrap the degrees to values between -180 and 180
        return Pair.of(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch));
    }

    /**
     * Checks if the AutoCrystal is desynced from the server
     *
     * @return Whether the AutoCrystal is desynced from the server
     */
    public boolean isDesynced() {

        // cannot be desynced in singleplayer
        if (mc.isSingleplayer()) {
            return false;
        }

        // sent too many packets with no response (40 seems like a good number may change in the future)
        return explosionPackets.size() > 40 || placementPackets.size() > 40;
    }


    /**
     * Gets the number of crystals in the last second
     *
     * @return The number of crystals in the last second
     */
    private int getAverageCrystalsPerSecond() {

        // average value
        return (int) getAverage(crystalCounts);
    }

//    /**
//     * Gets the time it took for the server to confirm the crystal
//     *
//     * @return The time it took for the server to confirm the crystal
//     */
//    private float getConfirmTime() {
//
//        // clamp
//        if (lastConfirmTime > 500) {
//            lastConfirmTime = 0;
//        }
//
//        // average value
//        return MathUtil.roundFloat(lastConfirmTime / 50F, 1);
//    }

    /**
     * Gets the average attack wait time
     *
     * @return the average attack wait time
     */
    private long getAverageWaitTime() {

        // average value
        float average = getAverage(attackTimes);

        if (average > 500) {
            average = 0;
        }

        // 10 slots
        return (long) average;
    }

    /**
     * Gets the average value of an array
     *
     * @param in The array
     * @return The average value of the array
     */
    private float getAverage(long[] in) {

        // average value
        float avg = 0;

        for (long time : in) {
            avg += time;
        }

        // average time
        return avg / 10F;
    }

    /**
     * Checks whether or not the player is holding a crystal
     *
     * @return Whether or not the player is holding a crystal
     */
    public boolean isHoldingCrystal() {
        return (InventoryUtil.isHolding(Items.END_CRYSTAL) || (!autoSwitch.getValue().equals(AutoSwitch.None) && InventoryUtil.find(Items.END_CRYSTAL) != -1));
    }

    /**
     * Finds whether or not a crystal can be placed on a specified block
     *
     * @param position The specified block to check if a crystal can be placed
     * @return Whether or not a crystal can be placed at the location
     */
    public boolean canPlaceCrystal(BlockPos position, boolean check) {
        if (position == null) return false;

        // block that we are placing on
        Block placeBlock = mc.world.getBlockState(position).getBlock();

        // crystals can only be placed on Obsidian and Bedrock
        if (!placeBlock.equals(Blocks.BEDROCK) && !placeBlock.equals(Blocks.OBSIDIAN) && check) {
            return false;
        }

        // the relative positions to check for air or fire, crystals can be placed on fire
        BlockPos nativePosition = position.up();
        BlockPos updatedPosition = nativePosition.up();

        // block that is above the one we are placing on
        Block nativeBlock = mc.world.getBlockState(nativePosition).getBlock();

        // check if the native position is air or fire
        if (!nativeBlock.equals(Blocks.AIR) && !nativeBlock.equals(Blocks.FIRE)) {
            return false;
        }

        // two block height needed for 1.12.2
        if (placements.getValue().equals(Placements.Native)) {

            // block that is above the air block
            Block updatedBlock = mc.world.getBlockState(updatedPosition).getBlock();

            // check if the updated position is air or fire
            if (!updatedBlock.equals(Blocks.AIR) && !updatedBlock.equals(Blocks.FIRE)) {
                return false;
            }
        }

        // check for any unsafe entities in the position
        int unsafeEntities = 0;

        // check all entities in the bounding box
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
                nativePosition.getX(), position.getY(), nativePosition.getZ(), nativePosition.getX() + 1, nativePosition.getY() + offset.getValue(), nativePosition.getZ() + 1 // offset for crystal bounding box, crystalpvp.cc allows you to place at a lower offset
        ))) {

            // if the entity will be removed the next tick, we can still place here
            if (entity == null || entity.isDead || deadCrystals.contains(entity.getEntityId())) {
                continue;
            }

            // we can place on these entities
            if (entity instanceof EntityXPOrb) {
                continue;
            }

            // if the entity is crystal, check it's on the same position
            if (entity instanceof EntityEnderCrystal) {

                // we've attacked and haven't "failed" to break yet
                if (attackedCrystals.containsKey(entity.getEntityId()) && entity.ticksExisted < 20) {
                    continue;
                }

                // distance to crystal
                double crystalRange = mc.player.getDistance(entity.posX, rangeEye.getValue() ? entity.posY + entity.getEyeHeight() : entity.posY, entity.posZ);

                // check if the entity is in range
                if (crystalRange <= explodeRange.getValue()) {
                    continue;
                }

                // local damage done by the crystal
                double localDamage = CrystalUtils.calculateDamage(entity, mc.player);

                // best damage
                double idealDamage = 0;

                // search all targets
                for (Entity target : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (target == null
                            || target.equals(mc.player)
                            || PlayerUtil.isDead(target)
                            || friendManager.isFriend(target.getName())) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
                    if (target instanceof EntityEnderCrystal) {
                        continue;
                    }

                    // don't attack our riding entity
                    if (target.isBeingRidden() && target.getPassengers().contains(mc.player)) {
                        continue;
                    }

                    // verify that the entity is a target
                    if (target instanceof EntityPlayer && !targetPlayers.getValue()
                            || EntityUtil.isPassiveMob(target) && !targetPassives.getValue()
                            || EntityUtil.isNeutralMob(target) && !targetNeutrals.getValue()
                            || EntityUtil.isHostileMob(target) && !targetHostiles.getValue()
                            || target instanceof EntityArrow && !targetArrows.getValue()
                            || target instanceof EntityEnderPearl && !targetPearls.getValue()
                    ) {
                        continue;
                    }

                    // distance to target
                    double entityRange = mc.player.getDistance(target);

                    // check if the target is in range
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the placement
                    double targetDamage = CrystalUtils.calculateDamage(entity, mc.player);

                    // check the safety of the placement
                    double safetyIndex = 1;

                    // check if we can take damage
                    if (DamageUtil.canTakeDamage()) {

                        // local health
                        double health = PlayerUtil.getHealth();

                        // incredibly unsafe
                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }

                        // unsafe -> if local damage is greater than target damage
                        else if (safety.getValue().equals(Safety.Stable)) {

                            // target damage and local damage scaled
                            double efficiency = targetDamage - localDamage;

                            // too small, we'll be fine :>
                            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                                efficiency = 0;
                            }

                            safetyIndex = efficiency;
                        }

                        // unsafe -> if local damage is greater than balanced target damage
                        else if (safety.getValue().equals(Safety.Balance)) {

                            // balanced target damage
                            double balance = targetDamage * safetyBalance.getValue();

                            // balanced damage, should be proportionate to local damage
                            safetyIndex = balance - localDamage;
                        }
                    }

                    // placement is unsafe
                    if (safetyIndex < 0) {
                        continue;
                    }

                    // update ideal damage
                    if (targetDamage > idealDamage) {
                        idealDamage = targetDamage;
                    }
                }

                // we will attack the crystal soon
                if (idealDamage > damage.getValue()) {
                    continue;
                }
            }

            unsafeEntities++;
        }

        // make sure there are not unsafe entities at the place position
        return unsafeEntities <= 0;
    }

    /**
     * Queues a crystal to be exploded
     */
    public void queue(EntityEnderCrystal in) {
        queuedCrystals.add(in);
    }

    public enum Placements {

        /**
         * Crystal placements for version 1.12.2
         */
        Native,

        /**
         * Crystal placements for version 1.13 and above
         */
        Updated
    }

    public enum Interact {

        /**
         * Places on the closest face, regardless of visibility, Allows placements at world borders
         */
        Vanilla,

        /**
         * Places on the closest visible face
         */
        Strict,

        /**
         * Places on the top block face, no facing directions
         */
        None
    }

    public enum YawStep {

        /**
         * Yaw steps when breaking and placing
         */
        Full,

        /**
         * Yaw steps when breaking
         */
        Semi,

        /**
         * Does not yaw step
         */
        None
    }

    public enum Inhibit {

        /**
         * Adds an additional delay for stricter servers
         */
        Full,

        /**
         * Does not wait and attacks straight away
         */
        Semi,

        /**
         * Does not inhibit
         */
        None
    }

    public enum Safety {

        /**
         * Considers an action unsafe if it does more damage than the multiplier
         */
        Balance,

        /**
         * Considers an action unsafe if it does more damage to the player than an enemy
         */
        Stable,

        /**
         * Actions are always considered safe
         */
        None
    }

    public enum Sequential {

        /**
         * One tick, quick adjust (balances out the timing)
         */
        Normal(1),

        /**
         * Two ticks, slower adjust (balances out the timing)
         */
        Strict(2),

        /**
         * No timing adjustment
         */
        None(1000);

        // ticks
        private final int ticks;

        Sequential(int ticks) {
            this.ticks = ticks;
        }

        /**
         * Gets the ticks
         *
         * @return The ticks
         */
        public double getTicks() {
            return ticks;
        }
    }

    public enum Text {

        /**
         * Render the damage done to the target
         */
        Target,

        /**
         * Render the damage done to the player
         */
        Local,

        /**
         * Render the damage done to the target and the damage done to the player
         */
        Both,

        /**
         * No damage render
         */
        None
    }

    public enum Page {
        AntiCheat,
        Place,
        Break,
        Damage,
        Predict,
        Targets,
        AutoObsidian,
        Render
    }

    public static class DamageHolder<T> {

        // damager
        private final T damageSource;

        private final Entity target;

        // damage info
        private final double targetDamage, localDamage;

        public DamageHolder(T damageSource, Entity target, double targetDamage, double localDamage) {
            this.damageSource = damageSource;

            // target
            this.target = target;

            // damage
            this.targetDamage = targetDamage;
            this.localDamage = localDamage;
        }

        /**
         * Gets the damage source
         *
         * @return The damage source
         */
        public T getDamageSource() {
            return damageSource;
        }

        /**
         * Gets the target
         *
         * @return The target
         */
        public Entity getTarget() {
            return target;
        }

        /**
         * Gets the damage to a target
         *
         * @return The damage to the target
         */
        public double getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the damage to the player
         *
         * @return The damage to the player
         */
        public double getLocalDamage() {
            return localDamage;
        }

        @Override
        public String toString() {
            return "DamageHolder{" +
                    "damageSource=" + damageSource +
                    ", target=" + target +
                    ", targetDamage=" + targetDamage +
                    ", localDamage=" + localDamage +
                    '}';
        }
    }
}