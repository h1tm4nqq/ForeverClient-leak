package we.devs.forever.client.command.impl.commands;

import we.devs.forever.api.manager.impl.render.WayPoint;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("WayPoint", new SyntaxChunk("<add/list/del>") {
                    @Override
                    public String predict(String currentArg) {
                        if (currentArg.startsWith("l")) return "list";
                        if (currentArg.startsWith("a")) return "add";
                        if (currentArg.startsWith("d")) return "del";
                        return currentArg;
                    }
                },
                new SyntaxChunk("<name>")
        );
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length >= 1) {
            if ("list".equals(commands[0])) {
                StringBuilder builder = new StringBuilder();
                wayPointManager.wayPoints.forEach(wayPoint ->{
                            String[] strings = wayPoint.toString().split("§");
                            String[] string = strings[2].split(":");
                            builder.append("Name: ").append(strings[0]).append(", Server: ").append(strings[1])
                                    .append(", Coords XYZ: ").append(string[0]).append(", ").append(string[1]).append(", ").append(string[2]).append(".")
                                    .append("\n");
                        });
                sendMessage(builder.toString());
            }
        }
        if (commands.length >= 2) {
            if ("add".equals(commands[0])) {
                String server;
                if(mc.currentServerData != null){
                    server = mc.currentServerData.serverIP;
                }else {
                    server = "offline";
                }

                wayPointManager.wayPoints.add(
                        new WayPoint(commands[1],
                                server,
                                mc.player.posX + ":" +
                                        mc.player.posY + ":" +
                                        mc.player.posZ,mc.world.getBiome(mc.player.getPosition()).getBiomeName()));
                sendMessage("Waypoint %s added .",commands[1]);
            }
            if ("del".equals(commands[0])) {
                wayPointManager.wayPoints.forEach(wayPoint ->{
                    if(wayPoint.getName().equals(commands[1])){
                        wayPointManager.wayPoints.remove(wayPoint);
                        sendMessage("Waypoint %s removed.",commands[1]);
                    }
                });


            }
        }
    }
}
