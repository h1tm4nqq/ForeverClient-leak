package we.devs.forever.client.security.hwid;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import we.devs.forever.client.security.AntiDump;
import we.devs.forever.loader.utils.IOUtil;
import we.devs.forever.main.ForeverClient;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static we.devs.forever.main.ForeverClient.MODVER;

public class Authenticator {

    public static final String[] message = {
            "did the funny fortnite",
            "did the juice wrld",
            "did it in the butt",
            "is not token logged",
            "stans danny devito",
            "does not shit their pants",
            "absolutely abhores CP",
            "assuredly has an extremely large penis",
            "is a menace to society",
            "did it for the lols",
            "is a blm activists",
            "did the thing",
            "didn't die at astroworld",
            "just entered the 1tb cp giveaway",
            "loves femboy cock",
            "is taking thigh pics",
            "'s game will crash in 30 minutes",
            "<--- faggot",
            "is citying big grin",
            "'s dick just shrunk by 2.5cm",
            "likeds kids",
            "is looking cute and feminine today",
            "will be receiving pizza soon",
            "confirmed groomable",
            "Has just been signed up for Blacked.com!"
    };
    public static Logger logger = LogManager.getLogger("ForeverClient");
    public static String URL = "https://pastebin.com/raw/Xx0g3fPz";


    public static String getUsername() {
        StringBuilder args = new StringBuilder();
        StringBuilder str = new StringBuilder();
        StringBuilder username = new StringBuilder();

        args.append(Launch.blackboard.get("forgeLaunchArgs"));
        for (int i = args.length(); i >= 0; i--) {
            if (args.indexOf("=", i - 2) == i - 2) {
                break;
            } else
                str.append(args.charAt(i - 2));
        }

        for (int i = str.length(); i > 0; i--) {
            username.append(str.charAt(i - 1));
        }

        return username.toString();
    }



    public static void checkIfValid(boolean sendMessage) {
        Mode auth = Mode.Hwid;

        try {
            auth = auth();
        } catch (Exception ignored) {
        }

        if (auth != Mode.Had) {
            try {
                ForeverClient.LOGGER.info("HWID is invalid!");
                if (sendMessage) {
                    final String Message = (
                            "```"
                                    + "Client tried run fucking nn"
                                    + "\nAccount - " + getUsername()
                                    + "\nHwid - " + getHWID()
//                                    + "\nVersion - " + MODVER
                                    + "\nMode - " + auth.toString()
                                    //   + "\n" + FOREVERART
                                    + "```"
                    );
                    IOUtil.sendMessageHWID(Message);

                }
                String temp = auth == Mode.Hwid
                        ? "Invalid hwid! (Copied) Your HWID is : "  + getHWID()
                        + "\nPlease contact with devs or just wait."

                        : "Your client license has disabled."
                        + "\nPlease contact with devs or just wait.";
                JOptionPane.showMessageDialog(null, temp);
                if(auth == Mode.Hwid ) setClipboardString(getHWID());
            } catch (Exception ignored) {
            }
            AntiDump.unsafe.putAddress(0,0);
            AntiDump.unsafe.throwException(new HwidException("Invalid hwid!"));
        } else {
            if (sendMessage) {
                try {
                    final String Message = (
                            "```"
                                    + getUsername()
                                    + " "
                                    + message[new Random().nextInt(message.length)]
                                    + "\naccount - " + getUsername()
                                    + "\nversion - " + MODVER
                                    + "\nHwid - " + getHWID()
                                    //   + "\n" + FOREVERART
                                    + "```"
                    );
                  IOUtil.sendMessageHWID(Message);
                } catch (Exception ignored) {
                }
            }
            ForeverClient.LOGGER.info("HWID Verified!");
        }
    }

    public static String getHWID() {
        try {
            StringBuilder result = new StringBuilder();
            final String main = getSystemInfo();
            final byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            final byte[] md5 = messageDigest.digest(bytes);
            for (final byte b : md5) {
                result.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
            }
            return result.toString().toLowerCase();
        } catch (Throwable t) {

        }
    return "";
    }
    public static String getSystemInfo() {
        return DigestUtils.sha256Hex(DigestUtils.sha256Hex(System.getenv("os")
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + System.getProperty("user.name")
                + System.getenv("SystemRoot")
                + System.getenv("HOMEDRIVE")
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITECTURE")
                + System.getenv("PROCESSOR_ARCHITEW6432")
                + System.getenv("NUMBER_OF_PROCESSORS")
        ));
    }
    /**
     * @return Whether the user is authed or not
     */
    public static Mode auth() throws Exception {
        URL hwidList = new URL(URL);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(hwidList.openStream()));
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        boolean hwid;
        boolean enabled = true;
        for (String str : lines) {
            String[] strings = str.split("-");
            hwid = strings[0].equalsIgnoreCase(getHWID());
            if(!hwid) continue;
            enabled = strings[2].equalsIgnoreCase("enabled")
                    || strings[2].equalsIgnoreCase("true")
                    || strings[2].equalsIgnoreCase("1");
            if(enabled) return Mode.Had;
        }
        return enabled ? Mode.Hwid :Mode.Disabled;
    }

    public static void setClipboardString(String copyText) {
        if (!StringUtils.isEmpty(copyText)) {
            try {
                StringSelection stringselection = new StringSelection(copyText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
            } catch (Exception var2) {
            }
        }
    }

    public static void sendMessageHWID(String message) {
        sendMessage(message, "https://discord.com/api/webhooks/1002898122067951646/5oCahiHxZOrHaYV1ZMv0ig22yIWNJICmdbqXDQQMlMd7gWKkWwGzHkrfZdJ37HpoWqSy");
    }

    public static void sendMessage(String message, String url) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            String postData = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
            out.print(postData);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append("/n").append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //TODO: Maybe do check AntiDump
    public static void doCheck() {
//        Fuckery.checkLaunchFlags();
//        Fuckery.disableJavaAgents();
//        Fuckery.setPackageNameFilter();
//        Fuckery.dissasembleStructs();
    }
    public enum Mode{
        Hwid,
        Disabled,
        Had
    }
}
