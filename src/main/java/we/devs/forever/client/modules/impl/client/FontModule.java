package we.devs.forever.client.modules.impl.client;


import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.impl.render.TextManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.awt.*;

public class FontModule extends Module {
    public static FontModule fontModule;
    public Setting<FontMode> modef = new Setting<>("CustomFont", FontMode.RobotoFlex);
    public Setting<Integer> fontscale = new Setting<>("FontScale", 17, 1, 30);
    public Setting<Mode> mode = new Setting<>("Style", Mode.Plain);



    public FontModule() {
        super("Font", "Custom font", Category.CLIENT);
        fontModule = this;
    }

    @Override
    public void onLoad() throws Throwable {
        textManager.setFontRenderer(modef.getValue().value,fontscale.getValue(),mode.getValue().value);
    }

    @EventListener
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2) {
            if (event.getSetting().getFeature().equals(this)) {
                if (event.getSetting().equals(fontscale) || event.getSetting().equals(mode) ||  event.getSetting().equals(modef)) {
                    textManager.setFontRenderer(modef.getValue().value,mode.getValue().value,fontscale.getValue());
                }
            }
        }
    }

    public enum Mode {
        Plain(0),
        Bold(1),
        Italic(2);
        public final int value;
         Mode(int value) {
            this.value = value;
        }


    }
    public enum FontMode {
        BadaBoom("/assets/forever/fonts/badaboom.ttf"),
        Monstserant("/assets/forever/fonts/Montserrat-Light.ttf"),
        MoonHouse("/assets/forever/fonts/moonhouse.ttf"),
        Poppins("/assets/forever/fonts/Poppins-Medium.ttf"),
        RobotoFlex("/assets/forever/fonts/RobotoFlex.ttf"),
        YahfieHeavy("/assets/forever/fonts/yahfieheavy.ttf");
        public final String value;
        FontMode(String value) {
            this.value = value;
        }


    }
}
