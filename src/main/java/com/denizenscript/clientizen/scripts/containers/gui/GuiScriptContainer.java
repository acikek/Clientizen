package com.denizenscript.clientizen.scripts.containers.gui;

import com.denizenscript.clientizen.objects.ItemTag;
import com.denizenscript.clientizen.scripts.containers.gui.elements.*;
import com.denizenscript.clientizen.tags.ClientizenTagContext;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GuiScriptContainer extends ScriptContainer {

    private static final Map<String, GuiElementParser> guiElementParsers = new HashMap<>();

    static {
        registerGuiElement("plain_panel", new PlainPanelElement());
        registerGuiElement("tab_panel", new TabPanelElement());
        registerGuiElement("scroll_panel", new ScrollPanelElement());
        registerGuiElement("grid_panel", new GridPanelElement());
        registerGuiElement("box", new BoxElement());
        registerGuiElement("button", new ButtonElement());
        registerGuiElement("text", new TextElement());
        registerGuiElement("label", new LabelElement());
        registerGuiElement("dynamic_label", new DynamicLabelElement());
        registerGuiElement("item", new ItemElement());
        registerGuiElement("text_field", new TextFieldElement());
        registerGuiElement("slider", new SliderElement());
        registerGuiElement("labeled_slider", new LabeledSliderElement());
        registerGuiElement("bar", new BarElement());
    }

    public static void registerGuiElement(String typeName, GuiElementParser parser) {
        if (guiElementParsers.putIfAbsent(typeName, parser) != null) {
            throw new InvalidArgumentsRuntimeException("A GUI element with type '" + typeName + "' is already registered.");
        }
    }

    public GuiScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    @FunctionalInterface
    public interface GuiElementParser {
        WWidget parse(GuiScriptContainer container, YamlConfiguration config, String pathToElement, TagContext context);
    }

    public static String getTaggedString(YamlConfiguration config, String path, TagContext context) {
        return getTaggedString(config, path, null, context);
    }

    public static String getTaggedString(YamlConfiguration config, String path, String defaultValue, TagContext context) {
        String str = config.getString(path);
        return str != null ? TagManager.tag(str, context) : defaultValue;
    }

    public static Integer getTaggedInt(YamlConfiguration config, String path, TagContext context) {
        return getTaggedInt(config, path, null, context);
    }

    public static Integer getTaggedInt(YamlConfiguration config, String path, Integer defaultValue, TagContext context) {
        String str = getTaggedString(config, path, context);
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException numberFormatException) {
            Debug.echoError("Invalid integer number specified under '" + path + "': " + str + '.');
            return null;
        }
    }

    public static Float getTaggedFloat(YamlConfiguration config, String path, TagContext context) {
        return getTaggedFloat(config, path, null, context);
    }

    public static Float getTaggedFloat(YamlConfiguration config, String path, Float defaultValue, TagContext context) {
        String str = getTaggedString(config, path, context);
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        }
        catch (NumberFormatException numberFormatException) {
            Debug.echoError("Invalid number specified under '" + path + "': " + str + '.');
            return null;
        }
    }

    public static Boolean getTaggedBoolean(YamlConfiguration config, String path, TagContext context) {
        String str = getTaggedString(config, path, context);
        if (str == null) {
            return null;
        }
        boolean equalsTrue = CoreUtilities.equalsIgnoreCase(str, "true");
        if (!equalsTrue && !CoreUtilities.equalsIgnoreCase(str, "false")) {
            Debug.echoError("Invalid boolean '" + str + "' specified under '" + path + "': must be either 'true' or 'false'.");
            return null;
        }
        return equalsTrue;
    }

    public static <T extends ObjectTag> T getTaggedObject(Class<T> objectType, YamlConfiguration config, String path, TagContext context) {
        String str = config.getString(path);
        if (str == null) {
            return null;
        }
        T converted = TagManager.tagObject(str, context).asType(objectType, context);
        if (converted == null) {
            Debug.echoError("Invalid " + DebugInternals.getClassNameOpti(objectType) + " specified under '" + path + "': " + str + '.');
            return null;
        }
        return converted;
    }

    public static <T extends ObjectTag> List<T> getTaggedObjectList(Class<T> objectType, YamlConfiguration config, String path, TagContext context) {
        Object object = config.get(path);
        if (object == null) {
            return null;
        }
        ListTag list = CoreUtilities.objectToTagForm(object, context, true, true, true).asType(ListTag.class, context);
        if (list == null) {
            return null;
        }
        return list.filter(objectType, context);
    }

    public static List<String> getTaggedStringList(YamlConfiguration config, String path, TagContext context) {
        List<String> stringList = config.getStringList(path);
        if (stringList == null) {
            return null;
        }
        List<String> taggedList = new ArrayList<>(stringList.size());
        for (String str : stringList) {
            taggedList.add(TagManager.tag(ScriptBuilder.stripLinePrefix(str), context));
        }
        return taggedList;
    }

    public static <T extends Enum<T>> T getTaggedEnum(Class<T> enumClass, YamlConfiguration config, String path, TagContext context) {
        return getTaggedEnum(enumClass, null, config, path, context);
    }

    public static <T extends Enum<T>> T getTaggedEnum(Class<T> enumClass, T defaultValue, YamlConfiguration config, String path, TagContext context) {
        String str = getTaggedString(config, path, context);
        if (str == null) {
            return defaultValue;
        }
        T converted = ElementTag.asEnum(enumClass, str);
        if (converted == null) {
            Debug.echoError("Invalid '" + path + "' value '" + str + "': must be one of " + Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", ")) + '.');
            return null;
        }
        return converted;
    }

    public static class ContextStringSupplier implements Supplier<String> {

        public String context;

        @Override
        public String get() {
            return context;
        }
    }

    public static ContextStringSupplier currentContextSupplier = null;

    public WPanel createGUIRoot() {
        currentContextSupplier = new ContextStringSupplier();
        Debug.pushErrorContext(currentContextSupplier);
        WWidget created = createGUI();
        Debug.popErrorContext();
        currentContextSupplier = null;
        if (!(created instanceof WPanel rootPanel)) {
            Debug.echoError("Invalid GUI script '" + getOriginalName() + "': must have a panel as the root element.");
            return null;
        }
        return rootPanel;
    }

    public WWidget createGUI() {
        return parseGUIWidget(getContents(), "", getOriginalName(), new ClientizenTagContext(this));
    }

    public WWidget parseGUIWidget(YamlConfiguration config, String key, String pathToWidgetConfig, TagContext context) {
        if (config == null) {
            return null;
        }
        String pathToWidget = key.isEmpty() ? pathToWidgetConfig : pathToWidgetConfig + '.' + key;
        YamlConfiguration widgetConfig = config.getConfigurationSection(key);
        if (widgetConfig == null) {
            String guiScriptName = config.getString(key);
            GuiScriptContainer guiScript = ScriptRegistry.getScriptContainerAs(guiScriptName, GuiScriptContainer.class);
            if (guiScript == null) {
                Debug.echoError("Invalid GUI script container specified for GUI element '" + pathToWidget + "': " + guiScriptName + '.');
                return null;
            }
            return guiScript.createGUI();
        }
        String uiType = widgetConfig.getString("ui_type");
        if (uiType == null) {
            Debug.echoError("Invalid GUI element '" + pathToWidget + "' is missing a type!");
            return null;
        }
        GuiElementParser parser = guiElementParsers.get(CoreUtilities.toLowerCase(uiType));
        if (parser == null) {
            Debug.echoError("Invalid type specified for GUI element '" + pathToWidget + "': " + uiType + '.');
            return null;
        }
        Integer width = getTaggedInt(widgetConfig, "width", 18, context), height = getTaggedInt(widgetConfig, "height", 18, context);
        if (width == null || height == null) {
            Debug.echoError("Invalid GUI element '" + pathToWidget + "': must have valid width and height.");
            return null;
        }
        Integer x = getTaggedInt(widgetConfig, "x", 0, context), y = getTaggedInt(widgetConfig, "y", 0, context);
        if (x == null || y == null) {
            Debug.echoError("Invalid GUI element '" + pathToWidget + "': must have valid x and y values.");
            return null;
        }
        String previousContext = currentContextSupplier.context;
        currentContextSupplier.context = "while parsing GUI element '<A>" + pathToWidget + "<LR>' of type '<A>" + uiType + "<LR>'";
        WWidget widget = parser.parse(this, widgetConfig, pathToWidget, context);
        currentContextSupplier.context = previousContext;
        if (widget == null) {
            return null;
        }
        widget.setLocation(x, y);
        widget.setSize(width, height);
        return widget;
    }

    public static void applyInsets(YamlConfiguration config, Consumer<Insets> setter, TagContext context) {
        Insets insets = parseInsets(config.getConfigurationSection("insets"), context);
        if (insets != null) {
            setter.accept(insets);
        }
    }

    public static Insets parseInsets(YamlConfiguration config, TagContext context) {
        if (config == null) {
            return null;
        }
        Integer all = getTaggedInt(config, "all", context);
        if (all != null) {
            return new Insets(all);
        }
        Integer top = getTaggedInt(config, "top", context),
                left = getTaggedInt(config, "left", context),
                bottom = getTaggedInt(config, "bottom", context),
                right = getTaggedInt(config, "right", context);
        if (top == null || left == null || bottom == null || right == null) {
            Debug.echoError("Invalid insets: must have top/left/bottom/right values.");
            return null;
        }
        return new Insets(top, left, bottom, right);
    }

    public static Texture parseTexture(YamlConfiguration config, String path, TagContext context) {
        if (config == null) {
            return null;
        }
        YamlConfiguration textureConfig = config.getConfigurationSection(path);
        String pathStr = textureConfig != null ? getTaggedString(textureConfig, "texture", context) : getTaggedString(config, path, context);
        if (pathStr == null) {
            Debug.echoError("Invalid texture: must specify a texture path.");
            return null;
        }
        Identifier texturePath = Identifier.tryParse(pathStr);
        if (texturePath == null) {
            Debug.echoError("Invalid texture: invalid texture path '" + pathStr + "' specified.");
            return null;
        }
        if (textureConfig == null) {
            return new Texture(texturePath);
        }
        Texture.Type type = getTaggedEnum(Texture.Type.class, Texture.Type.STANDALONE, textureConfig, "type", context);
        if (type == null) {
            Debug.echoError("Invalid texture: invalid type specified.");
            return null;
        }
        Float uStart = getTaggedFloat(textureConfig, "u_start", 0f, context),
                vStart = getTaggedFloat(textureConfig, "v_start", 0f, context),
                uEnd = getTaggedFloat(textureConfig, "u_end", 1f, context),
                vEnd = getTaggedFloat(textureConfig, "v_end", 1f, context);
        if (uStart == null || vStart == null || uEnd == null || vEnd == null) {
            Debug.echoError("Invalid texture: invalid UV coordinates specified.");
            return null;
        }
        return new Texture(texturePath, type, uStart, vStart, uEnd, vEnd);
    }

    public static Icon parseIcon(YamlConfiguration config, String path, TagContext context) {
        if (config == null) {
            return null;
        }
        ObjectTag itemObject = getTaggedObject(ObjectTag.class, config, path, context);
        if (itemObject != null) {
            ItemTag item = itemObject.asType(ItemTag.class, CoreUtilities.noDebugContext);
            if (item != null) {
                return new ItemIcon(item.getStack());
            }
        }
        Texture texture = parseTexture(config, path, context);
        if (texture == null) {
            Debug.echoError("Invalid icon: must have a valid item or texture.");
            return null;
        }
        return new TextureIcon(texture);
    }
}
