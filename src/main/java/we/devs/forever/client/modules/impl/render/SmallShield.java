package we.devs.forever.client.modules.impl.render;

import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class SmallShield extends Module {
    private static SmallShield INSTANCE;

    static {
        SmallShield.INSTANCE = new SmallShield();
    }

    public Setting<Boolean> normalOffset;
    public Setting<Float> offset;
    public Setting<Float> offX;
    public Setting<Float> offY;
    public Setting<Float> mainX;
    public Setting<Float> mainY;

    public SmallShield() {
        super("SmallShield", "Makes you offhand lower.", Category.RENDER);
        this.normalOffset = (Setting<Boolean>) (new Setting<>("OffNormal", false));
        this.offset = (Setting<Float>) (new Setting<>("Offset", 0.7f, 0.0f, 1.0f, v -> this.normalOffset.getValue()));
        this.offX = (Setting<Float>) (new Setting<>("OffX", 0.0f, (-1.0f), 1.0f, v -> !this.normalOffset.getValue()));
        this.offY = (Setting<Float>) (new Setting<>("OffY", 0.0f, (-1.0f), 1.0f, v -> !this.normalOffset.getValue()));
        this.mainX = (Setting<Float>) (new Setting<>("MainX", 0.0f, (-1.0f), 1.0f));
        this.mainY = (Setting<Float>) (new Setting<>("MainY", 0.0f, (-1.0f), 1.0f));
        this.setInstance();
    }

    public static SmallShield getInstance() {
        if (SmallShield.INSTANCE == null) {
            SmallShield.INSTANCE = new SmallShield();
        }
        return SmallShield.INSTANCE;
    }

    private void setInstance() {
        SmallShield.INSTANCE = this;
    }

    public void onUpdate() {
        if (this.normalOffset.getValue()) {
            SmallShield.mc.entityRenderer.itemRenderer.equippedProgressOffHand = this.offset.getValue();
        }
    }
}
