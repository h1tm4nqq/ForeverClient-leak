package we.devs.forever.client.modules.impl.render;

import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public
class CameraClip extends Module {

    private static CameraClip INSTANCE = new CameraClip();
    public Setting<Boolean> extend = (new Setting<>("Extend", false));
    public Setting<Double> distance = (new Setting<>("Distance", 10.0, 0.0, 50.0, "By how much you want to extend the distance.", v -> extend.getValue()));

    public CameraClip() {
        super("CameraClip", "Makes your Camera clip.", Category.RENDER);
        setInstance();
    }

    public static CameraClip getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CameraClip();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}
