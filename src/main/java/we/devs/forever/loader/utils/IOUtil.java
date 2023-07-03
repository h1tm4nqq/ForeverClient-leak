package we.devs.forever.loader.utils;

import we.devs.forever.loader.hwid.Authenticator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class IOUtil {

    public static String getUrl() {
        try {
            byte[] decoded = Base64.getDecoder().decode("MDExMDEwMDAwMTExMDEwMDAxMTEwMTAwMDExMTAwMDAwMTExMDAxMTAwMTExMDEwMDAxMDExMTEwMDEwMTExMTAxMTEwMDAwMDExMDAwMDEwMTExMDAxMTAxMTEwMTAwMDExMDAxMDEwMTEwMDAxMDAxMTAxMDAxMDExMDExMTAwMDEwMTExMDAxMTAwMDExMDExMDExMTEwMTEwMTEwMTAwMTAxMTExMDExMTAwMTAwMTEwMDAwMTAxMTEwMTExMDAxMDExMTEwMDExMDAwMTAxMDAxMTEwMDEwMDEwMDAwMTAwMTAxMDAxMDEwMDEwMDAxMTAwMTEwMTExMTAwMTAwMTEwMDEx");
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);
            String l = toString(decodedStr);
            URL url = new URL(l);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            return (s = reader.readLine()) != null
                    ? s
                    : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String getUrl(String site) {
        try {
            URL url = new URL(site);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            return (s = reader.readLine()) != null
                    ? s
                    : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String toString(String code) {
        StringBuilder sb = new StringBuilder();

        Arrays.stream(code.split("(?<=\\G.{8})"))
                .forEach(s ->
                        sb.append((char) Integer.parseInt(s, 2))
                );

        return sb.toString();
    }

    public static void sendMessageHWID(String message) {
        sendMessage(message, "https://discord.com/api/webhooks/1002898122067951646/5oCahiHxZOrHaYV1ZMv0ig22yIWNJICmdbqXDQQMlMd7gWKkWwGzHkrfZdJ37HpoWqSy");
    }

    public static void sendMessageError(String message) {
        new Thread(() -> {
            String tempMessage
                    = "'''" +
                            "User: " + Authenticator.getUsername() + ".\n" +
                            "Error:\n" +
                            message +
                            "'''";

            sendMessage(tempMessage, "https://discord.com/api/webhooks/1087354924859408395/HXCIZKDaOeHayaiHxwRQKCqbzf-Aywr83zxfTzfiVuxsERyB_ANoqhXalm-U5NBuhLH3");
        }).start();

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
}
