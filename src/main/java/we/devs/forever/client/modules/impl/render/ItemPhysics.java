/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.client.modules.impl.render;


import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class ItemPhysics
        extends Module {
    public static ItemPhysics INSTANCE = new ItemPhysics();
    public Setting<Float> scalingItem = (new Setting<>("ScalingItem", 0.5f, 0.0f, 10.0f));
    public Setting<Float> scalingBlock = (new Setting<>("ScalingBlock", 0.5f, 0.0f, 10.0f));

    public ItemPhysics() {
        super("ItemPhysics", "Apply physics to items.", Category.RENDER);
        setInstance();
    }

    public static ItemPhysics getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemPhysics();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}

