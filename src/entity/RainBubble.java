package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class RainBubble extends Entity {
    GamePanel gp;
    public boolean alive = false;
    public double speed;

    public RainBubble(GamePanel gp, int startX, int startY) {
        this.gp = gp;
        this.x = startX; // GÁN GIÁ TRỊ BAN ĐẦU
        this.y = startY; // GÁN GIÁ TRỊ BAN ĐẦU
        this.width = 15; 
        this.height = 15;
        this.alive = true; // Kích hoạt ngay khi tạo
        this.speed = 2.5;  // Tốc độ mặc định
        this.solidArea = new Rectangle(0, 0, width, height);
    }

    public void spawn(int x, int y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.alive = true;
    }

    public void update() {
        if (alive) {
            y += speed; // Rơi từ trên xuống

            // Cập nhật hitbox
            solidArea.x = x;
            solidArea.y = y;

            // Biến mất nếu rơi quá đáy màn hình
            if (y > gp.screenHeight) {
                alive = false;
            }
        }
    }

    public void draw(Graphics2D g2, BufferedImage image) {
        if (alive) {
            g2.drawImage(image, x, y, width, height, null);
        }
    }
}