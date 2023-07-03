package we.devs.forever.api.manager;

import we.devs.forever.api.manager.impl.client.*;
import we.devs.forever.api.manager.impl.config.ConfigManager;
import we.devs.forever.api.manager.impl.network.ServerManager;
import we.devs.forever.api.manager.impl.network.TickRateManager;
import we.devs.forever.api.manager.impl.player.*;
import we.devs.forever.api.manager.impl.player.interact.PlaceManager;
import we.devs.forever.api.manager.impl.render.NotificationManager;
import we.devs.forever.api.manager.impl.render.RenderRotationsManager;
import we.devs.forever.api.manager.impl.render.TextManager;
import we.devs.forever.api.manager.impl.render.WayPointManager;
import we.devs.forever.api.manager.impl.server.NCPManager;
import we.devs.forever.client.Client;

import java.io.File;

public class Managers {


    public void load0(boolean reload) {
        Client.eventManager = new EventManager();
        Client.textManager = new TextManager();
        Client.interactionManager = new InteractionManager();
        Client.placeManager = new PlaceManager();
        Client.renderRotationManager = new RenderRotationsManager();
        Client.rotationManager = new RotationManager();
        Client.positionManager = new PositionManager();
        Client.commandManager = new CommandManager();
        Client.notificationManager = new NotificationManager();
        Client.timerManager = new TimerManager();
        Client.friendManager = new FriendManager();
        Client.tickRateManager = new TickRateManager();
        Client.serverManager = new ServerManager();
        Client.speedManager = new SpeedManager();
        Client.potionManager = new PotionManager();
        Client.targetManager = new TargetManager();
        Client.moduleManager = new ModuleManager();
        Client.inventoryManager = new InventoryManager();
        Client.ncpManager = new NCPManager();
        Client.fpsManagemer = new FpsManagemer();
        Client.threadManager = new ThreadManager();
        try {
            Client.wayPointManager = new WayPointManager();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        load();
    }

    public void load() {
        Client.eventManager.load();
        Client.speedManager.load();
        Client.renderRotationManager.load();
        Client.rotationManager.load();
        Client.positionManager.load();
        Client.potionManager.load();
        Client.commandManager.load();
        Client.fpsManagemer.load();
        Client.notificationManager.load();
        Client.timerManager.load();
        Client.friendManager.load();
        Client.tickRateManager.load();
        Client.placeManager.load();
        Client.serverManager.load();
        Client.targetManager.load();
        Client.moduleManager.load();
        Client.inventoryManager.load();
        Client.wayPointManager.load();
        Client.ncpManager.load();
        Client.holeManager.load();
        Client.threadManager.load();
    }

    public void unload(boolean byError) {
        if (byError) ConfigManager.save(new File(ConfigManager.CONFIGS, ConfigManager.currentConfig.getName()));
        Client.wayPointManager.unload();
        Client.eventManager.unload();
        Client.moduleManager.unload();
        Client.commandManager.unload();
        Client.textManager.unload();
        Client.notificationManager.unload();
        Client.timerManager.unload();
        Client.friendManager.unload();
        Client.tickRateManager.unload();
        Client.placeManager.unload();
        Client.serverManager.unload();
        Client.speedManager.unload();
        Client.rotationManager.unload();
        Client.positionManager.unload();
        Client.potionManager.unload();
        Client.targetManager.unload();
        Client.inventoryManager.unload();
    }

    public void reload() {
        unload(false);
        load0(true);
    }

}















































