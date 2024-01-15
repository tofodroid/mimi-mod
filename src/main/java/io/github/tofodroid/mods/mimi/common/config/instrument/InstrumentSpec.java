package io.github.tofodroid.mods.mimi.common.config.instrument;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.item.IColorableItem;

public class InstrumentSpec {
    public Byte instrumentId;
    public Integer midiBankNumber;
    public Integer midiPatchNumber;
    public String registryName;
    public Boolean isBlock;
    private Boolean colorable;
    private Integer defaultColor;
    public List<String> collisionShapes;

    public Boolean isColorable() {
        return colorable != null ? colorable : false;
    }

    public Integer defaultColor() {
        return isColorable() ? (defaultColor != null ? defaultColor : IColorableItem.DEFAULT_WHITE_COLOR) : null;
    }
}
