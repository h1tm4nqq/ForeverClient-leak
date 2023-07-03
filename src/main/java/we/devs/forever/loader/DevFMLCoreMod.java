package we.devs.forever.loader;


import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.util.Objects;

public class DevFMLCoreMod {

    public static void load() throws IOException {
        MixinCache.refmapBytes = IOUtils.toByteArray(Objects.requireNonNull(DevFMLCoreMod.class.getResourceAsStream("/mixins.forever.refmap.json")));
        MixinCache.mixinBytes = IOUtils.toByteArray(Objects.requireNonNull(DevFMLCoreMod.class.getResourceAsStream("/mixins.forever.json")));


    }

}
