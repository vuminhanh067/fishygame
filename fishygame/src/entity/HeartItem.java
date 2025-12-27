package entity;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import java.awt.Color;

public class HeartItem extends Entity {
    GamePanel gp;
    public boolean active = true;
    public int type = 2;
    public BufferedImage heartImag;

    public HeartItem(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.speed = 1; // Tốc độ rơi chậm hơn bong bóng thường cho dễ ăn
        this.width = 20;
        this.height = 20;
        // Khởi tạo vùng va chạm
        this.solidArea = new Rectangle(0, 0, width, height); 
        loadImages();
    }

    public void loadImages() {
        try {
            heartImag = ImageIO.read(getClass().getResourceAsStream("/res/item/live.png"));
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        y += speed;

        // Cập nhật solidArea theo vị trí thực tế để va chạm nhạy
        solidArea.x = x;
        solidArea.y = y;

        // Tự hủy nếu rơi khỏi màn hình
        if (y > gp.screenHeight) {
            active = false;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(heartImag, x, y, gp.tileSize, gp.tileSize, null);
    }
}