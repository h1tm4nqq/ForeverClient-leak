package we.devs.forever.api.util.player;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.advancements.AdvancementManager.GSON;

public
class PlayerUtil extends Client  {

    private static final JsonParser PARSER = new JsonParser();
    public static TimerUtil timerUtil = new TimerUtil();

    public static String getNameFromUUID(UUID uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isEating() {
        if (mc.player.isHandActive()) {
            return (mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT) || mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK));
        }

        return false;
    }
    public static boolean isMending() {
        return InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    /**
     * Checks if the player is mining
     * @return Whether the player is mining
     */
//    public static boolean isMining() {
//        return InventoryUtil.isHolding(ItemTool.class) && mc.playerController.getIsHittingBlock() && BlockUtil.isBreakable(mc.objectMouseOver.getBlockPos()) && !mc.world.isAirBlock(mc.objectMouseOver.getBlockPos());
//    }

    public static boolean isPlayerMoving() {
        return mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f;
    }


    /**
     * Checks if a block is replaceable
     * @param pos the position to check
     * @return if this block pos can be placed at
     */

    public static void setSpeed(float speed) {
        if (mc.player.moveForward > 0) {
            mc.player.motionX = -Math.sin(Math.toRadians(mc.player.rotationYaw)) * speed;
            mc.player.motionZ = Math.cos(Math.toRadians(mc.player.rotationYaw)) * speed;
        }
        if (mc.player.moveForward < 0) {
            mc.player.motionX = Math.sin(Math.toRadians(mc.player.rotationYaw)) * speed;
            mc.player.motionZ = -Math.cos(Math.toRadians(mc.player.rotationYaw)) * speed;
        }
        if (mc.player.moveStrafing > 0) {
            mc.player.motionX = Math.cos(Math.toRadians(mc.player.rotationYaw)) * speed;
            mc.player.motionZ = Math.sin(Math.toRadians(mc.player.rotationYaw)) * speed;
        }
        if (mc.player.moveStrafing < 0) {
            mc.player.motionX = -Math.cos(Math.toRadians(mc.player.rotationYaw)) * speed;
            mc.player.motionZ = -Math.sin(Math.toRadians(mc.player.rotationYaw)) * speed;
        }
        if (mc.player.moveStrafing > 0 && mc.player.moveForward > 0) {
            mc.player.motionX = Math.cos(Math.toRadians(mc.player.rotationYaw + 45)) * speed;
            mc.player.motionZ = Math.sin(Math.toRadians(mc.player.rotationYaw + 45)) * speed;
        }
        if (mc.player.moveStrafing < 0 && mc.player.moveForward > 0) {
            mc.player.motionX = -Math.cos(Math.toRadians(mc.player.rotationYaw - 45)) * speed;
            mc.player.motionZ = -Math.sin(Math.toRadians(mc.player.rotationYaw - 45)) * speed;
        }
        if (mc.player.moveStrafing > 0 && mc.player.moveForward < 0) {
            mc.player.motionX = -Math.cos(Math.toRadians(mc.player.rotationYaw + 135)) * speed;
            mc.player.motionZ = -Math.sin(Math.toRadians(mc.player.rotationYaw + 135)) * speed;
        }
        if (mc.player.moveStrafing < 0 && mc.player.moveForward < 0) {
            mc.player.motionX = Math.cos(Math.toRadians(mc.player.rotationYaw - 135)) * speed;
            mc.player.motionZ = Math.sin(Math.toRadians(mc.player.rotationYaw - 135)) * speed;
        }
    }
    public static double getSpeed(boolean slowness, float s) {
        double defaultSpeed = 0.2873 * s;

        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            int amplifier = Objects.requireNonNull(
                            mc.player.getActivePotionEffect(MobEffects.SPEED))
                    .getAmplifier();

            defaultSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        if (slowness && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
            int amplifier = Objects.requireNonNull(
                            mc.player.getActivePotionEffect(MobEffects.SLOWNESS))
                    .getAmplifier();

            defaultSpeed /= 1.0 + 0.2 * (amplifier + 1);
        }

        return defaultSpeed;
    }
    public static boolean isDead(Entity entity) {
        return getHealth(entity) <= 0 || entity.isDead;
    }
    public static BlockPos getPosition() {
        return new BlockPos(mc.player.getPositionVector());
    }
    public static double getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }
    public static float getHealth(Entity entity) {
        // player
        if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).getHealth() + ((EntityPlayer) entity).getAbsorptionAmount();
        }

        // living
        else if (entity instanceof EntityLivingBase) {
            return ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount();
        }

        return 0;
    }
    public static boolean isMoving() {
        return mc.player.movementInput.forwardKeyDown
                || mc.player.movementInput.backKeyDown
                || mc.player.movementInput.leftKeyDown
                || mc.player.movementInput.rightKeyDown;
    }

    public static void strafe(MoveEvent event, double speed) {
        if (isMoving()) {
            double[] strafe = strafe(speed);
            event.setX(strafe[0]);
            event.setZ(strafe[1]);
        } else {
            event.setX(0.0);
            event.setZ(0.0);
        }
    }

    public static double[] strafe(double speed) {
        return strafe(mc.player, speed);
    }

    public static double[] strafe(Entity entity, double speed) {
        return strafe(entity, mc.player.movementInput, speed);
    }

    public static double[] strafe(Entity entity,
                                  MovementInput movementInput,
                                  double speed) {
        float moveForward = movementInput.moveForward;
        float moveStrafe = movementInput.moveStrafe;
        float rotationYaw = entity.prevRotationYaw
                + (entity.rotationYaw - entity.prevRotationYaw)
                * mc.getRenderPartialTicks();

        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
            } else if (moveStrafe < 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            } else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }

        double posX =
                moveForward * speed * -Math.sin(Math.toRadians(rotationYaw))
                        + moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        double posZ =
                moveForward * speed * Math.cos(Math.toRadians(rotationYaw))
                        - moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));

        return new double[]{posX, posZ};
    }

    public static String getNameFromUUID(String uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getUUIDFromName(String name) {
        try {
            lookUpUUID process = new lookUpUUID(name);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        } catch (Exception e) {
            return null;
        }
    }

    public static void send(Packet<?> packet) {
        NetHandlerPlayClient connection = mc.getConnection();
        if (connection != null) {
            connection.sendPacket(packet);
        }
    }

    public static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String res = convertStreamToString(in);
            in.close();
            conn.disconnect();
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }

    public static List<String> getHistoryOfNames(UUID id) {
        try {
            JsonArray array = getResources(new URL("https://api.mojang.com/user/profiles/" + getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
            List<String> temp = Lists.newArrayList();
            for (JsonElement e : array) {
                JsonObject node = e.getAsJsonObject();
                String name = node.get("name").getAsString();
                long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0L;
                temp.add(name + TextUtil.DARK_GRAY + new Date(changedAt));
            }
            Collections.sort(temp);
            return temp;
        } catch (Exception ignored) {
            return null;
        }
    }
    public static boolean anyMovementKeys()
    {
        return mc.player.movementInput.forwardKeyDown
                || mc.player.movementInput.backKeyDown
                || mc.player.movementInput.leftKeyDown
                || mc.player.movementInput.rightKeyDown
                || mc.player.movementInput.jump
                || mc.player.movementInput.sneak;
    }
    public static String getIdNoHyphens(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    private static JsonElement getResources(URL url, String request) throws Exception {
        return getResources(url, request, null);
    }

    private static JsonElement getResources(URL url, String request, JsonElement element) throws Exception {
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");
            if (element != null) {
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(GSON.toJson(element));
                output.close();
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();

            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }

            scanner.close();
            String json = builder.toString();
            JsonElement data = PARSER.parse(json);
            return data;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static boolean isPlayerClipped() {
        return !(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox()).isEmpty());
    }

    public static BlockPos getPlayerPosFloored() {
        return new BlockPos(Math.floor(Util.mc.player.posX), Math.floor(Util.mc.player.posY), Math.floor(Util.mc.player.posZ));
    }

    public static double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static EntityPlayer getNearestPlayer(final double range) {
        return PlayerUtil.mc.world.playerEntities.stream().filter(entityPlayer -> !friendManager.isFriend(entityPlayer)).filter(p -> PlayerUtil.mc.player.getDistance(p) <= range).filter(p -> PlayerUtil.mc.player.getEntityId() != p.getEntityId()).min(Comparator.comparing(p -> PlayerUtil.mc.player.getDistance(p))).orElse(null);
    }

    public static EntityPlayer getLookingPlayer(final double range) {
        final List<EntityPlayer> players = PlayerUtil.mc.world.playerEntities.stream().filter(entityPlayer -> !friendManager.isFriend(entityPlayer)).collect(Collectors.toList());
        for (int i = 0; i < players.size(); ++i) {
            if (getDistance(players.get(i)) > range) {
                players.remove(i);
            }
        }
        players.remove(PlayerUtil.mc.player);
        EntityPlayer target = null;
        final Vec3d positionEyes = PlayerUtil.mc.player.getPositionEyes(PlayerUtil.mc.getRenderPartialTicks());
        final Vec3d rotationEyes = PlayerUtil.mc.player.getLook(PlayerUtil.mc.getRenderPartialTicks());
        final int precision = 2;
        for (int j = 0; j < (int) range; ++j) {
            for (int k = precision; k > 0; --k) {
                for (final EntityPlayer targetTemp : players) {
                    final AxisAlignedBB playerBox = targetTemp.getEntityBoundingBox();
                    final double xArray = positionEyes.x + rotationEyes.x * j + rotationEyes.x / k;
                    final double yArray = positionEyes.y + rotationEyes.y * j + rotationEyes.y / k;
                    final double zArray = positionEyes.z + rotationEyes.z * j + rotationEyes.z / k;
                    if (playerBox.maxY >= yArray && playerBox.minY <= yArray && playerBox.maxX >= xArray && playerBox.minX <= xArray && playerBox.maxZ >= zArray && playerBox.minZ <= zArray) {
                        target = targetTemp;
                    }
                }
            }
        }
        return target;
    }

    public static double getDistance(final Entity entity) {
        return PlayerUtil.mc.player.getDistance(entity);
    }

    public static EntityPlayer copyPlayer(EntityPlayer playerIn, boolean animations) {
        int count = playerIn.getItemInUseCount();
        EntityPlayer copy = new EntityPlayer(mc.world, new GameProfile(UUID.randomUUID(), playerIn.getName())) {

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }

            @Override
            public int getItemInUseCount() {
                return count;
            }
        };
        if (animations) {
            copy.setSneaking(playerIn.isSneaking());
            copy.swingProgress = playerIn.swingProgress;
            copy.limbSwing = playerIn.limbSwing;
            copy.limbSwingAmount = playerIn.prevLimbSwingAmount;
            copy.inventory.copyInventory(playerIn.inventory);
        }
        copy.setPrimaryHand(playerIn.getPrimaryHand());
        copy.ticksExisted = playerIn.ticksExisted;
        copy.setEntityId(playerIn.getEntityId());
        copy.copyLocationAndAnglesFrom(playerIn);
        return copy;
    }

    public static double getDistance(final BlockPos pos) {
        return PlayerUtil.mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    public static
    class lookUpUUID implements Runnable {

        private final String name;
        private volatile UUID uuid;

        public lookUpUUID(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            NetworkPlayerInfo profile;
            try {
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<>(Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                assert profile != null;
                uuid = profile.getGameProfile().getId();
            } catch (Exception e) {
                profile = null;
            }

            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..");
                String s = requestIDs("[\"" + name + "\"]");
                if (s == null || s.isEmpty()) {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
                } else {
                    JsonElement element = new JsonParser().parse(s);
                    if (element.getAsJsonArray().size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)");
                    } else {
                        try {
                            String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            uuid = UUIDTypeAdapter.fromString(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Command.sendMessage("Couldn't find player ID. (2)");
                        }
                    }
                }
            }
        }

        public UUID getUUID() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }
    }

    public static
    class lookUpName implements Runnable {

        private final String uuid;
        private final UUID uuidID;
        private volatile String name;

        public lookUpName(String input) {
            this.uuid = input;
            this.uuidID = UUID.fromString(input);
        }

        public lookUpName(UUID input) {
            this.uuidID = input;
            this.uuid = input.toString();
        }

        @Override
        public void run() {
            name = lookUpName();
        }

        public String lookUpName() {
            EntityPlayer player = null;
            if (mc.world != null) {
                player = mc.world.getPlayerEntityByUUID(uuidID);
            }

            if (player == null) {
                String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names";
                try {
                    @SuppressWarnings("deprecation")
                    String nameJson = IOUtils.toString(new URL(url));
                    JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
                    String playerSlot = nameValue.get(nameValue.size() - 1).toString();
                    JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
                    return nameObject.get("name").toString();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return player.getName();
        }

        public String getName() {
            return this.name;
        }
    }

    public static BlockPos getBlockPos() {
        return new BlockPos(Math.floor(mc.player.posX), mc.player.posY, Math.floor(mc.player.posZ));
    }
}
