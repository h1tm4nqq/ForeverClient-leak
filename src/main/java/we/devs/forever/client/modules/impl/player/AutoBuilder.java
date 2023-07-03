package we.devs.forever.client.modules.impl.player;

import net.minecraft.block.BlockObsidian;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AutoBuilder
        extends Module {
    private final Setting<Settings> settings = (new Setting<>("Settings", Settings.Pattern));
    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Stairs, v -> this.settings.getValue() == Settings.Pattern));
    private final Setting<Direction> stairDirection = (new Setting<>("Direction", Direction.North, v -> this.mode.getValue() == Mode.Stairs && this.settings.getValue() == Settings.Pattern));
    private final Setting<Integer> width = (new Setting<>("StairWidth", 40, 1, 100, v -> this.mode.getValue() == Mode.Stairs && this.settings.getValue() == Settings.Pattern));
    private final Setting<Boolean> dynamic = (new Setting<>("Dynamic", true, v -> this.mode.getValue() == Mode.Flat && this.settings.getValue() == Settings.Pattern));
    private final Setting<Boolean> setPos = (new Setting<>("ResetPos", false, v -> this.settings.getValue() == Settings.Pattern && (this.mode.getValue() == Mode.Stairs || this.mode.getValue() == Mode.Flat && !this.dynamic.getValue())));
    private final Setting<Float> range = (new Setting<>("Range", 4.0f, 1.0f, 6.0f, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Integer> blocksPerTick = (new Setting<>("Blocks/Tick", 3, 1, 8, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Integer> placeDelay = (new Setting<>("PlaceDelay", 150, 0, 500, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> rotate = (new Setting<>("Rotate", true, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> altRotate = (new Setting<>("AltRotate", false, v -> this.rotate.getValue() && this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> ground = (new Setting<>("NoJump", true, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> noMove = (new Setting<>("NoMove", true, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> packet = (new Setting<>("Packet", true, v -> this.settings.getValue() == Settings.Place));
    private final Setting<Boolean> render = (new Setting<>("Render", true, v -> this.settings.getValue() == Settings.Render));
    private final Setting<Boolean> box = (new Setting<>("Box", true, v -> this.settings.getValue() == Settings.Render && this.render.getValue()));
    private final Setting<Integer> bRed = (new Setting<>("BoxRed", 150, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.box.getValue()));
    private final Setting<Integer> bGreen = (new Setting<>("BoxGreen", 0, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.box.getValue()));
    private final Setting<Integer> bBlue = (new Setting<>("BoxBlue", 150, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.box.getValue()));
    private final Setting<Integer> bAlpha = (new Setting<>("BoxAlpha", 40, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.box.getValue()));
    private final Setting<Boolean> outline = (new Setting<>("Outline", true, v -> this.settings.getValue() == Settings.Render && this.render.getValue()));
    private final Setting<Integer> oRed = (new Setting<>("OutlineRed", 255, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.outline.getValue()));
    private final Setting<Integer> oGreen = (new Setting<>("OutlineGreen", 50, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.outline.getValue()));
    private final Setting<Integer> oBlue = (new Setting<>("OutlineBlue", 255, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.outline.getValue()));
    private final Setting<Integer> oAlpha = (new Setting<>("OutlineAlpha", 255, 0, 255, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.outline.getValue()));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 1.5f, 0.1f, 5.0f, v -> this.settings.getValue() == Settings.Render && this.render.getValue() && this.outline.getValue()));
    private final Setting<Boolean> keepPos = (new Setting<>("KeepOldPos", false, v -> this.settings.getValue() == Settings.Misc));
    private final Setting<Updates> updates = (new Setting<>("Update", Updates.Tick, v -> this.settings.getValue() == Settings.Misc));
    private final Setting<Switch> switchMode = (new Setting<>("Switch", Switch.Silent, v -> this.settings.getValue() == Settings.Misc));
    private final Setting<Boolean> allBlocks = (new Setting<>("AllBlocks", true, v -> this.settings.getValue() == Settings.Misc));
    private final TimerUtil timerUtil = new TimerUtil();
    private final List<BlockPos> placepositions = new ArrayList<>();
    private BlockPos startPos;
    private int blocksThisTick;
    private int lastSlot;
    private int blockSlot;

    public AutoBuilder() {
        super("AutoBuilder", "Auto Builds.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (this.updates.getValue() == Updates.Tick) {
            this.doAutoBuilder();
        }
    }

    @Override
    public void onUpdate() {
        if (this.updates.getValue() == Updates.Update) {
            this.doAutoBuilder();
        }
    }

    @EventListener
    public void onUpdateWalkingPlayer(MotionEvent event) {
        if (this.updates.getValue() == Updates.Walking && event.getStage() != 1) {
            this.doAutoBuilder();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.placepositions == null || !this.render.getValue()) {
            return;
        }
        Color outline = new Color(this.oRed.getValue(), this.oGreen.getValue(), this.oBlue.getValue(), this.oAlpha.getValue());
        Color box = new Color(this.bRed.getValue(), this.bGreen.getValue(), this.bBlue.getValue(), this.bAlpha.getValue());
        this.placepositions.forEach(pos -> RenderUtil.drawSexyBoxPhobosIsRetardedFuckYouESP(new AxisAlignedBB(pos), box, outline, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), 1.0f, 1.0f, 1.0f));
    }

    @Override
    public void onEnable() {
        this.placepositions.clear();
        if (!this.keepPos.getValue() || this.startPos == null) {
            this.startPos = new BlockPos(AutoBuilder.mc.player.posX, Math.ceil(AutoBuilder.mc.player.posY), AutoBuilder.mc.player.posZ).down();
        }
        this.blocksThisTick = 0;
        this.lastSlot = AutoBuilder.mc.player.inventory.currentItem;
        this.timerUtil.reset();
        Command.sendMessage(TextUtil.LIGHT_PURPLE + "AutoBuilder on.");
    }

    private void doAutoBuilder() {
        if (!this.check()) {
            return;
        }
        for (BlockPos pos : this.placepositions) {
            if (this.blocksThisTick >= this.blocksPerTick.getValue()) {
                this.doSwitch(true);
                return;
            }
            int canPlace = BlockUtil.isPositionPlaceable(pos, false, true);
            if (canPlace == 3) {
                BlockUtil.placeBlockNotRetarded(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                ++this.blocksThisTick;
                continue;
            }
            if (canPlace != 2 || this.mode.getValue() != Mode.Stairs) continue;
            if (BlockUtil.isPositionPlaceable(pos.down(), false, true) == 3) {
                BlockUtil.placeBlockNotRetarded(pos.down(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                ++this.blocksThisTick;
                continue;
            }
            switch (this.stairDirection.getValue()) {
                case South: {
                    if (BlockUtil.isPositionPlaceable(pos.south(), false, true) != 3) break;
                    BlockUtil.placeBlockNotRetarded(pos.south(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                    ++this.blocksThisTick;
                    break;
                }
                case West: {
                    if (BlockUtil.isPositionPlaceable(pos.west(), false, true) != 3) break;
                    BlockUtil.placeBlockNotRetarded(pos.west(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                    ++this.blocksThisTick;
                    break;
                }
                case North: {
                    if (BlockUtil.isPositionPlaceable(pos.north(), false, true) != 3) break;
                    BlockUtil.placeBlockNotRetarded(pos.north(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                    ++this.blocksThisTick;
                    break;
                }
                case East: {
                    if (BlockUtil.isPositionPlaceable(pos.east(), false, true) != 3) break;
                    BlockUtil.placeBlockNotRetarded(pos.east(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                    ++this.blocksThisTick;
                }
            }
        }
        this.doSwitch(true);
    }

    private boolean doSwitch(boolean back) {
        Item i = AutoBuilder.mc.player.getHeldItemMainhand().getItem();
        switch (this.switchMode.getValue()) {
            case None: {
                if (i instanceof ItemBlock) {
                    if (this.allBlocks.getValue()) {
                        return true;
                    }
                    return ((ItemBlock) i).getBlock() instanceof BlockObsidian;
                }
                return false;
            }
            case Normal: {
                if (back) break;
                InventoryUtil.switchToHotbarSlot(this.blockSlot, false);
                break;
            }
            case Silent: {
                if (i instanceof ItemBlock && (this.allBlocks.getValue() || ((ItemBlock) i).getBlock() instanceof BlockObsidian) || this.lastSlot == -1)
                    break;
                if (back) {
                    AutoBuilder.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.lastSlot));
                    break;
                }
                AutoBuilder.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.blockSlot));
            }
        }
        return true;
    }

    private boolean check() {
        if (this.setPos.getValue()) {
            this.startPos = new BlockPos(AutoBuilder.mc.player.posX, Math.ceil(AutoBuilder.mc.player.posY), AutoBuilder.mc.player.posZ).down();
            this.setPos.setValue(false);
        }
        this.getPositions();
        if (this.placepositions.isEmpty()) {
            return false;
        }
        if (!this.timerUtil.passedMs(this.placeDelay.getValue())) {
            return false;
        }
        this.timerUtil.reset();
        this.blocksThisTick = 0;
        this.lastSlot = AutoBuilder.mc.player.inventory.currentItem;
        int n = this.blockSlot = this.allBlocks.getValue() ? InventoryUtil.findAnyBlock() : InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (this.ground.getValue() && !AutoBuilder.mc.player.onGround) {
            return false;
        }
        if (this.blockSlot == -1) {
            return false;
        }
        if (this.noMove.getValue() && (AutoBuilder.mc.player.moveForward != 0.0f || AutoBuilder.mc.player.moveStrafing != 0.0f)) {
            return false;
        }
        return this.doSwitch(false);
    }

    private void getPositions() {
        if (this.startPos == null) {
            return;
        }
        this.placepositions.clear();
        for (BlockPos pos : BlockUtil.getSphere(new BlockPos(AutoBuilder.mc.player.posX, Math.ceil(AutoBuilder.mc.player.posY), AutoBuilder.mc.player.posZ).up(), this.range.getValue(), this.range.getValue().intValue(), false, true, 0)) {
            if (this.placepositions.contains(pos) || !AutoBuilder.mc.world.isAirBlock(pos)) continue;
            if (this.mode.getValue() == Mode.Stairs) {
                switch (this.stairDirection.getValue()) {
                    case North: {
                        if (this.startPos.getZ() - pos.getZ() != pos.getY() - this.startPos.getY() || Math.abs(pos.getX() - this.startPos.getX()) >= this.width.getValue() / 2)
                            break;
                        this.placepositions.add(pos);
                        break;
                    }
                    case East: {
                        if (pos.getX() - this.startPos.getX() != pos.getY() - this.startPos.getY() || Math.abs(pos.getZ() - this.startPos.getZ()) >= this.width.getValue() / 2)
                            break;
                        this.placepositions.add(pos);
                        break;
                    }
                    case South: {
                        if (pos.getZ() - this.startPos.getZ() != pos.getY() - this.startPos.getY() || Math.abs(this.startPos.getX() - pos.getX()) >= this.width.getValue() / 2)
                            break;
                        this.placepositions.add(pos);
                        break;
                    }
                    case West: {
                        if (this.startPos.getX() - pos.getX() != pos.getY() - this.startPos.getY() || Math.abs(this.startPos.getZ() - pos.getZ()) >= this.width.getValue() / 2)
                            break;
                        this.placepositions.add(pos);
                    }
                }
                continue;
            }
            if (this.mode.getValue() != Mode.Flat || (double) pos.getY() != (this.dynamic.getValue() ? Math.ceil(AutoBuilder.mc.player.posY) - 1.0 : (double) this.startPos.getY()))
                continue;
            this.placepositions.add(pos);
        }
    }

    public enum Settings {
        Misc,
        Pattern,
        Place,
        Render

    }

    public enum Direction {
        West,
        South,
        East,
        North

    }

    public enum Updates {
        Tick,
        Update,
        Walking

    }

    public enum Switch {
        None,
        Normal,
        Silent

    }

    public enum Mode {
        Stairs,
        Flat

    }
}

