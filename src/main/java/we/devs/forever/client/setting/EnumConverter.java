package we.devs.forever.client.setting;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public
class   EnumConverter extends Converter<Enum, JsonElement> {

    private final Class<? extends Enum> clazz;

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    public static int currentEnum(Enum clazz) {
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; i++) {
            final Enum e = clazz.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(clazz.name())) {
                return i;
            }
        }
        return -1;
    }

    public static Enum increaseEnum(Enum clazz) {
        int index = currentEnum(clazz);
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; i++) {
            final Enum e = clazz.getClass().getEnumConstants()[i];
            if (i == index + 1) {
                return e;
            }
        }
        return clazz.getClass().getEnumConstants()[0];
    }

    public static Enum notincreaseEnum(Enum clazz) {
        int index = currentEnum(clazz);
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; i++) {
            final Enum e = clazz.getClass().getEnumConstants()[i];
            if (i == index - 1) {
                return e;
            }
        }
        return clazz.getClass().getEnumConstants()[0];
    }

    public static Enum<?> next(Enum<?> entry) {
        Enum<?>[] array = entry.getDeclaringClass().getEnumConstants();
        return array.length - 1 == entry.ordinal()
                ? array[0]
                : array[entry.ordinal() + 1];
    }

    /**
     * @param entry the Enum value to get the previous one from.
     * @return the previous enum value in the enum,
     * or the last one if the given
     * one is the first value in the enum.
     */
    public static Enum<?> previous(Enum<?> entry) {
        Enum<?>[] array = entry.getDeclaringClass().getEnumConstants();
        return entry.ordinal() - 1 < 0 ? array[array.length - 1] : array[entry.ordinal() - 1];
    }

    public static String getProperName(Enum clazz) {
        return Character.toUpperCase(clazz.name().charAt(0)) + clazz.name()./*toLowerCase().*/substring(1);
    }

    @Override
    public JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.toString());
    }

    @Override
    public Enum doBackward(JsonElement jsonElement) {
        try {
            return Enum.valueOf(clazz, jsonElement.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
