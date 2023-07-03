package we.devs.forever.client.command.impl.chunks;

import we.devs.forever.api.manager.impl.config.ConfigManager;
import we.devs.forever.client.command.api.SyntaxChunk;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigChunk extends SyntaxChunk {
    public ConfigChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
      List<String> files = ConfigManager.getConfigList().stream()
              .map(file -> file.getName().replace(".json",""))
              .collect(Collectors.toList());
      for (String str : files) {
          if(str.toLowerCase().startsWith(currentArg.toLowerCase())) {
              return str;
          }
      }
        return super.predict(currentArg);
    }
}
