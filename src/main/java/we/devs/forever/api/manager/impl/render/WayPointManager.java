package we.devs.forever.api.manager.impl.render;


import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.manager.impl.config.ConfigManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class WayPointManager extends AbstractManager {
    public final Set<WayPoint> wayPoints = new HashSet<>();
    File wayPointFile = new File(ConfigManager.ForeverFolder, "waypoints.json");

    public WayPointManager() {
        super("WayPointManager");
    }

    @Override
    protected void onLoad() {
        if (!wayPointFile.exists()) {
            try {
                wayPointFile.createNewFile();
            } catch (IOException ignored) {
            }
        }


        try {
            Scanner sc = new Scanner(wayPointFile);
            while(sc.hasNextLine()){
                String s = sc.nextLine();
                System.out.println(s);
                String[] strs = s.split("§");
                wayPoints.add(new WayPoint(strs[0], strs[1], strs[2], strs[3]));
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    protected void onUnload() {
        if (!wayPointFile.exists()) {
            try {
                wayPointFile.createNewFile();
            } catch (IOException ignored) {
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(wayPointFile);
            for (WayPoint wayPoint : wayPoints) {
                fileWriter.write(wayPoint.toString() + "\n");
            }
            fileWriter.close();
        } catch (IOException ignored) {
        }
    }
}
