package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import main.GamePanel;

public class Enemy extends Entity {
    
    GamePanel gp;
    Random rand = new Random();
    
    // AI Variables (Chuyển từ Entity sang đây)
    public int dy; 
    public int actionLockCounter = 0; 
    
    public String name = "";

    public Enemy(GamePanel gp) {
        this.gp = gp;
        // Ensure hitbox is always initialized to a safe default.
        // Specific monster types will overwrite this after creation.
        this.solidArea = new Rectangle(0, 0, this.width, this.height);
    }

    /**
     * Hàm update xử lý toàn bộ logic hành vi của Enemy
     * @param allowMove: Biến này nhận từ Aquarium (để xử lý Slow Motion)
     */
    public void update(boolean allowMove) {
        
        // 1. LOGIC DI CHUYỂN & AI (Chỉ chạy khi được phép)
        if (allowMove) {
            // Nếu đang quay đầu (Turn) thì đứng yên, không di chuyển vị trí
            if (!state.equals("turn")) {
                updateAI();
                updatePosition();
            }
        }
        
        // 2. LOGIC ANIMATION (Chạy liên tục mỗi frame)
        updateAnimation();
    }

    private void updateAI() {
        // Nếu là Level 2 trở lên VÀ là cá barracuda thì dùng logic đuổi
        if (gp.currentLevel.levelNum >= 2 && "Anglerfish".equals(this.name)) {
            huntPlayer();
        } else {
            // Nếu không phải barracuda hoặc không phải level 2, bơi bình thường
            normalSwimAI();
        }
    }
    // Tách logic bơi ngẫu nhiên ra hàm riêng
    private void normalSwimAI() {
        actionLockCounter++;
        int changeTime = 30 + rand.nextInt(30);
        if (actionLockCounter >= changeTime) {
            dy = rand.nextInt(3) - 1; 
            if (rand.nextInt(100) < 20) {
                if (dy > 0) dy = 2; else if (dy < 0) dy = -2;
            }
            
            if (rand.nextInt(100) < 2) {
                startTurning();
            }
            actionLockCounter = 0;
        }
    }
    

    private void huntPlayer() {
        double distance = Math.sqrt(Math.pow(gp.player.x - this.x, 2) + Math.pow(gp.player.y - this.y, 2));

        if (distance < 180) {
            
            if (state.equals("swim")) { 
                if (gp.player.x < this.x && direction.equals("right")) {
                    startTurning(); 
                } else if (gp.player.x > this.x && direction.equals("left")) {
                    startTurning();
                }
            }
            
            // Tăng tốc nhẹ
            if (direction.equals("left")) x -= 2 ; 
            else x += 1;

            if (gp.player.y > this.y) dy = 1;
            else if (gp.player.y < this.y) dy = -1;
        } else {
            // QUAN TRỌNG: Gọi hàm bơi bình thường, KHÔNG gọi updateAI()
            normalSwimAI();
        }
    }


    private void updatePosition() {
        // Di chuyển ngang
        if (direction.equals("left")) x -= speed;
        else x += speed;

        // Di chuyển dọc
        y += dy;

        // Boundary Check (World)
        if (y < 0) { y = 0; dy = 1; }
        if (y > gp.worldHeight - height) { y = gp.worldHeight - height; dy = -1; }
        
        // Chạm biên ngang -> Quay đầu
        if (x <= 0 && direction.equals("left")) startTurning();
        if (x >= gp.worldWidth - width && direction.equals("right")) startTurning();

        // Update Hitbox
        solidArea.x = x;
        solidArea.y = y;
    }

    private void updateAnimation() {
        spriteCounter++;
        int animSpeed = 5; 
        
        if (spriteCounter > animSpeed) {
            spriteNum++;
            spriteCounter = 0;

            // --- STATE MACHINE ---
            if (state.equals("eat")) {
                if (eatFrames != null && spriteNum >= eatFrames.length) {
                    state = "swim"; spriteNum = 0;
                }
            } 
            else if (state.equals("turn")) {
                if (turnFrames != null && spriteNum >= turnFrames.length) {
                    flipDirection(); 
                    state = "swim"; spriteNum = 0;
                }
            } 
            else { // swim
                if (swimFrames != null && spriteNum >= swimFrames.length) {
                    spriteNum = 0;
                }
            }
        }
    }



    // Hàm vẽ riêng của Enemy
    public void draw(Graphics2D g2) {
        BufferedImage currentFrame = null;
        
        // Chọn Frame
        if (state.equals("eat") && eatFrames != null) {
            if (spriteNum < eatFrames.length) currentFrame = eatFrames[spriteNum];
        } 
        else if (state.equals("turn") && turnFrames != null) {
            if (spriteNum < turnFrames.length) currentFrame = turnFrames[spriteNum];
        } 
        else { // swim
            if (swimFrames != null && swimFrames.length > 0) {
                int idx = (spriteNum < swimFrames.length) ? spriteNum : 0;
                currentFrame = swimFrames[idx];
            }
        }
        
        if(currentFrame != null) {
            AffineTransform oldTransform = g2.getTransform();
            
            // Tính tọa độ màn hình
            int screenX = x - gp.cameraX;
            int screenY = y - gp.cameraY;
            
            // Culling (Tối ưu: Không vẽ nếu ngoài màn hình)
            if (screenX + width < 0 || screenX > gp.screenWidth ||
                screenY + height < 0 || screenY > gp.screenHeight) {
                return;
            }

            g2.translate(screenX, screenY); 

            // Logic Flip
            boolean needFlip = false;
            if (!state.equals("turn")) {
                if (direction.equals("right")) needFlip = true;
            } else {
                if (direction.equals("right")) needFlip = true;
            }

            if (needFlip) {
                g2.transform(AffineTransform.getScaleInstance(-1, 1));
                g2.translate(-width, 0); 
            }
            
            g2.drawImage(currentFrame, 0, 0, width, height, null);
            g2.setTransform(oldTransform);
        }
    }
}