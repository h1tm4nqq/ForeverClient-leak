package we.devs.forever.client.modules.impl.chat;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class StashLogger extends Module {
    final Iterator<NBTTagCompound> iterator;
    public Setting<Boolean> chests = (new Setting<>("Chests", true, ""));
    public Setting<Integer> chestsValue = (new Setting<>("ChestsValue", 4, 1, 30, v -> this.chests.getValue()));
    public Setting<Boolean> echests = (new Setting<>("EnderChests", true));
    public Setting<Integer> echestsValue = (new Setting<>("EnderChestsValue", 4, 1, 30, v -> this.echests.getValue()));
    public Setting<Boolean> shulkers = (new Setting<>("Shulkers", true));
    public Setting<Integer> shulkersValue = (new Setting<>("ShulkersValue", 4, 1, 30, v -> this.shulkers.getValue()));
    public Setting<Boolean> writeToFile = (new Setting<>("CoordsSaver", true));
    File mainFolder;

    public StashLogger() {
        super("StashLogger", "Notifies when storages are found", Category.CHAT);
        this.iterator = null;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!nullCheck()) {
            if (event.getPacket() instanceof SPacketChunkData) {
                SPacketChunkData l_Packet = event.getPacket();
                int l_ChestsCount = 0;
                int shulkers = 0;
                int enderChests = 0;
                Iterator<NBTTagCompound> var5 = l_Packet.getTileEntityTags().iterator();
                while (true) {
                    while (var5.hasNext()) {
                        NBTTagCompound l_Tag = var5.next();
                        String l_Id = l_Tag.getString("id");
                        if (l_Id.equals("minecraft:chest") && (this.chests.getValue())) {
                            ++l_ChestsCount;
                        } else if (l_Id.equals("minecraft:ender_chest") && (this.echests.getValue())) {
                            ++enderChests;
                        } else if (l_Id.equals("minecraft:shulker_box") && (this.shulkers.getValue())) {
                            ++shulkers;
                        }
                    }
                    if (l_ChestsCount >= this.chestsValue.getValue()) {
                        this.SendMessage(String.format("%s chests located at X: %s, Z: %s", l_ChestsCount, l_Packet.getChunkX() * 16, l_Packet.getChunkZ() * 16), true);
                    }
                    if (enderChests >= this.echestsValue.getValue()) {
                        this.SendMessage(String.format("%s ender chests located at X: %s, Z: %s", enderChests, l_Packet.getChunkX() * 16, l_Packet.getChunkZ() * 16), true);
                    }
                    if (shulkers >= this.shulkersValue.getValue()) {
                        this.SendMessage(String.format("%s shulker boxes at X: %s, Z: %s", shulkers, l_Packet.getChunkX() * 16, l_Packet.getChunkZ() * 16), true);
                    }
                    break;
                }
            }

        }
    }

    private void SendMessage(String message, boolean save) {
        String server = Minecraft.getMinecraft().isSingleplayer() ? "singleplayer".toUpperCase() : Objects.requireNonNull(mc.getCurrentServerData()).serverIP;
        if ((this.writeToFile.getValue()) && save) {
            try {
                FileWriter writer = new FileWriter(this.mainFolder + "Forever/util/Stashes.txt", true);
                writer.write("[" + server + "]: " + message + "\n");
                writer.close();
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }
        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F));
        Command.sendMessage(ChatFormatting.GREEN + message);
    }
}
