package we.devs.forever.client.modules.impl.misc;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class F3Spoofer extends Module {
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Hide);
    public Setting<Coords> coords = new Setting<>("Coords", Coords.Normal, "Hides your coords in f3 menu");
    public Setting<Boolean> block = new Setting<>("Block", true, "Hides the coords of block you look at in f3 menu", v -> coords.getValue() != Coords.None);
    public Setting<Boolean> chunk = new Setting<>("Chunk", true, "Hides chunk coords in f3 menu", v -> coords.getValue() != Coords.None);
    public Setting<Boolean> fps1 = new Setting<>("FPS", true, "Hides fps in f3 menu");
    public Setting<Boolean> direction = new Setting<>("Direction", true, "Hides your direction in f3 menu");
    public Setting<Boolean> biome = new Setting<>("Biome", true, "Hides the biome where are you in f3 menu");
   // public Setting<Boolean> system = new Setting<>("System", true);

    public F3Spoofer() {
        super("F3Spoofer", "Spoof f3 menu", Category.MISC);
        spoofX = (int) Math.sqrt(ThreadLocalRandom.current().nextInt());
        spoofZ = (int) Math.sqrt(ThreadLocalRandom.current().nextInt());
        spoofMode = ThreadLocalRandom.current().nextInt(5);
        System.out.println(spoofMode);


    }

    private int spoofMode;
    private int spoofX;
    private int spoofZ;
    Random random = ThreadLocalRandom.current();
    TimerUtil timerUtil = new TimerUtil();
    private long randomX;
    private long randomY;
    private long randomZ;
    private int fps;
    private int height;
    private int width;
    private EnumFacing enumFacing;

    @EventListener
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (mc.gameSettings.showDebugInfo) {
            for (int i = 0; i < event.getLeft().size(); i++) {
                if (coords.getValue() != Coords.None) {
                    if (event.getLeft().get(i).contains("Looking"))
                        event.getLeft().set(i, "look at porn");
                    if (event.getLeft().get(i).contains("XYZ")) {
                        if (coords.getValue() == Coords.Normal) {
                            if (mode.getValue() == Mode.Random) {
                                event.getLeft().set(i, "XYZ: " + randomX + " / " + randomY + " / " + randomZ);
                            } else {
                                event.getLeft().set(i, "XYZ: idk(");
                            }
                        } else {
                            switch (spoofMode) {
                                case 0: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", mc.player.posX - spoofX * Math.PI) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - spoofZ * Math.E));
                                    break;
                                }
                                case 1: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", Math.abs(mc.player.posX + spoofX * Math.sqrt(spoofX))) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", Math.abs(mc.player.posZ + spoofZ * Math.sqrt(spoofZ))));
                                    break;
                                }
                                case 2: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", mc.player.posX - (spoofX * spoofZ)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - (spoofX * spoofZ)));
                                    break;
                                }
                                case 3: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", mc.player.posX + spoofX * spoofZ) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ + spoofX * Math.sqrt(spoofX)));
                                    break;
                                }
                                case 4: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", mc.player.posZ - spoofX * Math.sin(spoofX) == 1 || Math.sin(spoofX) == -1 ? .34837467347 : Math.sin(spoofX)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posX + spoofZ * Math.cos(spoofZ) == 1 || Math.cos(spoofZ) == -1 ? .32834872347 : Math.cos(spoofZ)));
                                    break;
                                }
                                case 5: {
                                    event.getLeft().set(i, "XYZ: "
                                            + String.format("%.3f", mc.player.posX + Math.sqrt(spoofX)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - Math.sqrt(spoofZ)));
                                    break;
                                }
                            }


                        }
                    }
                    if (event.getLeft().get(i).contains("Block:") && block.getValue()) {
                        if (coords.getValue() == Coords.Normal) {
                            event.getLeft().set(i, "Block: хз!");
                        } else {
                            switch (spoofMode) {
                                case 0: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", mc.player.posX - spoofX * Math.PI) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - spoofZ * Math.E));
                                    break;
                                }
                                case 1: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", Math.abs(mc.player.posX + spoofX * Math.sqrt(spoofX))) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", Math.abs(mc.player.posZ + spoofZ * Math.sqrt(spoofZ))));
                                    break;
                                }
                                case 2: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", mc.player.posX - (spoofX * spoofZ)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - (spoofX * spoofZ)));
                                    break;
                                }
                                case 3: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", mc.player.posX + spoofX * spoofZ) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ + spoofX * Math.sqrt(spoofX)));
                                    break;
                                }
                                case 4: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", mc.player.posZ - spoofX * Math.sin(spoofX) == 1 || Math.sin(spoofX) == -1 ? .34837467347 : Math.sin(spoofX)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posX + spoofZ * Math.cos(spoofZ) == 1 || Math.cos(spoofZ) == -1 ? .32834872347 : Math.cos(spoofZ)));
                                    break;
                                }
                                case 5: {
                                    event.getLeft().set(i, "Block: "
                                            + String.format("%.3f", mc.player.posX + Math.sqrt(spoofX)) + " / "
                                            + String.format("%.3f", mc.player.posY) + " / "
                                            + String.format("%.3f", mc.player.posZ - Math.sqrt(spoofZ)));
                                    break;
                                }
                            }
                        }
                    }

                    if (event.getLeft().get(i).contains("Chunk:") && chunk.getValue())
                        event.getLeft().set(i, "Chunk: Я ебу?");
                }
                if (fps1.getValue())
                    if (event.getLeft().get(i).contains("fps")) {
                        if (mode.getValue() == Mode.Random) {
                            event.getLeft().set(i, "fps: " + fps);
                        } else {
                            event.getLeft().set(i, "fps: large");
                        }
                    }
                if (direction.getValue())
                    if (event.getLeft().get(i).contains("Facing:")) {
                        if (mode.getValue() == Mode.Random) {
                            event.getLeft().set(i, "Facing: " + enumFacing);
                        } else {
                            event.getLeft().set(i, "Facing: facing");
                        }
                    }

                if (biome.getValue())
                    if (event.getLeft().get(i).contains("Biome:"))
                        event.getLeft().set(i, "Biome: cosmos!");
//                if (system.getValue()) {
//                    if (event.getRight().get(i).contains("Display:")) {
//                        if (mode.getValue() == Mode.Random) {
//                            event.getRight().set(i, "Display: " + width + "x " + height);
//                        } else {
//                            event.getRight().set(i, "Display: square");
//                        }
//
//                        continue;
//                    }
//                    if (event.getRight().get(i).contains("CPU:")) {
//                        event.getRight().set(i, "CPU: 256x NVIDIA");
//                        continue;
//                    }
//                    if (event.getRight().get(i).contains("NVIDIA") || event.getRight().get(i).contains("AMD") || event.getRight().get(i).contains("PCIe")) {
//                        event.getRight().set(i, "Potato!");
//                    }
//                }
            }
        }

    }

    @Override
    public void onTick() {
        if (mc.gameSettings.showDebugInfo && mode.getValue() == Mode.Random && timerUtil.passedMs(1000)) {
            randomX = random.nextLong();
            randomY = random.nextLong();
            randomZ = random.nextLong();
            fps = random.nextInt();
            height = random.nextInt();
            width = random.nextInt();
            EnumFacing[] temp = EnumFacing.values();
            enumFacing = temp[random.nextInt(temp.length)];
        }
    }

    public enum Mode {
        Hide,
        Random;
    }

    public enum Coords {
        None,
        Spoof,
        Normal
    }
}
