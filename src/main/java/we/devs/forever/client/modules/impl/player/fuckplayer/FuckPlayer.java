package we.devs.forever.client.modules.impl.player.fuckplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.GameType;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.render.TotemPopEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.player.fuckplayer.enums.MoveMode;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.main.ForeverClient;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;


public class FuckPlayer extends Module {

    final static private ItemStack[] armors = new ItemStack[]{
            new ItemStack(Items.DIAMOND_BOOTS),
            new ItemStack(Items.DIAMOND_LEGGINGS),
            new ItemStack(Items.DIAMOND_CHESTPLATE),
            new ItemStack(Items.DIAMOND_HELMET)
    };

    public static Setting<Boolean> copyInventory = new Setting<Boolean>("Copy Inventory", false);
    public static Setting<Boolean> playerStacked = new Setting<Boolean>("Player Stacked", false);
    public static Setting<Boolean> onShift = new Setting<Boolean>("On Shift",  false);
    public static Setting<Boolean> simulateDamage = new Setting<Boolean>("Simulate Damage", true);
    public static Setting<String> nameFakePlayer = new Setting<>("Name FakePlayer", "Terpila");
    public static Setting<Integer> vulnerabilityTick = new Setting<Integer>("Vulnerability Tick", 4, 1, 10);
    public static Setting<Integer> resetHealth = new Setting<Integer>("Reset Health", 10, 1, 36);
    public static Setting<Integer> tickRegenVal = new Setting<Integer>("Tick Regen", 4, 1, 30);
    public static Setting<Integer> startHealth = new Setting<Integer>("Start Health", 36, 1, 36);
    public static Setting<MoveMode> moving = new Setting<>("Moving", MoveMode.None);
    public static Setting<Float> speed = new Setting<>("Speed", 0.36F, 1F, 4F);
    public static Setting<Integer> range = new Setting<Integer>("Range", 3, 1, 14);
    public static Setting<Boolean> followPlayer = new Setting<Boolean>("Follow Player", true);
    public static Setting<Boolean> resistance = new Setting<Boolean>("Resistance", true);
    public static Setting<Boolean> pop = new Setting<Boolean>("Pop", true);
    boolean beforePressed;
    int incr;
    // Simple list of players for the pop
    ArrayList<playerInfo> listPlayers = new ArrayList<>();
    // This just manage the entire fakePlayer moving
    movingManager manager = new movingManager();
    
    public FuckPlayer() {
        super("FakePlayer", "Spawn in world fake player", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        incr = 0;
        beforePressed = false;
        if (mc.player == null || mc.player.isDead) {
            disable();
            return;
        }
        if (!onShift.getValue()) {
            spawnPlayer();
        }
    }

    @Override
    public void onUnload() {
        disable();
    }

    public void spawnPlayer() {
        // Clone empty player
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), nameFakePlayer.getValue() + incr));
        // Copy angles
        clonedPlayer.copyLocationAndAnglesFrom(mc.player);
        clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
        clonedPlayer.rotationYaw = mc.player.rotationYaw;
        clonedPlayer.rotationPitch = mc.player.rotationPitch;
        // set gameType
        clonedPlayer.setGameType(GameType.SURVIVAL);
        clonedPlayer.setHealth(startHealth.getValue().intValue());
        // Add entity id
        mc.world.addEntityToWorld((-1234 + incr), clonedPlayer);
        incr++;
        // Set invenotry
        if (copyInventory.getValue())
            clonedPlayer.inventory.copyInventory(mc.player.inventory);
        else
            // If enchants
            if (playerStacked.getValue()) {
                // Iterate
                for (int i = 0; i < 4; i++) {
                    // Create base
                    ItemStack item = armors[i];
                    // Add enchants
                    item.addEnchantment(
                            i == 3 ? Enchantments.BLAST_PROTECTION : Enchantments.PROTECTION,
                            4);
                    // Add it to the player
                    clonedPlayer.inventory.armorInventory.set(i, item);

                }
            }
        if (resistance.getValue())
            clonedPlayer.addPotionEffect(new PotionEffect(Potion.getPotionById(11), 123456789, 0));
        clonedPlayer.onEntityUpdate();
        listPlayers.add(new playerInfo(clonedPlayer.getName()));
        if (!moving.getValue().equals("None"))
            manager.addPlayer(clonedPlayer.entityId, moving.getValue().toString(), speed.getValue().doubleValue(),
                    moving.getValue().equals("Line") ? (
                            getDirection()
                    ) : -1, range.getValue().doubleValue(), followPlayer.getValue());
    }

    @Override
    public void onUpdate() {
        // OnShift add
        if (onShift.getValue() && mc.gameSettings.keyBindSneak.isPressed() && !beforePressed) {
            beforePressed = true;
            spawnPlayer();
        } else beforePressed = false;

        // Update tick explosion
        for (int i = 0; i < listPlayers.size(); i++) {
            if (listPlayers.get(i).update()) {
                int finalI = i;
                Optional<EntityPlayer> temp = mc.world.playerEntities.stream().filter(
                        e -> e.getName().equals(listPlayers.get(finalI).name)
                ).findAny();
                if (temp.isPresent())
                    if (temp.get().getHealth() < 36)
                        temp.get().setHealth(temp.get().getHealth() + 1);
            }
        }

        // This manage moving fakePlayers
        manager.update();
    }

    // Idk from who i skidded this, it was in a forum
    public int getDirection() {
        int yaw = (int) RotationUtil.normalizeAngle(mc.player.getPitchYaw().y);


        if (yaw < 0)              //due to the yaw running a -360 to positive 360

            yaw += 360;    //not sure why it's that way


        yaw += 22;    //centers coordinates you may want to drop this line

        yaw %= 360;  //and this one if you want a strict interpretation of the zones


        return yaw / 45;  //  360degrees divided by 45 == 8 zones
    }

    public void onDisable() {
        if (mc.world != null) {
            for (int i = 0; i < incr; i++) {
                mc.world.removeEntityFromWorld((-1234 + i));
            }
        }
        listPlayers.clear();
        manager.remove();
    }

    @EventListener
    private void packetReceiveListener(PacketEvent.Receive event) {
        // Simple crystal damage
        if (simulateDamage.getValue()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packetSoundEffect = (SPacketSoundEffect) packet;
                if (packetSoundEffect.getCategory() == SoundCategory.BLOCKS && packetSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                        if (entity instanceof EntityEnderCrystal) {
                            if (entity.getDistanceSq(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()) <= 36.0f) {
                                for (EntityPlayer entityPlayer : mc.world.playerEntities) {
                                    // If the player is like we want to be
                                    if (entityPlayer.getName().split(nameFakePlayer.getValue()).length == 2) {

                                        Optional<playerInfo> temp = listPlayers.stream().filter(
                                                e -> e.name.equals(entityPlayer.getName())
                                        ).findAny();
                                        // If he is in wait, continue
                                        if (!temp.isPresent() || !temp.get().canPop())
                                            continue;

                                        // Calculate damage
                                        float damage = DamageUtil.calculateDamage(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ(), entityPlayer, false);
                                        if (damage > entityPlayer.getHealth()) {
                                            // If higher, new health and pop
                                            entityPlayer.setHealth(resetHealth.getValue().floatValue());
                                            if (pop.getValue()) {
                                                mc.effectRenderer.emitParticleAtEntity(entityPlayer, EnumParticleTypes.TOTEM, 30);
                                                mc.world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
                                            }
                                            ForeverClient.EVENT_BUS.post(new TotemPopEvent(entityPlayer, 0));

                                            // Else, setHealth
                                        } else entityPlayer.setHealth(entityPlayer.getHealth() - damage);

                                        // Add vulnerability
                                        temp.get().tickPop = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static class movingPlayer {
        private final int id;
        private final String type;
        private final double speed;
        private final int direction;
        private final double range;
        private final boolean follow;
        int rad = 0;

        public movingPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
            this.id = id;
            this.type = type;
            this.speed = speed;
            this.direction = Math.abs(direction);
            this.range = range;
            this.follow = follow;
        }

        public void move() {
            Entity player = mc.world.getEntityByID(id);
            if (player != null) {
                switch (type) {
                    case "Line":

                        double posX = follow ? mc.player.posX : player.posX,
                                posY = follow ? mc.player.posY : player.posY,
                                posZ = follow ? mc.player.posZ : player.posZ;


                        switch (direction) {
                            case 0:
                                posZ += speed;
                                break;
                            case 1:
                                posX -= speed / 2;
                                posZ += speed / 2;
                                break;
                            case 2:
                                posX -= speed / 2;
                                break;
                            case 3:
                                posZ -= speed / 2;
                                posX -= speed / 2;
                                break;
                            case 4:
                                posZ -= speed;
                                break;
                            case 5:
                                posX += speed / 2;
                                posZ -= speed / 2;
                                break;
                            case 6:
                                posX += speed;
                                break;
                            case 7:
                                posZ += speed / 2;
                                posX += speed / 2;
                                break;
                        }

                        if (BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir) {
                            for (int i = 0; i < 5; i++) {
                                if (BlockUtil.getBlock(posX, posY - 1, posZ) instanceof BlockAir) {
                                    posY--;
                                } else break;
                            }
                        } else {
                            for (int i = 0; i < 5; i++) {
                                if (!(BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir)) {
                                    posY++;
                                } else break;
                            }
                        }

                        player.setPositionAndUpdate(
                                posX,
                                posY,
                                posZ
                        );
                        break;
                    case "Circle":

                        double posXCir = Math.cos(rad / 100.0) * range + mc.player.posX, posZCir = Math.sin(rad / 100.0) * range + mc.player.posZ, posYCir = mc.player.posY;

                        if (BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir) {
                            for (int i = 0; i < 5; i++) {
                                if (BlockUtil.getBlock(posXCir, posYCir - 1, posZCir) instanceof BlockAir) {
                                    posYCir--;
                                } else break;
                            }
                        } else {
                            for (int i = 0; i < 5; i++) {
                                if (!(BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir)) {
                                    posYCir++;
                                } else break;
                            }
                        }

                        player.setPositionAndUpdate(
                                posXCir,
                                posYCir,
                                posZCir
                        );
                        rad += speed * 10;
                        break;
                    case "Random":
                        break;
                }
            }
        }

    }

    static class movingManager {
        // List of players
        private final ArrayList<movingPlayer> players = new ArrayList<>();

        // Just add a new player
        void addPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
            players.add(new movingPlayer(id, type, speed, direction, range, follow));
        }

        // Update every fakePlayer' position
        void update() {
            this.players.forEach(movingPlayer::move);
        }

        void remove() {
            players.clear();
        }
    }

    class playerInfo {
        final String name;
        int tickPop = -1;
        int tickRegen = 0;

        // We just set the new name
        public playerInfo(String name) {
            this.name = name;
        }

        // If update, we have to regen and decrease vulnerability tick
        boolean update() {
            if (tickPop != -1) {
                if (++tickPop >= vulnerabilityTick.getValue().intValue())
                    tickPop = -1;
            }
            if (++tickRegen >= tickRegenVal.getValue().intValue()) {
                tickRegen = 0;
                return true;
            } else return false;
        }

        boolean canPop() {
            return this.tickPop == -1;
        }
    }
}
