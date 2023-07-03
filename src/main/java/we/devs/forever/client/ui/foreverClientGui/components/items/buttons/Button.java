package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.Component;
import we.devs.forever.client.ui.foreverClientGui.components.items.Item;

import java.util.Map;

public class Button extends Item {
    public Map<Integer, Integer> colorMap;


    private boolean state;

    public Button(String name) {
        super(name);
        this.height = 12 * ForeverClientGui.getScale();
        width = 15  * ForeverClientGui.getScale();
    }

    public static void drawGuiGradientRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ClickGui.getInstance().rainbowRolling.getValue()) {
            final int color = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y, 0, this.textManager.scaledHeight)), ClickGui.getInstance().mainColor.getColor().getAlpha());
            final int color2 = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.textManager.scaledHeight)), ClickGui.getInstance().mainColor.getColor().getAlpha());
            RenderUtil.drawGradientRect(this.x, this.y, this.width, this.height - 0.5f,
                    this.getState() ? (this.isHovering(mouseX, mouseY) ? color : colorMap.get(MathUtil.clamp((int) this.y, 0, this.textManager.scaledHeight))) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077),
                    this.getState() ? (this.isHovering(mouseX, mouseY) ? color2 : colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.textManager.scaledHeight))) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        } else {
            //  drawGuiGradientRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? colorManager.getColorWithAlpha(ClickGui.getInstance().hoverAlpha.getValue()) : colorManager.getColorWithAlpha(ClickGui.getInstance().mainColor.getColor().getAlpha())) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
            RenderUtil.drawRect(this.x, this.y, this.x + this.width, this.y + this.height - 0.5f,
                    this.getState() ? (this.isHovering(mouseX, mouseY) ?
                            ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()) :
                            ClickGui.getInstance().mainColor.getColor().getRGB())
                            : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        }
        textManager.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 1.965f - ForeverClientGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);

        //
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            onMouseClick();
            LeftClick( mouseX,  mouseY);
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            RightClick( mouseX,  mouseY);
        }
    }

    public void onMouseClick() {
        state = !state;
        toggle();
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public void LeftClick(int mouseX, int mouseY) {

    }

    public void RightClick(int mouseX, int mouseY) {

    }

    public void updateVisibility() {

    }
    public void toggle() {
    }

    public boolean getState() {
        return state;
    }

    @Override
    public float getHeight() {
        return 12* ForeverClientGui.getScale();
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return mouseX >= getX()
                && mouseX <= getX() + getWidth()
                && mouseY >= getY()
                && mouseY <= getY() + height;
    }

    public boolean isHovering(int mouseX, int mouseY, float x1, float y1, float x2, float y2) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return x1 < mouseX && x2 > mouseX && y1 < mouseY && y2 > mouseY;
    }
}
