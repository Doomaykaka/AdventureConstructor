package adventureconstructor.models;

import java.util.ArrayList;
import java.util.List;

public class Player {
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

    private List<Item> inv = new ArrayList<>();
    private Item weapon = null;
    private Item armor = null;

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
