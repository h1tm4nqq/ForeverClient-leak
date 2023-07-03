package we.devs.forever.client.modules.impl.render;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public
class BreakHighlight extends Module {
    public static BreakHighlight breakHighlight;
    private final Setting<Float> range = (new Setting<>("Range", 1.0f, 0.1f, 10.0f));
    public Setting<Boolean> showPercentage = (new Setting<>("ShowPercentage", true));
    public Setting<Boolean> self = (new Setting<>("SelfRender", true));
    public Setting<Boolean> box = (new Setting<>("Box", true));
    public Setting<Boolean> outline = (new Setting<>("Outline", true));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    protected Setting<Color> boxColor = (new Setting<>("BoxColor", new Color(0, 39, 255, 84), ColorPickerButton.Mode.Normal, 100, v -> box.getValue()));
    protected Setting<Color> outlineColor = (new Setting<>("OutlineColor", new Color(0, 34, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> outline.getValue()));
    ArrayList<ArrayList<Object>> possiblePacket = new ArrayList<>();

    public BreakHighlight() {
        super("BreakHighlight", "Highlights the block u look at.", Category.RENDER);
        breakHighlight = this;
    }

    boolean havePos(BlockPos pos) {
        for (ArrayList<Object> part : possiblePacket) {
            // If we already have it
            BlockPos temp = (BlockPos) part.get(0);
            if (temp.getX() == pos.getX() && temp.getY() == pos.getY() && temp.getZ() == pos.getZ()) {
                // Remove
                return true;
            }
        }
        return false;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            // Get it
            SPacketBlockBreakAnim pack = event.getPacket();
            if(mc.world.getBlockState(pack.getPosition()).getBlock().equals(Blocks.BEDROCK)) return;
            // If we dont have it
            if (!havePos(pack.getPosition()))
                possiblePacket.add(new ArrayList<Object>() {{
                    add(pack.getPosition());
                    add(0);
                    add(pack.getBreakerId());
                }});
        }
    }

    @Override
    public void onAltRender3D(float partialTicks) {
        if (fullNullCheck()) return;
        Set<BlockPos> displayed = new HashSet<>();
        for (int i = 0; i < possiblePacket.size(); i++) {
            BlockPos temp = (BlockPos) possiblePacket.get(i).get(0);
            int tick = (int) possiblePacket.get(i).get(1);
            EntityPlayer tempp = (EntityPlayer) mc.world.getEntityByID((int) possiblePacket.get(i).get(2));
            String name = "";
            if (tempp != null) {
                name = tempp.getName();

            }
            if (BlockUtil.getBlock(temp) == Blocks.AIR
                    || BlockUtil.getBlock(temp) == Blocks.BEDROCK) {
                possiblePacket.remove(i);
                i--;
                continue;
            }
            if (temp.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {
                displayed.add(temp);
                RenderUtil.drawSexyBoxEspNotRetardForeverFuckingCodeBitch(temp, boxColor.getColor(), outlineColor.getColor(),
                        lineWidth.getValue(),
                        outline.getValue(), box.getValue(),
                        false
                );
                if (showPercentage.getValue())
                    RenderUtil.drawText(temp, String.format("%.1f", (float) (Math.min(tick, 200)) / 200 * 100) + "% " + name, true);
            } else possiblePacket.get(i).set(1, ++tick);
            if (++tick > 200 + 200) {
                possiblePacket.remove(i);
                i--;
            } else possiblePacket.get(i).set(1, tick);
        }
        {
            mc.renderGlobal.damagedBlocks.forEach((integer, destroyBlockProgress) -> {
                if (destroyBlockProgress != null) {
                    EntityPlayer target = (EntityPlayer) mc.world.getEntityByID(integer);
                    String name = "";
                    if (target != null) {
                        if (target == mc.player && !self.getValue()) return;
                        name = target.getName();
                    }
                    BlockPos blockPos = destroyBlockProgress.getPosition();
                    if (mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR || displayed.contains(blockPos))
                        return;
                    if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {

                        RenderUtil.drawSexyBoxEspNotRetardForeverFuckingCodeBitch(blockPos, boxColor.getColor(), outlineColor.getColor(),
                                lineWidth.getValue(),
                                outline.getValue(), box.getValue(),
                                false
                        );
                        float f2 = destroyBlockProgress.getPartialBlockDamage() * 10.0f;
                        if (showPercentage.getValue())
                            RenderUtil.drawText(blockPos, String.format("%.1f", f2) + "% " + name, true);
                        //  RenderUtil.drawText(blockPos.getX() ,blockPos.getY() + lineWidth.getValue(),blockPos.getZ(),,true);
                    }
                }
            });
        }


//        RayTraceResult ray = mc.objectMouseOver;
//        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
//            BlockPos blockpos = ray.getBlockPos();
//
//        }
    }
}
