package main;

import entity.MonsterType;
import java.util.ArrayList;

public class Level {
    
    public int levelNum;
    public int winScore;
    public ArrayList<MonsterType> monsterTypes = new ArrayList<>();
    
    public Level(int levelNum) {
        this.levelNum = levelNum;
        setupLevel();
    }
    
    private void setupLevel() {
        if (levelNum == 1) {
            this.winScore = 2000; 
            
            // 1. MINNOW (40x30) - 30 điểm
            // Player Lv1 (50x40) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "minnow", "/res/minnow/", 2, 40, 30 , 30, 
                15, 7, 0, 0 
            ));
            // 2. SURGEONFISH (85x80) - 60 điểm
            // Player Lv2 (150x126) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "surgeonfish", "/res/surgeonfish/", 3, 65, 60, 60, 
                14, 5, 5, 0 
            ));
            // 3. LIONFISH (110x104) - 90 điểm
            // Player Lv3 (175x140) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "lionfish", "/res/lionfish/", 4, 90, 80, 150, 
                14, 5, 6, 6 
            ));
        }
    }
}