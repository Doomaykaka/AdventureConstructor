package adventureconstructor.models;

import java.util.ArrayList;
import java.util.List;

public class LocTemp {
    private String id;
    private String type;
    private String name;
    private String desc;
    private String image = "";
    private String ambient = "";
    private boolean autoTransition = true;

    private String npcName;
    private String dialogId;
    private String onSuccess;
    private String onFail;

    private String enemyName;
    private int enemyHp;
    private int enemyDmg;
    private int enemyDef;
    private String onVictory;
    private String onDefeat;
    private boolean scaleEnemy = true;

    private List<LocAct> actions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(String onSuccess) {
        this.onSuccess = onSuccess;
    }

    public String getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        this.onFail = onFail;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public void setEnemyName(String enemyName) {
        this.enemyName = enemyName;
    }

    public int getEnemyHp() {
        return enemyHp;
    }

    public void setEnemyHp(int enemyHp) {
        this.enemyHp = enemyHp;
    }

    public int getEnemyDmg() {
        return enemyDmg;
    }

    public void setEnemyDmg(int enemyDmg) {
        this.enemyDmg = enemyDmg;
    }

    public int getEnemyDef() {
        return enemyDef;
    }

    public void setEnemyDef(int enemyDef) {
        this.enemyDef = enemyDef;
    }

    public String getOnVictory() {
        return onVictory;
    }

    public void setOnVictory(String onVictory) {
        this.onVictory = onVictory;
    }

    public String getOnDefeat() {
        return onDefeat;
    }

    public void setOnDefeat(String onDefeat) {
        this.onDefeat = onDefeat;
    }

    public List<LocAct> getActions() {
        return actions;
    }

    public void setActions(List<LocAct> actions) {
        this.actions = actions;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAmbient() {
        return ambient;
    }

    public void setAmbient(String ambient) {
        this.ambient = ambient;
    }

    public boolean isAutoTransition() {
        return autoTransition;
    }

    public void setAutoTransition(boolean autoTransition) {
        this.autoTransition = autoTransition;
    }

    public boolean isScaleEnemy() {
        return scaleEnemy;
    }

    public void setScaleEnemy(boolean scaleEnemy) {
        this.scaleEnemy = scaleEnemy;
    }
}
