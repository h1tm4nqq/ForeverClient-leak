package we.devs.forever.client.command.impl.chunks;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.command.api.SyntaxChunk;

public class KeyChunk extends SyntaxChunk {
    public KeyChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
        for (int i = 0; i < 84; i++) {
            try {
                if (Keyboard.getKeyName(i).toLowerCase().startsWith(currentArg.toLowerCase())) {
                    return Keyboard.getKeyName(i);
                }
            } catch (NullPointerException oops) {
                oops.printStackTrace();
            }
        }
        return currentArg;
    }
}
