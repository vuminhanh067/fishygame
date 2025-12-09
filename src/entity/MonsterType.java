package entity;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

import main.GamePanel;

public class MonsterType extends Entity
{
        String name;
        String folderPath; // Thư mục chứa ảnh (VD: /res/minnow/)
        int speed;
        int width, height;
        int scoreValue;
        
        // Mảng chứa animation frames
        public BufferedImage[] swimFrames;
        public BufferedImage[] turnFrames;
        public BufferedImage[] eatFrames;
        public BufferedImage[] idleFrames;

        public MonsterType(String name, String folder, int speed, int w, int h, int score, 
                           int swimCount, int turnCount, int eatCount, int idleCount) {
            this.name = name;
            this.folderPath = folder;
            this.speed = speed;
            this.width = w;
            this.height = h;
            this.scoreValue = score;
            
            // Load ảnh động
            this.swimFrames = loadFrames(name + "swim", swimCount);
            this.turnFrames = loadFrames(name + "turn", turnCount);
            
            if (eatCount > 0) this.eatFrames = loadFrames(name + "eat", eatCount);
            if (idleCount > 0) this.idleFrames = loadFrames(name + "idle", idleCount);
        }

        private BufferedImage[] loadFrames(String prefix, int count) {
            BufferedImage[] frames = new BufferedImage[count];
            try {
                for (int i = 0; i < count; i++) {
                    // Path: /res/folder/prefix + index + .png
                    String path = folderPath + prefix + (i + 1) + ".png";
                    frames[i] = ImageIO.read(getClass().getResourceAsStream(path));
                }
            } catch (Exception e) {
                System.err.println("Error loading: " + prefix);
            }
            return frames;
        }
    public Enemy createMonster(GamePanel gp) {
        // >> KHỞI TẠO ENEMY từ chính MonsterType này
        Enemy monster = new Enemy(gp);

        monster.name = this.name;
        monster.speed = this.speed;
        monster.width = this.width;
        monster.height = this.height;
        monster.scoreValue = this.scoreValue;

        // Copy references to frames
        monster.swimFrames = this.swimFrames;
        monster.turnFrames = this.turnFrames;
        monster.eatFrames = this.eatFrames;
        monster.idleFrames = this.idleFrames;

        // Setup hitbox based on this type's dimensions
        monster.solidArea = new Rectangle(0, 0, this.width, this.height);
        return monster;
    }
}
