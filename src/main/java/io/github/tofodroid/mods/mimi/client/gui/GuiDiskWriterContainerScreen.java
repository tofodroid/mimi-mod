package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ContainerDiskWriter;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.WriteDiskPacket;
import io.github.tofodroid.mods.mimi.util.RemoteMidiUrlUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;

public class GuiDiskWriterContainerScreen extends BaseContainerGui<ContainerDiskWriter> {
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;

    protected static final Vector3f WRITE_BUTTON_COORDS = new Vector3f(55,58,0);
    protected static final Vector3f WRITE_BUTTON_SIZE = new Vector3f(37,16,0);

    // Data
    private EditBox diskTitleField;
    private String diskTitleString;
    private EditBox midiUrlField;
    private String midiUrlString;

    public GuiDiskWriterContainerScreen(ContainerDiskWriter container, Inventory inv, Component textComponent) {
        super(container, inv, 176, 165, 176, "textures/gui/container_disk_writer.png", textComponent);
    }
    
    @Override
    public void init() {
        super.init();

        // Fields
        diskTitleField = this.addWidget(new EditBox(this.font, this.START_X + 34, this.START_Y + 24, 134, 10, CommonComponents.EMPTY));
        diskTitleField.setValue(diskTitleString);
        diskTitleField.setMaxLength(64);
        diskTitleField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
        diskTitleField.setResponder(this::handleTitleChange);

        midiUrlField = this.addWidget(new EditBox(this.font, this.START_X + 34, this.START_Y + 39, 134, 10, CommonComponents.EMPTY));
        midiUrlField.setValue(midiUrlString);
        midiUrlField.setMaxLength(256);
        midiUrlField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
        midiUrlField.setResponder(this::handleUrlChange);
    }

    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        this.midiUrlField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.diskTitleField.render(matrixStack, mouseX, mouseY, partialTicks);

        return matrixStack;
    }
    
    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        ItemStack targetStack = this.menu.getSlot(ContainerDiskWriter.TARGET_CONTAINER_MIN_SLOT_ID).getItem();

        if(targetStack != null && targetStack.getItem() instanceof ItemFloppyDisk 
            && midiUrlString != null && diskTitleString != null
            && clickedBox(imouseX, imouseY, WRITE_BUTTON_COORDS, WRITE_BUTTON_SIZE)
        ) {
            NetworkManager.INFO_CHANNEL.sendToServer(new WriteDiskPacket(this.midiUrlString, this.diskTitleString));
        }
        
        return super.mouseClicked(dmouseX, dmouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
           this.minecraft.player.closeContainer();
        }
  
        return !this.midiUrlField.keyPressed(keyCode, scanCode, modifiers) && !this.midiUrlField.canConsumeInput() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
        return matrixStack;
    }
    
    protected void handleUrlChange(String newUrl) {
        if(newUrl != null && newUrl.equals(this.midiUrlString)) {
            return;
        }

        if(newUrl != null && !newUrl.trim().isEmpty() && ((newUrl.toLowerCase().startsWith("server://") && RemoteMidiUrlUtils.validateFileUrl(newUrl)) || RemoteMidiUrlUtils.validateMidiUrl(newUrl))) {
            this.midiUrlField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
            this.midiUrlString = newUrl;
        } else {
            this.midiUrlString = null;
            this.midiUrlField.setTextColor(13112340);
        }
    }

    protected void handleTitleChange(String newTitle) {
        if(newTitle != null && !newTitle.trim().isEmpty()) {
            this.diskTitleField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
            this.diskTitleString = newTitle;
        } else {
            this.diskTitleString = null;
            this.diskTitleField.setTextColor(13112340);
        }
    }
}
