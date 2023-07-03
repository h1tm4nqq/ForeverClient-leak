package we.devs.forever.client.modules.impl.render;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class Crosshair extends Module {
    public static Crosshair INSTANCE;
    private final Setting<Boolean> dynamic  =  (new Setting<>("Dynamic", true));
    private final Setting<Float> width =  (new Setting<>("Width", 1.0f, 0.5f, 10.0f));
    private final Setting<Float> gap=  (new Setting<>("Gap", 3.0f, 0.5f, 10.0f));
    private final Setting<Float> length=  (new Setting<>("Length", 7.0f, 0.5f, 100.0f));
    private final Setting<Float> dynamicGap= (new Setting<>("DynamicGap", 1.5f, 0.5f, 10.0f));
    private final Setting<Color> color= (new Setting<>("Color", new Color(30, 30, 30, 30), ColorPickerButton.Mode.Normal, 100));


    public Crosshair() {
        super("Crosshair", "Lets you customize your in game crosshair", Category.RENDER);
        INSTANCE = this;
    }


    public void onRender2D(final Render2DEvent event) {
        final int color = this.color.getColor().getRGB();
        final ScaledResolution resolution = new ScaledResolution(mc);
        final float middlex = resolution.getScaledWidth() / 2.0f;
        final float middley = resolution.getScaledHeight() / 2.0f;
        RenderUtil.drawBordered(middlex - this.width.getValue(), middley - (this.gap.getValue() + this.length.getValue()) - ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middlex + this.width.getValue(), middley - this.gap.getValue() - ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), 0.5f, color, -16777216);
        RenderUtil.drawBordered(middlex - this.width.getValue(), middley + this.gap.getValue() + ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middlex + this.width.getValue(), middley + (this.gap.getValue() + this.length.getValue()) + ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), 0.5f, color, -16777216);
        RenderUtil.drawBordered(middlex - (this.gap.getValue() + this.length.getValue()) - ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middley - this.width.getValue(), middlex - this.gap.getValue() - ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middley + this.width.getValue(), 0.5f, color, -16777216);
        RenderUtil.drawBordered(middlex + this.gap.getValue() + ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middley - this.width.getValue(), middlex + (this.gap.getValue() + this.length.getValue()) + ((this.isMoving() && this.dynamic.getValue()) ? this.dynamicGap.getValue() : 0.0f), middley + this.width.getValue(), 0.5f, color, -16777216);
    }

    @EventListener
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (mc.gameSettings.thirdPersonView != 0) {
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
        }
    }
    public boolean isMoving() {
        return  mc.player.moveStrafing != 0.0f || mc.player.moveForward != 0.0f;
    }
}