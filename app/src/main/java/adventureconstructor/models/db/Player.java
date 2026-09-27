package adventureconstructor.models.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name = "Hero";

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "hp")
    private int hp = 100;

    @Column(name = "max_hp")
    private int maxHp = 100;

    @Column(name = "gold")
    private int gold = 0;

    @Column(name = "level")
    private int level = 1;

    @Column(name = "exp")
    private int exp = 0;

    @Column(name = "exp_next")
    private int expNext = 100;

    @Column(name = "sp")
    private int sp = 0;

    @Column(name = "str")
    private int str = 5;

    @Column(name = "agi")
    private int agi = 5;

    @Column(name = "intl")
    private int intl = 5;

    @Column(name = "end")
    private int end = 5;

    @Column(name = "weapon_id")
    private Long weaponId = null;

    @Column(name = "armor_id")
    private Long armorId = null;

    @Column(name = "current_location_id")
    private String currentLocationId = null;

    @Column(name = "score")
    private int score = 0;

    @Column(name = "is_alive")
    private boolean isAlive = true;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Item> inventory = new ArrayList<>();

    // --- Геттеры и сеттеры ---

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public Long getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(Long weaponId) {
        this.weaponId = weaponId;
    }

    public Long getArmorId() {
        return armorId;
    }

    public void setArmorId(Long armorId) {
        this.armorId = armorId;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public void setCurrentLocationId(String currentLocationId) {
        this.currentLocationId = currentLocationId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(List<Item> inventory) {
        this.inventory = inventory;
    }

    public void addItem(Item item) {
        item.setPlayer(this);
        inventory.add(item);
    }

    public void removeItem(Item item) {
        inventory.remove(item);
        item.setPlayer(null);
    }
}
