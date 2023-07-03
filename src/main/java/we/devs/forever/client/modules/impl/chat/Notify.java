package we.devs.forever.client.modules.impl.chat;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Notify extends Module {
    public static Notify INSTANCE;
    private final TimerUtil timer;
    private final ConcurrentHashMap<Object, Object> players = new ConcurrentHashMap<>();
    private final Map<EntityPlayer, Integer> entityArmorArraylist = new HashMap<>();
    private final HashMap<Entity, Vec3d> knownPlayers = new HashMap<>();

    public static Setting<Boolean> rainbowPrefix = (new Setting<>("RainbowPrefix", true));
    public static Setting<TextUtil.Color> bracketColor = (new Setting<>("BracketColor", TextUtil.Color.DARK_BLUE, v -> !rainbowPrefix.getValue()));
    public static Setting<TextUtil.Color> commandColor = (new Setting<>("ForeverColor", TextUtil.Color.BLUE, v -> !rainbowPrefix.getValue()));

    public Setting<Boolean> moduleMessage = (new Setting<>("ModuleMessage", true, "ModuleMessage"));
    public Setting<Boolean> watermark = (new Setting<>("Watermark", true,"When watermark is on", v -> this.moduleMessage.getValue()));
    public Setting<Boolean> italic = (new Setting<>("ItalicMsgs", false, v -> this.moduleMessage.getValue()));
    public Setting<Boolean> history = (new Setting<>("History", false, v -> this.moduleMessage.getValue()));
    public Setting<Boolean> armor = (new Setting<>("Armor", false));
    public Setting<Integer> armorThreshhold = (new Setting<>("Armor%", 20, 1, 100, v -> this.armor.getValue()));
    public Setting<Boolean> notifySelf = (new Setting<>("NotifySelf", false, v -> this.armor.getValue()));
    public Setting<Boolean> notification = (new Setting<>("Notification", false, v -> this.armor.getValue()));
    public Setting<Boolean> deathCoords = (new Setting<>("DeathCoords", false, "When you died the client remembered your death coords"));
    public Setting<Boolean> holeBreak = (new Setting<>("HoleBreak", false, "When the client saw that player breaks hole"));
    public Setting<Boolean> pearls = (new Setting<>("Pearl", true, "When the client saw that player threw pearl"));
    public Setting<Boolean> burrow = (new Setting<>("Burrow", false, "When the client saw that the player in burrow"));
    public Setting<Boolean> exploits = (new Setting<>("Exploits", false));
    public Setting<Boolean> tp = (new Setting<>("TP", true, "When the client heard", v -> this.exploits.getValue()));
    public Setting<Boolean> lightning = (new Setting<>("Thunder", true, "When the client heard thunder", v -> this.exploits.getValue()));
    public Setting<Boolean> portal = (new Setting<>("EndPortal", true, "When the client heard end portal", v -> this.exploits.getValue()));
    public Setting<Boolean> wither = (new Setting<>("Wither", true, "When the client heard wither", v -> this.exploits.getValue()));
    public Setting<Boolean> dragon = (new Setting<>("Dragon", true, "When the client heard Dragon", v -> this.exploits.getValue()));
    public Setting<Boolean> animals = (new Setting<>("Animals", false, "When the client found animals "));
    public Setting<Boolean> donkeys = (new Setting<>("Donkeys", false, "When the client found donkey", v -> this.animals.getValue()));
    public Setting<Boolean> llamas = (new Setting<>("Llamas", false, "When the client found llamas", v -> this.animals.getValue()));
    public Setting<Boolean> mules = (new Setting<>("Mules", false, "When the client found mules", v -> this.animals.getValue()));
    public Setting<Boolean> slimes = (new Setting<>("Slimes", false, "When the client found slimes", v -> this.animals.getValue()));
    public TimerUtil totemAnnounce = new TimerUtil();
    List<Entity> burrowedPlayers = new ArrayList<>();
    List anti_spam = new ArrayList<>();
    int delay;
    private boolean flag;

    public Notify() {
        super("Notify", "Logging", Category.CHAT);
        this.timer = new TimerUtil();
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        delay = 0;
        if (this.burrow.getValue()) {
            this.players.clear();
            this.anti_spam.clear();
        }
    }




    @Override
    public void onTick() {
        if (nullCheck()) return;
        if (timer.passedMs(2100)) {
            timer.reset();
        }
        if (mc.currentScreen instanceof GuiGameOver) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
        if (mc.currentScreen instanceof GuiGameOver && this.timer.getPassedTimeMs() > 200) {
            if (deathCoords.getValue()) {
                Command.sendMessage(ChatFormatting.GRAY + "[DeathCoords] " + ChatFormatting.WHITE + (int) mc.player.posX + " " + (int) mc.player.posY + " " + (int) mc.player.posZ);
            }
            timer.reset();
        }
        if (mc.currentScreen instanceof GuiGameOver && this.timer.getPassedTimeMs() > 1000) {
            if (deathCoords.getValue()) {
                Command.sendMessage(ChatFormatting.GRAY + "[DeathCoords] " + ChatFormatting.WHITE + (int) mc.player.posX + " " + (int) mc.player.posY + " " + (int) mc.player.posZ);
            }
            timer.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (this.exploits.getValue()) {
            if (!this.tp.getValue() || mc.player == null) {
                return;
            }
            final List<Entity> tickEntityList = mc.world.getLoadedEntityList();
            for (final Entity entity : tickEntityList) {
                if (entity instanceof EntityPlayer && !entity.getName().equals(mc.player.getName())) {
                    final Vec3d targetPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                    if (this.knownPlayers.containsKey(entity)) {
                        if (Math.abs(mc.player.getPositionVector().distanceTo(targetPos)) >= 128.0 && this.knownPlayers.get(entity).distanceTo(targetPos) >= 64.0) {
                            Command.sendMessage(ChatFormatting.RED + "Player " + entity.getName() + " moved to Position " + targetPos);
                        }
                    }
                    this.knownPlayers.put(entity, targetPos);
                }
            }
        }
        if (delay > 0) {
            --delay;
        }
        if (donkeys.getValue()) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity instanceof EntityDonkey && delay == 0) {
                    Command.sendMessage("Found a donkey at: " + Math.round(entity.lastTickPosX) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity.lastTickPosY) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity.lastTickPosZ));
                    delay = 200;
                }
                if (llamas.getValue()) {
                    mc.world.loadedEntityList.forEach(entity2 -> {
                        if (entity2 instanceof EntityLlama && delay == 0) {
                            Command.sendMessage("Found a llama at: " + Math.round(entity2.lastTickPosX) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity2.lastTickPosY) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity2.lastTickPosZ));
                            delay = 200;
                        }
                        if (mules.getValue()) {
                            mc.world.loadedEntityList.forEach(entity3 -> {
                                if (entity3 instanceof EntityMule && delay == 0) {
                                    Command.sendMessage("Found a mule at: " + Math.round(entity3.lastTickPosX) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity3.lastTickPosY) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity3.lastTickPosZ));
                                    delay = 200;
                                }
                                if (slimes.getValue()) {
                                    mc.world.loadedEntityList.forEach(entity4 -> {
                                        if (entity4 instanceof EntitySlime && delay == 0) {
                                            Command.sendMessage("Found a slime at: " + Math.round(entity4.lastTickPosX) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity4.lastTickPosY) + ChatFormatting.GRAY + ", " + ChatFormatting.WHITE + Math.round(entity4.lastTickPosZ));
                                            delay = 200;
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
        if (this.pearls.getValue()) {
            if (Notify.mc.world == null || Notify.mc.player == null) {
                return;
            }
            Entity enderPearl = null;
            for (Entity e : Notify.mc.world.loadedEntityList) {
                if (!(e instanceof EntityEnderPearl)) continue;
                enderPearl = e;
                break;
            }
            if (enderPearl == null) {
                this.flag = true;
                return;
            }
            EntityPlayer closestPlayer = null;
            for (EntityPlayer entity : Notify.mc.world.playerEntities) {
                if (closestPlayer == null) {
                    closestPlayer = entity;
                    continue;
                }
                if (closestPlayer.getDistance(enderPearl) <= entity.getDistance(enderPearl)) continue;
                closestPlayer = entity;
            }
            if (closestPlayer == Notify.mc.player) {
                this.flag = false;
            }
            if (closestPlayer != null && this.flag) {
                String facing = enderPearl.getHorizontalFacing().toString();
                if (facing.equals("west")) {
                    facing = "west(X+)";
                } else if (facing.equals("east")) {
                    facing = "east(X-)";
                } else if (facing.equals("north")) {
                    facing = "north(Z-)";
                } else if (facing.equals("south")) {
                    facing = "south(Z+)";
                }
                Command.sendSilentMessage(friendManager.isFriend(closestPlayer.getName()) ? ChatFormatting.AQUA + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + facing + "!" : ChatFormatting.RED + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + facing + "!");
                this.flag = false;
            }
        }
        if (this.burrow.getValue()) {
            if (mc.player != null && mc.world != null) {
                Iterator var1 = ((List) mc.world.loadedEntityList.stream().filter((e) -> e instanceof EntityPlayer).collect(Collectors.toList())).iterator();

                while (true) {
                    while (true) {
                        Entity entity;
                        do {
                            if (!var1.hasNext()) {
                                return;
                            }

                            entity = (Entity) var1.next();
                        } while (!(entity instanceof EntityPlayer));

                        if (!this.burrowedPlayers.contains(entity) && this.isBurrowed(entity)) {
                            this.burrowedPlayers.add(entity);
                            Command.sendMessage(ChatFormatting.RED + entity.getName() + " has just burrowed!");
                        } else if (this.burrowedPlayers.contains(entity) && !this.isBurrowed(entity)) {
                            this.burrowedPlayers.remove(entity);
                            Command.sendMessage(ChatFormatting.GREEN + entity.getName() + " is no longer burrowed!");
                        }
                    }
                }
            }
        }
        if (this.armor.getValue()) {
            for (final EntityPlayer player : Notify.mc.world.playerEntities) {
                if (!player.isDead) {
                    if (!friendManager.isFriend(player.getName())) {
                        continue;
                    }
                    for (final ItemStack stack : player.inventory.armorInventory) {
                        if (stack != ItemStack.EMPTY) {
                            final int percent = DamageUtil.getRoundedDamage(stack);
                            if (percent <= this.armorThreshhold.getValue() && !this.entityArmorArraylist.containsKey(player)) {
                                if (player == Notify.mc.player && this.notifySelf.getValue()) {
                                    Command.sendMessage(player.getName() + " watchout your " + this.getArmorPieceName(stack) + " low dura!", this.notification.getValue());
                                } else {
                                    Notify.mc.player.sendChatMessage("/msg " + player.getName() + " " + player.getName() + " watchout your " + this.getArmorPieceName(stack) + " low dura!");
                                }
                                this.entityArmorArraylist.put(player, player.inventory.armorInventory.indexOf(stack));
                            }
                            if (!this.entityArmorArraylist.containsKey(player) || this.entityArmorArraylist.get(player) != player.inventory.armorInventory.indexOf(stack) || percent <= this.armorThreshhold.getValue()) {
                                continue;
                            }
                            this.entityArmorArraylist.remove(player);
                        }
                    }
                    if (!this.entityArmorArraylist.containsKey(player) || player.inventory.armorInventory.get(this.entityArmorArraylist.get(player)) != ItemStack.EMPTY) {
                        continue;
                    }
                    this.entityArmorArraylist.remove(player);
                }
            }
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (this.exploits.getValue()) {
            if (this.lightning.getValue() && event.getPacket() instanceof SPacketSoundEffect) {
                SPacketSoundEffect packet = event.getPacket();
                if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER) {
                    Command.sendMessage(ChatFormatting.RED + "Lightning spawned at X" + packet.getX() + " Z" + packet.getZ());
                }
            }

            if (event.getPacket() instanceof SPacketEffect) {
                SPacketEffect packet2 = event.getPacket();
                if (this.portal.getValue() && packet2.getSoundType() == 1038) {
                    Command.sendMessage(ChatFormatting.RED + "End Portal activated at X" + packet2.getSoundPos().getX() + " Y" + packet2.getSoundPos().getY() + " Z" + packet2.getSoundPos().getZ());
                }
                if (this.wither.getValue() && packet2.getSoundType() == 1023) {
                    Command.sendMessage(ChatFormatting.RED + "Wither spawned at X" + packet2.getSoundPos().getX() + " Y" + packet2.getSoundPos().getY() + " Z" + packet2.getSoundPos().getZ());
                }
                if (this.dragon.getValue() && packet2.getSoundType() == 1028) {
                    Command.sendMessage(ChatFormatting.RED + "Dragon killed at X" + packet2.getSoundPos().getX() + " Y" + packet2.getSoundPos().getY() + " Z" + packet2.getSoundPos().getZ());
                }
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (this.holeBreak.getValue()) {
            if (event.getPacket() instanceof SPacketBlockBreakAnim) {
                SPacketBlockBreakAnim packet = event.getPacket();
                if (isHoleBlock(packet.getPosition())) {
                    TextComponentString text = new TextComponentString(commandManager.getClientMessage() + "The hole block to your " + getBlockDirectionFromPlayer(packet.getPosition()) + " is being broken by " + Objects.requireNonNull(mc.world.getEntityByID(packet.getBreakerId())).getName());
                    Module.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
                }
            }
        }
    }

    public String getBlockDirectionFromPlayer(BlockPos pos) {
        if (this.holeBreak.getValue()) {
            double posX = Math.floor(mc.player.posX);
            double posZ = Math.floor(mc.player.posZ);
            double x = posX - pos.getX();
            double z = posZ - pos.getZ();
            switch (mc.player.getHorizontalFacing()) {
                case SOUTH:
                    if (x == 1) {
                        return "right";
                    } else if (x == -1) {
                        return "left";
                    } else if (z == 1) {
                        return "back";
                    } else if (z == -1) {
                        return "front";
                    }
                    break;
                case WEST:
                    if (x == 1) {
                        return "front";
                    } else if (x == -1) {
                        return "back";
                    } else if (z == 1) {
                        return "right";
                    } else if (z == -1) {
                        return "left";
                    }
                    break;
                case NORTH:
                    if (x == 1) {
                        return "left";
                    } else if (x == -1) {
                        return "right";
                    } else if (z == 1) {
                        return "front";
                    } else if (z == -1) {
                        return "back";
                    }
                    break;
                case EAST:
                    if (x == 1) {
                        return "back";
                    } else if (x == -1) {
                        return "front";
                    } else if (z == 1) {
                        return "left";
                    } else if (z == -1) {
                        return "right";
                    }
                    break;
                default:
                    return "undetermined";
            }
            return null;
        }
        return null;
    }

    private boolean isHoleBlock(BlockPos pos) {
        if (this.holeBreak.getValue()) {
            double posX = Math.floor(mc.player.posX);
            double posZ = Math.floor(mc.player.posZ);
            Block block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN) {
                if (pos.getX() == (posX + 1) && pos.getY() == mc.player.getPosition().getY()) {
                    return true;
                } else if (pos.getX() == (posX - 1) && pos.getY() == mc.player.getPosition().getY()) {
                    return true;
                } else if (pos.getZ() == (posZ + 1) && pos.getY() == mc.player.getPosition().getY()) {
                    return true;
                } else return pos.getZ() == (posZ - 1) && pos.getY() == mc.player.getPosition().getY();
            }
            return false;
        }
        return false;
    }

    private boolean isBurrowed(Entity entity) {
        BlockPos entityPos = new BlockPos(this.roundValueToCenter(entity.posX), entity.posY + 0.2D, this.roundValueToCenter(entity.posZ));
        return mc.world.getBlockState(entityPos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(entityPos).getBlock() == Blocks.ENDER_CHEST;
    }

    private double roundValueToCenter(double inputVal) {
        double roundVal = (double) Math.round(inputVal);
        if (roundVal > inputVal) {
            roundVal -= 0.5D;
        } else if (roundVal <= inputVal) {
            roundVal += 0.5D;
        }

        return roundVal;
    }

    private String getArmorPieceName(final ItemStack stack) {
        if (this.armor.getValue()) {
            if (stack.getItem() == Items.DIAMOND_HELMET || stack.getItem() == Items.GOLDEN_HELMET || stack.getItem() == Items.IRON_HELMET || stack.getItem() == Items.CHAINMAIL_HELMET || stack.getItem() == Items.LEATHER_HELMET) {
                return "helmet is";
            }
            if (stack.getItem() == Items.DIAMOND_CHESTPLATE || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_CHESTPLATE || stack.getItem() == Items.LEATHER_CHESTPLATE) {
                return "chestplate is";
            }
            if (stack.getItem() == Items.DIAMOND_LEGGINGS || stack.getItem() == Items.GOLDEN_LEGGINGS || stack.getItem() == Items.IRON_LEGGINGS || stack.getItem() == Items.CHAINMAIL_LEGGINGS || stack.getItem() == Items.LEATHER_LEGGINGS) {
                return "leggings are";
            }
            if (stack.getItem() == Items.DIAMOND_BOOTS || stack.getItem() == Items.GOLDEN_BOOTS || stack.getItem() == Items.IRON_BOOTS || stack.getItem() == Items.CHAINMAIL_BOOTS || stack.getItem() == Items.LEATHER_BOOTS) {
                return "boots are";
            }
            return null;
        }
        return null;
    }
}