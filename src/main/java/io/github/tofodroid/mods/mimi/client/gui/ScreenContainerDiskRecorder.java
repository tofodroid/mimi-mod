package io.github.tofodroid.mods.mimi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;

import java.io.File;
import java.net.URL;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerDiskRecorder;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.WriteDiskDataPacket;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ScreenContainerDiskRecorder extends ContainerScreen<ContainerDiskRecorder> implements IContainerListener {
    // Texture
    private static final ResourceLocation guiTexture = new ResourceLocation(MIMIMod.MODID, "textures/gui/gui_floppy_disk.png");
    private static final Integer GUI_WIDTH = 176;
    private static final Integer GUI_HEIGHT = 161;
    private static final Integer BUTTON_SIZE = 15;
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;
    
    // Widgets
    private TextFieldWidget nameField;
    private TextFieldWidget urlField;

    private Integer startX;
    private Integer startY;

    private Boolean isValid = false;
    private String nameString = null;
    private String urlString = null;
        
    // Button Boxes
    private static final Vector2f WRITE_DISK_BUTTON = new Vector2f(151,54);

    public ScreenContainerDiskRecorder(ContainerDiskRecorder container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        xSize = GUI_WIDTH;
        ySize = GUI_HEIGHT;
    }

    @Override
    public void init() {
        super.init();
        
        startX = (this.width - this.xSize) / 2;
        startY = (this.height - this.ySize) / 2;

        nameField = this.addListener(new TextFieldWidget(this.font, this.startX + 41, this.startY + 25, 125, 10, StringTextComponent.EMPTY));
        nameField.setMaxStringLength(256);
        nameField.setResponder(this::handleNameChange);
        urlField = this.addListener(new TextFieldWidget(this.font, this.startX + 41, this.startY + 38, 125, 10, StringTextComponent.EMPTY));
        urlField.setMaxStringLength(256);
        urlField.setResponder(this::handlePathChange);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
           this.minecraft.player.closeScreen();
        }
  
        return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canWrite() && !this.urlField.keyPressed(keyCode, scanCode, modifiers) && !this.urlField.canWrite() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(this.isValid && this.container.canWriteDisk() && this.clickedBox(imouseX, imouseY, WRITE_DISK_BUTTON)) {
            // send packet
            WriteDiskDataPacket packet = new WriteDiskDataPacket(nameString, urlString);
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.urlField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(guiTexture);

        this.blit(matrixStack, startX, startY, 0, 0, this.xSize, this.ySize);

        if(!this.isValid || !this.container.canWriteDisk()) {
            this.blit(matrixStack, startX + 151, startY + 54, 177, 0, 17, 17);
        }
    }
    
    @Override
    public void setListener(@Nullable IGuiEventListener listener) {
        if(listener instanceof TextFieldWidget) {
            this.nameField.setFocused2(false);
            this.urlField.setFocused2(false);
            ((TextFieldWidget)listener).setFocused2(true);
        }
        super.setListener(listener);
    }

    protected void handleNameChange(String name) {
        if(name != null && !name.trim().isEmpty()) {
            this.nameString = this.nameField.getText().trim();
        } else {
            this.nameString = null;
        }
        this.updateValidity();
    }

    protected void handlePathChange(String filePath) {
        if(filePath != null && !filePath.trim().isEmpty() && filePath.matches("^.*\\/+.*.midi?$")) {
            try {
                if(new File(filePath).exists()) {
                    this.urlString = filePath.trim();
                    this.urlField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
                } else {
                    throw new RuntimeException("File not found: " + filePath);
                }
            } catch(Exception e) {
                this.urlString = null;
                this.urlField.setTextColor(13112340);
            }
        } else {
            this.urlString = null;
            this.urlField.setTextColor(13112340);
        }
        this.updateValidity();
    }

    // TODO: Currently unused
    protected void handleUrlChange(String url) {
        if(url != null && !url.trim().isEmpty() && url.matches("https?:\\/\\/.*\\/[^\\/,\\s]+\\.midi?$")) {
            try {
                new URL(url);
                this.urlString = url.trim();
                this.urlField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
            } catch(Exception e) {
                this.urlString = null;
                this.urlField.setTextColor(13112340);
            }
        } else {
            this.urlString = null;
            this.urlField.setTextColor(13112340);
        }
        this.updateValidity();
    }

    protected void updateValidity() {
        this.isValid = this.nameString != null && this.urlString != null;
    }
    
    private Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos) {
        Integer buttonMinX = startX + new Float(buttonPos.x).intValue();
        Integer buttonMaxX = buttonMinX + BUTTON_SIZE;
        Integer buttonMinY = startY + new Float(buttonPos.y).intValue();
        Integer buttonMaxY = buttonMinY + BUTTON_SIZE;

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
        this.sendSlotContents(containerToSend, 1, containerToSend.getSlot(1).getStack());
    }

    @Override
    public void sendSlotContents(Container containerToSend, int index, ItemStack stack) {
        if (index == 1) {
            this.nameField.setText(stack.isEmpty() ? "" : stack.getDisplayName().getString());
            this.nameField.setEnabled(!stack.isEmpty());
        }
    }
        

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) { }
}
