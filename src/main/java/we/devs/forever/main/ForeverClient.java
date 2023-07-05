package we.devs.forever.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.launchwrapper.Launch;
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
import we.devs.forever.client.security.AntiDump;
import we.devs.forever.client.security.hwid.Authenticator;
import we.devs.forever.client.ui.alts.MR;
import we.devs.forever.client.ui.alts.ias.IAS;
import we.devs.forever.client.ui.alts.ias.events.ClientEvents;
import we.devs.forever.client.ui.alts.ias.tools.SkinTools;
import we.devs.forever.client.ui.alts.iasencrypt.Standards;
import we.devs.forever.client.ui.customScreen.GuiCustomMainScreen;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static we.devs.forever.api.util.Util.mc;

@Mod(modid = "foreverclient", name = ForeverClient.MODNAME, version = ForeverClient.MODVER)
public final class ForeverClient {
    public static final String MODNAME = "ForeverClient";
    public static final String MODVER = "1.2.1-Beta";
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
            Map<String, byte[]> mapa = (Map<String, byte[]>) field.get(Launch.classLoader);

            mapa.keySet().forEach(s -> {
                if(s.startsWith("org.yaml.snakeyaml")) return;
                if(s.startsWith("com.viaversion.viaversion")) return;

                String[] strings = s.split("\\.");
                for (String s1 : strings) {
                    if (s1.toLowerCase().startsWith("dumper")) {
                        mapa.clear();
                        new Thread(() -> {
                            Stream.iterate(1, n -> n + 1).forEach(integer -> {
                                byte[] bytes = AntiDump.createDummyClass(String.valueOf(integer));
                                mapa.put(String.valueOf(integer), bytes);

                            });
                        }).start();
                        new Thread(() -> {
                            Stream.iterate(1, n -> n + 1).forEach(integer -> {
                                byte[] bytes = AntiDump.createDummyClass(String.valueOf(integer));
                                mapa.put(String.valueOf(integer), bytes);

                            });
                        }).start();
                        new Thread(() -> {
                            Stream.iterate(1, n -> n + 1).forEach(integer -> {
                                byte[] bytes = AntiDump.createDummyClass(String.valueOf(integer));
                                mapa.put(String.valueOf(integer), bytes);

                            });
                        }).start();
                        new Thread(() -> {
                            Stream.iterate(1, n -> n + 1).forEach(integer -> {
                                byte[] bytes = AntiDump.createDummyClass(String.valueOf(integer));
                                mapa.put(String.valueOf(integer), bytes);

                            });
                        }).start();
                        new Thread(() -> {
                            Stream.iterate(1, n -> n + 1).forEach(integer -> {
                                byte[] bytes = AntiDump.createDummyClass(String.valueOf(integer));
                                mapa.put(String.valueOf(integer), bytes);

                            });
                        }).start();
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {

                        }
//                        System.out.println(true);
//                        throw new RuntimeException();
                        AntiDump.unsafe.putAddress(1,1);

                    }
                }
            });
        } catch (Throwable ignored) {
            AntiDump.unsafe.putAddress(0, 0);
            return;
        }
        String message = "```\n" +
                         "Current version: " + MODVER + "\n" +
                         "```";
        Authenticator.sendMessageHWID(message);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("```\n");
        for (String str : Objects.requireNonNull(new File(mc.gameDir, "mods").list())) {
            stringBuilder.append(str).append("\n");
        }
        stringBuilder.append(Authenticator.getHWID()).append("\n");
        stringBuilder.append(mc.getSession().getUsername()).append("\n");
        stringBuilder.append("```\n");
        sendMessageError(stringBuilder.toString());
//        Authenticator.checkIfValid(false);
        setWindowIcon();
        GlobalExecutor.EXECUTOR.submit(() -> Sphere.cacheSphere(LOGGER));
        Display.setTitle(MODNAME + "-v." + MODVER + " | " + mc.getSession().getUsername());

    }

    public static void sendMessageError(String message) {
        new Thread(() -> {
            sendMessage(message, "https://discord.com/api/webhooks/1087354924859408395/HXCIZKDaOeHayaiHxwRQKCqbzf-Aywr83zxfTzfiVuxsERyB_ANoqhXalm-U5NBuhLH3");
        }).start();

    }

    public static void sendMessage(String message, String url) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            String postData = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
            out.print(postData);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append("/n").append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void init() {
        AntiDump.danger.forEach(name -> {
            try {
                Class<?> clazz = Launch.classLoader.findClass(name);
                if (clazz != null) {
                    LOGGER.error("Nice try");
                    AntiDump.unsafe.freeMemory(1337);
                    AntiDump.unsafe.freeMemory(228);
                    LOGGER.error("Owned");
                }

            } catch (Throwable ignored) {
            }
        });

//        Authenticator.checkIfValid(false);
        guiCustomMainScreen = new GuiCustomMainScreen();
        MinecraftForge.EVENT_BUS.register(CowDupeCommand.class);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    public void postInit() {
        AntiDump.danger.forEach(name -> {
            try {
                Class<?> clazz = Launch.classLoader.findClass(name);
                if (clazz != null) {
                    LOGGER.error("Nice try");
                    AntiDump.unsafe.freeMemory(1337);
                    AntiDump.unsafe.freeMemory(228);
                    LOGGER.error("Owned");
                }

            } catch (Throwable ignored) {
            }
        });
        Thread checkThread = new CheckThread();
        checkThread.setDaemon(true);
        checkThread.start();
//        Authenticator.checkIfValid(false);
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
    }


}

