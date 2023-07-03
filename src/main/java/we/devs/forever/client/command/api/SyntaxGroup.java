package we.devs.forever.client.command.api;

import java.util.ArrayList;
import java.util.Collections;

public class SyntaxGroup {
    private final ArrayList<SyntaxChunk> syntaxChunks = new ArrayList<>();

    public SyntaxGroup(SyntaxChunk... chunks) {

        Collections.addAll(syntaxChunks, chunks);
        syntaxChunks.forEach(syntaxChunk -> syntaxChunk.setGroup(this));
    }

    public ArrayList<SyntaxChunk> getSyntaxChunks() {
        return syntaxChunks;
    }

    public <T> T findChunk(Class<T> clazz) {
        for (SyntaxChunk chunk : syntaxChunks) {
            if (chunk.getClass().equals(clazz)) {
                return (T) chunk;
            }
        }
        return null;
    }
    public <T> T findChunk(String str) {
        for (SyntaxChunk chunk : syntaxChunks) {
            if (chunk.getName().equals(str)) {
                return (T) chunk;
            }
        }
        return null;
    }
}
