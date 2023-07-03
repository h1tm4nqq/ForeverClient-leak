package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class ImageView extends Hud {
    public Setting<Image> imageSetting = new Setting<>("Image",new Image("NONE"));
    public Setting<Float> scaleX = new Setting<>("ScaleX",1F,0.1F,1000F);
    public Setting<Float> scaleY = new Setting<>("ScaleY",1F,0.1F,1000F);

    public ImageView() {
        super("ImageView", true);
    }

    @Override
    protected void onRenderHud() {
        width = scaleX.getValue();
        height = scaleY.getValue();
        if(imageSetting.getValue().getImage() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.resetColor();
            mc.getTextureManager().bindTexture(imageSetting.getValue().getImage());
            RenderUtil.drawCompleteImage(X.getValue(),Y.getValue(), scaleX.getValue(), scaleY.getValue());
            GlStateManager.resetColor();
            GlStateManager.popMatrix();
        }
    }
}
