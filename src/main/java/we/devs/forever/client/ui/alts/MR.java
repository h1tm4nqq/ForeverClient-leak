package we.devs.forever.client.ui.alts;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import we.devs.forever.client.ui.alts.tools.Config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class MR {
    public static void init() {
        Config.load();

    }

    public static void setSession(Session s) throws Exception {
        Class<?> mc = Minecraft.getMinecraft().getClass();
        try {
            AccessibleObject session = null;
            for (Field f : mc.getDeclaredFields()) {
                if (!f.getType().isInstance(s)) continue;
                session = f;
                System.out.println("Found field " + f + ", injecting...");
            }
            if (session == null) {
                throw new IllegalStateException("No field of type " + Session.class.getCanonicalName() + " declared.");
            }
            session.setAccessible(true);
            ((Field) session).set(Minecraft.getMinecraft(), s);
            session.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

