package we.devs.forever.client.modules.impl.render;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public
class BlockHighlight extends Module {
    public Setting<Boolean> depth = (new Setting<>("Depth", true));
    public Setting<Boolean> box = (new Setting<>("Box", true));
    public Setting<Boolean> outline = (new Setting<>("Outline", true));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    protected Setting<Color> boxColor = (new Setting<>("BoxColor", new Color(0, 39, 255, 84), ColorPickerButton.Mode.Normal, 100, v -> box.getValue()));
    protected Setting<Color> outlineColor = (new Setting<>("OutlineColor", new Color(0, 34, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> outline.getValue()));


    public BlockHighlight() {
        super("BlockHighlight", "Highlights the block u look at.", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = mc.objectMouseOver;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ray.getBlockPos();
            RenderUtil.drawSexyBoxEspNotRetardForeverFuckingCodeBitch(blockpos, boxColor.getColor(), outlineColor.getColor(),
                    lineWidth.getValue(),
                    outline.getValue(), box.getValue(),
                    depth.getValue()
            );
        }
    }
}