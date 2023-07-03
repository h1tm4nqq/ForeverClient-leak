package we.devs.forever.client.ui.foreverClientGui.hud.component;


import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;


public class ArmorHud extends Hud {
    public ArmorHud() {
        super("ArmorHud", true);
    }

    Setting<Boolean> percent = (new Setting<>("ArmorPrecent", true));
    protected Setting<Integer> xx = (new Setting<>("XX", 72, -100,100));
    protected Setting<Integer> yy = (new Setting<>("YY", 26, 0, 40));
    protected Setting<Float> factor = (new Setting<>("Factor", 18f, 0f, 50f));

    @Override
    public void onRenderHud() {
        GlStateManager.enableTexture2D();
        width = xx.getValue();
        height = yy.getValue();
        int y = this.Y.getValue().intValue() + (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 0 : 10);
        int x = this.X.getValue().intValue();
        for (ItemStack is : mc.player.inventory.armorInventory) {
            if (is.isEmpty()) continue;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            renderer.drawStringWithShadow(s, x + 17 - renderer.getStringWidth(s), y + 9, 0xffffff);
            if (percent.getValue()) {
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                int dmg = 100 - (int) (red * 100);
                renderer.drawStringWithShadow(dmg + "", x + 8 - renderer.getStringWidth(String.valueOf(dmg)) / 2F, y - 11, ColorUtil.toRGBA((int) (red * 255), (int) (green * 255), 0));
            }
            x += factor.getValue();
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }
}
