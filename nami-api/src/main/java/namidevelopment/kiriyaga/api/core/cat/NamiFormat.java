package namidevelopment.kiriyaga.api.core.cat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

/*
Simple color formatter for minecraft Component
inspired by Cat-Format by cattyn
TODO: someday we need to write our own formatting api to avoid touching minecraft text components at all
 */
public class NamiFormat {

    protected final Map<String, Integer> static_colors = new HashMap<>();
    protected final Map<String, IntSupplier> dynamic_colors = new HashMap<>();

    public NamiFormat() {}

    public void add(String key, int rgb) {
        static_colors.put(key.toLowerCase(), rgb);
    }

    public void add(String key, Color color) {
        add(key, color.getRGB() & 0xFFFFFF);
    }

    public void addDynamic(String key, IntSupplier supplier) {
        dynamic_colors.put(key.toLowerCase(), supplier);
    }

    public void add(Object styleClass) {
        Class<?> clazz = styleClass.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) continue;
            if (!Color.class.isAssignableFrom(method.getReturnType())) continue;

            method.setAccessible(true);

            String name = method.getName().toLowerCase();

            if (Modifier.isStatic(method.getModifiers())) {
                try {
                    Color c = (Color) method.invoke(null);
                    if (c == null) continue;

                    add(name, c);
                } catch (Exception ignored) {}
                continue;
            }
            dynamic_colors.put(name, () -> {
                try {
                    Color c = (Color) method.invoke(styleClass);
                    if (c == null) return 0xFFFFFF;

                    return c.getRGB() & 0xFFFFFF;
                } catch (Exception e) {
                    return 0xFFFFFF;
                }
            });
        }
    }

    private Integer getColor(String tag) {
        if (static_colors.containsKey(tag)) {
            return static_colors.get(tag);
        }

        IntSupplier dyn = dynamic_colors.get(tag);
        if (dyn != null) {
            return dyn.getAsInt();
        }

        return null;
    }

    public MutableComponent format(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty().copy();
        }

        MutableComponent result = Component.empty().copy();
        Style currentStyle = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);

            if (ch == '{') {
                int end = input.indexOf('}', i);
                if (end != -1) {
                    String tag = input.substring(i + 1, end).trim().toLowerCase();

                    if (!buffer.isEmpty()) {
                        result.append(Component.literal(buffer.toString()).setStyle(currentStyle));
                        buffer.setLength(0);
                    }

                    Integer rgb = getColor(tag);
                    if (rgb != null) {
                        currentStyle = currentStyle.withColor(rgb);
                    }

                    i = end + 1;
                    continue;
                }
            }

            buffer.append(ch);
            i++;
        }

        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(currentStyle));
        }

        return result;
    }

    public MutableComponent format(Component component) {
        if (component == null) return Component.empty().copy();
        return format(component.getString());
    }
}
