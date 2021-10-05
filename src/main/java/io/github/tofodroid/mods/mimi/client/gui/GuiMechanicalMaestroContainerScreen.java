package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class GuiMechanicalMaestroContainerScreen extends ASwitchboardBlockGui<ContainerMechanicalMaestro> {
    public GuiMechanicalMaestroContainerScreen(ContainerMechanicalMaestro container, PlayerInventory inv, ITextComponent textComponent) {
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
    protected Vector2f titleBoxPos() {
        return new Vector2f(78,8);
	}

    @Override
    protected Vector2f titleBoxBlit() {
        return new Vector2f(0,265);
    }

    @Override
    protected Vector2f titleBoxSize() {
        return new Vector2f(171,16);
    }
    
    @Override
    protected Vector2f switchboardSlotPos() {
        return new Vector2f(9,205);
    }

    protected Vector2f instrumentSlotPos() {
        return new Vector2f(9,168);
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
    protected MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack = super.renderGraphics(matrixStack, mouseX, mouseY, partialTicks);

        // Instrument Slot
        blit(matrixStack, this.guiLeft + new Float(instrumentSlotPos().x).intValue(), this.guiTop + new Float(instrumentSlotPos().y).intValue(), this.getBlitOffset(), 1, 367, 140, 28, TEXTURE_SIZE, TEXTURE_SIZE);

        return matrixStack;
    }

    private void allNotesOff() {
		TileEntity tile = player.world.getTileEntity(container.getTilePos());
		TileMechanicalMaestro mechTile = tile != null && ModTiles.MECHANICALMAESTRO.equals(tile.getType()) ? (TileMechanicalMaestro) tile : null;

		if(mechTile != null) {
			mechTile.allNotesOff();
		}
    }
}