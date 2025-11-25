package enity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.KeyHandler;
import main.MouseHandler; // Import MouseHandler

public class Player extends Enity {
    GamePanel gp;
    KeyHandler keyH;
    MouseHandler mouseH; // Khai báo biến

    // Constructor nhận thêm MouseHandler
    public Player(GamePanel gp, KeyHandler keyH, MouseHandler mouseH) {
        this.gp = gp;
        this.keyH = keyH;
        this.mouseH = mouseH; // Gán giá trị
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = gp.screenWidth / 2 - gp.tileSize / 2;
        y = gp.screenHeight / 2 - gp.tileSize / 2;
        speed = 4; // Tốc độ di chuyển (dùng cho bàn phím hoặc interpolation nếu cần)
        direction = "down";
    }

    public void getPlayerImage() {
        try {
            // (Giữ nguyên code load ảnh của bạn)
            up1 = ImageIO.read(getClass().getResourceAsStream("/res/eat1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/res/eat2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/res/eat3.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/res/eat4.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/res/eat5.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/res/eat6.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/res/eat7.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/res/eat8.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // Lưu vị trí cũ để tính hướng di chuyển
        int prevX = x;
        int prevY = y;

        // LOGIC DI CHUYỂN THEO CHUỘT
        // Cập nhật x, y theo tọa độ chuột (căn giữa nhân vật)
        // Kiểm tra xem chuột có nằm trong màn hình không (tránh bug khi khởi động)
        if (mouseH.mouseX != 0 || mouseH.mouseY != 0) { 
             x = mouseH.mouseX - gp.tileSize / 2;
             y = mouseH.mouseY - gp.tileSize / 2;
        }

        // Giới hạn biên màn hình (Boundary Check)
        if (x < 0) x = 0;
        if (x > gp.screenWidth - gp.tileSize) x = gp.screenWidth - gp.tileSize;
        if (y < 0) y = 0;
        if (y > gp.screenHeight - gp.tileSize) y = gp.screenHeight - gp.tileSize;

        // TÍNH TOÁN HƯỚNG (DIRECTION) ĐỂ VẼ SPRITE
        int deltaX = x - prevX;
        int deltaY = y - prevY;

        // Ưu tiên hướng ngang hoặc dọc dựa trên độ lớn thay đổi
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) direction = "right";
            else if (deltaX < 0) direction = "left";
        } else {
            if (deltaY > 0) direction = "down";
            else if (deltaY < 0) direction = "up";
        }

        // Chỉ cập nhật animation khi nhân vật thực sự di chuyển
        if (deltaX != 0 || deltaY != 0) {
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        // (Giữ nguyên code draw của bạn)
        BufferedImage image = null;
        if (direction == null) direction = "down";
        
        switch(direction) {
            case "up":
                image = (spriteNum == 1) ? up1 : up2; break;
            case "down":
                image = (spriteNum == 1) ? down1 : down2; break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2; break;
            case "right":
                image = (spriteNum == 1) ? right1 : right2; break;
        }
        if (image != null) {
            g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
        }
    }
}