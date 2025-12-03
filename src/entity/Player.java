package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.MouseHandler;

public class Player extends Entity {
    GamePanel gp;
    MouseHandler mouseH;
    
    // Animation Constants
    final int SWIM_IDLE_FRAMES = 12;
    final int TURN_FRAMES = 6;
    
    // Logic Variables for Easing Movement (Sub-pixel precision)
    // Cần thiết vì Entity.x, Entity.y là int, sẽ làm tròn số gây mất mượt mà khi tốc độ thấp.
    private double exactX, exactY;
    private double easing = 0.05; // Hệ số nội suy (5% khoảng cách mỗi frame)
    
    // State variables
    private String currentFacing = "right"; 
    
    public Player(GamePanel gp, MouseHandler mouseH) {
        this.gp = gp;
        this.mouseH = mouseH;
        
        // Khởi tạo mảng frames
        idleFrames = new BufferedImage[SWIM_IDLE_FRAMES];
        swimFrames = new BufferedImage[SWIM_IDLE_FRAMES];
        turnFrames = new BufferedImage[TURN_FRAMES];
        
        setDefaultValues();
        getPlayerImageByLoop(); 
    }

    public void setDefaultValues() {
        // Đặt vị trí ban đầu
        x = gp.screenWidth / 2 - gp.tileSize / 2;
        y = gp.screenHeight / 2 - gp.tileSize / 2;
        
        // Đồng bộ hóa tọa độ thực
        exactX = x;
        exactY = y;

        speed = 5; // Biến này hiện tại dùng để tham chiếu nếu cần maxSpeed, logic chính dùng easing
        width = 64; 
        height = 64;
        state = "idle";
        direction = "right";
        currentFacing = "right";
        solidArea = new Rectangle((int)x, (int)y, width, height);
    }

    public void getPlayerImageByLoop() {
        try {
            for (int i = 0; i < SWIM_IDLE_FRAMES; i++) {
                idleFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/idle" + (i + 1) + ".png"));
                swimFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/swim" + (i + 1) + ".png"));
            }
            for (int i = 0; i < TURN_FRAMES; i++) {
                turnFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/turn" + (i + 1) + ".png"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // --- 1. MOVEMENT LOGIC (LINEAR INTERPOLATION - LERP) ---
        // Tính vector khoảng cách từ tâm hitbox đến trỏ chuột
        double centerX = exactX + width / 2.0;
        double centerY = exactY + height / 2.0;
        
        double dx = mouseH.mouseX - centerX;
        double dy = mouseH.mouseY - centerY;
        
        // Cập nhật vị trí thực (double) dựa trên hệ số easing
        // P_new = P_old + (Distance * EasingFactor)
        exactX += dx * easing;
        exactY += dy * easing;

        // Đồng bộ về int để tương thích với hệ thống render/collision cũ
        x = (int) exactX;
        y = (int) exactY;

        // --- 2. BOUNDARY CHECK ---
        // Kiểm tra va chạm biên màn hình
        if (exactX < 0) exactX = 0;
        if (exactX > gp.screenWidth - width) exactX = gp.screenWidth - width;
        if (exactY < 0) exactY = 0;
        if (exactY > gp.screenHeight - height) exactY = gp.screenHeight - height;

        // Cập nhật lại x, y sau khi check biên
        x = (int) exactX;
        y = (int) exactY;

        // --- 3. FACING LOGIC (Vector Direction) ---
        // Logic quay đầu: Dùng dx để xác định hướng
        // Thêm ngưỡng (deadzone) 1.0 để tránh lật mặt liên tục khi chuột đứng yên
        if (Math.abs(dx) > 1.0) {
            String newFacing = (dx > 0) ? "right" : "left";
            
            if (!newFacing.equals(currentFacing)) {
                state = "turn";
                currentFacing = newFacing;
                spriteNum = 0; // Reset animation quay đầu
            }
        }

        // --- 4. ANIMATION STATE LOGIC ---
        // Tính độ lớn vector di chuyển trong frame này để xác định trạng thái
        // Vận tốc thực tế = khoảng cách di chuyển trong 1 frame
        double velocity = Math.sqrt(dx * easing * dx * easing + dy * easing * dy * easing);

        if (!state.equals("turn")) {
            // Nếu vận tốc > 0.5 pixel/frame thì coi như đang bơi
            if (velocity > 0.5) {
                state = "swim";
            } else {
                state = "idle";
            }
        }

        // --- 5. HITBOX UPDATE ---
        solidArea.x = x;
        solidArea.y = y;

        // --- 6. ANIMATION COUNTER ---
        spriteCounter++;
        // Có thể làm animation nhanh hơn nếu cá bơi nhanh (Optional dynamic framerate)
        int animationSpeed = 4; 

        if (spriteCounter > animationSpeed) {
            spriteNum++;
            spriteCounter = 0;

            if (state.equals("turn")) {
                if (spriteNum >= TURN_FRAMES) {
                    spriteNum = 0;
                    state = "swim"; // Quay xong -> Bơi
                }
            } else {
                // Swim hoặc Idle
                if (spriteNum >= SWIM_IDLE_FRAMES) {
                    spriteNum = 0;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage currentFrame = null;

        if (idleFrames == null || swimFrames == null || turnFrames == null) return;

        // Chọn frame dựa trên state
        if (state.equals("turn")) {
            if (spriteNum < TURN_FRAMES) currentFrame = turnFrames[spriteNum];
        } else if (state.equals("swim")) {
            if (spriteNum < SWIM_IDLE_FRAMES) currentFrame = swimFrames[spriteNum];
        } else { // idle
            if (spriteNum < SWIM_IDLE_FRAMES) currentFrame = idleFrames[spriteNum];
        }

        if (currentFrame != null) {
            int targetWidth = gp.tileSize; 
            int targetHeight = gp.tileSize;
            
            AffineTransform oldTransform = g2.getTransform();
            g2.translate(x, y);

            // Logic Flip
            if (currentFacing.equals("right") && !state.equals("turn")) {
                g2.transform(AffineTransform.getScaleInstance(-1, 1));
                g2.translate(-targetWidth, 0);
            }

            g2.drawImage(currentFrame, 0, 0, targetWidth, targetHeight, null);
            g2.setTransform(oldTransform);
        }
    }
}