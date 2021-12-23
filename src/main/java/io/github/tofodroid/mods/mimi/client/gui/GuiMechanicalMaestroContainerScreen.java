package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiMechanicalMaestroContainerScreen extends ASwitchboardBlockGui<ContainerMechanicalMaestro> {
    public GuiMechanicalMaestroContainerScreen(ContainerMechanicalMaestro container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }

	@Override
	public void loadSelectedSwitchboard() {
		super.loadSelectedSwitchboard();
		this.allNotesOff();
	}

	@Override
	public void clearSwitchboard() {
		super.clearSwitchboard();
		this.allNotesOff();
	}
    
    @Override
    protected Vector3f titleBoxPos() {
        return new Vector3f(78,8,0);
	}

    @Override
    protected Vector3f titleBoxBlit() {
        return new Vector3f(0,265,0);
    }

    @Override
    protected Vector3f titleBoxSize() {
        return new Vector3f(171,16,0);
    }
    
    @Override
    protected Vector3f switchboardSlotPos() {
        return new Vector3f(9,205,0);
    }

    protected Vector3f instrumentSlotPos() {
        return new Vector3f(9,168,0);
    }
    
    @Override
    protected Boolean channelWidgetEnabled() {
        return true;
    }
    
    @Override
    protected Boolean linkedTransmitterWidgetEnabled() {
        return true;
    }

    @Override
    protected Boolean instrumentVolumeWidgetEnabled() {
        return true;
    }

    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack = super.renderGraphics(matrixStack, mouseX, mouseY, partialTicks);

        // Instrument Slot
        blit(matrixStack, START_X + Float.valueOf(instrumentSlotPos().x()).intValue(), START_Y + Float.valueOf(instrumentSlotPos().y()).intValue(), this.getBlitOffset(), 1, 367, 140, 28, TEXTURE_SIZE, TEXTURE_SIZE);

        return matrixStack;
    }

    private void allNotesOff() {
		BlockEntity tile = player.level.getBlockEntity(container.getTilePos());
		TileMechanicalMaestro mechTile = tile != null && ModTiles.MECHANICALMAESTRO.equals(tile.getType()) ? (TileMechanicalMaestro) tile : null;

		if(mechTile != null) {
			mechTile.allNotesOff();
		}
    }
}