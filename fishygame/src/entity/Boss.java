package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Color;



public class Boss extends Entity {
    GamePanel gp;
    public boolean alive = false;
    public boolean isDead = false;
    public BufferedImage boss1;
    public double maxHP = 100; 
    public double currentHP = maxHP;
    public boolean isInvincible = false;
    private int invincibilityCounter = 0;
    
    // Biến điều khiển animation
    MonsterType bossType;
    String action = "swim"; // Trạng thái hiện tại
    int spriteNum = 0;
    int spriteCounter = 0;
    int attackTimer = 0;
    // AI Variables (Giai đoạn 2)
    double velocityX = 0, velocityY = 0;
    double acceleration = 0.1, maxSpeed = 2.0, friction = 0.98;// Độ nhạy khi đuổi theo
    public int fatigue = 0;// Biến tích lũy mệt mỏi
    String lastDirection = "";// Để kiểm tra việc quay đầu

    public Boss(GamePanel gp) {
        this.gp = gp;
        // Thông số Boss
        this.name = "Leviathan";
        speed = 1;
        this.maxHP = 100.0;
        this.currentHP = maxHP;// w 220, h195
        bossType = new MonsterType("Anglerfish", "/res/Anglerfish/", 1,220, 195, 0,
         14, 5, 6, 0);
        // QUAN TRỌNG: Gán ảnh từ bossType vào các mảng ảnh của Entity
        this.swimFrames = bossType.swimFrames;
        this.eatFrames = bossType.eatFrames;
        this.turnFrames = bossType.turnFrames;
        this.idleFrames = bossType.idleFrames;
        
        this.width = bossType.width;
        this.height = bossType.height;
        this.state = "swim"; // Trạng thái mặc định khi mới sinh ra
         width = bossType.width;
        height = bossType.height;
        // Vị trí spawn (Thường là bên phải màn hình)
        this.x = gp.screenWidth + 500;
        this.y = gp.screenHeight / 2 - (height / 2);
        this.direction = "left";
        this.solidArea = new Rectangle(50, 50, width - 100, height - 100); // Hitbox nhỏ hơn ảnh một chút
        //alive = true;
    }



    public void update() {
        if(!alive) return;

        if(gp.level4Timer < 900){
            this.x -= 1;
            updateAnimation();
            updateHitbox();
            return;
        }
        // Giai đoạn 0: Đứng yên
        updateAnimation();
        if (state.equals("turn")) {
            // Sau khi chạy hết sprite quay đầu (ví dụ 6 ảnh)
            if (spriteNum == turnFrames.length - 1) {
                state = "swim"; // Quay lại bơi bình thường
                flipDirection(); // Thực hiện đổi hướng hướng logic
            }
            return; 
        }
        // Xử lý thời gian bất tử sau khi trúng đòn (nếu cần)
        if (isInvincible) {
            invincibilityCounter++;
            if (invincibilityCounter > 30) {
                isInvincible = false;
                invincibilityCounter = 0;
            }
        }
        // 1. TÍNH TOÁN HƯỚNG ĐUỔI THEO PLAYER
        double diffX = gp.player.x - this.x;
        double diffY = gp.player.y - this.y;
        
        // Chuẩn hóa hướng để Boss bơi mượt hơn
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);
        if (distance > 0) {
            // Boss đẩy vận tốc dần dần về phía player
            velocityX += (diffX / distance) * acceleration;
            velocityY += (diffY / distance) * acceleration;
        }

        // 2. GIỚI HẠN TỐC ĐỘ TỐI ĐA (Max Speed)
        double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (currentSpeed > maxSpeed) {
            velocityX = (velocityX / currentSpeed) * maxSpeed;
            velocityY = (velocityY / currentSpeed) * maxSpeed;
        }

        // 3. QUÁN TÍNH & MA SÁT (Để Boss không bơi quá trớn khi Player đứng yên)
        velocityX *= friction;
        velocityY *= friction;

        // 4. CẬP NHẬT TỌA ĐỘ
        this.x += (int)velocityX;
        this.y += (int)velocityY;
        checkBoundaries();
        // 5. LOGIC QUAY ĐẦU & TÍCH LŨY FATIGUE
        updateFatigue();

        // Cập nhật Hitbox bám theo đầu cá (như giai đoạn trước)
        updateHitbox();
    }
    public void updateAttack() {
        if (!alive || gp.level4Timer < 900) return;
        attackTimer++;
        
        // 10 giây bắn một lần (60 fps * 10 = 600)
        if (attackTimer >= 300) {
            fireStunBubbles();
            attackTimer = 0;
        }
    }
    private void fireStunBubbles() {
        
        
        // Tính toán hướng bay cơ bản về phía Player
        //double angleToPlayer = Math.atan2(gp.player.y - this.y, gp.player.x - this.x);
        
        for (int i = 0; i < 50; i++) {
            StunBubble sb = new StunBubble(gp);
            double baseAngle = (direction.equals("left")) ? Math.PI : 0;
            // Độ loe rộng khoảng 45 độ
            double spread = Math.toRadians(new Random().nextInt(60) - 30);
            double finalAngle = baseAngle + spread;
            double speed = 4.0 + new Random().nextDouble() * 3.0;

            sb.spawn((int)this.x + width/2, (int)this.y + height/2, finalAngle, speed);
            gp.bossBubbles.add(sb);
        }
    }
    private void checkBoundaries() {
        // Giới hạn trục X (Trái - Phải)
        if (x < 0) {
            x = 0;
            velocityX = 0; // Dừng vận tốc nếu đập tường
        } else if (x > gp.screenWidth - width) {
            x = gp.screenWidth - width;
            velocityX = 0;
        }

        // Giới hạn trục Y (Trên - Dưới)
        if (y < 0) {
            y = 0;
            velocityY = 0;
        } else if (y > gp.screenHeight - height) {
            y = gp.screenHeight - height;
            velocityY = 0;
        }
    }
    private void updateFatigue() {
        String currentDir = (velocityX > 0) ? "left" : "right";
        
        // Nếu hướng hiện tại khác hướng frame trước đó và tốc độ đủ nhanh
        if (!currentDir.equals(lastDirection) && Math.abs(velocityX) > 0.5) {
            startTurning(); 
            // Cập nhật hướng quay để hiển thị hình ảnh (Flip Image)
            this.direction = currentDir.toLowerCase();
            lastDirection = currentDir;
        }
    }
    
    private void updateHitbox() {
        // Tinh chỉnh để vùng va chạm nằm ở đầu cá (Ví dụ: 1/3 chiều rộng phía trước)
       
        solidArea.y = y + height/3;
        solidArea.width = width/2;
        solidArea.height = height/2;
        // TRỤC X: Thay đổi dựa trên hướng bơi
        if ("left".equals(direction)) {
            // Nếu bơi trái, miệng nằm ở bên trái ảnh (offset nhỏ)
            solidArea.x = x + 20; 
        } else {
            // Nếu bơi phải, miệng nằm ở bên phải ảnh
            // Công thức: Tọa độ x + (Toàn bộ chiều rộng - Chiều rộng vùng rắn - bù trừ)
            solidArea.x = x + width - solidArea.width - 20;
        }
    }

    public void takeDamage(int damage) {
        if (!isInvincible) {
            currentHP -= damage;
            isInvincible = true;
            if (currentHP <= 0) {
                currentHP = 0;
                isDead = true;
                
                // Kích hoạt trạng thái chết
            }
        }
    }

    private void updateAnimation() {
        spriteCounter++;
        if (spriteCounter > 6) { // Tốc độ chuyển ảnh
            spriteNum++;
            spriteCounter = 0;
            BufferedImage[] frames = getCurrentFrames();
            if (spriteNum >= frames.length) {
                spriteNum = 0;
            }
            // Xử lý vòng lặp ảnh dựa trên trạng thái
            if (state.equals("eat")) {
                if (eatFrames != null && spriteNum >= eatFrames.length) {
                    state = "swim"; spriteNum = 0;
                }
            } 
            else if (state.equals("turn")) {
                if (turnFrames != null && spriteNum >= turnFrames.length) {
                    flipDirection(); // Đổi hướng logic (trái <-> phải)
                    state = "swim"; 
                    spriteNum = 0;
                }
            } 
            else { // swim
                if (swimFrames != null && spriteNum >= swimFrames.length) {
                    spriteNum = 0;
                }
            }
            
        }
    }
    public BufferedImage[] getCurrentFrames() {
        switch (state) {
            case "idle": return bossType.idleFrames;
            case "eat":  return bossType.eatFrames;
            case "turn": return bossType.turnFrames;
            default:     return bossType.swimFrames;
        }
    }
   
    public void draw(Graphics2D g2) {
        //BufferedImage[] frames = getCurrentFrames();
        BufferedImage currentFrame = null;
    
        // 1. CHỌN FRAME (Giữ nguyên logic của bạn)
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
            
            // 2. TÍNH TOẠ ĐỘ (Ở màn Boss, cameraX/Y thường là 0)
            int screenX = (int)x;
            int screenY = (int)y;
            
            // 3. CULLING (Sửa lại để Boss lấp ló ở mép màn hình vẫn vẽ)
            // Nới rộng vùng kiểm tra ra 500 pixel hoặc bỏ qua Culling cho Boss
            if (screenX + width < -500 || screenX > gp.screenWidth + 500) {
                return;
            }
            g2.translate(screenX, screenY); 
            // 4. LOGIC FLIP (Lật ảnh dựa trên direction)
            // Nếu state là "turn", thường bộ ảnh đã có sẵn hướng xoay nên có thể không cần Flip
            boolean needFlip = false;
            if (!state.equals("turn")) {
                if (direction.equals("right")) needFlip = true;
            }
            if (needFlip) {
                // Lật trục X quanh tâm hoặc góc để Boss quay đầu
                g2.scale(-1, 1);
                g2.translate(-width, 0); 
            }
        
            // 5. HIỆU ỨNG NHẤP NHÁY (Thêm vào nếu Boss trúng đòn)
            if (isInvincible && (invincibilityCounter / 5) % 2 == 0) {
                // Không vẽ ảnh ở frame này
            } else {
                g2.drawImage(currentFrame, 0, 0, width, height, null);
            }
             g2.setTransform(oldTransform);
             if (gp.level4Timer >= 900) {
                drawHPBar(g2);
            }
        }
       
        //Vẽ khung va chạm để debug (Xóa khi xong)
        // g2.setColor(Color.GREEN);
        // g2.drawRect(solidArea.x, solidArea.y, solidArea.width, solidArea.height);
    
    }
    private void drawHPBar(Graphics2D g2) {
        // Vẽ thanh máu Boss trên đầu
        g2.setColor(Color.GRAY);
        g2.fillRect(x + 50, y - 20, width - 100, 10);
        g2.setColor(Color.RED);
        int hpWidth = (int)((width - 100) * ((double)currentHP / maxHP));
        g2.fillRect(x + 50, y - 20, hpWidth, 10);
    }
}