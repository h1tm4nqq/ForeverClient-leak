package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.awt.*;

public class InventoryHud extends Hud {
    public Setting<Color> color = (new Setting<>("BGColor", new Color(20, 20, 20, 145), ColorPickerButton.Mode.Normal, 100));
    protected Setting<Integer> xx = (new Setting<>("XX", 143, 0, 200));
    protected Setting<Integer> yy = (new Setting<>("YY", 49, 0, 100));
    public InventoryHud() {
        super("Inventory",true);
    }

    @Override
    public void onRenderHud() {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        height = yy.getValue();
        width = xx.getValue();
        RenderUtil.drawRectangleCorrectly((int) (X.getValue() + 0), (int) (Y.getValue() + 0), xx.getValue(), yy.getValue(), color.getColor().getRGB());
        for (int i = 0; i < 27; ++i) {
            final ItemStack item_stack = InventoryHud.mc.player.inventory.mainInventory.get(i + 9);
            final int item_position_x = (int) (this.X.getValue() + i % 9 * 16);
            final int item_position_y = (int) (this.Y.getValue() + i / 9 * 16);
            InventoryHud.mc.getRenderItem().renderItemAndEffectIntoGUI(item_stack, item_position_x, item_position_y);
            InventoryHud.mc.getRenderItem().renderItemOverlayIntoGUI(InventoryHud.mc.fontRenderer, item_stack, item_position_x, item_position_y, null);
        }
        InventoryHud.mc.getRenderItem().zLevel = -5.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}