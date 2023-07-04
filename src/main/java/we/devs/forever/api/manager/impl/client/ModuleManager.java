package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.chat.*;
import we.devs.forever.client.modules.impl.client.*;
import we.devs.forever.client.modules.impl.client.hud.HUD;
import we.devs.forever.client.modules.impl.combat.*;
import we.devs.forever.client.modules.impl.combat.autoMine.AutoMine;
import we.devs.forever.client.modules.impl.combat.autocrystal.AutoCrystal;
import we.devs.forever.client.modules.impl.combat.burrow.Burrow;
import we.devs.forever.client.modules.impl.combat.holefill.HoleFill;
import we.devs.forever.client.modules.impl.combat.offhandold.Offhand;
import we.devs.forever.client.modules.impl.exploit.*;
import we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly;
import we.devs.forever.client.modules.impl.exploit.speedmine.SpeedMine;
import we.devs.forever.client.modules.impl.misc.*;
import we.devs.forever.client.modules.impl.movement.*;
import we.devs.forever.client.modules.impl.movement.boatfly.BoatFly;
import we.devs.forever.client.modules.impl.movement.fastfall.FastFall;
import we.devs.forever.client.modules.impl.movement.speed.Speed;
import we.devs.forever.client.modules.impl.player.*;
import we.devs.forever.client.modules.impl.player.fuckplayer.FuckPlayer;
import we.devs.forever.client.modules.impl.render.*;
import we.devs.forever.client.modules.impl.render.breadcrumbs.BreadCrumbs;
import we.devs.forever.client.modules.impl.render.holeesp.HoleESP;
import we.devs.forever.client.modules.impl.render.logoutspots.LogoutSpots;
import we.devs.forever.client.modules.impl.render.nametags.Nametags;
import we.devs.forever.client.modules.impl.render.radar.Radar;
import we.devs.forever.client.modules.impl.render.search.Search;
import we.devs.forever.client.modules.impl.render.shader.Shader;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;
import we.devs.forever.client.ui.foreverClientGui.hud.component.*;
import we.devs.forever.main.ForeverClient;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public
class ModuleManager extends AbstractManager {

    public static Set<Module> modules = new HashSet<>();
    public static List<Hud> Hudmodules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();
    public static final Map<Module, ArrayList<Setting<?>>> moduleSettings = new HashMap<>();

    public ModuleManager() {
        super("ModuleManager");
    }


    public static ArrayList<Setting<?>> getSettings(Module module) {
        return moduleSettings.get(module);
    }

    public static Setting<?> getSettingByName(Module module, String name) {
        for (Setting<?> setting : moduleSettings.get(module)) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    //ретард код? да детка ретард код
    //так скажем, насрал блять
    private static ArrayList<Setting<?>> getSettingList(Module inputModule) {
        Module module = (Module) inputModule.getClass().getSuperclass().cast(inputModule);
        ArrayList<Setting<?>> settingList = new ArrayList<>();
        for (Field field : module.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Setting<?> setting = (Setting<?>) field.get(module);
                    setting.setFieldName(field.getName());
                    settingList.add(setting);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : module.getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Setting<?> setting = (Setting<?>) field.get(module);
                    setting.setFieldName(field.getName());
                    settingList.add(setting);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : module.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Setting<?> setting = (Setting<?>) field.get(module);
                    setting.setFieldName(field.getName());
                    settingList.add(setting);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        settingList.stream().filter(Objects::isNull).forEach(settingList::remove);
        settingList.forEach(setting -> setting.setFeature(module));
        return settingList;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }


        throw new IllegalStateException("Cannot find module " + clazz.getName());
    }

    public void disableModule(String name) {
        Module module = getModuleByName(name);
        if (module != null) {
            module.disable();
        }
    }

    public Module getModuleByDisplayName(String displayName) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(displayName)) {
                return module;
            }
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabledModules.add(module);
            }
        }
        return enabledModules;
    }

    public List<Hud> getHudModules() {
        return Hudmodules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
        modules.forEach(module -> {
            if (module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    @Override
    public void onLoad() {
        registerModules();
        modules.forEach(module -> moduleSettings.put(module, getSettingList(module)));
        Hudmodules = modules.stream()
                .filter(x -> x instanceof Hud)
                .map(x -> (Hud) x)
                .collect(Collectors.toList());
        ForeverClient.LOGGER.info("Loaded HUD");
        modules.stream().filter(Module::isEnabled).forEach(ForeverClient.EVENT_BUS::register);
        modules.stream().filter(Module::isDisabled).forEach(ForeverClient.EVENT_BUS::unregister);
    }
    public void load0(){
        modules.forEach(x -> {
            try {
                x.onLoad();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onUnload() {
        modules.forEach(ForeverClient.EVENT_BUS::unregister);
        modules.forEach(x -> {
            try {
                x.onUnload();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        for (Module module : modules) {
            module.enabled.setValue(false);
        }
        modules.clear();
        moduleSettings.clear();
        Hudmodules.clear();
    }

    public void test() {
        modules.stream().filter(Module::isEnabled).forEach(Module::enable);
        modules.stream().filter(Module::isDisabled).forEach(Module::disable);
    }

    public void registerModules() {
        //COMBAT
        modules.add(new Anchor());
        modules.add(new AntiRegear());
        modules.add(new AutoArmor());
        modules.add(new AutoMine());
        modules.add(new AutoTotem());
        modules.add(new AutoPiston());
        modules.add(new AutoCrystal());
        modules.add(new PistonPush());
        modules.add(new AutoTrap());
        modules.add(new AutoWeb());
        modules.add(new BowSpam());
        modules.add(new Blocker());
        modules.add(new Criticals());
        modules.add(new Burrow());
        modules.add(new HoleFill());
        modules.add(new Aura());
        modules.add(new Offhand());
        modules.add(new Quiver());
        modules.add(new SelfAnvil());
        modules.add(new SelfTrap());
        modules.add(new SilentXP());
        modules.add(new Surround());
        modules.add(new Velocity());
        ForeverClient.LOGGER.info("Loaded COMBAT");
        //MISC
        //    modules.add(new AntiPotion());
        modules.add(new AntiAim());
        modules.add(new AutoLog());
        modules.add(new AutoReconnect());
        modules.add(new AutoRespawn());
        modules.add(new AutoRegear());
        modules.add(new AutoSortInv());
        modules.add(new BuildHeight());
        modules.add(new NoAFK());
        modules.add(new MCF());
        modules.add(new TimerMod());
        modules.add(new NoSoundLag());
        modules.add(new F3Spoofer());
        modules.add(new WayPoints());
        modules.add(new PacketLogger());
        modules.add(new Portals());
        ForeverClient.LOGGER.info("Loaded MISC");
        //MOVEMENT
        modules.add(new AutoWalk());
        modules.add(new BoatFly());
        modules.add(new ElytraFlight());
        modules.add(new EntityControl());
        modules.add(new EntitySpeed());
        modules.add(new FastSwim());
        modules.add(new Flight());
        modules.add(new Jesus());
        modules.add(new Levitation());
        modules.add(new LongJump());
        modules.add(new NoFall());
        modules.add(new NoSlow());
        modules.add(new SafeWalk());
        modules.add(new Scaffold());
        modules.add(new Speed());
        modules.add(new Sprint());
        modules.add(new TickShift());
        modules.add(new PhaseWalk());
        modules.add(new FastFall());
        ForeverClient.LOGGER.info("Loaded MOVEMENT");
        //PLAYER
        modules.add(new AutoBuilder());
        modules.add(new AutoFish());
        modules.add(new Blink());
        modules.add(new FuckPlayer());
        modules.add(new FastPlace());
        modules.add(new Freecam());
        modules.add(new FreeLook());
        modules.add(new LiquidInteract());
        modules.add(new MiddleClickPearl());
        modules.add(new MultiTask());
        modules.add(new NoInteract());
        modules.add(new NoOpen());
        modules.add(new NoRotate());
        modules.add(new Refill());
        modules.add(new XCarry());
        modules.add(new Yaw());
        ForeverClient.LOGGER.info("Loaded PLAYER");
        //RENDER

        modules.add(new Swings());
        //  modules.add(new Search());
        modules.add(new Skeleton());
        modules.add(new Search());
        modules.add(new SuperheroFX());
        modules.add(new BlockHighlight());
        modules.add(new BreakHighlight());
        modules.add(new BreadCrumbs());
        modules.add(new BurrowHighlight());
        modules.add(new CameraClip());
        modules.add(new Chams());
        modules.add(new ChinaHat());
        modules.add(new Crosshair());
        modules.add(new CrystalModify());
        modules.add(new ESP());
        modules.add(new ExtraTab());
        modules.add(new Fullbright());
        modules.add(new HandChams());
        modules.add(new HitParticles());
        modules.add(new HoleESP());
        modules.add(new ItemPhysics());
        modules.add(new LogoutSpots());
        modules.add(new MobOwner());
        modules.add(new Nametags());
        modules.add(new NoRender());
        modules.add(new Radar());
        modules.add(new Sky());
        modules.add(new ToolTips());
        modules.add(new Tracers());
        modules.add(new Trails());
        modules.add(new Trajectories());
        modules.add(new ViewModel());
        modules.add(new PopChams());

        modules.add(new Shader());//не трогай блять иначе я тебя захуярю
        ForeverClient.LOGGER.info("Loaded RENDER");
        //CHAT
        modules.add(new AntiSpam());
        modules.add(new AutoTox());
        modules.add(new AutoBackUp());
        modules.add(new Chat());
        modules.add(new Spammer());
        modules.add(new StashLogger());
        modules.add(new TotemPopCounter());
        ForeverClient.LOGGER.info("Loaded CHAT");
        //EXPLOIT
        modules.add(new AntiHunger());
        modules.add(new AutoDupe());
        modules.add(new Bypass());
//        modules.add(new ECExploit());
        modules.add(new EntityDesync());
        modules.add(new NoEntityTrace());
        modules.add(new SpeedMine());
        modules.add(new SetPos());
        modules.add(new ChorusControl());
        modules.add(new ChorusTracker());
        modules.add(new FastProjectile());
        modules.add(new Predict());
        modules.add(new HitboxCity());
        modules.add(new InstantMine());
        modules.add(new NewChunks());
        modules.add(new PacketCanceller());
        modules.add(new Phase());
        modules.add(new LagBack());
        modules.add(new Reach());//какого хуя оно не выкладывается СУКА
        modules.add(new PacketFly());
        modules.add(new PearlPhase());
        ForeverClient.LOGGER.info("Loaded EXPLOIT");
        //CLIENT
        modules.add(new ClickGui());
        modules.add(new CustomMainMenu());
        modules.add(new Colors());
        modules.add(new Cape());
        modules.add(new HUD());
        modules.add(new HudEditor());
          modules.add(new FontModule());
        modules.add(new Notify());
        modules.add(new RPC());
        modules.add(new Friends());
        ForeverClient.LOGGER.info("Loaded CLIENT");

        modules.add(new ArrayListHud());
        modules.add(new CompassHud());
        modules.add(new CrystalHud());
        modules.add(new EXPHud());
        modules.add(new PingHud());
        modules.add(new ServerBrandHud());
        modules.add(new SpeedHud());
        modules.add(new TargetHud());
        modules.add(new TestHud());
        modules.add(new TotemHud());
        modules.add(new TPSHud());
        modules.add(new WaterMarkHud());
        modules.add(new ArmorHud());
        modules.add(new CoordsHud());
        modules.add(new GappleHud());
        modules.add(new HoleHud());
        modules.add(new PlayerViewer());
        modules.add(new InventoryHud());
        modules.add(new IPHud());
        modules.add(new PotionsHud());
        modules.add(new ArraylistHudNew());
        modules.add(new ImageView());
        modules.add(new TextRadar());
    }

    public void onThread() {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onThread();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
    public void onUpdate() {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onUpdate();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onTick() {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onTick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onRender2D(Render2DEvent event) {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onRender2D(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        Hudmodules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onRender2D(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onRender3D(Render3DEvent event) {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onRender3D(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onAltRender3D(float ticks) {
        modules.stream().filter(Module::isEnabled).forEach(module -> {
            try {
                module.onAltRender3D(ticks);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void sortModules(boolean reverse) {
        this.sortedModules = getEnabledModules()
                .stream().filter(Module::getVisible)
                .sorted(Comparator.comparing(module -> textManager.getStringWidth(module.getFullArrayString()) * (reverse ? -1 : 1)))
                .collect(Collectors.toList());
    }

    public void onLogout() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onLogout);
    }

    public void onLogin() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onLogin);
    }

    @EventListener
    public void onKeyPressed(KeyEvent event) {
        if (event.getKey() == 0 || mc.currentScreen instanceof ForeverClientGui) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == event.getKey()) {
                switch (module.keyMode.getValue()) {
                    case Hold: {
                        if (event.iskeyDown()) {
                        if(module.isDisabled())
                            module.enable();
                        } else {
                            module.disable();
                        }
                        break;
                    }
                    case RevHold: {
                        if (event.iskeyDown()) {
                            if(module.isEnabled())
                                module.disable();
                        } else {
                            module.enable();
                        }
                        break;
                    }
                    case Release: {
                        if (event.iskeyDown()) {
                            module.toggle();
                        }
                        break;
                    }
                }
            }
        });


    }




}
