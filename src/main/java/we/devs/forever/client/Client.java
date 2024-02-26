package we.devs.forever.client;

import net.minecraft.server.management.PlayerList;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.manager.impl.client.*;
import we.devs.forever.api.manager.impl.network.ServerManager;
import we.devs.forever.api.manager.impl.network.TickRateManager;
import we.devs.forever.api.manager.impl.player.*;
import we.devs.forever.api.manager.impl.player.holeManager.thread.holes.HoleManager;
import we.devs.forever.api.manager.impl.player.interact.PlaceManager;
import we.devs.forever.api.manager.impl.render.NotificationManager;
import we.devs.forever.api.manager.impl.render.RenderRotationsManager;
import we.devs.forever.api.manager.impl.render.TextManager;
import we.devs.forever.api.manager.impl.render.WayPointManager;
import we.devs.forever.api.manager.impl.server.NCPManager;
import we.devs.forever.api.util.Util;

public
class Client implements Util {

    public static EventManager eventManager;
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static NotificationManager notificationManager;
    public static TextManager textManager;
    public static TimerManager timerManager;
    public static FriendManager friendManager;
    public static TickRateManager tickRateManager;
    public static ServerManager serverManager;
    public static SpeedManager speedManager;
    public static RenderRotationsManager renderRotationManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static PotionManager potionManager;
    public static TargetManager targetManager;
    public static InteractionManager interactionManager;
    public static InventoryManager inventoryManager;
    public static FixManager fixManager;
    public static WayPointManager wayPointManager;
    public static PlaceManager placeManager;
    public static FpsManagemer fpsManagemer;
    public static ThreadManager threadManager;
    public static NCPManager ncpManager;
    public static final HoleManager holeManager = new HoleManager();

    private String name;
    public static MotionEvent motionEvent;

    protected static PlayerList playerList;

    public Client() {
    }

    public Client(String name) {
        this.name = name;
    }


    public String getName() {
        return this.name;
    }


    protected static boolean nullCheck() {
        return mc.world == null;
    }

    protected static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
//    public static void sendError(Throwable error) {
//        IOUtil.sendMessageError(error.getMessage());
//    }

    public static void setPlayerList(PlayerList playerList1) {
        playerList = playerList1;
    }

}
