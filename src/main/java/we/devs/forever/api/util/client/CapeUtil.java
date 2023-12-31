package we.devs.forever.api.util.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class CapeUtil {

    final static ArrayList<String> final_uuid_list = get_uuids();

    static {



    }
    public static byte index = 1;

    public static ArrayList<String> get_uuids() {
        try {
            URL url = new URL("https://pastebin.com/raw/FdfhzQWP");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            final ArrayList<String> uuid_list = new ArrayList<>();

            String s;

            while ((s = reader.readLine()) != null) {
                uuid_list.add(s);
            }

            return uuid_list;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean isVip(UUID uuid) {
        for (String u : Objects.requireNonNull(final_uuid_list)) {
            if (u.equals(uuid.toString())) {
                return true;
            }
        }
        return false;
    }

}

