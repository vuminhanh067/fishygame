package entity;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import main.GamePanel;

public class Aquarium {
    GamePanel gp;
    Random rand = new Random();
    
    // Danh sách chứa Enemy
    public ArrayList<Enemy> entities = new ArrayList<>();
    public Boss finalBoss; // Biến giữ Boss
    // Các biến đếm nội bộ (Internal State)
    private int spawnCounter = 0;
    private int moveTick = 0; 
    final int SLOW_DOWN_FACTOR = 2; 
    
    public Aquarium(GamePanel gp) {
        this.gp = gp;
    }

    // >> PHƯƠNG THỨC MỚI: Dùng để Reset game sạch sẽ
    public void reset() {
        entities.clear();
        spawnCounter = 0;
        moveTick = 0;
        finalBoss = null;
    }
    public void spawnBoss() {
        // Khởi tạo Boss
        finalBoss = new Boss(gp);

    }
    public void spawnEntity() {
        // Lấy danh sách quái từ Level hiện tại
        ArrayList<MonsterType> types = gp.currentLevel.monsterTypes;
        if (types == null || types.isEmpty()) return;

        // --- Logic Spawn có trọng số (Weighted Random) ---
        int index = 0;
        int dice = rand.nextInt(100);
        
        if (gp.currentLevel.levelNum == 1){
               if (types.size() >= 3) {
                    if (gp.score < 500) {
                        // Tỉ lệ: 70% con 0, 28% con 1, 2% con 2
                        if (dice < 70) index = 0;      
                        else if (dice < 90) index = 1; 
                        else index = 2;                
                    } else {
                        // Tỉ lệ: 50% con 0, 35% con 1, 15% con 2
                        if (dice < 50) index = 0;      
                        else if (dice < 80) index = 1; 
                        else index = 2;                
                    }
                }
            
        } else if (gp.currentLevel.levelNum == 2) {
            if (types.size() >= 3) {
                if(gp.score < 2500){
                    if (dice < 70) index = 0;      // 50% Barracuda (Con thứ 3 trong danh sách)
                    else if (dice < 90) index = 1; // 30% Parrotfish (Con thứ 1)
                    else index = 2;  
                } else if(gp.score < 3500) {
                    if(dice < 50) index = 0;
                    else if(dice < 80) index = 1;
                    else index = 2;
                } else {
                    if(dice < 50) index = 0;
                    else if (dice < 70) index = 1;
                    else index = 2;
                }
               
            }
        } else if(gp.currentLevel.levelNum == 3){
            if (types.size() >= 3) {
                if(gp.score < 5500){
                    if (dice < 70) index = 0;      // 50% Barracuda (Con thứ 3 trong danh sách)
                    else if (dice < 90) index = 1; // 30% Parrotfish (Con thứ 1)
                    else index = 2;  
                } else if (gp.score < 6300){
                    if(dice < 50) index = 0;
                    else if(dice < 75) index = 1;
                    else index = 2;
                } else {
                    if(dice < 60) index = 0;
                    else  if(dice < 85) index = 1;
                    else index = 2;
                }
               
            }
        }
            
            MonsterType selectedType = types.get(index);

            // Tạo Enemy từ MonsterType đã chọn
            Enemy monster = selectedType.createMonster(gp);

            // Random vị trí & hướng
            boolean isRight = rand.nextBoolean();
            monster.direction = isRight ? "right" : "left";
            monster.y = rand.nextInt(gp.worldHeight - monster.height);
            
            if (isRight) monster.x = -monster.width;
            else monster.x = gp.worldWidth;

            monster.dy = rand.nextInt(3) - 1; 
            monster.actionLockCounter = 0;
            
            entities.add(monster);
        }
    

    public void update() {
        if (gp.currentLevel.levelNum < 4){
            spawnCounter++;
            if (spawnCounter > 30) { 
                spawnEntity();
                spawnCounter = 0;
            }
        }
        
        
        moveTick++;
        boolean allowMove = (moveTick % SLOW_DOWN_FACTOR == 0);

        for (int i = 0; i < entities.size(); i++) {
            Enemy e = entities.get(i);
            if (e != null) {
                // Update AI & Position
                e.update(allowMove);
                
                // Garbage Collection
                if (e.x < -200 || e.x > gp.worldWidth + 200) {
                    entities.remove(i);
                    i--; 
                }
            }
        }
        // 4. CẬP NHẬT RIÊNG CHO BOSS (Nếu đang ở màn Boss)
        if (gp.currentLevel.levelNum == 4 && finalBoss != null) {
            finalBoss.update(); // Cập nhật AI và hành động của Boss
        }

        if (moveTick > 1000) moveTick = 0;
        // Chỉ check cá ăn nhau nếu không phải màn Boss (hoặc tùy bạn muốn Boss ăn cá con không)
        if (gp.currentLevel.levelNum < 4) {
            checkPredatorCollision();
        }
        
    }

    private void checkPredatorCollision() {
        for (int i = 0; i < entities.size(); i++) {
            Enemy predator = entities.get(i);
            for (int j = 0; j < entities.size(); j++) {
                if (i == j) continue; 
                Enemy prey = entities.get(j);
                
                if (predator.solidArea.intersects(prey.solidArea)) {
                    int sizeA = predator.width * predator.height;
                    int sizeB = prey.width * prey.height;

                    if (sizeA > sizeB * 1.2) {
                        predator.startEating();
                        entities.remove(j);
                        if (j < i) i--; 
                        j--; 
                    }
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        // if (gp.currentLevel.levelNum == 4) {
        //     if (finalBoss != null) finalBoss.draw(g2);
        // } else {
        //     for (Enemy e : entities) {
        //         e.draw(g2);
        //     }
        //  }
       for (int i = 0; i < entities.size(); i++) {
        Enemy e = entities.get(i);
            if (e != null) {
                e.draw(g2);
            }
        }
        
    }
}