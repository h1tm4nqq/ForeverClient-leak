package we.devs.forever.client.modules.impl.combat.burrow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.Page;
import we.devs.forever.client.modules.impl.combat.burrow.enums.Mode;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Burrow extends Module {


    private final Setting<Page> page = (new Setting<>("Page", Page.Place));
    public Setting<Mode> mode = new Setting<>("PlaceMode", Mode.Obsidian,
            "Mode All - using all block for place" +
                    "\nMode Obsidian - using priority Obsidian" +
                    "\nMode EChest - using priority EChest" +
                    "\nMode Block - using All block except Obsidian,EChest,Anvil" +
                    "\nMode Web - using web only" +
                    "\nMode Skulls - using skulls only", v -> page.getValue().equals(Page.Place));

    public Setting<RotationType> rotate = new Setting<>("Rotate", RotationType.Normal, "Rotate place and break crystals", v -> page.getValue().equals(Page.Place));
    public Setting<AutoSwitch> switchs = new Setting<>("Switch", AutoSwitch.Silent, "Switch", v -> page.getValue().equals(Page.Place));
    public Setting<Swing> swing = new Setting<>("Swing", Swing.Mainhand, "Swing anim", v -> page.getValue().equals(Page.Place));
    public Setting<Boolean> strict = new Setting<>("Strict", false, "Strict place and break crystals", v -> page.getValue().equals(Page.Place));
    public Setting<Boolean> bypass = new Setting<>("Bypass", false, "Shitty bypass for cc", v -> page.getValue().equals(Page.Place));
    public Setting<Integer> retries = new Setting<>("Retries", 10, 5, 100, "How many retries we will send", v -> page.getValue().equals(Page.Place) && bypass.getValue());
    public Setting<Boolean> twice = new Setting<>("Twice", true, "Send packets twice", v -> page.getValue().equals(Page.Place) && bypass.getValue());
    public Setting<Boolean> attackCrystal = new Setting<>("AttackCrystal", true, "Attack crystals for place conceal", v -> page.getValue().equals(Page.Place));
    public Setting<Boolean> center = new Setting<>("Center", true, "Center you work well", v -> page.getValue().equals(Page.Place));
    public Setting<Float> tp = new Setting<>("SetBackTP", -10F, -200F, 200F, "SetBack height by packet teleport", v -> page.getValue().equals(Page.Place));
    public Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false, "Disable after place", v -> page.getValue().equals(Page.Logic));
    public Setting<Boolean> autoBurrow = new Setting<>("AutoBurrow", false, "Automatically burrow when target near", v -> page.getValue().equals(Page.Logic));
    public Setting<Boolean> onlyInHole = new Setting<>("OnlyInHole", false, "Only In Hole", v -> page.getValue().equals(Page.Logic) && autoBurrow.getValue());
    public Setting<Integer> extrapolationTicks = new Setting<>("ExtrapolationTicks", 0, 0, 15, v -> page.getValue().equals(Page.Logic) && autoBurrow.getValue());
    public Setting<Float> smartRange = (new Setting<>("Smart Range", 2f, 0f, 6f, v -> page.getValue().equals(Page.Logic) && autoBurrow.getValue()));

    public Setting<Boolean> render = (new Setting<>("Render", true, v -> page.getValue().equals(Page.Render)));
    public Setting<Integer> duration = (new Setting<>("Duration", 1000, 1, 1000, v -> render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<RenderMode> renderMode = (new Setting<>("RenderMode", RenderMode.Fill, v -> render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.Fill || renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline) && render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));

    final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();

    BlockPos startPos;
    SwitchUtil switchUtil = new SwitchUtil(switchs);
    TimerUtil timerUtil = new TimerUtil();
    int ticks;
    boolean isActive;
    public static Burrow burrow;

    public Burrow() {
        super("Burrow", "Burrow your feet", Category.COMBAT);
        addModuleListeners(new ListenerMotionPre(this));
        burrow = this;
    }

    @Override
    public void onUnload() throws Throwable {
        disable();
    }

    @Override
    public void onLoad() throws Throwable {
        disable();
    }

    @Override
    public void onEnable() {
        startPos = new BlockPos(new Vec3d(MathUtil.round(mc.player.getPositionVector().x, 0), MathUtil.round(mc.player.getPositionVector().y, 0), MathUtil.round(mc.player.getPositionVector().z, 0)));

        if (center.getValue()) {
            double x = mc.player.posX - Math.floor(mc.player.posX);
            double z = mc.player.posZ - Math.floor(mc.player.posZ);
            if (x <= 0.3 || x >= 0.7) {
                x = x > 0.5 ? 0.69 : 0.31;
            }
            if (z < 0.3 || z > 0.7) {
                z = z > 0.5 ? 0.69 : 0.31;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Math.floor(mc.player.posX) + x, mc.player.posY, Math.floor(mc.player.posZ) + z, mc.player.onGround));
            mc.player.setPosition(Math.floor(mc.player.posX) + x, mc.player.posY, Math.floor(mc.player.posZ) + z);
        }
        ticks= 0;
    }


    private boolean checkCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {
            return false;
        }
        return true;
    }
    @Override
    public void onAltRender3D(float ticks) {

        if (render.getValue()) {

            renderBlocks.forEach((pos, time) -> {
                if (System.currentTimeMillis() > time + duration.getValue()) {
                    renderBlocks.remove(pos);
                } else {

                    final float maxBoxAlpha = fillColor.getColor().getAlpha();
                    final float maxOutlineAlpha = outLineColor.getColor().getAlpha();

                    float alphaBoxAmount = maxBoxAlpha / this.duration.getValue();
                    float alphaOutlineAmount = maxOutlineAlpha / this.duration.getValue();

                    int fadeBoxAlpha = MathUtil.clamp((int) (alphaBoxAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxBoxAlpha);
                    int fadeOutlineAlpha = MathUtil.clamp((int) (alphaOutlineAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxOutlineAlpha);

                    Color outLine = new Color(outLineColor.getColor().getRed(), outLineColor.getColor().getGreen(), outLineColor.getColor().getBlue(), fadeOutlineAlpha);
                    Color fill = new Color(fillColor.getColor().getRed(), fillColor.getColor().getGreen(), fillColor.getColor().getBlue(), fadeBoxAlpha);
                    BlockRenderUtil.drawBlock(pos, fill, outLine, lineWidth.getValue(), renderMode.getValue());
                }
            });
        }
    }
    @Override
    public boolean isActive() {
        return isActive && isEnabled();
    }
}
