package adventureconstructor.utils;

import adventureconstructor.controllers.GameEngine;
import adventureconstructor.models.Player;
import java.util.Random;

public class SyntaxParser {
    private Random rng = new Random();
    private Player player;
    private GameEngine engine;
    private String nav = null;

    private static String[] funcs = {
        "itemConsumable",
        "itemWeapon",
        "itemArmor",
        "nextSearch",
        "tryEscape",
        "nextFight",
        "scaleRND",
        "nextNPC",
        "random",
        "damage",
        "defend",
        "scale",
        "block",
        "heal",
        "gold",
        "death",
        "item",
        "exp",
        "sp"
    };

    public String execute(String expr, Player p, GameEngine e) {
        this.setPlayer(p);
        this.setEngine(e);
        this.nav = null;
        if (expr == null || expr.trim().isEmpty()) return null;
        for (String part : expr.split(";")) {
            part = part.trim();
            if (!part.isEmpty()) execOne(part);
        }
        return nav;
    }

    public void execOne(String expr) {
        int qi = expr.indexOf('?');
        if (qi >= 0) {
            String cond = expr.substring(0, qi).trim();
            int ci = expr.lastIndexOf(':');
            String t = expr.substring(qi + 1, ci).trim();
            String f = expr.substring(ci + 1).trim();
            execOne(evalCond(cond) ? t : f);
            return;
        }
        for (String op : new String[] {"+=", "-=", "="}) {
            int oi = expr.indexOf(op);
            if (oi > 0) {
                String tgt = expr.substring(0, oi).trim();
                String val = expr.substring(oi + op.length()).trim();
                int v = evalVal(val);
                applyMod(tgt, op, v);
                return;
            }
        }
        execFunc(expr);
    }

    public boolean evalCond(String c) {
        for (String op : new String[] {">=", "<=", "==", "!=", "<", ">"}) {
            int i = c.indexOf(op);
            if (i > 0) {
                String l = c.substring(0, i).trim();
                String r = c.substring(i + op.length()).trim();
                int lv = getStat(l), rv = evalVal(r);
                if (">=".equals(op)) return lv >= rv;
                if ("<=".equals(op)) return lv <= rv;
                if ("==".equals(op)) return lv == rv;
                if ("!=".equals(op)) return lv != rv;
                if (">".equals(op)) return lv > rv;
                if ("<".equals(op)) return lv < rv;
            }
        }
        return false;
    }

    public int getStat(String s) {
        switch (s) {
            case "hp":
            case "char.hp":
                return getPlayer().getHp();
            case "maxHp":
            case "char.maxHp":
                return getPlayer().getMaxHp();
            case "gold":
                return getPlayer().getGold();
            case "level":
            case "char.level":
                return getPlayer().getLevel();
            case "exp":
                return getPlayer().getExp();
            case "sp":
                return getPlayer().getSp();
            case "str":
            case "char.str":
                return getPlayer().getStr();
            case "agi":
            case "char.agi":
                return getPlayer().getAgi();
            case "intl":
            case "char.intl":
                return getPlayer().getIntl();
            case "end":
            case "char.end":
                return getPlayer().getEnd();
        }
        return 0;
    }

    int evalVal(String e) {
        e = e.trim();
        try {
            return Integer.parseInt(e);
        } catch (Exception ex) {
        }
        for (String fn : funcs) {
            if (e.startsWith(fn)) {
                String np = e.substring(fn.length());
                int n = 0;
                if (!np.isEmpty())
                    try {
                        n = Integer.parseInt(np);
                    } catch (Exception x) {
                    }
                if ("random".equals(fn) || "damage".equals(fn)) return rng.nextInt(n) + 1;
                if ("heal".equals(fn) || "gold".equals(fn) || "exp".equals(fn) || "sp".equals(fn)) return n;
                if ("scale".equals(fn)) return n * getPlayer().getLevel();
                if ("scaleRND".equals(fn)) return rng.nextInt(n * getPlayer().getLevel()) + 1;
                return 0;
            }
        }
        return 0;
    }

    void applyMod(String tgt, String op, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().heal(v);
            else if ("-=".equals(op)) getPlayer().damage(v);
            else getPlayer().setHp(v);
        } else if ("gold".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setGold(getPlayer().getGold() + v);
            else if ("-=".equals(op))
                getPlayer().setGold(Math.max(0, getPlayer().getGold() - v));
            else getPlayer().setGold(v);
        } else if ("exp".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().gainExp(v);
            else if ("-=".equals(op)) getPlayer().setExp(Math.max(0, getPlayer().getExp() - v));
            else getPlayer().setExp(v);
        } else if ("sp".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setSp(getPlayer().getSp() + v);
            else if ("-=".equals(op)) getPlayer().setSp(Math.max(0, getPlayer().getSp() - v));
            else getPlayer().setSp(v);
        } else if ("str".equals(tgt) || "char.str".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setStr(getPlayer().getStr() + v);
            else if ("-=".equals(op)) getPlayer().setStr(getPlayer().getStr() - v);
        } else if ("agi".equals(tgt) || "char.agi".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setAgi(getPlayer().getAgi() + v);
            else if ("-=".equals(op)) getPlayer().setAgi(getPlayer().getAgi() - v);
        } else if ("intl".equals(tgt) || "char.intl".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setIntl(getPlayer().getIntl() + v);
            else if ("-=".equals(op)) getPlayer().setIntl(getPlayer().getIntl() - v);
        } else if ("end".equals(tgt) || "char.end".equals(tgt)) {
            if ("+=".equals(op)) getPlayer().setEnd(getPlayer().getEnd() + v);
            else if ("-=".equals(op)) getPlayer().setEnd(getPlayer().getEnd() - v);
        }
    }

    void execFunc(String e) {
        for (String fn : funcs) {
            if (e.startsWith(fn)) {
                String np = e.substring(fn.length());
                int n = 0;
                if (!np.isEmpty())
                    try {
                        n = Integer.parseInt(np);
                    } catch (Exception x) {
                    }
                switch (fn) {
                    case "nextFight":
                        nav = "nextFight";
                        return;
                    case "nextNPC":
                        nav = "nextNPC";
                        return;
                    case "nextSearch":
                        nav = "nextSearch";
                        return;
                    case "tryEscape":
                        nav = "tryEscape";
                        return;
                    case "death":
                        nav = "death";
                        return;
                    case "item":
                        getEngine().genItem("random");
                        return;
                    case "itemWeapon":
                        getEngine().genItem("weapon");
                        return;
                    case "itemArmor":
                        getEngine().genItem("armor");
                        return;
                    case "itemConsumable":
                        getEngine().genItem("consumable");
                        return;
                    case "damage":
                        getPlayer().damage(rng.nextInt(n) + 1);
                        return;
                    case "heal":
                        getPlayer().heal(n);
                        return;
                    case "gold":
                        getPlayer().setGold(getPlayer().getGold() + n);
                        return;
                    case "exp":
                        getPlayer().gainExp(n);
                        return;
                    case "sp":
                        getPlayer().setSp(getPlayer().getSp() + n);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GameEngine getEngine() {
        return engine;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}
