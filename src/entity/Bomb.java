package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import java.awt.Color;

public class Bomb extends Entity {
    GamePanel gp;
    public BufferedImage mineImg;
    public boolean active = true;
    public BufferedImage[] explosionFrames = new BufferedImage[5]; // Theo ảnh bạn gửi có 5 frames nổ
    public boolean exploded = false;
    public int explosionCounter = 0;
    public int explosionIndex = 0;
    public boolean damageDealt = false;
    public Bomb(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.width = 25; // Kích thước quả bom
        this.height = 25;
        this.solidArea = new Rectangle(5, 5, 20, 20);
        loadImages();
    }

    private void loadImages() {
        try {
            mineImg = ImageIO.read(getClass().getResourceAsStream("/res/bomb/mine.png"));
            for (int i = 0; i < 5; i++) {
                explosionFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/bomb/explosion" + (i + 1) + ".png"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        if (exploded) {
            explosionCounter++;
            if (!damageDealt) {
                checkExplosionDamage();
                damageDealt = true;
            }
            if (explosionCounter > 6) { // Tốc độ nổ
                explosionIndex++;
                explosionCounter = 0;
            }
            // Nếu nổ xong 5 frames thì xóa quả bom
            if (explosionIndex >= 5) {
                // Đánh dấu để xóa khỏi list trong GamePanel
                this.active = false; 
            }
        } else {
            this.y += 1; // Rơi xuống
           // GỌI CCHECKER ĐỂ KIỂM TRA VA CHẠM
           this.solidArea.x = this.x; 
            this.solidArea.y = this.y;
            if (gp.cChecker.checkBombCollision(this, gp.player, gp.aquarium.finalBoss, gp.playerAttackBubbles)) {
                explode();
                
            }
            // Nếu bom rơi quá sâu dưới màn hình, tự hủy để tiết kiệm bộ nhớ
            if (this.y > gp.screenHeight + 100) {
                active = false;
            }
        }
    }
    private void checkExplosionDamage() {
        int radius = 50;
        // Tâm quả bom
        int centerX = this.x + (width / 2);
        int centerY = this.y + (height / 2);
        // kiểm tra boss
        if (this.solidArea.intersects(gp.aquarium.finalBoss.solidArea)) {
            gp.aquarium.finalBoss.currentHP -= 7;
            //System.out.println("Boss trung bom: -10HP");
        } else if (gp.aquarium.finalBoss != null && gp.aquarium.finalBoss.alive) {
            double distToBoss = getDistance(centerX, centerY, 
                gp.aquarium.finalBoss.x + gp.aquarium.finalBoss.width/2, 
                gp.aquarium.finalBoss.y + gp.aquarium.finalBoss.height/2);
            
            if (distToBoss <= radius) {
                gp.aquarium.finalBoss.currentHP -= 7;
                //System.out.println("-hpboss");
            }
        }

        // 2. Kiểm tra Player
        double distToPlayer = getDistance(centerX, centerY, 
            gp.player.x + gp.player.width/2, 
            gp.player.y + gp.player.height/2);
        
        if (distToPlayer <= radius) {
            System.out.println("-hp");
            gp.lives-= 1; // Hoặc logic trừ mạng của bạn
            gp.player.invincible = true; // Cho player bất tử tạm thời để không chết ngay lập tức
        }

        // 3. Kiểm tra Bong bóng (Bubble) của Player
        for (int i = 0; i < gp.playerAttackBubbles.size(); i++) {
            Entity b = gp.playerAttackBubbles.get(i);
            if (getDistance(centerX, centerY, b.x, b.y) <= radius) {
                gp.playerAttackBubbles.remove(i);
                i--;
            }
        }
        // B. Bóng của Boss bắn ra (bossBubbles)
        for (int i = 0; i < gp.bossBubbles.size(); i++) {
            Entity sb = gp.bossBubbles.get(i); // Thường là StunBubble
            if (getDistance(centerX, centerY, sb.x, sb.y) <= radius) {
                gp.bossBubbles.remove(i);
                i--;
            }
        }

        // C. Bóng mưa từ trên rơi xuống (rainBubbles - giả sử bạn đặt tên này)
        // Tùy vào cách bạn đặt tên list chứa bong bóng mưa trong GamePanel
        for (int i = 0; i < gp.rainBubbles.size(); i++) {
            Entity rb = gp.rainBubbles.get(i);
            if (getDistance(centerX, centerY, rb.x, rb.y) <= radius) {
                gp.rainBubbles.remove(i);
                i--;
            }
        }
    }
    // Hàm tính khoảng cách giữa 2 điểm
    private double getDistance(int x1, int y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
    public void explode() {
        if (!exploded) {
            exploded = true;
            //gp.playSE(4); // Âm thanh nổ
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = x;
        int screenY = y;
       
        if (!exploded) {
            g2.drawImage(mineImg, screenX, screenY, width, height, null);
            // VẼ KHUNG ĐỎ ĐỂ XEM VÙNG VA CHẠM ĐANG Ở ĐÂU
        } else if (explosionIndex < 5) {
            // Vẽ hiệu ứng nổ to hơn quả bom một chút
            g2.drawImage(explosionFrames[explosionIndex], screenX - 20, screenY - 20, width + 40, height + 40, null);
            g2.setColor(new Color(255, 255, 255, 100)); // Màu trắng trong suốt
            g2.drawOval(screenX - 80, screenY - 80, 100, 100); // Vẽ vòng tròn bán kính 100
        }
    }
}