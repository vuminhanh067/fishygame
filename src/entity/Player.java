package entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import input.MouseHandler;

public class Player extends Entity {
    GamePanel gp;
    MouseHandler mouseH;
    
    // Constants
    final int EAT_FRAMES = 6, IDLE_FRAMES = 6, SWIM_FRAMES = 15, TURN_FRAMES = 5;
    final int BASE_WIDTH = 125, BASE_HEIGHT = 105; 
    public BufferedImage[] eatFrames, idleFrames, swimFrames, turnFrames;
    public BufferedImage upBubble; 
    
    private double exactX, exactY;
    private double easing = 0.05;
    private String currentFacing = "right"; 
    private int currentLevel = 1; 
    private int effectCounter = 0; 
    private boolean showEffect = false;
    
    // >> BIẾN BẤT TỬ
    public boolean invincible = false;
    public int invincibleCounter = 0;

    public Player(GamePanel gp, MouseHandler mouseH) {
        this.gp = gp;
        this.mouseH = mouseH;
        eatFrames = new BufferedImage[EAT_FRAMES];
        idleFrames = new BufferedImage[IDLE_FRAMES];
        swimFrames = new BufferedImage[SWIM_FRAMES];
        turnFrames = new BufferedImage[TURN_FRAMES];
        setDefaultValues();
        getPlayerImageByLoop(); 
    }

    public void setDefaultValues() {
        currentLevel = 1;
        updateSize(1.0); 
        resetPosition();
        speed = 5; 
        state = "idle";
        direction = "right";
        currentFacing = "right";
        invincible = false;
    }
    
    // >> HÀM RESET VỊ TRÍ AN TOÀN
    public void resetPosition() {
        x = gp.worldWidth / 2 - width / 2;
        y = gp.worldHeight / 2 - height / 2;
        exactX = x;
        exactY = y;
        solidArea = new Rectangle((int)x, (int)y, width, height);
    }
    
    // >> HÀM KÍCH HOẠT BẤT TỬ
    public void enableInvincibility() {
        invincible = true;
        invincibleCounter = 180; // 3 giây
    }
    
    private void updateSize(double scale) {
        this.width = (int)(BASE_WIDTH * scale);
        this.height = (int)(BASE_HEIGHT * scale);
        if (solidArea != null) { solidArea.width = width; solidArea.height = height; }
    }

    public void getPlayerImageByLoop() {
        try {
            for (int i = 0; i < EAT_FRAMES; i++) 
                eatFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/angelfish/angelfisheat" + (i + 1) + ".png"));
            for (int i = 0; i < IDLE_FRAMES; i++) 
                idleFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/angelfish/angelfishidle" + (i + 1) + ".png"));
            for (int i = 0; i < SWIM_FRAMES; i++) 
                swimFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/angelfish/angelfishswim" + (i + 1) + ".png"));
            for (int i = 0; i < TURN_FRAMES; i++) 
                turnFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/angelfish/angelfishturn" + (i + 1) + ".png"));
            upBubble = ImageIO.read(getClass().getResourceAsStream("/res/animation/up.png"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        checkLevelUp();

        // >> ĐẾM NGƯỢC BẤT TỬ
        if (invincible) {
            invincibleCounter--;
            if (invincibleCounter <= 0) invincible = false;
        }

        double centerX = exactX + width / 2.0;
        double centerY = exactY + height / 2.0;
        double mouseWorldX = mouseH.mouseX + gp.cameraX;
        double mouseWorldY = mouseH.mouseY + gp.cameraY;
        double dx = mouseWorldX - centerX;
        double dy = mouseWorldY - centerY;
        
        exactX += dx * easing;
        exactY += dy * easing;

        if (exactX < 0) exactX = 0;
        if (exactX > gp.worldWidth - width) exactX = gp.worldWidth - width;
        if (exactY < 0) exactY = 0;
        if (exactY > gp.worldHeight - height) exactY = gp.worldHeight - height;

        x = (int) exactX; y = (int) exactY;

        if (!state.equals("eat") && !state.equals("turn")) {
            if (Math.abs(dx) > 1.0) {
                String newFacing = (dx > 0) ? "right" : "left";
                if (!newFacing.equals(currentFacing)) {
                    state = "turn"; currentFacing = newFacing; spriteNum = 0; 
                }
            }
        }
        if (!state.equals("turn") && !state.equals("eat")) {
            double velocity = Math.sqrt(dx * easing * dx * easing + dy * easing * dy * easing);
            state = (velocity > 0.5) ? "swim" : "idle";
        }

        solidArea.x = x; solidArea.y = y;

        spriteCounter++;
        if (spriteCounter > 3) {
            spriteNum++; spriteCounter = 0;
            if (state.equals("eat") && spriteNum >= EAT_FRAMES) { state = "swim"; spriteNum = 0; }
            else if (state.equals("turn") && spriteNum >= TURN_FRAMES) { state = "swim"; spriteNum = 0; }
            else if (state.equals("swim") && spriteNum >= SWIM_FRAMES) spriteNum = 0;
            else if (state.equals("idle") && spriteNum >= IDLE_FRAMES) spriteNum = 0;
        }
        
        if (showEffect) {
            effectCounter--;
            if (effectCounter <= 0) showEffect = false;
        }
    }
    
    private void checkLevelUp() {
        int newLevel = currentLevel; double scale = 1.0;
        if (gp.score >= 900) { newLevel = 3; scale = 1.6; } 
        else if (gp.score >= 300) { newLevel = 2; scale = 1.2; } 
        else { newLevel = 1; scale = 1.0; }

        if (newLevel > currentLevel) {
            currentLevel = newLevel; updateSize(scale); 
            showEffect = true; effectCounter = 60; 
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage currentFrame = null;
        if (state.equals("eat") && spriteNum < EAT_FRAMES) currentFrame = eatFrames[spriteNum];
        else if (state.equals("turn") && spriteNum < TURN_FRAMES) currentFrame = turnFrames[spriteNum];
        else if (state.equals("swim") && spriteNum < SWIM_FRAMES) currentFrame = swimFrames[spriteNum];
        else if (spriteNum < IDLE_FRAMES) currentFrame = idleFrames[spriteNum];

        if (currentFrame != null) {
            int drawWidth = this.width;
            int drawHeight = this.height;
            AffineTransform oldTransform = g2.getTransform();
            
            // >> HIỆU ỨNG MỜ KHI BẤT TỬ
            if (invincible) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            }

            int screenX = x - gp.cameraX;
            int screenY = y - gp.cameraY;
            g2.translate(screenX, screenY);

            if (currentFacing.equals("right") && !state.equals("turn")) {
                g2.transform(AffineTransform.getScaleInstance(-1, 1));
                g2.translate(-drawWidth, 0);
            }

            g2.drawImage(currentFrame, 0, 0, drawWidth, drawHeight, null);
            
            // Reset alpha
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setTransform(oldTransform);
            
            if (showEffect && upBubble != null) {
                int bubbleSize = 64; 
                int bubbleX = (int)(x + width/2 - bubbleSize/2) - gp.cameraX;
                int bubbleY = (int)(y - 20) - gp.cameraY;
                int floatOffset = (60 - effectCounter); 
                g2.drawImage(upBubble, bubbleX, bubbleY - floatOffset, bubbleSize, bubbleSize, null);
            }
        }
    }
    
    public void eating() { state = "eat"; spriteNum = 0; spriteCounter = 0; }
}