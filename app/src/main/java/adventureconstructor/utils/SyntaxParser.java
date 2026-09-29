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
        "clearInventory",
        "log",
        "removeWeapon",
        "removeArmor",
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

            if (!part.isEmpty()) execOneExpression(part);
        }

        return nav;
    }

    private void execOneExpression(String expr) {
        int qi = expr.indexOf('?');

        if (qi >= 0) {
            parseCondition(expr, qi);

            return;
        }

        for (String op : new String[] {"+=", "-=", "*=", "/=", "^=", "="}) {
            int oi = expr.indexOf(op);

            if (oi > 0) {
                parseOperator(expr, op, oi);

                return;
            }
        }

        execFunc(expr);
    }

    private void parseCondition(String expr, int qi) {
        String cond = expr.substring(0, qi).trim();
        int ci = expr.lastIndexOf(':');
        String t = expr.substring(qi + 1, ci).trim();
        String f = expr.substring(ci + 1).trim();
        execOneExpression(evalCondition(cond) ? t : f);
    }

    private void parseOperator(String expr, String op, int oi) {
        String tgt = expr.substring(0, oi).trim();
        String val = expr.substring(oi + op.length()).trim();
        int v = evalValue(val);
        applyModifier(tgt, op, v);
    }

    public boolean evalCondition(String c) {
        for (String op : new String[] {">=", "<=", "==", "!=", "<", ">"}) {
            int i = c.indexOf(op);

            if (i > 0) {
                String l = c.substring(0, i).trim();
                String r = c.substring(i + op.length()).trim();
                int lv = evalValue(l), rv = evalValue(r);

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

    private int getStat(String s) {
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
            case "score":
                return getPlayer().getScore();
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

    private void setStatValue(String tgt, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) getPlayer().setHp(v);
        else if ("gold".equals(tgt)) getPlayer().setGold(v);
        else if ("exp".equals(tgt)) getPlayer().setExp(v);
        else if ("score".equals(tgt)) getPlayer().setScore(v);
        else if ("sp".equals(tgt)) getPlayer().setSp(v);
        else if ("str".equals(tgt) || "char.str".equals(tgt)) getPlayer().setStr(v);
        else if ("agi".equals(tgt) || "char.agi".equals(tgt)) getPlayer().setAgi(v);
        else if ("intl".equals(tgt) || "char.intl".equals(tgt)) getPlayer().setIntl(v);
        else if ("end".equals(tgt) || "char.end".equals(tgt)) getPlayer().setEnd(v);
    }

    private int evalValue(String e) {
        e = e.trim();
        if (e.isEmpty()) return 0;

        try {
            return Integer.parseInt(e);
        } catch (Exception ex) {
        }

        int idx = findLastBinaryOp(e, '+', '-');
        if (idx > 0) {
            return evalSumSub(e, idx);
        }

        idx = findLastBinaryOp(e, '*', '/');
        if (idx > 0) {
            return evalMulDiv(e, idx);
        }

        idx = e.indexOf('^');
        if (idx > 0) {
            return evalPow(e, idx);
        }

        for (String fn : funcs) {
            if (e.startsWith(fn)) {
                return evalFunc(e, fn);
            }
        }

        return getStat(e);
    }

    private int evalFunc(String e, String fn) {
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

    private int evalSumSub(String e, int idx) {
        char op = e.charAt(idx);
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        return op == '+' ? left + right : left - right;
    }

    private int evalMulDiv(String e, int idx) {
        char op = e.charAt(idx);
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        if (op == '*') return left * right;
        return right != 0 ? left / right : 0;
    }

    private int evalPow(String e, int idx) {
        int left = evalValue(e.substring(0, idx).trim());
        int right = evalValue(e.substring(idx + 1).trim());
        return (int) Math.pow(left, right);
    }

    private int findLastBinaryOp(String e, char op1, char op2) {
        for (int i = e.length() - 1; i > 0; i--) {
            char c = e.charAt(i);
            if (c == op1 || c == op2) {
                char prev = e.charAt(i - 1);
                if (prev != '+' && prev != '-' && prev != '*' && prev != '/' && prev != '^') {
                    return i;
                }
            }
        }
        return -1;
    }

    private void applyModifier(String tgt, String op, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) {
            if ("+=".equals(op)) {
                getPlayer().heal(v);
                return;
            }
            if ("-=".equals(op)) {
                getPlayer().damage(v);
                return;
            }
        }
        if ("exp".equals(tgt) && "+=".equals(op)) {
            getPlayer().gainExp(v);
            return;
        }

        int cur = getStat(tgt);
        int newVal;
        switch (op) {
            case "+=":
                newVal = cur + v;
                break;
            case "-=":
                if ("gold".equals(tgt) || "sp".equals(tgt)) newVal = Math.max(0, cur - v);
                else newVal = cur - v;
                break;
            case "*=":
                newVal = cur * v;
                break;
            case "/=":
                newVal = v != 0 ? cur / v : 0;
                break;
            case "^=":
                newVal = (int) Math.pow(cur, v);
                break;
            default:
                newVal = v;
                break;
        }
        setStatValue(tgt, newVal);
    }

    private void execFunc(String e) {
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
                    case "log":
                        String message = np.trim();
                        if (message.startsWith("(") && message.endsWith(")"))
                            message = message.substring(1, message.length() - 1).trim();
                        if (message.length() >= 2
                                && ((message.startsWith("\"") && message.endsWith("\""))
                                        || (message.startsWith("'") && message.endsWith("'"))))
                            message = message.substring(1, message.length() - 1);
                        getEngine().getCombatLog().add(getEngine().formatText(message));
                        return;
                    case "clearInventory":
                        getPlayer().getInv().clear();
                        return;
                    case "removeWeapon":
                        getPlayer().setWeapon(null);
                        return;
                    case "removeArmor":
                        getPlayer().setArmor(null);
                        return;
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
