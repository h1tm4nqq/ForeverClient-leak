package we.devs.forever.api.util.client;

import net.minecraft.client.Minecraft;

public
class Mapping {
    public static final String tickLength = Mapping.isObfuscated() ? "field_194149_e" : "tickLength";
    public static final String timer = Mapping.isObfuscated() ? "field_71428_T" : "timer";
    public static final String placedBlockDirection = Mapping.isObfuscated() ? "field_149579_d" : "placedBlockDirection";
    public static final String playerPosLookYaw = Mapping.isObfuscated() ? "field_148936_d" : "yaw";
    public static final String playerPosLookPitch = Mapping.isObfuscated() ? "field_148937_e" : "pitch";
    public static final String isInWeb = Mapping.isObfuscated() ? "field_70134_J" : "isInWeb";
    public static final String cPacketPlayerYaw = Mapping.isObfuscated() ? "field_149476_e" : "yaw";
    public static final String cPacketPlayerPitch = Mapping.isObfuscated() ? "field_149473_f" : "pitch";
    public static final String renderManagerRenderPosX = Mapping.isObfuscated() ? "field_78725_b" : "renderPosX";
    public static final String renderManagerRenderPosY = Mapping.isObfuscated() ? "field_78726_c" : "renderPosY";
    public static final String renderManagerRenderPosZ = Mapping.isObfuscated() ? "field_78723_d" : "renderPosZ";
    public static final String rightClickDelayTimer = Mapping.isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer";
    public static final String sPacketEntityVelocityMotionX = Mapping.isObfuscated() ? "field_70159_w" : "motionX";
    public static final String sPacketEntityVelocityMotionY = "motionY";
    public static final String sPacketEntityVelocityMotionZ = Mapping.isObfuscated() ? "field_70179_y" : "motionZ";

    public static boolean isObfuscated() {
        try {
            return Minecraft.class.getDeclaredField("instance") == null;
        } catch (Exception e) {
            return true;
        }
    }
}
