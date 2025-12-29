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
                "minnow", "/res/minnow/", 2, 40, 30, 30, 
                15, 7, 0, 0 
            ));
            // 2. SURGEONFISH (75x55) - 60 điểm
            // Player Lv2 (75x63) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "surgeonfish", "/res/surgeonfish/", 3, 90, 70, 60, 
                14, 5, 5, 0 
            ));
            // 3. LIONFISH (90x60) - 90 điểm
            // Player Lv3 (100x80) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "lionfish", "/res/lionfish/", 4, 140, 120, 150, 
                14, 5, 6, 6 
            ));
        } else if( levelNum == 2){
            this.winScore = 5000;
            monsterTypes.add(new MonsterType(
                "barracuda", "/res/barracuda/", 2, 50, 35, 30, 
                14, 5, 0, 0 
            ));
             monsterTypes.add(new MonsterType(
                "parrotfish", "/res/parrotfish/", 3, 110, 80, 60, 
                14, 5, 6, 0 
            ));
            monsterTypes.add(new MonsterType(
                "Anglerfish", "/res/Anglerfish/", 4, 140, 120, 150, 
                15, 5, 6, 7
            ));

        } else if( levelNum == 3){
            this.winScore = 8000;
            monsterTypes.add(new MonsterType(
                "Tuna", "/res/Tuna/", 2, 50, 35, 30, 
                15, 5, 5, 0 
            ));
             monsterTypes.add(new MonsterType(
                "Pufferfish", "/res/Pufferfish/", 3, 110, 80, 60, 
                15, 5, 6, 7
            ));
            monsterTypes.add(new MonsterType(
                "JohnDory", "/res/JohnDory/", 4, 140, 120, 150, 
                15, 5, 6, 7
            ));
        } else if( levelNum == 4){
            this.winScore = 99999;
            this.monsterTypes.clear();// xoa sach danh sach quai thuong
        }
    }
}