package we.devs.forever.client.modules.api;


import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.manager.impl.render.TextManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.listener.Listener;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.client.modules.impl.chat.Notify;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.main.ForeverClient;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public
abstract
class Module extends Client {

    private final ArrayList<Listener<?>> moduleListeners = new ArrayList<>();
    private final String name;
    public static final AtomicBoolean tickOngoing = new AtomicBoolean(false);
    private final String description;
    private final Category category;
    public Setting<Boolean> enabled = new Setting<>("Enabled", false);
    public Setting<Boolean> visible;
    public Setting<KeyMode> keyMode;
    public Setting<Bind> bind;

    public boolean isActive = isEnabled();
    public float offset;

    public TextManager renderer = textManager;


    public static boolean nullCheck() {
        return mc.player == null;
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public Module(String name, String description, Category category) {
        super(name);
        visible = new Setting<>("Visible", true, "Visibility in ArrayList");
        keyMode = new Setting<>("KeyMode", KeyMode.Release, "Modes of bind module");
        bind = new Setting<>("Bind", new Bind(Keyboard.KEY_NONE), "Bind module");
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, String description, Category category, boolean visible, boolean hideVisible, int keyBind, boolean hideKeyBind, KeyMode keyMode, boolean hideKeyMode) {
        super(name);
        this.visible = new Setting<>("Visible", visible, v -> !hideVisible);
        this.keyMode = new Setting<>("KeyMode", keyMode, v -> !hideKeyMode);
        bind = new Setting<>("Bind", new Bind(keyBind), v -> !hideKeyBind);
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, String description, Category category, boolean visible, boolean hideVisible, int keyBind, boolean hideKeyBind, KeyMode keyMode, boolean hideKeyMode, boolean defaultEnable) {
        super(name);
        this.visible = new Setting<>("Visible", visible, v -> !hideVisible);
        this.keyMode = new Setting<>("KeyMode", keyMode, v -> !hideKeyMode);
        bind = new Setting<>("Bind", new Bind(keyBind), v -> !hideKeyBind);
        enabled = new Setting<>("Enabled", defaultEnable);
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {
        //Is called on Enableing.
    }

    public void onDisable() {
        //Is called on disableing.
    }

    public boolean isActive() {
        return isActive;
    }

    private void onEnableNotify() {
        String str;
        String[] message = getEnableNotify();
        String name = message[0];
        String end = " " + message[1];
        if (Notify.INSTANCE.watermark.getValue()) {
            if (Notify.INSTANCE.italic.getValue()) {
                str = commandManager.getClientMessage() + " " + TextUtil.WHITE + TextUtil.ITALIC + TextUtil.BOLD + name + TextUtil.GREEN + end;
            } else {
                str = commandManager.getClientMessage() + " " + TextUtil.WHITE + name + TextUtil.GREEN + end;
            }
        } else {
            if (Notify.INSTANCE.italic.getValue()) {
                str = TextUtil.WHITE + TextUtil.ITALIC + TextUtil.BOLD + name + TextUtil.GREEN + end;

            } else {
                str = TextUtil.WHITE + name + TextUtil.GREEN + end;
            }
        }
        if (Notify.INSTANCE.history.getValue()) {
            Command.sendMessage(str);
        } else {
            Command.sendMessagepref(str, name);
        }

    }

    public final boolean isEnabled() {
        return enabled.getValue();
    }

    public final boolean isDisabled() {
        return !enabled.getValue();
    }

    private void onDisableNotify() {
        String str;
        String[] message = getDisableNotify();
        String name = message[0];
        String end = " " + message[1];
        if (Notify.INSTANCE.watermark.getValue()) {
            if (Notify.INSTANCE.italic.getValue()) {
                str = commandManager.getClientMessage() + " " + TextUtil.WHITE + TextUtil.ITALIC + TextUtil.BOLD + name + TextUtil.DARK_RED + end;
            } else {
                str = commandManager.getClientMessage() + " " + TextUtil.WHITE + name + TextUtil.DARK_RED + end;
            }
        } else {
            if (Notify.INSTANCE.italic.getValue()) {
                str = TextUtil.WHITE + TextUtil.ITALIC + TextUtil.BOLD + name + TextUtil.DARK_RED + end;

            } else {
                str = TextUtil.WHITE + name + TextUtil.DARK_RED + end;
            }
        }
        if (Notify.INSTANCE.history.getValue()) {
            Command.sendMessage(str);
        } else {
            Command.sendMessagepref(str, name);
        }
    }

    public String[] getEnableNotify() {
        return new String[]{
                name,
                "on."
        };
    }

    public String[] getDisableNotify() {
        return new String[]{
                name,
                "off."
        };
    }

    public void onThread() {
    }

    public void onToggle() {
        //Is called on both enableing and disableing
    }

    public void onLoad() throws Throwable {
        //Called for every module after loading the client.
    }

    public void onTick() {
        //Called on Client TickEvent. (That one has phases...)
    }

    public void onLogin() {
        //Called when the player logs in.
    }

    public void onLogout() {
        //Called when the player logs out.
    }

    public void onUpdate() {
        //Called onLivingUpdate
    }

    public void onRender2D(Render2DEvent event) {
        //Called instead of Render2DEvent //TODO: Maybe on the bus?
    }

    public void onRender3D(Render3DEvent event) {
        //Called instead of Render3DEvent //TODO: Maybe on the bus?
    }

    public void onAltRender3D(float partialTicks) {

    }

    public void onUnload() throws Throwable {
        //Called when the client is unloaded or shut down.
    }


    public String getDisplayInfo() {
        return null;
    }


    public final void setEnabled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable(String message, Object... args) {
        enable();
        Command.sendMessage("<" + name + "> " + String.format(message,args));
    }

    public void disable(String message, Object... args) {
        disable();
        Command.sendMessage("<" + name + "> " + String.format(message,args));
    }
    public void sendMessage(String message, Object... args) {
        Command.sendMessage("<" + name + "> " + String.format(message,args));
    }

    public final void enable() {
        ForeverClient.EVENT_BUS.register(this);
        this.enabled.setValue(true);
        if (!fullNullCheck()) {
            this.onToggle();
            try {
                this.onEnable();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        onEnableNotify();


    }

    public final void disable() {
        ForeverClient.EVENT_BUS.unregister(this);
        this.enabled.setValue(false);
        if (!fullNullCheck()) {
            this.onToggle();
            try {
                this.onDisable();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        onDisableNotify();
    }

    public final void toggle() {
        ClientEvent event = new ClientEvent(/*!this.isEnabled() ? 1 : 0,*/ this);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.setEnabled(!this.isEnabled());
        }
    }


    public final String getDescription() {
        return this.description;
    }

    public final boolean getVisible() {
        return this.visible.getValue();
    }


    public final Category getCategory() {
        return this.category;
    }


    public final Bind getBind() {
        return this.bind.getValue();
    }

    public final void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }


    public final String getFullArrayString() {
        return this.getName() + TextUtil.DARK_GRAY + (this.getDisplayInfo() != null ? " [" + TextUtil.RESET + this.getDisplayInfo() + TextUtil.DARK_GRAY + "]" : "");
    }

    public final ArrayList<Listener<?>> getModuleListeners() {
        return moduleListeners;
    }

    protected final void addModuleListeners(ModuleListener<? extends Module, ?> moduleListener) {
        moduleListeners.add(moduleListener);
    }

    public enum KeyMode {
        Hold,
        RevHold,
        Release
    }

    public
    enum Category {
        COMBAT("Combat", new ResourceLocation("textures/icons/combat.png")),
        MISC("Misc", new ResourceLocation("textures/icons/misc.png")),
        RENDER("Render", new ResourceLocation("textures/icons/render.png")),
        MOVEMENT("Movement", new ResourceLocation("textures/icons/movement.png")),
        PLAYER("Player", new ResourceLocation("textures/icons/player.png")),
        CHAT("Chat", new ResourceLocation("textures/icons/chat.png")),
        EXPLOIT("Exploit", new ResourceLocation("textures/icons/exploit.png")),
        CLIENT("Client", new ResourceLocation("textures/icons/client.png")),
        HUD("Hud", new ResourceLocation("textures/icons/client.png"));

        private final String name;
        private final ResourceLocation image;


        Category(String name, ResourceLocation image) {
            this.image = image;
            this.name = name;
        }

        public ResourceLocation getImage() {
            return image;
        }

        public String getName() {
            return name;
        }
    }
}
