package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;

public class Banner {
    GamePanel gp;
    private BufferedImage[] images;
    private double[] letterY;
    private double targetY;
    private double riseSpeed = 8.0; 
    
    private boolean active = false;
    private int visibleCounter = 0; 
    private int maxVisibleTime = -1; 
    private int tick = 0; 
    
    private final int IMG_WIDTH = 80;
    private final int IMG_HEIGHT = 80;
    // >> SỬA: Tăng khoảng cách giữa các chữ (10 -> 20)
    private final int SPACING = 20; 
    private final int LETTER_DELAY = 5; 

    public Banner(GamePanel gp) {
        this.gp = gp;
    }

    public void show(String text, int duration) {
        this.maxVisibleTime = duration;
        this.visibleCounter = 0;
        this.tick = 0;
        loadImages(text);
        
        // Vị trí đích: 1/3 màn hình
        this.targetY = gp.screenHeight / 3.0;
        
        if (images != null) {
            letterY = new double[images.length];
            for (int i = 0; i < letterY.length; i++) {
                letterY[i] = gp.screenHeight + 100; 
            }
        }
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    private void loadImages(String text) {
        text = text.toUpperCase();
        int len = text.length();
        images = new BufferedImage[len];
        try {
            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (c == ' ') { images[i] = null; continue; }
                
                // Hỗ trợ cả chữ cái và số
                String path = "/res/text/" + c + ".png";
                switch (c){
                    case '&':
                        path = "/res/text/symbol_ampersand.png";
                        break; 
                    case '*':
                        path = "/res/text/symbol_asterisk.png";
                        break;
                    case '.':
                        path = "/res/text/symbol_dot.png";
                        break;
                    case '!':
                        path = "/res/text/symbol_exclamation.png";
                        break;
                    case '~':
                        path = "/res/text/symbol_tilde.png";
                        break;
                    default:
                        path = "/res/text/" + c + ".png";
                        break;
                }
                if (getClass().getResourceAsStream(path) != null) {
                    images[i] = ImageIO.read(getClass().getResourceAsStream(path));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        if (!active || letterY == null) return;
        tick++;
        for (int i = 0; i < letterY.length; i++) {
            int startFrame = i * LETTER_DELAY;
            if (tick >= startFrame) {
                if (letterY[i] > targetY) {
                    letterY[i] -= riseSpeed;
                    double distance = letterY[i] - targetY;
                    if (distance < 150) {
                        letterY[i] -= (riseSpeed * 0.15); 
                        if (letterY[i] - targetY < 2) letterY[i] = targetY;
                    }
                } else {
                    double floatOffset = Math.sin((tick * 0.05) + i) * 2;
                    letterY[i] = targetY + floatOffset;
                }
            }
        }
        if (maxVisibleTime != -1) {
            if (tick > letterY.length * LETTER_DELAY + 30) { 
                visibleCounter++;
                if (visibleCounter > maxVisibleTime) {
                    active = false;
                }
            }
        }
    }


    public void draw(Graphics2D g2) {
        if (!active || images == null || images.length == 0) return;

        // 1. Calculate the raw total width with original size
        int rawTotalWidth = (images.length * IMG_WIDTH) + ((images.length - 1) * SPACING);

        // 2. Calculate scaling factor to fit screen (with padding)
        // Reserve 50px padding on each side (total 100px)
        double scale = 1.0;
        int maxScreenWidth = gp.screenWidth - 100; 

        if (rawTotalWidth > maxScreenWidth) {
            scale = (double) maxScreenWidth / rawTotalWidth;
        }

        // 3. Calculate scaled dimensions
        int scaledWidth = (int) (IMG_WIDTH * scale);
        int scaledHeight = (int) (IMG_HEIGHT * scale);
        int scaledSpacing = (int) (SPACING * scale);

        // 4. Recalculate total width and starting X based on scaled values
        int realTotalWidth = (images.length * scaledWidth) + ((images.length - 1) * scaledSpacing);
        int startX = (gp.screenWidth - realTotalWidth) / 2;

        for (int i = 0; i < images.length; i++) {
            int drawX = startX + i * (scaledWidth + scaledSpacing);
            // Adjust Y based on scale to keep alignment (optional, usually center aligned)
            // Or keep original Y logic. Here we use original letterY logic.
            int drawY = (int) letterY[i];

            if (images[i] != null) {
                // Draw with scaled width and height
                g2.drawImage(images[i], drawX, drawY, scaledWidth, scaledHeight, null);
            }
        }
    }
    
    // 1. Hàm nạp ảnh cho text tĩnh (không dùng hiệu ứng bay)
    public void prepareStatic(String text) {
        loadImages(text);
    }

    // 2. Hàm vẽ tĩnh tại vị trí cụ thể với chiều cao tùy chỉnh (desiredHeight)
    public void drawStatic(Graphics2D g2, int centerX, int centerY, int desiredHeight) {
        if (images == null) return;

        // Tính tỷ lệ scale dựa trên chiều cao mong muốn (Ví dụ: muốn cao 25px thay vì 80px)
        double scale = (double) desiredHeight / IMG_HEIGHT;
        
        int scaledWidth = (int) (IMG_WIDTH * scale);
        int scaledHeight = desiredHeight; // = IMG_HEIGHT * scale
        int scaledSpacing = (int) (SPACING * scale); // Thu nhỏ khoảng cách luôn

        // Tính tổng chiều rộng để căn giữa
        int totalWidth = (images.length * scaledWidth) + ((images.length - 1) * scaledSpacing);
        
        // Tính điểm bắt đầu vẽ (X, Y)
        int startX = centerX - (totalWidth / 2);
        int startY = centerY - (scaledHeight / 2);

        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) {
                int drawX = startX + i * (scaledWidth + scaledSpacing);
                g2.drawImage(images[i], drawX, startY, scaledWidth, scaledHeight, null);
            }
        }
    }
}