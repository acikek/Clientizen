package com.denizenscript.clientizen.objects.properties;

import com.denizenscript.clientizen.mixin.IntPropertyAccessor;
import com.denizenscript.clientizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;

public class MaterialLevel extends IntMaterialProperty {
    public MaterialLevel(String name, MaterialTag material, IntProperty internalProperty) {
        super(name, material, internalProperty);
    }

    static IntProperty[] handledProperties = { Properties.CANDLES, Properties.BITES, Properties.LEVEL_3, Properties.LEVEL_8, Properties.LEVEL_1_8, Properties.LEVEL_15 };

    public static void register() {
        registerTag(ElementTag.class, "minimum_level", (attribute, object) -> {
            return new ElementTag(((IntPropertyAccessor) object.internalProperty).getMin());
        });
        registerTag(ElementTag.class, "maximum_level", (attribute, object) -> {
            return new ElementTag(((IntPropertyAccessor) object.internalProperty).getMax());
        });
        IntMaterialProperty.register();
    }
}
