package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class AttackBubble extends Entity {
    GamePanel gp;
    public boolean alive = false;
    double speed;
    double angle; // Góc bay để tạo hiệu ứng loe rộng

    public AttackBubble(GamePanel gp) {
        this.gp = gp;
        this.width = 20;
        this.height = 20;
        this.solidArea = new Rectangle(0, 0, width, height);
    }

    public void spawn(int startX, int startY, double angle, double speed) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.speed = speed;
        this.alive = true;
    }

    public void update() {
        if (alive) {
            // Bay theo góc đã tính toán
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;

            solidArea.x = (int)x;
            solidArea.y = (int)y;

            // Biến mất nếu ra khỏi màn hình
            if (x < 0 || x > gp.screenWidth || y < 0 || y > gp.screenHeight) {
                alive = false;
            }
        }
    }

    public void draw(Graphics2D g2, BufferedImage img) {
        if (alive) {
            g2.drawImage(img, (int)x, (int)y, width, height, null);
        }
    }
}