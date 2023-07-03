package we.devs.forever.client.modules.impl.misc;


import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.StringUtils;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.main.ForeverClient;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PacketLogger
        extends Module {
    private final Setting<Mode> mode = (new Setting<Mode>("Packets", Mode.Client));
    public final Setting<Boolean> logdata = (new Setting<>("LogData", true, v -> mode.getValue() == Mode.Main));
    public final Setting<Boolean> savefile = (new Setting<>("SaveFile", true, v -> mode.getValue() == Mode.Main));
    public final Setting<Boolean> showinchat = (new Setting<>("ShowInChat", true, v -> mode.getValue() == Mode.Main));
    public final Setting<Boolean> cancel = (new Setting<>("Cancel", true, v -> mode.getValue() == Mode.Main));
    private final Setting<Integer> page = (new Setting<>("SPackets", 1, 1, 10, v -> mode.getValue() == Mode.Server));
    private final Setting<Boolean> AdvancementInfo = (new Setting<>("AdvancementInfo", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> Animation = (new Setting<>("Animation", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> BlockAction = (new Setting<>("BlockAction", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> BlockBreakAnim = (new Setting<>("BlockBreakAnim", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> BlockChange = (new Setting<>("BlockChange", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> Camera = (new Setting<>("Camera", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> ChangeGameState = (new Setting<>("ChangeGameState", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> Chat = (new Setting<>("Chat", false, v -> mode.getValue() == Mode.Server && page.getValue() == 1));
    private final Setting<Boolean> ChunkData = (new Setting<>("ChunkData", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> CloseWindow = (new Setting<>("CloseWindow", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> CollectItem = (new Setting<>("CollectItem", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> CombatEvent = (new Setting<>("Combatevent", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> ConfirmTransaction = (new Setting<>("ConfirmTransaction", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> Cooldown = (new Setting<>("Cooldown", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> CustomPayload = (new Setting<>("CustomPayload", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> CustomSound = (new Setting<>("CustomSound", false, v -> mode.getValue() == Mode.Server && page.getValue() == 2));
    private final Setting<Boolean> DestroyEntities = (new Setting<>("DestroyEntities", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> Disconnect = (new Setting<>("Disconnect", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> DisplayObjective = (new Setting<>("DisplayObjective", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> Effect = (new Setting<>("Effect", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> Entity = (new Setting<>("Entity", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> EntityAttach = (new Setting<>("EntityAttach", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> EntityEffect = (new Setting<>("EntityEffect", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> EntityEquipment = (new Setting<>("EntityEquipment", false, v -> mode.getValue() == Mode.Server && page.getValue() == 3));
    private final Setting<Boolean> EntityHeadLook = (new Setting<>("EntityHeadLook", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> EntityMetadata = (new Setting<>("EntityMetadata", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> EntityProperties = (new Setting<>("EntityProperties", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> EntityStatus = (new Setting<>("EntityStatus", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> EntityTeleport = (new Setting<>("EntityTeleport", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> EntityVelocity = (new Setting<>("EntityVelocity", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> Explosion = (new Setting<>("Explosion", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> HeldItemChange = (new Setting<>("HeldItemChange", false, v -> mode.getValue() == Mode.Server && page.getValue() == 4));
    private final Setting<Boolean> JoinGame = (new Setting<>("JoinGame", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> KeepAlive = (new Setting<>("KeepAlive", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> Maps = (new Setting<>("Maps", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> MoveVehicle = (new Setting<>("MoveVehicle", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> MultiBlockChange = (new Setting<>("MultiBlockChange", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> OpenWindow = (new Setting<>("OpenWindow", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> Particles = (new Setting<>("Particles", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> PlaceGhostRecipe = (new Setting<>("PlaceGhostRecipe", false, v -> mode.getValue() == Mode.Server && page.getValue() == 5));
    private final Setting<Boolean> PlayerAbilities = (new Setting<>("PlayerAbilities", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> PlayerListHeaderFooter = (new Setting<>("PlayerListHeaderFooter", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> PlayerListItem = (new Setting<>("PlayerListItem", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> PlayerPosLook = (new Setting<>("PlayerPosLook", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> RecipeBook = (new Setting<>("RecipeBook", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> RemoveEntityEffect = (new Setting<>("RemoveEntityEffect", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> ResourcePackSend = (new Setting<>("ResourcePackSend", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> Respawn = (new Setting<>("Respawn", false, v -> mode.getValue() == Mode.Server && page.getValue() == 6));
    private final Setting<Boolean> ScoreboardObjective = (new Setting<>("ScoreboardObjective", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SelectAdvancementsTab = (new Setting<>("SelectAdvancementsTab", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> ServerDifficulty = (new Setting<>("ServerDifficulty", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SetExperience = (new Setting<>("SetExperience", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SetPassengers = (new Setting<>("SetPassengers", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SetSlot = (new Setting<>("SetSlot", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SignEditorOpen = (new Setting<>("SignEditorOpen", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SoundEffect = (new Setting<>("SoundEffect", false, v -> mode.getValue() == Mode.Server && page.getValue() == 7));
    private final Setting<Boolean> SpawnExperienceOrb = (new Setting<>("SpawnExperienceOrb", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnGlobalEntity = (new Setting<>("SpawnGlobalEntity", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnMob = (new Setting<>("SpawnMob", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnObject = (new Setting<>("SpawnObject", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnPainting = (new Setting<>("SpawnPainting", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnPlayer = (new Setting<>("SpawnPlayer", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> SpawnPosition = (new Setting<>("SpawnPosition", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> Statistics = (new Setting<>("Statistics", false, v -> mode.getValue() == Mode.Server && page.getValue() == 8));
    private final Setting<Boolean> TabComplete = (new Setting<>("TabComplete", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> Teams = (new Setting<>("Teams", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> TimeUpdate = (new Setting<>("TimeUpdate", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> Title = (new Setting<>("Title", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> UnloadChunk = (new Setting<>("UnloadChunk", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> UpdateBossInfo = (new Setting<>("UpdateBossInfo", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> UpdateHealth = (new Setting<>("UpdateHealth", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> UpdateScore = (new Setting<>("UpdateScore", false, v -> mode.getValue() == Mode.Server && page.getValue() == 9));
    private final Setting<Boolean> UpdateTileEntity = (new Setting<>("UpdateTileEntity", false, v -> mode.getValue() == Mode.Server && page.getValue() == 10));
    private final Setting<Boolean> UseBed = (new Setting<>("UseBed", false, v -> mode.getValue() == Mode.Server && page.getValue() == 10));
    private final Setting<Boolean> WindowItems = (new Setting<>("WindowItems", false, v -> mode.getValue() == Mode.Server && page.getValue() == 10));
    private final Setting<Boolean> WindowProperty = (new Setting<>("WindowProperty", false, v -> mode.getValue() == Mode.Server && page.getValue() == 10));
    private final Setting<Boolean> WorldBorder = (new Setting<>("WorldBorder", false, v -> mode.getValue() == Mode.Server && page.getValue() == 10));
    private final Setting<Boolean> PlayerDigging = (new Setting<>("PlayerDigging", false, v -> mode.getValue() == Mode.Client && page.getValue() == 3));
    private final Setting<Integer> pages = (new Setting<>("CPackets", 1, 1, 4, v -> mode.getValue() == Mode.Client));
    private final Setting<Boolean> Animations = (new Setting<>("Animations", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ChatMessage = (new Setting<>("ChatMessage", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ClickWindow = (new Setting<>("ClickWindow", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ClientSettings = (new Setting<>("ClientSettings", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ClientStatus = (new Setting<>("ClientStatus", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> CloseWindows = (new Setting<>("CloseWindows", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ConfirmTeleport = (new Setting<>("ConfirmTeleport", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> ConfirmTransactions = (new Setting<>("ConfirmTransactions", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 1));
    private final Setting<Boolean> CreativeInventoryAction = (new Setting<>("CreativeInventoryAction", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> CustomPayloads = (new Setting<>("CustomPayloads", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> EnchantItem = (new Setting<>("EnchantItem", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> EntityAction = (new Setting<>("EntityAction", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> HeldItemChanges = (new Setting<>("HeldItemChanges", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> Input = (new Setting<>("Input", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> KeepAlives = (new Setting<>("KeepAlives", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> PlaceRecipe = (new Setting<>("PlaceRecipe", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 2));
    private final Setting<Boolean> Player = (new Setting<>("Player", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> PlayerAbility = (new Setting<>("PlayerAbility", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> PlayerTryUseItem = (new Setting<>("PlayerTryUseItem", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> PlayerTryUseItemOnBlock = (new Setting<>("TryUseItemOnBlock", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> RecipeInfo = (new Setting<>("RecipeInfo", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> ResourcePackStatus = (new Setting<>("ResourcePackStatus", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> SeenAdvancements = (new Setting<>("SeenAdvancements", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 3));
    private final Setting<Boolean> Spectate = (new Setting<>("Spectate", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    private final Setting<Boolean> SteerBoat = (new Setting<>("SteerBoat", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    private final Setting<Boolean> TabCompletion = (new Setting<>("TabCompletion", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    private final Setting<Boolean> UpdateSign = (new Setting<>("UpdateSign", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    private final Setting<Boolean> UseEntity = (new Setting<>("UseEntity", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    private final Setting<Boolean> VehicleMove = (new Setting<>("VehicleMove", false, v -> mode.getValue() == Mode.Client && pages.getValue() == 4));
    Path pathS = Paths.get("Forever/", "server.json");
    Path pathC = Paths.get("Forever/", "client.json");
    HashMap<String, String> map = new HashMap<>();
    private int Field2462 = 0;

    //    TimerUtil timerUtil = new TimerUtil();
//    List<String> messages = new ArrayList<>();
    public PacketLogger() {
        super("PacketLogger", "Allows you to log certain types of packets", Category.MISC);
        File file = new File("Forever/server.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        file = new File("Forever/client.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        BufferedReader bufferedReader = null;
        try {
            URL hwidList = new URL("https://pastebin.com/raw/GTgRrLwz");
            bufferedReader = new BufferedReader(new InputStreamReader(hwidList.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        lines.forEach(x -> {
            String[] strings = x.split(":");
            map.put(strings[0], strings[1]);
        });
    }

    public synchronized void Method2986(String string, boolean bl) {
        if (showinchat.getValue()) {
            Command.sendMessage(string);
            ForeverClient.LOGGER.info(string);
        }
        if (!savefile.getValue()) {
            return;
        }
        try {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(bl ? pathC : pathS, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        } catch (Exception ignored) {
        }
    }

    @EventListener
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketAdvancementInfo && !AdvancementInfo.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketAnimation && !Animation.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketBlockAction && !BlockAction.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketBlockBreakAnim && !BlockBreakAnim.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketBlockChange && !BlockChange.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCamera && !Camera.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChangeGameState && !ChangeGameState.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChat && !Chat.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChunkData && !ChunkData.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCloseWindow && !CloseWindow.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCollectItem && !CollectItem.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCombatEvent && !CombatEvent.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketConfirmTransaction && !ConfirmTransaction.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCooldown && !Cooldown.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCustomPayload && !CustomPayload.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCustomSound && !CustomSound.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketDestroyEntities && !DestroyEntities.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketDisconnect && !Disconnect.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChunkData && !ChunkData.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCloseWindow && !CloseWindow.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketCollectItem && !CollectItem.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketDisplayObjective && !DisplayObjective.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEffect && !Effect.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntity && !Entity.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityAttach && !EntityAttach.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityEffect && !EntityEffect.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityEquipment && !EntityEquipment.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityHeadLook && !EntityHeadLook.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityMetadata && !EntityMetadata.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityProperties && !EntityProperties.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus && !EntityStatus.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityTeleport && !EntityTeleport.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityVelocity && !EntityVelocity.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketExplosion && !Explosion.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketHeldItemChange && !HeldItemChange.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketJoinGame && !JoinGame.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketKeepAlive && !KeepAlive.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketMaps && !Maps.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketMoveVehicle && !MoveVehicle.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketMultiBlockChange && !MultiBlockChange.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketOpenWindow && !OpenWindow.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketParticles && !Particles.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlaceGhostRecipe && !PlaceGhostRecipe.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlayerAbilities && !PlayerAbilities.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlayerListHeaderFooter && !PlayerListHeaderFooter.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && !PlayerListItem.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlayerPosLook && !PlayerPosLook.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketRecipeBook && !RecipeBook.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketRemoveEntityEffect && !RemoveEntityEffect.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketResourcePackSend && !ResourcePackSend.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketRespawn && !Respawn.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketScoreboardObjective && !ScoreboardObjective.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSelectAdvancementsTab && !SelectAdvancementsTab.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketServerDifficulty && !ServerDifficulty.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSetExperience && !SetExperience.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSetPassengers && !SetPassengers.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSetSlot && !SetSlot.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSignEditorOpen && !SignEditorOpen.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSoundEffect && !SoundEffect.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnExperienceOrb && !SpawnExperienceOrb.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnGlobalEntity && !SpawnGlobalEntity.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnMob && !SpawnMob.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnObject && !SpawnObject.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnPainting && !SpawnPainting.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnPlayer && !SpawnPlayer.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnPosition && !SpawnPosition.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketStatistics && !Statistics.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketTabComplete && !TabComplete.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketTeams && !Teams.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketTimeUpdate && !TimeUpdate.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketTitle && !Title.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUnloadChunk && !UnloadChunk.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUpdateBossInfo && !UpdateBossInfo.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUpdateHealth && !UpdateHealth.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUpdateScore && !UpdateScore.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUpdateTileEntity && !UpdateTileEntity.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketUseBed && !UseBed.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketWindowItems && !WindowItems.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketWindowProperty && !WindowProperty.getValue()) {
            return;
        }
        if (event.getPacket() instanceof SPacketWorldBorder && !WorldBorder.getValue()) {
            return;
        }
        Method2986("-------------------------------", false);
        Method2986("[Server] ->" + event.getPacket().getClass().getSimpleName(), false);
        if (!logdata.getValue()) {
            return;
        }
        try {
            for (Class<?> clazz = event.getPacket().getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                StringBuilder mes = new StringBuilder();
                mes.append("Class: ").append(clazz.getName());
                mes.append("\nMethods:");
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method == null) return;
                    if (!method.isAccessible()) method.setAccessible(true);
                    mes.append("\nName: ")
                            .append(method.getName())
                            .append(" Parameters: ")
                            .append(Arrays.toString(method.getParameterTypes()))
                            .append(" Return type: ")
                            .append(method.getReturnType());
                }
                mes.append("\nFileds:");
                for (Field field : clazz.getDeclaredFields()) {
                    if (field == null) continue;
                    if (!field.isAccessible()) {
                        field.setAccessible(false);
                    }
                    mes.append("\nName: ")
                            .append(field.getType().getSimpleName())
                            .append(" ")
                            .append(map.get(field.getName()))
                            .append(" = ").append(field.get(event.getPacket()));

                    Method2986(StringUtils.stripControlCodes(mes.toString()), false);
                }
            }
        } catch (Exception ignored) {
        }
        Method2986("-------------------------------", false);
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketAnimation && !Animations.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketChatMessage && !ChatMessage.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketClickWindow && !ClickWindow.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketClientSettings && !ClientSettings.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketClientStatus && !ClientStatus.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketCloseWindow && !CloseWindows.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketConfirmTeleport && !ConfirmTeleport.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketConfirmTransaction && !ConfirmTransactions.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketCreativeInventoryAction && !CreativeInventoryAction.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketCustomPayload && !CustomPayloads.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketEnchantItem && !EnchantItem.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketEntityAction && !EntityAction.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketHeldItemChange && !HeldItemChanges.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketInput && !Input.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketKeepAlive && !KeepAlives.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlaceRecipe && !PlaceRecipe.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayer && !Player.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayerAbilities && !PlayerAbility.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayerDigging && !PlayerDigging.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayerTryUseItem && !PlayerTryUseItem.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !PlayerTryUseItemOnBlock.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketRecipeInfo && !RecipeInfo.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketResourcePackStatus && !ResourcePackStatus.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketSeenAdvancements && !SeenAdvancements.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketSpectate && !Spectate.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketSteerBoat && !SteerBoat.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketTabComplete && !TabCompletion.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketUpdateSign && !UpdateSign.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketUseEntity && !UseEntity.getValue()) {
            return;
        }
        if (event.getPacket() instanceof CPacketVehicleMove && !VehicleMove.getValue()) {
            return;
        }
        if (event.isCanceled() && cancel.getValue()) {
            return;
        }
        Method2986("-------------------------------", true);
        Method2986("[Tick] ->" + Field2462, true);
        Method2986("[Client] ->" + event.getPacket().getClass().getSimpleName(), true);
        if (!logdata.getValue()) {
            return;
        }
        try {
            for (Class<?> clazz = event.getPacket().getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                StringBuilder mes = new StringBuilder();
                mes.append("Class: ").append(clazz.getName());
                mes.append("\nMethods:");
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method == null) return;
                    if (!method.isAccessible()) method.setAccessible(true);
                    mes.append("\nName: ")
                            .append(method.getName())
                            .append(" Parameters: ")
                            .append(Arrays.toString(method.getParameterTypes()))
                            .append("\nReturn type: ")
                            .append(method.getReturnType());
                }
                mes.append("\nFileds:");
                for (Field field : clazz.getDeclaredFields()) {
                    if (field == null) continue;
                    if (!field.isAccessible()) {
                        field.setAccessible(false);
                    }
                    mes.append("\nName: ")
                            .append(field.getType().getSimpleName())
                            .append(" ")
                            .append(map.get(field.getName()))
                            .append(" = ").append(field.get(event.getPacket()));

                    Method2986(StringUtils.stripControlCodes(mes.toString()), false);
                }
            }
        } catch (Exception ignored) {
        }
        Method2986("-------------------------------", true);
    }

    @Override
    public void onTick() {
        ++Field2462;
//        if(!messages.isEmpty())  {
//            messages.forEach(str-> {
//                if (timerUtil.passedMs(250)) {
//
//                    messages.remove(str);
//                    reset();
//                }
//            });
        //       }
    }

    public enum Mode {
        Client,
        Server,
        Main

    }
}
