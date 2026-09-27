package adventureconstructor.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Player {
    private Long id = null;
    private String name = "Hero";
    private Date creationDate = new Date();
    private int level = 1;
    private int hp = 50;
    private int maxHp = 50;
    private int gold = 0;
    private int exp = 0;
    private int expNext = 100;
    private int sp = 0;
    private int str = 5;
    private int agi = 5;
    private int intl = 5;
    private int end = 5;
    private int score = 0;
    private boolean alive = true;
    private String currentLocationId = null;

    private List<Item> inv = new ArrayList<>();
    private Item weapon = null;
    private Item armor = null;

    // ---- Бизнес-логика (без изменений) ----

    public int atkDmg() {
        return (weapon != null ? weapon.getValue() : 2) + str;
    }

    public int def() {
        return armor != null ? armor.getValue() : 0;
    }

    public int blockChance() {
        return 20 + agi * 2;
    }

    public int fleeChance() {
        return 30 + agi * 2;
    }

    public int skill(String s) {
        if ("persuasion".equals(s)) return intl;
        if ("intimidation".equals(s)) return str;
        return 0;
    }

    public void gainExp(int a) {
        exp += a;
        while (exp >= expNext) {
            exp -= expNext;
            level++;
            sp += 3;
            expNext = level * 100;
            maxHp = 50 + end * 10;
            hp = maxHp;
        }
    }

    public void allocSP(String st) {
        if (sp <= 0) return;
        sp--;
        if ("str".equals(st)) str++;
        else if ("agi".equals(st)) agi++;
        else if ("intl".equals(st)) intl++;
        else if ("end".equals(st)) {
            end++;
            maxHp += 10;
        }
    }

    public void heal(int a) {
        hp = Math.min(maxHp, hp + a);
    }

    public void damage(int a) {
        hp = Math.max(0, hp - a);
    }

    public void equip(Item it) {
        if ("weapon".equals(it.getType())) {
            if (weapon != null) inv.add(weapon);
            weapon = it;
        } else if ("armor".equals(it.getType())) {
            if (armor != null) inv.add(armor);
            armor = it;
        }
        inv.remove(it);
    }

    // ---- Конверсия: бизнес → DB ----

    public adventureconstructor.models.db.Player toDb() {
        adventureconstructor.models.db.Player db = new adventureconstructor.models.db.Player();

        if (id != null) db.setId(id);
        db.setName(name);
        db.setCreationDate(creationDate);
        db.setHp(hp);
        db.setMaxHp(maxHp);
        db.setGold(gold);
        db.setLevel(level);
        db.setExp(exp);
        db.setExpNext(expNext);
        db.setSp(sp);
        db.setStr(str);
        db.setAgi(agi);
        db.setIntl(intl);
        db.setEnd(end);
        db.setScore(score);
        db.setAlive(alive);
        db.setCurrentLocationId(currentLocationId);

        // Инвентарь
        List<adventureconstructor.models.db.Item> dbInv = new ArrayList<>();
        for (Item biz : inv) {
            adventureconstructor.models.db.Item dbItem = biz.toDb();
            dbItem.setPlayer(db);
            dbInv.add(dbItem);
        }
        db.setInventory(dbInv);

        // weaponId / armorId — по имени предмета в инвентаре
        if (weapon != null) {
            for (adventureconstructor.models.db.Item di : dbInv) {
                if (di.getName().equals(weapon.getName()) && "weapon".equals(di.getType())) {
                    di.setEquipped(true);
                    di.setSlot("weapon");
                    db.setWeaponId(di.getId());
                    break;
                }
            }
        }
        if (armor != null) {
            for (adventureconstructor.models.db.Item di : dbInv) {
                if (di.getName().equals(armor.getName()) && "armor".equals(di.getType())) {
                    di.setEquipped(true);
                    di.setSlot("armor");
                    db.setArmorId(di.getId());
                    break;
                }
            }
        }

        return db;
    }

    // ---- Конверсия: DB → бизнес ----

    public static Player fromDb(adventureconstructor.models.db.Player db) {
        Player biz = new Player();

        biz.setId(db.getId());
        biz.setName(db.getName());
        biz.setCreationDate(db.getCreationDate());
        biz.setHp(db.getHp());
        biz.setMaxHp(db.getMaxHp());
        biz.setGold(db.getGold());
        biz.setLevel(db.getLevel());
        biz.setExp(db.getExp());
        biz.setExpNext(db.getExpNext());
        biz.setSp(db.getSp());
        biz.setStr(db.getStr());
        biz.setAgi(db.getAgi());
        biz.setIntl(db.getIntl());
        biz.setEnd(db.getEnd());
        biz.setScore(db.getScore());
        biz.setAlive(db.isAlive());
        biz.setCurrentLocationId(db.getCurrentLocationId());

        List<Item> bizInv = new ArrayList<>();
        for (adventureconstructor.models.db.Item di : db.getInventory()) {
            bizInv.add(Item.fromDb(di));
        }
        biz.setInv(bizInv);

        // Восстановление weapon/armor по equipped + slot
        for (int i = 0; i < bizInv.size(); i++) {
            adventureconstructor.models.db.Item di = db.getInventory().get(i);
            if (di.isEquipped() && "weapon".equals(di.getSlot())) {
                biz.setWeapon(bizInv.get(i));
            } else if (di.isEquipped() && "armor".equals(di.getSlot())) {
                biz.setArmor(bizInv.get(i));
            }
        }

        // Оружие/броня не дублируются в inv
        if (biz.getWeapon() != null) bizInv.remove(biz.getWeapon());
        if (biz.getArmor() != null) bizInv.remove(biz.getArmor());

        return biz;
    }

    // ---- Геттеры и сеттеры (расширенные) ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExpNext() {
        return expNext;
    }

    public void setExpNext(int expNext) {
        this.expNext = expNext;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getAgi() {
        return agi;
    }

    public void setAgi(int agi) {
        this.agi = agi;
    }

    public int getIntl() {
        return intl;
    }

    public void setIntl(int intl) {
        this.intl = intl;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public void setCurrentLocationId(String currentLocationId) {
        this.currentLocationId = currentLocationId;
    }

    public List<Item> getInv() {
        return inv;
    }

    public void setInv(List<Item> inv) {
        this.inv = inv;
    }

    public Item getWeapon() {
        return weapon;
    }

    public void setWeapon(Item weapon) {
        this.weapon = weapon;
    }

    public Item getArmor() {
        return armor;
    }

    public void setArmor(Item armor) {
        this.armor = armor;
    }
}
