package io.github.tofodroid.mods.mimi.common.config.instrument;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.item.IDyeableItem;

public class InstrumentSpec {
    public Byte instrumentId;
    public Integer midiBankNumber;
    public Integer midiPatchNumber;
    public String registryName;
    public Boolean isBlock;
    private Boolean dyeable;
    private Integer defaultColor;
    public List<String> collisionShapes;

    public Boolean isDyeable() {
        return dyeable != null ? dyeable : false;
    }

    public Integer defaultColor() {
        return isDyeable() ? (defaultColor != null ? defaultColor : IDyeableItem.DEFAULT_WHITE_COLOR) : null;
    }
}
