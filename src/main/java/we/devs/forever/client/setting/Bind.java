package we.devs.forever.client.setting;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.input.Keyboard;

public
class Bind {

    private int key;

    public Bind(int key) {
        this.key = key;
    }

    public static Bind none() {
        return new Bind(Keyboard.KEY_NONE);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEmpty() {
        return key < 0;
    }

    @Override
    public String toString() {
        return isEmpty() ? "None" : (key < 0 ? "None" : capitalise(Keyboard.getKeyName(key)));
    }

    public boolean isDown() {
        return !isEmpty()
                &&(Keyboard.isKeyDown(getKey())) ;
    }

    private String capitalise(String str) {
        if (str.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }

    public static
    class BindConverter extends Converter<Bind, JsonElement> {
        @Override
        public JsonElement doForward(Bind bind) {
            return new JsonPrimitive(bind.toString());
        }

        @Override
        public Bind doBackward(JsonElement jsonElement) {
            String s = jsonElement.getAsString();
            if (s.equalsIgnoreCase("None")) {
                return Bind.none();
            }

            int key = -1;
            try {
                key = Keyboard.getKeyIndex(s.toUpperCase());
            } catch (Exception ignored) {
            }

            if (key == 0) {
                return Bind.none();
            }
            return new Bind(key);
        }
    }
}
