package we.devs.forever.client.modules.impl.combat.offhandold;

import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerKey extends ModuleListener<Offhand, KeyEvent> {
    public ListenerKey(Offhand module) {
        super(module, KeyEvent.class);
    }

    @Override
    public void invoke(KeyEvent event) {
        if (Keyboard.getEventKeyState() && module.switchmode.getValue() && module.SwitchBind.getValue().getKey() == Keyboard.getEventKey()) {
//            if (module.switchval < 6 && event.iskeyDown()) {
//                Offhand.Mode newMode = (Offhand.Mode) EnumConverter.increaseEnum(module.currentMode);
//                module.offhandmode.setValue(newMode);
//                module.setMode(newMode);
//                module.countItems();
//                module.doSwitch();
//            }
        }
    }
}
