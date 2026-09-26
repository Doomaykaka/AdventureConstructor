import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

// ============ MINIMAL JSON PARSER ============
class JSON {
    private String s;
    private int p;

    private JSON(String s) {
        this.s = s;
        this.p = 0;
    }

    public static Object parse(String s) {
        JSON j = new JSON(s.trim());
        j.skip();
        return j.val();
    }

    void skip() {
        while (p < s.length() && Character.isWhitespace(s.charAt(p))) p++;
    }

    Object val() {
        skip();
        char c = s.charAt(p);
        if (c == '{') return obj();
        if (c == '[') return arr();
        if (c == '"') return str();
        if (c == 't' || c == 'f') return bool();
        if (c == 'n') {
            p += 4;
            return null;
        }
        return num();
    }

    Map<String, Object> obj() {
        Map<String, Object> m = new LinkedHashMap<>();
        p++;
        skip();
        if (s.charAt(p) == '}') {
            p++;
            return m;
        }
        while (true) {
            skip();
            String k = str();
            skip();
            p++;
            Object v = val();
            m.put(k, v);
            skip();
            if (s.charAt(p) == ',') {
                p++;
                continue;
            }
            if (s.charAt(p) == '}') {
                p++;
                break;
            }
            p++;
        }
        return m;
    }

    List<Object> arr() {
        List<Object> l = new ArrayList<>();
        p++;
        skip();
        if (s.charAt(p) == ']') {
            p++;
            return l;
        }
        while (true) {
            l.add(val());
            skip();
            if (s.charAt(p) == ',') {
                p++;
                continue;
            }
            if (s.charAt(p) == ']') {
                p++;
                break;
            }
            p++;
        }
        return l;
    }

    String str() {
        p++;
        StringBuilder sb = new StringBuilder();
        while (s.charAt(p) != '"') {
            if (s.charAt(p) == '\\') {
                p++;
                char c = s.charAt(p);
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
            } else sb.append(s.charAt(p));
            p++;
        }
        p++;
        return sb.toString();
    }

    Object num() {
        skip();
        int st = p;
        while (p < s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p) == '.' || s.charAt(p) == '-')) p++;
        String n = s.substring(st, p);
        if (n.contains(".")) return Double.parseDouble(n);
        return Integer.parseInt(n);
    }

    Boolean bool() {
        if (s.startsWith("true", p)) {
            p += 4;
            return true;
        }
        p += 5;
        return false;
    }

    @SuppressWarnings("unchecked")
    public static String gs(Map<String, Object> m, String k, String d) {
        Object v = m.get(k);
        return v != null ? v.toString() : d;
    }

    @SuppressWarnings("unchecked")
    public static int gi(Map<String, Object> m, String k, int d) {
        Object v = m.get(k);
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double) return ((Double) v).intValue();
        if (v != null)
            try {
                return Integer.parseInt(v.toString());
            } catch (Exception e) {
            }
        return d;
    }

    @SuppressWarnings("unchecked")
    public static boolean gb(Map<String, Object> m, String k, boolean d) {
        Object v = m.get(k);
        if (v instanceof Boolean) return (Boolean) v;
        return d;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> gl(Map<String, Object> m, String k) {
        Object v = m.get(k);
        List<Map<String, Object>> r = new ArrayList<>();
        if (v instanceof List) for (Object o : (List<Object>) v) if (o instanceof Map) r.add((Map<String, Object>) o);
        return r;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> gm(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Map) return (Map<String, Object>) v;
        return null;
    }
}

// ============ MODELS ============
class Item {
    String name, type;
    int value;

    Item(String n, String t, int v) {
        name = n;
        type = t;
        value = v;
    }

    public String toString() {
        return name + " (" + type + ": " + value + ")";
    }
}

class Player {
    int level = 1, hp = 50, maxHp = 50, gold = 0, exp = 0, expNext = 100, sp = 0;
    int str = 5, agi = 5, intl = 5, end = 5;
    List<Item> inv = new ArrayList<>();
    Item weapon = null, armor = null;

    int atkDmg() {
        return (weapon != null ? weapon.value : 2) + str;
    }

    int def() {
        return armor != null ? armor.value : 0;
    }

    int blockChance() {
        return 20 + agi * 2;
    }

    int fleeChance() {
        return 30 + agi * 2;
    }

    int skill(String s) {
        if ("persuasion".equals(s)) return intl;
        if ("intimidation".equals(s)) return str;
        return 0;
    }

    void gainExp(int a) {
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

    void allocSP(String st) {
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

    void heal(int a) {
        hp = Math.min(maxHp, hp + a);
    }

    void damage(int a) {
        hp = Math.max(0, hp - a);
    }

    void equip(Item it) {
        if ("weapon".equals(it.type)) {
            if (weapon != null) inv.add(weapon);
            weapon = it;
        } else if ("armor".equals(it.type)) {
            if (armor != null) inv.add(armor);
            armor = it;
        }
        inv.remove(it);
    }
}

class Enemy {
    String name;
    int hp, maxHp, dmg, def;
    boolean guarding = false;

    void scale(int pl) {
        int el = Math.max(1, pl - 2);
        maxHp = hp = 20 + el * 10;
        dmg = 4 + el;
        def = el / 2;
    }

    String chooseAction(Player p) {
        double hr = (double) hp / maxHp, pr = (double) p.hp / p.maxHp;
        if (hr < 0.2) return Math.random() < 0.7 ? "flee" : "defend";
        if (pr < 0.3) return Math.random() < 0.8 ? "attack" : "defend";
        if (hr < 0.6) return Math.random() < 0.5 ? "defend" : "attack";
        return Math.random() < 0.7 ? "attack" : "defend";
    }
}

class LocTemp {
    String id, type, name, desc;
    // peaceful
    String npcName, dialogId, onSuccess, onFail;
    // hostile
    String enemyName;
    int enemyHp, enemyDmg, enemyDef;
    String onVictory, onDefeat;
    // exploratory
    List<LocAct> actions = new ArrayList<>();
}

class LocAct {
    String text, result, next;
}

class LocTempCloner {
    static LocTemp clone(LocTemp src) {
        LocTemp l = new LocTemp();
        l.id = src.id;
        l.type = src.type;
        l.name = src.name;
        l.desc = src.desc;
        l.npcName = src.npcName;
        l.dialogId = src.dialogId;
        l.onSuccess = src.onSuccess;
        l.onFail = src.onFail;
        l.enemyName = src.enemyName;
        l.enemyHp = src.enemyHp;
        l.enemyDmg = src.enemyDmg;
        l.enemyDef = src.enemyDef;
        l.onVictory = src.onVictory;
        l.onDefeat = src.onDefeat;
        l.actions.addAll(src.actions);
        return l;
    }
}

class DialogData {
    String npcName, greeting;
    List<DialogNode> nodes = new ArrayList<>();

    DialogNode findNode(String id) {
        for (DialogNode n : nodes) if (n.id.equals(id)) return n;
        return null;
    }
}

class DialogNode {
    String id, text, reward;
    boolean endDialog = false, triggerCombat = false, success = false;
    List<DialogOption> options = new ArrayList<>();
}

class DialogOption {
    String text, type, next, skill, success, fail, check;
    int dc = 10;
}

// ============ SYNTAX PARSER ============
class SyntaxParser {
    Random rng = new Random();
    Player player;
    GameEngine engine;
    String nav = null;

    static String[] funcs = {
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

    String execute(String expr, Player p, GameEngine e) {
        this.player = p;
        this.engine = e;
        this.nav = null;
        if (expr == null || expr.trim().isEmpty()) return null;
        for (String part : expr.split(";")) {
            part = part.trim();
            if (!part.isEmpty()) execOne(part);
        }
        return nav;
    }

    void execOne(String expr) {
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

    boolean evalCond(String c) {
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

    int getStat(String s) {
        switch (s) {
            case "hp":
            case "char.hp":
                return player.hp;
            case "maxHp":
            case "char.maxHp":
                return player.maxHp;
            case "gold":
                return player.gold;
            case "level":
            case "char.level":
                return player.level;
            case "exp":
                return player.exp;
            case "sp":
                return player.sp;
            case "str":
            case "char.str":
                return player.str;
            case "agi":
            case "char.agi":
                return player.agi;
            case "intl":
            case "char.intl":
                return player.intl;
            case "end":
            case "char.end":
                return player.end;
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
                if ("scale".equals(fn)) return n * player.level;
                if ("scaleRND".equals(fn)) return rng.nextInt(n * player.level) + 1;
                return 0;
            }
        }
        return 0;
    }

    void applyMod(String tgt, String op, int v) {
        if ("hp".equals(tgt) || "char.hp".equals(tgt)) {
            if ("+=".equals(op)) player.heal(v);
            else if ("-=".equals(op)) player.damage(v);
            else player.hp = v;
        } else if ("gold".equals(tgt)) {
            if ("+=".equals(op)) player.gold += v;
            else if ("-=".equals(op)) player.gold = Math.max(0, player.gold - v);
            else player.gold = v;
        } else if ("exp".equals(tgt)) {
            if ("+=".equals(op)) player.gainExp(v);
            else if ("-=".equals(op)) player.exp = Math.max(0, player.exp - v);
            else player.exp = v;
        } else if ("sp".equals(tgt)) {
            if ("+=".equals(op)) player.sp += v;
            else if ("-=".equals(op)) player.sp = Math.max(0, player.sp - v);
            else player.sp = v;
        } else if ("str".equals(tgt) || "char.str".equals(tgt)) {
            if ("+=".equals(op)) player.str += v;
            else if ("-=".equals(op)) player.str -= v;
        } else if ("agi".equals(tgt) || "char.agi".equals(tgt)) {
            if ("+=".equals(op)) player.agi += v;
            else if ("-=".equals(op)) player.agi -= v;
        } else if ("intl".equals(tgt) || "char.intl".equals(tgt)) {
            if ("+=".equals(op)) player.intl += v;
            else if ("-=".equals(op)) player.intl -= v;
        } else if ("end".equals(tgt) || "char.end".equals(tgt)) {
            if ("+=".equals(op)) player.end += v;
            else if ("-=".equals(op)) player.end -= v;
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
                        engine.genItem("random");
                        return;
                    case "itemWeapon":
                        engine.genItem("weapon");
                        return;
                    case "itemArmor":
                        engine.genItem("armor");
                        return;
                    case "itemConsumable":
                        engine.genItem("consumable");
                        return;
                    case "damage":
                        player.damage(rng.nextInt(n) + 1);
                        return;
                    case "heal":
                        player.heal(n);
                        return;
                    case "gold":
                        player.gold += n;
                        return;
                    case "exp":
                        player.gainExp(n);
                        return;
                    case "sp":
                        player.sp += n;
                        return;
                    default:
                        return;
                }
            }
        }
    }
}

// ============ GAME ENGINE ============
class GameEngine {
    Player player;
    List<LocTemp> locPool = new ArrayList<>();
    Map<String, DialogData> dialogs = new HashMap<>();
    Map<String, List<String>> names = new HashMap<>();
    SyntaxParser parser = new SyntaxParser();
    Random rng = new Random();

    String mode = "START";
    LocTemp curLoc;
    DialogData curDialog;
    DialogNode curNode;
    Enemy curEnemy;
    boolean playerGuarding = false;
    List<String> combatLog = new ArrayList<>();
    String transitionMsg = "";

    @SuppressWarnings("unchecked")
    void loadData() {
        try {
            String locText = new String(Files.readAllBytes(Paths.get("data/locations.json")));
            Object parsed = JSON.parse(locText);
            if (parsed instanceof Map) {
                Map<String, Object> root = (Map<String, Object>) parsed;
                for (Map<String, Object> lm : JSON.gl(root, "locations")) {
                    LocTemp l = new LocTemp();
                    l.id = JSON.gs(lm, "id", "");
                    l.type = JSON.gs(lm, "type", "");
                    l.name = JSON.gs(lm, "name", "");
                    l.desc = JSON.gs(lm, "description", "");
                    if ("peaceful".equals(l.type)) {
                        l.npcName = JSON.gs(lm, "npc_name", "");
                        l.dialogId = JSON.gs(lm, "dialog_id", "");
                        l.onSuccess = JSON.gs(lm, "on_success", "");
                        l.onFail = JSON.gs(lm, "on_fail", "");
                    } else if ("hostile".equals(l.type)) {
                        l.enemyName = JSON.gs(lm, "enemy_name", "");
                        l.enemyHp = JSON.gi(lm, "enemy_hp", 30);
                        l.enemyDmg = JSON.gi(lm, "enemy_damage", 6);
                        l.enemyDef = JSON.gi(lm, "enemy_defense", 2);
                        l.onVictory = JSON.gs(lm, "on_victory", "");
                        l.onDefeat = JSON.gs(lm, "on_defeat", "");
                    } else if ("exploratory".equals(l.type)) {
                        for (Map<String, Object> am : JSON.gl(lm, "actions")) {
                            LocAct a = new LocAct();
                            a.text = JSON.gs(am, "text", "");
                            a.result = JSON.gs(am, "result", "");
                            a.next = JSON.gs(am, "next", "");
                            l.actions.add(a);
                        }
                    }
                    locPool.add(l);
                }
            }
        } catch (Exception e) {
            System.err.println("Load locations: " + e);
        }

        try {
            String dlgText = new String(Files.readAllBytes(Paths.get("data/dialogs.json")));
            Object parsed = JSON.parse(dlgText);
            if (parsed instanceof Map) {
                Map<String, Object> root = (Map<String, Object>) parsed;
                for (Map.Entry<String, Object> en : root.entrySet()) {
                    if (!(en.getValue() instanceof Map)) continue;
                    Map<String, Object> dm = (Map<String, Object>) en.getValue();
                    DialogData d = new DialogData();
                    d.npcName = JSON.gs(dm, "npc_name", "");
                    d.greeting = JSON.gs(dm, "greeting", "");
                    for (Map<String, Object> nm : JSON.gl(dm, "nodes")) {
                        DialogNode node = new DialogNode();
                        node.id = JSON.gs(nm, "id", "");
                        node.text = JSON.gs(nm, "text", "");
                        node.reward = JSON.gs(nm, "reward", "");
                        node.endDialog = JSON.gb(nm, "end_dialog", false);
                        node.triggerCombat = JSON.gb(nm, "trigger_combat", false);
                        node.success = JSON.gb(nm, "success", false);
                        for (Map<String, Object> om : JSON.gl(nm, "options")) {
                            DialogOption o = new DialogOption();
                            o.text = JSON.gs(om, "text", "");
                            o.type = JSON.gs(om, "type", "choice");
                            o.next = JSON.gs(om, "next", "");
                            o.skill = JSON.gs(om, "skill", "");
                            o.dc = JSON.gi(om, "dc", 10);
                            o.success = JSON.gs(om, "success", "");
                            o.fail = JSON.gs(om, "fail", "");
                            o.check = JSON.gs(om, "check", "");
                            node.options.add(o);
                        }
                        d.nodes.add(node);
                    }
                    dialogs.put(en.getKey(), d);
                }
            }
        } catch (Exception e) {
            System.err.println("Load dialogs: " + e);
        }

        try {
            String nmText = new String(Files.readAllBytes(Paths.get("data/names.json")));
            Object parsed = JSON.parse(nmText);
            if (parsed instanceof Map) {
                Map<String, Object> root = (Map<String, Object>) parsed;
                for (Map.Entry<String, Object> en : root.entrySet()) {
                    if (en.getValue() instanceof List) {
                        List<String> sl = new ArrayList<>();
                        for (Object o : (List<Object>) en.getValue()) if (o != null) sl.add(o.toString());
                        names.put(en.getKey(), sl);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Load names: " + e);
        }
    }

    void newGame() {
        player = new Player();
        genLocation(null);
        enterLoc();
    }

    String pickName(String pool) {
        List<String> l = names.get(pool);
        if (l == null || l.isEmpty()) return pool;
        return l.get(rng.nextInt(l.size()));
    }

    void genLocation(String forcedType) {
        List<LocTemp> pool = new ArrayList<>();
        for (LocTemp l : locPool) if (forcedType == null || l.type.equals(forcedType)) pool.add(l);
        if (pool.isEmpty()) pool = locPool;
        curLoc = LocTempCloner.clone(pool.get(rng.nextInt(pool.size())));
    }

    void enterLoc() {
        if ("peaceful".equals(curLoc.type)) {
            DialogData d = dialogs.get(curLoc.dialogId);
            if (d != null) {
                curDialog = d;
                curNode = d.findNode("start");
                mode = "DIALOG";
                return;
            }
        }
        if ("hostile".equals(curLoc.type)) {
            curEnemy = new Enemy();
            curEnemy.name = curLoc.enemyName;
            curEnemy.scale(player.level);
            playerGuarding = false;
            combatLog.clear();
            combatLog.add(curEnemy.name + " (HP: " + curEnemy.hp + "/" + curEnemy.maxHp + ") appears!");
            mode = "COMBAT";
            return;
        }
        mode = "EXPLORE";
    }

    String formatText(String text) {
        if (text == null) return "";
        String r = text;
        r = r.replace("{gold}", String.valueOf(player.gold));
        r = r.replace("{hp}", String.valueOf(player.hp));
        r = r.replace("{maxHp}", String.valueOf(player.maxHp));
        r = r.replace("{level}", String.valueOf(player.level));
        r = r.replace("{item}", pickName("weapon_names"));
        r = r.replace("{char.str}", String.valueOf(player.str));
        r = r.replace("{char.agi}", String.valueOf(player.agi));
        r = r.replace("{char.intl}", String.valueOf(player.intl));
        r = r.replace("{char.end}", String.valueOf(player.end));
        return r;
    }

    // ---- Dialog ----
    void chooseOption(DialogOption o) {
        if ("choice".equals(o.type)) {
            curNode = curDialog.findNode(o.next);
        } else if ("dice".equals(o.type)) {
            int roll = rng.nextInt(20) + 1;
            int sk = player.skill(o.skill);
            boolean ok = (roll + sk) >= o.dc;
            combatLog.add("Dice: " + roll + " + " + sk + " = " + (roll + sk) + " vs DC " + o.dc + " -> "
                    + (ok ? "SUCCESS" : "FAIL"));
            curNode = curDialog.findNode(ok ? o.success : o.fail);
        } else if ("condition".equals(o.type)) {
            parser.player = player;
            parser.engine = this;
            boolean ok = parser.evalCond(o.check);
            curNode = curDialog.findNode(ok ? o.success : o.fail);
        }
        if (curNode == null) {
            endDialog(false);
            return;
        }

        if (curNode.reward != null && !curNode.reward.isEmpty()) parser.execute(curNode.reward, player, this);

        if (curNode.triggerCombat) {
            curEnemy = new Enemy();
            curEnemy.name = curLoc.npcName;
            curEnemy.scale(player.level);
            playerGuarding = false;
            combatLog.clear();
            mode = "COMBAT";
            return;
        }
        if (curNode.endDialog) endDialog(curNode.success);
    }

    void endDialog(boolean success) {
        if (success && curLoc.onSuccess != null) parser.execute(curLoc.onSuccess, player, this);
        else if (!success && curLoc.onFail != null) parser.execute(curLoc.onFail, player, this);
        player.heal(player.maxHp / 4);
        transitionMsg = success ? "Dialog succeeded." : "Dialog failed.";
        mode = "TRANSITION";
    }

    // ---- Combat ----
    void pAttack() {
        int dmg = Math.max(1, player.atkDmg() - curEnemy.def);
        if (curEnemy.guarding) dmg = Math.max(1, dmg / 2);
        curEnemy.hp -= dmg;
        curEnemy.guarding = false;
        combatLog.add("You hit " + curEnemy.name + " for " + dmg + ".");
        if (checkCombatEnd()) return;
        enemyTurn();
    }

    void pDefend() {
        playerGuarding = true;
        combatLog.add("You brace for impact.");
        enemyTurn();
    }

    void pUseItem(Item it) {
        if ("consumable".equals(it.type)) {
            player.heal(it.value);
            player.inv.remove(it);
            combatLog.add("You use " + it.name + " (+HP " + it.value + ").");
        }
        enemyTurn();
    }

    void pFlee() {
        if (rng.nextInt(100) < player.fleeChance()) {
            combatLog.add("You escaped!");
            player.heal(player.maxHp / 4);
            transitionMsg = "Escaped successfully.";
            mode = "TRANSITION";
        } else {
            combatLog.add("Failed to escape!");
            enemyTurn();
        }
    }

    void enemyTurn() {
        if (curEnemy.hp <= 0 || player.hp <= 0) return;
        String act = curEnemy.chooseAction(player);
        if ("attack".equals(act)) {
            int dmg = Math.max(1, curEnemy.dmg - player.def());
            if (playerGuarding) {
                dmg = dmg / 2;
                if (rng.nextInt(100) < player.blockChance()) {
                    dmg = 0;
                    combatLog.add("You block completely!");
                } else combatLog.add("You reduce damage to " + dmg + ".");
            } else combatLog.add(curEnemy.name + " hits you for " + dmg + ".");
            player.damage(dmg);
        } else if ("defend".equals(act)) {
            curEnemy.guarding = true;
            combatLog.add(curEnemy.name + " guards.");
        } else if ("flee".equals(act)) {
            if (rng.nextInt(100) < 50) {
                combatLog.add(curEnemy.name + " flees!");
                player.gainExp(curEnemy.maxHp / 2);
                player.heal(player.maxHp / 4);
                transitionMsg = curEnemy.name + " fled! +" + (curEnemy.maxHp / 2) + " exp.";
                mode = "TRANSITION";
                return;
            } else combatLog.add(curEnemy.name + " tries to flee but fails!");
        }
        playerGuarding = false;
        checkCombatEnd();
    }

    boolean checkCombatEnd() {
        if (player.hp <= 0) {
            mode = "GAMEOVER";
            return true;
        }
        if (curEnemy.hp <= 0) {
            int exp = curEnemy.maxHp + (player.maxHp - player.hp);
            player.gainExp(exp);
            if (curLoc.onVictory != null) parser.execute(curLoc.onVictory, player, this);
            player.heal(player.maxHp / 4);
            transitionMsg = "Victory! +" + exp + " exp.";
            mode = "TRANSITION";
            return true;
        }
        return false;
    }

    // ---- Exploration ----
    void execAction(LocAct a) {
        String nav = parser.execute(a.result, player, this);
        if (player.hp <= 0) {
            mode = "GAMEOVER";
            return;
        }
        if (nav == null) nav = a.next;
        player.heal(player.maxHp / 4);
        if ("nextFight".equals(nav)) genLocation("hostile");
        else if ("nextNPC".equals(nav)) genLocation("peaceful");
        else if ("nextSearch".equals(nav)) genLocation("exploratory");
        else genLocation(null);
        enterLoc();
    }

    void continueAfterTransition() {
        genLocation(null);
        enterLoc();
    }

    void genItem(String type) {
        if ("random".equals(type)) {
            String[] t = {"weapon", "armor", "consumable"};
            type = t[rng.nextInt(3)];
        }
        Item it;
        if ("weapon".equals(type)) {
            it = new Item(pickName("weapon_names"), "weapon", rng.nextInt(3) + 1 + player.level);
        } else if ("armor".equals(type)) {
            it = new Item(pickName("armor_names"), "armor", rng.nextInt(2) + 1 + player.level / 2);
        } else {
            it = new Item(pickName("consumable_names"), "consumable", rng.nextInt(10) + 5 + player.level);
        }
        player.inv.add(it);
    }
}

// Allow cloning location templates

// ============ SCENE PANEL ============
class ScenePanel extends JPanel {
    String locName = "", locType = "";

    void setScene(String name, String type) {
        this.locName = name;
        this.locType = type;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg, border;
        if ("peaceful".equals(locType)) {
            bg = new Color(30, 70, 40);
            border = Color.GREEN;
        } else if ("hostile".equals(locType)) {
            bg = new Color(80, 20, 20);
            border = Color.RED;
        } else if ("exploratory".equals(locType)) {
            bg = new Color(80, 70, 20);
            border = Color.YELLOW;
        } else if ("GAMEOVER".equals(locType)) {
            bg = new Color(40, 10, 10);
            border = Color.RED;
        } else {
            bg = new Color(30, 30, 50);
            border = Color.CYAN;
        }
        g2.setColor(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(border);
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(3, 3, getWidth() - 6, getHeight() - 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(locName);
        g2.drawString(locName, (getWidth() - tw) / 2, getHeight() / 2 + 10);
        // Type label
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String tl = "";
        if ("peaceful".equals(locType)) tl = "PEACEFUL";
        else if ("hostile".equals(locType)) tl = "HOSTILE";
        else if ("exploratory".equals(locType)) tl = "EXPLORATORY";
        if (!tl.isEmpty()) {
            int tw2 = g2.getFontMetrics().stringWidth(tl);
            g2.drawString(tl, (getWidth() - tw2) / 2, getHeight() - 20);
        }
    }
}

// ============ GAME UI ============
class GameUI extends JFrame {
    GameEngine eng = new GameEngine();
    ScenePanel scenePanel = new ScenePanel();
    JTextArea textArea = new JTextArea();
    JPanel statsPanel = new JPanel();
    JPanel actionPanel = new JPanel();
    JLabel[] statLabels = new JLabel[12];
    JButton invBtn = new JButton("Inventory");
    JButton lvlBtn = new JButton("Level Up");

    GameUI() {
        setTitle("Endless Journey - MVP");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Scene panel (top center)
        scenePanel.setPreferredSize(new Dimension(900, 180));
        add(scenePanel, BorderLayout.NORTH);

        // Text area (center)
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(new Color(20, 20, 30));
        textArea.setForeground(Color.WHITE);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Stats panel (right)
        statsPanel.setPreferredSize(new Dimension(200, 0));
        statsPanel.setBackground(new Color(25, 25, 35));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        for (int i = 0; i < 12; i++) {
            statLabels[i] = new JLabel(" ");
            statLabels[i].setForeground(Color.WHITE);
            statLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 13));
            statsPanel.add(statLabels[i]);
        }
        statsPanel.add(Box.createVerticalStrut(10));
        invBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        lvlBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(invBtn);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lvlBtn);
        invBtn.addActionListener(e -> showInventory());
        lvlBtn.addActionListener(e -> showLevelUp());
        add(statsPanel, BorderLayout.EAST);

        // Action panel (bottom)
        actionPanel.setPreferredSize(new Dimension(900, 60));
        actionPanel.setBackground(new Color(30, 30, 40));
        add(actionPanel, BorderLayout.SOUTH);

        eng.loadData();
        updateUI();
    }

    void updateUI() {
        updateStats();
        SwingUtilities.invokeLater(() -> {
            actionPanel.removeAll();
            textArea.setText("");
            switch (eng.mode) {
                case "START":
                    showStart();
                    break;
                case "EXPLORE":
                    showExplore();
                    break;
                case "COMBAT":
                    showCombat();
                    break;
                case "DIALOG":
                    showDialog();
                    break;
                case "TRANSITION":
                    showTransition();
                    break;
                case "GAMEOVER":
                    showGameOver();
                    break;
            }
            actionPanel.revalidate();
            actionPanel.repaint();
        });
    }

    void updateStats() {
        if (eng.player == null) {
            for (JLabel l : statLabels) l.setText(" ");
            invBtn.setEnabled(false);
            lvlBtn.setEnabled(false);
            return;
        }
        Player p = eng.player;
        statLabels[0].setText(" HP: " + p.hp + "/" + p.maxHp);
        statLabels[1].setText(" Gold: " + p.gold);
        statLabels[2].setText(" Level: " + p.level);
        statLabels[3].setText(" EXP: " + p.exp + "/" + p.expNext);
        statLabels[4].setText(" SP: " + p.sp);
        statLabels[5].setText(" STR: " + p.str);
        statLabels[6].setText(" AGI: " + p.agi);
        statLabels[7].setText(" INT: " + p.intl);
        statLabels[8].setText(" END: " + p.end);
        String wpn = p.weapon != null ? p.weapon.name : "-";
        String arm = p.armor != null ? p.armor.name : "-";
        statLabels[9].setText(" WPN: " + wpn);
        statLabels[10].setText(" ARM: " + arm);
        statLabels[11].setText(" Items: " + p.inv.size());
        invBtn.setEnabled(true);
        lvlBtn.setEnabled(p.sp > 0);
    }

    void addButton(String label, ActionListener al) {
        JButton b = new JButton(label);
        b.addActionListener(al);
        b.setPreferredSize(new Dimension(160, 35));
        actionPanel.add(b);
    }

    void showStart() {
        scenePanel.setScene("Endless Journey", "start");
        textArea.setText(
                "Welcome to Endless Journey!\n\n" + "A procedurally generated RPG. Travel through endless locations,\n"
                        + "fight enemies, talk to NPCs, explore ruins.\n\n"
                        + "Death resets everything. Choose wisely.\n\n"
                        + "Stats:\n"
                        + "  STR - damage in combat\n"
                        + "  AGI - block & flee chance\n"
                        + "  INT - persuasion skill\n"
                        + "  END - max HP\n\n"
                        + "Click NEW GAME to begin.");
        addButton("New Game", e -> {
            eng.newGame();
            updateUI();
        });
    }

    void showExplore() {
        scenePanel.setScene(eng.curLoc.name, eng.curLoc.type);
        textArea.setText(eng.formatText(eng.curLoc.desc));
        for (LocAct a : eng.curLoc.actions) {
            addButton(eng.formatText(a.text), e -> {
                eng.execAction(a);
                updateUI();
            });
        }
    }

    void showCombat() {
        scenePanel.setScene("Combat: " + eng.curEnemy.name, "hostile");
        StringBuilder sb = new StringBuilder();
        sb.append("Enemy: ")
                .append(eng.curEnemy.name)
                .append(" (HP: ")
                .append(eng.curEnemy.hp)
                .append("/")
                .append(eng.curEnemy.maxHp)
                .append(")\n");
        sb.append("Enemy DEF: ")
                .append(eng.curEnemy.def)
                .append("  DMG: ")
                .append(eng.curEnemy.dmg)
                .append("\n\n");
        for (String log : eng.combatLog) sb.append(log).append("\n");
        textArea.setText(sb.toString());
        addButton("Attack", e -> {
            eng.pAttack();
            updateUI();
        });
        addButton("Defend", e -> {
            eng.pDefend();
            updateUI();
        });
        addButton("Item", e -> {
            showCombatInventory();
        });
        addButton("Flee", e -> {
            eng.pFlee();
            updateUI();
        });
    }

    void showDialog() {
        scenePanel.setScene("Talk: " + eng.curDialog.npcName, "peaceful");
        StringBuilder sb = new StringBuilder();
        sb.append(eng.curDialog.npcName).append(":\n");
        if (eng.curNode != null) {
            sb.append(eng.formatText(eng.curNode.text)).append("\n");
            textArea.setText(sb.toString());
            int idx = 1;
            for (DialogOption o : eng.curNode.options) {
                String label = "[" + idx + "] " + eng.formatText(o.text);
                if ("dice".equals(o.type)) label += " (DC:" + o.dc + ")";
                addButton(label, e -> {
                    eng.chooseOption(o);
                    updateUI();
                });
                idx++;
            }
        }
    }

    void showTransition() {
        scenePanel.setScene("...", "");
        textArea.setText(eng.transitionMsg + "\n\nHP restored: +" + eng.player.maxHp / 4);
        addButton("Continue", e -> {
            eng.continueAfterTransition();
            updateUI();
        });
    }

    void showGameOver() {
        scenePanel.setScene("YOU DIED", "GAMEOVER");
        textArea.setText("Your journey ends here.\nLevel reached: " + eng.player.level + "\nGold: " + eng.player.gold
                + "\n\nAll progress lost.");
        addButton("New Game", e -> {
            eng.newGame();
            updateUI();
        });
    }

    void showInventory() {
        if (eng.player == null) return;
        JDialog d = new JDialog(this, "Inventory", true);
        d.setSize(350, 400);
        d.setLayout(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        for (Item it : eng.player.inv) model.addElement(it.toString());
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel bp = new JPanel();
        JButton eq = new JButton("Equip/Use");
        JButton cl = new JButton("Close");
        eq.addActionListener(e -> {
            int i = list.getSelectedIndex();
            if (i >= 0 && i < eng.player.inv.size()) {
                Item it = eng.player.inv.get(i);
                if ("consumable".equals(it.type)) {
                    eng.player.heal(it.value);
                    eng.player.inv.remove(i);
                } else {
                    eng.player.equip(it);
                }
                model.clear();
                for (Item it2 : eng.player.inv) model.addElement(it2.toString());
                updateStats();
            }
        });
        cl.addActionListener(e -> d.dispose());
        bp.add(eq);
        bp.add(cl);
        d.add(bp, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    void showCombatInventory() {
        if (eng.player == null || eng.player.inv.isEmpty()) {
            textArea.setText("No consumables available.");
            return;
        }
        JDialog d = new JDialog(this, "Use Item", true);
        d.setSize(300, 350);
        d.setLayout(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        for (Item it : eng.player.inv) if ("consumable".equals(it.type)) model.addElement(it.toString());
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel bp = new JPanel();
        JButton use = new JButton("Use");
        JButton cl = new JButton("Cancel");
        use.addActionListener(e -> {
            int i = list.getSelectedIndex();
            if (i >= 0) {
                int cnt = 0;
                for (Item it : eng.player.inv) {
                    if ("consumable".equals(it.type)) {
                        if (cnt == i) {
                            eng.pUseItem(it);
                            d.dispose();
                            updateUI();
                            return;
                        }
                        cnt++;
                    }
                }
            }
        });
        cl.addActionListener(e -> d.dispose());
        bp.add(use);
        bp.add(cl);
        d.add(bp, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    void showLevelUp() {
        if (eng.player == null || eng.player.sp <= 0) return;
        JDialog d = new JDialog(this, "Allocate SP (" + eng.player.sp + ")", true);
        d.setSize(250, 200);
        d.setLayout(new GridLayout(5, 1));
        JLabel info = new JLabel("SP: " + eng.player.sp, SwingConstants.CENTER);
        d.add(info);
        String[] stats = {"str", "agi", "intl", "end"};
        for (String st : stats) {
            JButton b = new JButton("+ " + st.toUpperCase());
            b.addActionListener(e -> {
                eng.player.allocSP(st);
                info.setText("SP: " + eng.player.sp);
                if (eng.player.sp <= 0) {
                    d.dispose();
                    updateStats();
                }
                updateStats();
            });
            d.add(b);
        }
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}

// ============ MAIN ============
public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameUI ui = new GameUI();
            ui.setLocationRelativeTo(null);
            ui.setVisible(true);
        });
    }
}
