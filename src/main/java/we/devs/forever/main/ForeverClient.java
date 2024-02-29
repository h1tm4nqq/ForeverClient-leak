package we.devs.forever.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import we.devs.forever.api.event.eventsys.annotated.AnnotatedEventManager;
import we.devs.forever.api.manager.Managers;
import we.devs.forever.api.manager.impl.config.ConfigManager;
import we.devs.forever.api.util.client.IconUtil;
import we.devs.forever.api.util.math.Sphere;
import we.devs.forever.api.util.thread.GlobalExecutor;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.impl.commands.CowDupeCommand;
import we.devs.forever.client.modules.impl.client.RPC;
import we.devs.forever.client.ui.alts.MR;
import we.devs.forever.client.ui.alts.ias.IAS;
import we.devs.forever.client.ui.alts.ias.events.ClientEvents;
import we.devs.forever.client.ui.alts.ias.tools.SkinTools;
import we.devs.forever.client.ui.alts.iasencrypt.Standards;
import we.devs.forever.client.ui.customScreen.GuiCustomMainScreen;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static we.devs.forever.api.util.Util.mc;

@Mod(modid = "foreverclient", name = ForeverClient.MODNAME, version = ForeverClient.MODVER)
public final class ForeverClient {
    public static final String MODNAME = "ForeverClient";
    public static final String MODVER = "1.2.2";
    public static volatile boolean IsDisabledAcc = false;
    public static final we.devs.forever.api.event.eventsys.EventManager EVENT_BUS = new AnnotatedEventManager();
    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    public static final ForeverClient INSTANCE = new ForeverClient();
    public static final Managers managers = new Managers();
    public static GuiCustomMainScreen guiCustomMainScreen;


    public static void reload() {
//        managers.unload(false);
//        managers.load();
        managers.reload();
    }


    public static void setWindowIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            try (InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/minecraft/logo/16.png");
                 InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/minecraft/logo/32.png")) {
                ByteBuffer[] icons = new ByteBuffer[]{IconUtil.INSTANCE.readImageToBuffer(inputStream16x), IconUtil.INSTANCE.readImageToBuffer(inputStream32x)};
                Display.setIcon(icons);
            } catch (Exception e) {
                ForeverClient.LOGGER.error("Couldn't set Windows Icon", e);
            }
        }
    }

    public void preInit() {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("resourceCache");
            field.setAccessible(true);
            } catch (Throwable ignored) {
        }
        setWindowIcon();
        GlobalExecutor.EXECUTOR.submit(() -> Sphere.cacheSphere(LOGGER));
        Display.setTitle(MODNAME + "-v." + MODVER + " | " + mc.getSession().getUsername());

    }

    public void init() {
        guiCustomMainScreen = new GuiCustomMainScreen();
        MinecraftForge.EVENT_BUS.register(CowDupeCommand.class);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    public void postInit() {
        Thread checkThread = new CheckThread();
        checkThread.setDaemon(true);
        checkThread.start();
        managers.load0(false);
        ConfigManager.init();
        Client.moduleManager.load0();
        Client.textManager.load();
        IAS.config = new Configuration(new File("Forever/Alts.json"));
        IAS.CASESENSITIVE_PROPERTY = IAS.config.get("general", "ias.cfg.casesensitive", false, I18n.format("ias.cfg.casesensitive.tooltip"));
        IAS.ENABLERELOG_PROPERTY = IAS.config.get("general", "ias.cfg.enablerelog", false, I18n.format("ias.cfg.enablerelog.tooltip"));
        IAS.syncConfig();
        IAS.config.load();
        MR.init();
        Standards.importAccounts();
        SkinTools.cacheSkins();
        if (RPC.INSTANCE.isEnabled()) {
            RPC.INSTANCE.onEnable();
        }
        Client.friendManager.addFriend(mc.getSession().getUsername());
    }
}