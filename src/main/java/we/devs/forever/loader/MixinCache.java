package we.devs.forever.loader;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MixinCache {
    public static byte[] mixinBytes;
    public static byte[] refmapBytes;
    public static final String tempDir= System.getProperty("java.io.tmpdir");
    private final static char[] ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final List<String> strings = new ArrayList<>();

    public static File randomFile() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++)
            sb.append(ALPHA_NUM[ThreadLocalRandom.current().nextInt(ALPHA_NUM.length)]);
        if (strings.contains(sb.toString())) {
            randomFile();
        }
        strings.add(sb.toString());
        return new File(tempDir, "+~JF" + sb  + ".tmp");
    }

    public static File getRefMapFile() {
        File var2 = randomFile();
        try {
            FileOutputStream  stream = new FileOutputStream(var2);
            stream.write(refmapBytes);
            stream.flush();
            stream.close();
        } catch (Exception var8) {
//            var8.printStackTrace();
        }
        return var2;
    }


    public static  List<String> getMixins() {
        List<String> mixinCache = new ArrayList<>();
        JsonObject jsonObject = new Gson().fromJson(new String(mixinBytes, StandardCharsets.UTF_8), JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("client");
        jsonArray.forEach(x -> mixinCache.add(x.getAsString()));

        jsonArray = jsonObject.getAsJsonArray("mixins");
        jsonArray.forEach(x -> mixinCache.add(x.getAsString()));

        return mixinCache;
    }
}

