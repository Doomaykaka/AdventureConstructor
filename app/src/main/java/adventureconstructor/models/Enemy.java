package adventureconstructor.models;

public class Enemy {
    private String name;
    private int hp;
    private int maxHp;
    private int dmg;
    private int def;
    private boolean guarding = false;

    public void scale(int pl) {
        int el = Math.max(1, pl - 2);
        maxHp = hp = 20 + el * 10;
        dmg = 4 + el;
        def = el / 2;
    }

    public String chooseAction(Player p) {
        double hr = (double) hp / maxHp, pr = (double) p.getHp() / p.getMaxHp();
        if (hr < 0.2) return Math.random() < 0.7 ? "flee" : "defend";
        if (pr < 0.3) return Math.random() < 0.8 ? "attack" : "defend";
        if (hr < 0.6) return Math.random() < 0.5 ? "defend" : "attack";
        return Math.random() < 0.7 ? "attack" : "defend";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = dmg;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public boolean isGuarding() {
        return guarding;
    }

    public void setGuarding(boolean guarding) {
        this.guarding = guarding;
    }
}
