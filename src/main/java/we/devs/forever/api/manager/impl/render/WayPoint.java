package we.devs.forever.api.manager.impl.render;

import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.Util;

public class WayPoint implements Util {

    private final String name;
    private final String server;
    private final String cords;

    public String getWorld() {
        return world;
    }

    private final String world;
    private final BlockPos pos;
    private final WorldType worldType;

    public WayPoint(String name, String server, String cords, String world) {
        this.name = name;
        this.server = server;
        this.cords = cords;
        this.world = world;
        if(mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell")){
            worldType = WorldType.HELL;
        } else if(mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("End")){
            worldType = WorldType.END;
        } else {
            worldType = WorldType.OVERWORLD;
        }
        String[] c = cords.split(":");
        pos = new BlockPos(Double.parseDouble(c[0]),Double.parseDouble(c[1]),Double.parseDouble(c[2]));
    }
    public BlockPos getPos() {
        return pos;
    }

    public String getCords() {
        return cords;
    }

    public String getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + "§" +
                server + "§" +
                cords + "§" +
                world;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public enum WorldType{
        END,
        OVERWORLD,
        HELL
    }
}
