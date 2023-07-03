package we.devs.forever.client.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public
class AutoWeb extends Module {
    public static boolean isPlacing = false;
    private final Setting<Integer> delay = (new Setting<>("Delay", 50, 0, 500));
    private final Setting<Integer> blocksPerPlace = (new Setting<>("BlocksPerTick", 8, 1, 30));
    private final Setting<RotationType> rotate = (new Setting<>("Rotate", RotationType.Legit));
    private final Setting<Boolean> packet = (new Setting<>("Packet", true));
    private final Setting<Boolean> strict = (new Setting<>("Strict", true));
    private final Setting<Boolean> boost = (new Setting<>("Boost", false));
    private final Setting<Boolean> crys = (new Setting<>("breakCrystals", true));
    public final Setting<AutoSwitch> swap = new Setting<>("Swap", AutoSwitch.Silent);
    private final Setting<Boolean> disable = (new Setting<>("AutoDisable", false));
    private final Setting<Boolean> raytrace = (new Setting<>("Raytrace", false));
    private final Setting<Boolean> lowerbody = (new Setting<>("Feet", true));
    private final Setting<Boolean> upperBody = (new Setting<>("Face", false));
    private final TimerUtil timerUtil = new TimerUtil();
    public EntityPlayer target;
    private boolean didPlace = false;
    private final SwitchUtil switchUtil = new SwitchUtil(swap);
    private boolean switchedItem;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private List<BlockPos> posList = new ArrayList<>();
    private int placements = 0;

    public AutoWeb() {
        super("AutoWeb", "Traps other players in webs", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
    }

    @EventListener
    public void onPreMotion(MotionEvent.Pre event) {
        if (AutoTrap.fullNullCheck()) {
            return;
        }
        if (!switchUtil.check(Blocks.WEB)) {
            disable(ChatFormatting.RED + "No Webs in hotbar disabling...");
            return;
        }
        if (check()) return;
        BlockPos target = new BlockPos(this.target.getPositionVector());
        if(BlockUtil.isPosEmpty(target) && BlockUtil.canPlace(target) && lowerbody.getValue()) {
            placeBlock(target);

        } else if(BlockUtil.isPosEmpty(target.up()) && BlockUtil.canPlace(target.up()) && upperBody.getValue()) {
            placeBlock(target.up());

        }
        if (didPlace) {
            timerUtil.reset();
        }
    }

    @Override
    public String getDisplayInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    @Override
    public void onDisable() {
        isPlacing = false;

    }


    private boolean check() {
        isPlacing = false;
        didPlace = false;
        placements = 0;
        target = getTarget();
        return target == null;
    }

    private EntityPlayer getTarget() {
        return mc.world.playerEntities.stream()
                .filter(Objects::nonNull)
                .filter(entityPlayer -> !entityPlayer.isDead)
                .filter(entityPlayer ->!EntityUtil.isntValid(entityPlayer, 11))
                .filter(entityPlayer -> speedManager.getPlayerSpeed(entityPlayer) < 30.0)
                .min(Comparator.comparing(entityPlayer -> entityPlayer.getDistanceSq(mc.player))).orElse(null);

    }


    private void placeBlock(BlockPos pos) {
        if (mc.player.getDistanceSq(pos) <= MathUtil.square(5.0) && timerUtil.passedMs(delay.getValue())) {
            isPlacing = true;
            if (switchUtil.switchTo(Blocks.WEB)) {
                interactionManager.placeBlockIgnore(pos, rotate.getValue(), packet.getValue(), strict.getValue(), boost.getValue(), crys.getValue());
                switchUtil.switchBack();
                 didPlace = true;
            }
        }
    }

}