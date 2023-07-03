package we.devs.forever.client.setting;

import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.Colors;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.main.ForeverClient;

import java.awt.*;
import java.util.function.Predicate;

//TODO: visibility
public
class Setting<T> extends Client {

    private final String name;

    private final T defaultValue;
    float brightness, saturation;
    private T value;
    private T plannedValue;
    private T min;
    private T max;
    private int speed;
    private Color color;
    private boolean hasRestriction, shouldRenderStringName;
    private ColorPickerButton.Mode colorMode;
    public boolean hide = false;

    private Predicate<T> visibility;
    private String fieldName = "NULL";

    private String description;
    private Client feature;

    public Setting(final String name, final T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = "";
    }

    public Setting(final String name, final T defaultValue, final String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = description;
    }
    public Setting(final String name, final T defaultValue, final String description, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility;
        this.plannedValue = defaultValue;
        this.description = description;
    }

    public Setting(final String name, final T defaultValue, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility;
        this.plannedValue = defaultValue;
    }

    public Setting(final String name, final T defaultValue, final T min, final T max, final String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.description = description;
        this.hasRestriction = true;
    }

    public Setting(final String name, final T defaultValue, ColorPickerButton.Mode colorMode, int speed, final String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.colorMode = colorMode;
        this.color = (Color) defaultValue;
        this.speed = speed;
        this.plannedValue = defaultValue;
        this.description = description;
        this.hasRestriction = true;
    }

    public Setting(final String name, final T defaultValue, ColorPickerButton.Mode colorMode, int speed) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.colorMode = colorMode;
        this.speed = speed;
        this.color = (Color) defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.hasRestriction = true;
    }
    public Setting(final String name, final T defaultValue, ColorPickerButton.Mode colorMode, int speed,boolean hide) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.colorMode = colorMode;
        this.speed = speed;
        this.color = (Color) defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.hasRestriction = true;
        this.hide = hide;
    }
    public Setting(final String name, final T defaultValue, ColorPickerButton.Mode colorMode, int speed, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.colorMode = colorMode;
        this.speed = speed;
        this.color = (Color) defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.hasRestriction = true;
    }

    public Setting(final String name, final T defaultValue, ColorPickerButton.Mode colorMode, int speed, final String description, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.colorMode = colorMode;
        this.color = (Color) defaultValue;
        this.speed = speed;
        this.description = description;
        this.visibility = visibility;
        this.hasRestriction = true;
    }

    public Setting(final String name, final T defaultValue, final T min, final T max) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.description = "";
        this.hasRestriction = true;
    }

    public Setting(final String name, final T defaultValue, final T min, final T max, final String description, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.description = description;
        this.hasRestriction = true;
    }


    public Setting(final String name, final T defaultValue, final T min, final T max, final Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.description = "";
        this.hasRestriction = true;
    }
    public void setFieldName(String s) {
        fieldName = s;
    }
    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.setPlannedValue(value);
        if (hasRestriction) {
            if (((Number) min).floatValue() > ((Number) value).floatValue()) {
                this.setPlannedValue(min);
            }

            if (((Number) max).floatValue() < ((Number) value).floatValue()) {
                this.setPlannedValue(max);
            }
        }
        ClientEvent event = new ClientEvent(this);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }
    }

    public T getPlannedValue() {
        return this.plannedValue;
    }

    public void setPlannedValue(T value) {
        this.plannedValue = value;
    }

    public boolean getRainbow() {
        return colorMode.equals(ColorPickerButton.Mode.Rainbow);
    }
    public ColorPickerButton.Mode getColorMode() {
        return colorMode;
    }

    @Deprecated
    public void setRainbow(boolean rainbow) {
        throw new RuntimeException("old");
    }

    public Color getColor() {
        Color c;
        if (colorMode.equals(ColorPickerButton.Mode.Rainbow)) {
            c = new Color(Color.HSBtoRGB(RenderUtil.getHui(speed * 100), saturation, brightness));
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), color.getAlpha());
        } else if(colorMode.equals(ColorPickerButton.Mode.Sync)) {
            c = c = new Color(Colors.getInstance().getCurrentColor().getRed(), Colors.getInstance().getCurrentColor().getGreen(), Colors.getInstance().getCurrentColor().getBlue(), color.getAlpha());
        }
        else {
            c = color;
        }
        return c;
    }

    public float[] getHSB() {
        return new float[]{saturation, brightness};
    }

    public int getSpeedColor() {
        return speed;
    }

    public void setSpeedColor(int speed) {
        this.speed = speed;
    }

    public void setColor(float saturation, float brightness, Color color) {
        this.saturation = saturation;
        this.brightness = brightness;
        this.color = color;
        this.setPlannedValue((T) color);
        ClientEvent event = new ClientEvent(this);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }
    }

    public T getMin() {
        return this.min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return this.max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public void setValueNoEvent(T value) {
        this.setPlannedValue(value);
        if (hasRestriction) {
            if (((Number) min).floatValue() > ((Number) value).floatValue()) {
                this.setPlannedValue(min);
            }

            if (((Number) max).floatValue() < ((Number) value).floatValue()) {
                this.setPlannedValue(max);
            }
        }
        this.value = this.plannedValue;
    }

    public Client getFeature() {
        return this.feature;
    }

    public void setFeature(Client feature) {
        this.feature = feature;
    }


    public String currentEnumName() {
        return EnumConverter.getProperName((Enum) this.value);
    }

    public void increaseEnum() {
        this.plannedValue = (T) EnumConverter.next((Enum) this.value);
        ClientEvent event = new ClientEvent(this);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }

    }

    public void notincreaseEnum() {
        this.plannedValue = (T) EnumConverter.previous((Enum) this.value);
        ClientEvent event = new ClientEvent(this);
        ForeverClient.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.value = this.plannedValue;
        } else {
            this.plannedValue = this.value;
        }

    }

    public void increaseEnumNoEvent() {
        this.value = (T) EnumConverter.increaseEnum((Enum) this.value);
    }

    public String getType() {
        if (this.isEnumSetting()) {
            return "Enum";
        }
        return this.getClassName(this.defaultValue);
    }

    public <T> String getClassName(T value) {
        return value.getClass().getSimpleName();
    }

    public String getDescription() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public boolean isNumberSetting() {
        return (value instanceof Double || value instanceof Integer || value instanceof Short || value instanceof Long || value instanceof Float);
    }
    public boolean isFloat() {
        return (value instanceof Double || value instanceof Float);
    }

    public boolean isEnumSetting() {
        return value instanceof Enum;
    }

    public boolean isColorSetting() {
        return value instanceof Color;
    }

    public boolean isStringSetting() {
        return value instanceof String;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public String getValueAsString() {
        return this.value.toString();
    }

    public boolean hasRestriction() {
        return this.hasRestriction;
    }

    public void setVisibility(Predicate<T> visibility) {
        this.visibility = visibility;
    }

    public Setting setRenderName(boolean renderName) {
        this.shouldRenderStringName = renderName;
        return this;
    }

    public boolean shouldRenderName() {
        if (this.isStringSetting()) return this.shouldRenderStringName;
        return true;
    }

    public boolean isVisible() {
        if (visibility == null) {
            return true;
        }
        return visibility.test(getValue());
    }

    public void setMode(ColorPickerButton.Mode mode) {
        colorMode = mode;
    }
}
