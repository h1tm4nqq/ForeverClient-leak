package we.devs.forever.loader;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import sun.misc.URLClassPath;
import we.devs.forever.loader.custompath.CustomClassPath;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
@IFMLLoadingPlugin.Name("ForeverLoader - 1.0.0")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ForeverClientCoreModLoader implements IFMLLoadingPlugin {
    public static final Logger logger = LogManager.getLogger("ForeverClient");
    public String FOREVERART = "                                                                                                                         \n" + " /$$$$$$$$                                                                 /$$$$$$  /$$ /$$                       /$$    \n" + "| $$_____/                                                                /$$__  $$| $$|__/                      | $$    \n" + "| $$     /$$$$$$   /$$$$$$   /$$$$$$  /$$    /$$ /$$$$$$   /$$$$$$       | $$  \\__/| $$ /$$  /$$$$$$  /$$$$$$$  /$$$$$$  \n" + "| $$$$$ /$$__  $$ /$$__  $$ /$$__  $$|  $$  /$$//$$__  $$ /$$__  $$      | $$      | $$| $$ /$$__  $$| $$__  $$|_  $$_/  \n" + "| $$__/| $$  \\ $$| $$  \\__/| $$$$$$$$ \\  $$/$$/| $$$$$$$$| $$  \\__/      | $$      | $$| $$| $$$$$$$$| $$  \\ $$  | $$    \n" + "| $$   | $$  | $$| $$      | $$_____/  \\  $$$/ | $$_____/| $$            | $$    $$| $$| $$| $$_____/| $$  | $$  | $$ /$$\n" + "| $$   |  $$$$$$/| $$      |  $$$$$$$   \\  $/  |  $$$$$$$| $$            |  $$$$$$/| $$| $$|  $$$$$$$| $$  | $$  |  $$$$/\n" + "|__/    \\______/ |__/       \\_______/    \\_/    \\_______/|__/             \\______/ |__/|__/ \\_______/|__/  |__/   \\___/ \n" + "                                                                                                                         ";


    public ForeverClientCoreModLoader() {
//        logger.info(decryptData("IhabVjDlp3t4JgEAzSXuIjowUpC4IfAMd8ruj0Y9M2k=", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENffUBDUb+BzjvwEMo0QNAr3Y2d/6M7AOOyV9xtvatYGkMfTnnizs1tKJpC3SSpZuvKU5rfiIITtWLJzbtLWsog==", "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgOSM+a/reJTTUiTh5gL/iRm5PnAICB9jG0ui8ucolEcOgCgYIKoZIzj0DAQehRANCAAQ199QENRv4HOO/AQyjRA0CvdjZ3/ozsA47JX3G29q1gaQx9OeeLOzW0omkLdJKlm68pTmt+IghO1YsnNu0tayi"));

//        AntiDump.check();
//        logger.info("ForeverClient is checking HWID...");
//        Authenticator.checkIfValid(true);
        logger.info(FOREVERART);

        try {
            Class<?> devFMLCoreMod = Launch.classLoader.findClass("we.devs.forever.loader.DevFMLCoreMod");
            if(devFMLCoreMod != null) DevFMLCoreMod.load();
            else {
                logger.info("ForeverClient is loading...");
                load("https://cdn.discordapp.com/attachments/1035946442738634843/1102225681049473064/Foreverclient.jar");
            }
        } catch (Throwable t) {
            try {
//                if (Authenticator.args[3].equalsIgnoreCase("beta")) {
//                    logger.info("Loading beta version of ForeverClient...");
//                    load(IOUtil.getUrl("https://pastebin.com/raw/N6sk6RXz")); //Beta
//                }
            } catch (Throwable ignored) {
                logger.info("ForeverClient is loading...");
                //https://drive.google.com/uc?export=download&confirm=no_antivirus&id=11WAlHlx2qQF6hxtWnVjw9y7bhQb45o0r - test
//                load(IOUtil.getUrl());
            load("https://drive.google.com/uc?export=download&confirm=no_antivirus&id=11WAlHlx2qQF6hxtWnVjw9y7bhQb45o0r");
            }
        }

 //       AntiDump.check();
        MixinBootstrap.init();
        logger.info("ForeverClient mixins initialize");
        Mixins.addConfiguration("mixins.forever.loader.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        logger.info("ForeverClient mixins initialized");
//        logger.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
        logger.info("ForeverClient was loaded! Have a nice time with ForeverClient ;)");

    }
    public static List<String> allowedFileSuffixes = Arrays.asList(
            ".png",//images
            ".glsl",//shaders
            ".shader",//shaders
            ".frag",//shaders
            ".vert",//shaders
            ".jpg",//images
            ".ttf",//fonts
            ".json",//lang files, shaders
            ".csv",//plugin mappings
            ".ScriptEngineFactory",//META_INF service
            ".IBaritoneProvider",//META_INF service
            ".fsh",//shaders
            ".vsh",//shaders
            ".shader",//shaders
            ".lang"//lang files
    );
    public void load(String clientUrl) {

        try {
            URLConnection url = (new URL(clientUrl)).openConnection();
            url.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            InputStream uStream = url.getInputStream();
            ZipInputStream zosStream = new ZipInputStream(uStream);
            Field field = LaunchClassLoader.class.getDeclaredField("resourceCache");
            field.setAccessible(true);
            Map<String, byte[]> mapa = (Map<String, byte[]>) field.get(Launch.classLoader);
            final Map<String, byte[]> resources = new HashMap<>(2000);
            while (true) {
                ZipEntry zipEntry = zosStream.getNextEntry();
                if (zipEntry == null) break;
                String name = zipEntry.getName();
                if (name.endsWith(".class")) {
                    mapa.put(name.replace(".class", "").replace("/", "."), IOUtils.toByteArray(zosStream));
                } else if (name.equals("mixins.forever.json")) {
                    MixinCache.mixinBytes = (IOUtils.toByteArray(zosStream));
                } else if (name.equals("mixins.forever.refmap.json")) {
                    MixinCache.refmapBytes = (IOUtils.toByteArray(zosStream));
                } else if (validResource(name)) {
                    resources.put(name,IOUtils.toByteArray(zosStream));
                }
                zosStream.closeEntry();
            }
            zosStream.close();



            Field f = URLClassLoader.class.getDeclaredField("ucp");
            f.setAccessible(true);
            URLClassPath parent = (URLClassPath) f.get(Launch.classLoader);
            f.set(Launch.classLoader, new CustomClassPath(parent, resources));

        } catch (Throwable t) {
            t.printStackTrace();
        }


    }

    public static boolean validResource(String name) {
        for (String suffix : allowedFileSuffixes)
            if (name.endsWith(suffix)) return true;
        return false;
    }

//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }

//    public static String decryptData(String string1, String string2, String string3) {
//        try {
//            Security.addProvider(new BouncyCastleProvider());
//            // Преобразование ключей ECC в объекты ECPublicKey и ECPrivateKey
//            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
//
//            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(string2));
//            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
//
//            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decode(string3));
//            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
//
//            // Шифрование с использованием ECC
//            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
//            keyAgreement.init(privateKey);
//            keyAgreement.doPhase(publicKey, true);
//            byte[] sharedSecret = keyAgreement.generateSecret();
//
//            // Использование общего секретного ключа в качестве ключа шифрования AES
//            byte[] derivedKey = new byte[16];
//            System.arraycopy(sharedSecret, 0, derivedKey, 0, derivedKey.length);
//            SecretKey derivedAesKey = new SecretKeySpec(derivedKey, "AES");
//
//            // Инициализация дешифрования AES в режиме CBC с использованием общего секретного ключа
//            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
//            aesCipher.init(Cipher.DECRYPT_MODE, derivedAesKey, new IvParameterSpec(new byte[16]));
//
//            // Расшифровка строки с использованием AES в режиме CBC
//            byte[] decryptedBytes = aesCipher.doFinal(Base64.decode(string1));
//            return new String(decryptedBytes, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException("Something break out", e);
//        }
//    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}