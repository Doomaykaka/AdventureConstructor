package adventureconstructor.controllers;

import adventureconstructor.models.DialogData;
import adventureconstructor.models.DialogNode;
import adventureconstructor.models.DialogOption;
import adventureconstructor.models.Enemy;
import adventureconstructor.models.Item;
import adventureconstructor.models.LocAct;
import adventureconstructor.models.LocTemp;
import adventureconstructor.models.LocTempCloner;
import adventureconstructor.models.Player;
import adventureconstructor.utils.SyntaxParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GameEngine {
    private Player player;
    private List<LocTemp> locPool = new ArrayList<>();
    private Map<String, DialogData> dialogs = new HashMap<>();
    private Map<String, List<String>> names = new HashMap<>();
    private SyntaxParser parser = new SyntaxParser();
    private Random rng = new Random();

    private String mode = "START";
    private LocTemp curLoc;
    private DialogData curDialog;
    private DialogNode curNode;
    private Enemy curEnemy;
    private boolean playerGuarding = false;
    private List<String> combatLog = new ArrayList<>();
    private String transitionMsg = "";

    private static final String DATA_PARENT_FOLDER_NAME = "user.dir";

    @SuppressWarnings("unchecked")
    public void loadData() {
        JSONParser parser = new JSONParser();

        // ---- locations.json ----
        try {
            Path locPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/locations.json")
                    .toFile()
                    .getAbsolutePath());
            String locText = new String(Files.readAllBytes(locPath));
            JSONObject root = (JSONObject) parser.parse(locText);
            JSONArray locs = (JSONArray) root.get("locations");
            if (locs != null) {
                for (Object lo : locs) {
                    JSONObject lm = (JSONObject) lo;
                    LocTemp l = new LocTemp();
                    l.setId(jStr(lm, "id", ""));
                    l.setType(jStr(lm, "type", ""));
                    l.setName(jStr(lm, "name", ""));
                    l.setDesc(jStr(lm, "description", ""));
                    if ("peaceful".equals(l.getType())) {
                        l.setNpcName(jStr(lm, "npc_name", ""));
                        l.setDialogId(jStr(lm, "dialog_id", ""));
                        l.setOnSuccess(jStr(lm, "on_success", ""));
                        l.setOnFail(jStr(lm, "on_fail", ""));
                    } else if ("hostile".equals(l.getType())) {
                        l.setEnemyName(jStr(lm, "enemy_name", ""));
                        l.setEnemyHp(jInt(lm, "enemy_hp", 30));
                        l.setEnemyDmg(jInt(lm, "enemy_damage", 6));
                        l.setEnemyDef(jInt(lm, "enemy_defense", 2));
                        l.setOnVictory(jStr(lm, "on_victory", ""));
                        l.setOnDefeat(jStr(lm, "on_defeat", ""));
                    } else if ("exploratory".equals(l.getType())) {
                        JSONArray acts = (JSONArray) lm.get("actions");
                        if (acts != null) {
                            for (Object ao : acts) {
                                JSONObject am = (JSONObject) ao;
                                LocAct a = new LocAct();
                                a.setText(jStr(am, "text", ""));
                                a.setResult(jStr(am, "result", ""));
                                a.setNext(jStr(am, "next", ""));
                                l.getActions().add(a);
                            }
                        }
                    }
                    locPool.add(l);
                }
            }
        } catch (Exception e) {
            System.err.println("Load locations: " + e);
        }

        // ---- dialogs.json ----
        try {
            Path dlgPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/dialogs.json")
                    .toFile()
                    .getAbsolutePath());
            String dlgText = new String(Files.readAllBytes(dlgPath));
            JSONObject root = (JSONObject) parser.parse(dlgText);
            for (Object eo : root.entrySet()) {
                Map.Entry<String, Object> en = (Map.Entry<String, Object>) eo;
                if (!(en.getValue() instanceof JSONObject)) continue;
                JSONObject dm = (JSONObject) en.getValue();
                DialogData d = new DialogData();
                d.setNpcName(jStr(dm, "npc_name", ""));
                d.setGreeting(jStr(dm, "greeting", ""));
                JSONArray nodes = (JSONArray) dm.get("nodes");
                if (nodes != null) {
                    for (Object no : nodes) {
                        JSONObject nm = (JSONObject) no;
                        DialogNode node = new DialogNode();
                        node.setId(jStr(nm, "id", ""));
                        node.setText(jStr(nm, "text", ""));
                        node.setReward(jStr(nm, "reward", ""));
                        node.setEndDialog(jBool(nm, "end_dialog", false));
                        node.setTriggerCombat(jBool(nm, "trigger_combat", false));
                        node.setSuccess(jBool(nm, "success", false));
                        JSONArray opts = (JSONArray) nm.get("options");
                        if (opts != null) {
                            for (Object oo : opts) {
                                JSONObject om = (JSONObject) oo;
                                DialogOption o = new DialogOption();
                                o.setText(jStr(om, "text", ""));
                                o.setType(jStr(om, "type", "choice"));
                                o.setNext(jStr(om, "next", ""));
                                o.setSkill(jStr(om, "skill", ""));
                                o.setDc(jInt(om, "dc", 10));
                                o.setSuccess(jStr(om, "success", ""));
                                o.setFail(jStr(om, "fail", ""));
                                o.setCheck(jStr(om, "check", ""));
                                node.getOptions().add(o);
                            }
                        }
                        d.getNodes().add(node);
                    }
                }
                dialogs.put(en.getKey(), d);
            }
        } catch (Exception e) {
            System.err.println("Load dialogs: " + e);
        }

        // ---- names.json ----
        try {
            Path nmPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/names.json")
                    .toFile()
                    .getAbsolutePath());
            String nmText = new String(Files.readAllBytes(nmPath));
            JSONObject root = (JSONObject) parser.parse(nmText);
            for (Object eo : root.entrySet()) {
                Map.Entry<String, Object> en = (Map.Entry<String, Object>) eo;
                if (en.getValue() instanceof JSONArray) {
                    List<String> sl = new ArrayList<>();
                    for (Object o : (JSONArray) en.getValue()) {
                        if (o != null) sl.add(o.toString());
                    }
                    names.put(en.getKey(), sl);
                }
            }
        } catch (Exception e) {
            System.err.println("Load names: " + e);
        }
    }

    // ---- Вспомогательные методы для json-simple ----

    private String jStr(JSONObject obj, String key, String def) {
        Object v = obj.get(key);
        return v != null ? v.toString() : def;
    }

    private int jInt(JSONObject obj, String key, int def) {
        Object v = obj.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return def;
        }
    }

    private boolean jBool(JSONObject obj, String key, boolean def) {
        Object v = obj.get(key);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(v.toString());
    }

    public void newGame() {
        setPlayer(new Player());
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
        for (LocTemp l : locPool) if (forcedType == null || l.getType().equals(forcedType)) pool.add(l);
        if (pool.isEmpty()) pool = locPool;
        setCurLoc(LocTempCloner.clone(pool.get(rng.nextInt(pool.size()))));
    }

    void enterLoc() {
        if ("peaceful".equals(getCurLoc().getType())) {
            DialogData d = dialogs.get(getCurLoc().getDialogId());
            if (d != null) {
                setCurDialog(d);
                setCurNode(d.findNode("start"));
                setMode("DIALOG");
                return;
            }
        }
        if ("hostile".equals(getCurLoc().getType())) {
            setCurEnemy(new Enemy());
            getCurEnemy().setName(getCurLoc().getEnemyName());
            getCurEnemy().scale(getPlayer().getLevel());
            playerGuarding = false;
            getCombatLog().clear();
            getCombatLog()
                    .add(getCurEnemy().getName() + " (HP: " + getCurEnemy().getHp() + "/"
                            + getCurEnemy().getMaxHp() + ") appears!");
            setMode("COMBAT");
            return;
        }
        setMode("EXPLORE");
    }

    public String formatText(String text) {
        if (text == null) return "";
        String r = text;
        r = r.replace("{gold}", String.valueOf(getPlayer().getGold()));
        r = r.replace("{hp}", String.valueOf(getPlayer().getHp()));
        r = r.replace("{maxHp}", String.valueOf(getPlayer().getMaxHp()));
        r = r.replace("{level}", String.valueOf(getPlayer().getLevel()));
        r = r.replace("{item}", pickName("weapon_names"));
        r = r.replace("{char.str}", String.valueOf(getPlayer().getStr()));
        r = r.replace("{char.agi}", String.valueOf(getPlayer().getAgi()));
        r = r.replace("{char.intl}", String.valueOf(getPlayer().getIntl()));
        r = r.replace("{char.end}", String.valueOf(getPlayer().getEnd()));
        return r;
    }

    // ---- Dialog ----
    public void chooseOption(DialogOption o) {
        if ("choice".equals(o.getType())) {
            setCurNode(getCurDialog().findNode(o.getNext()));
        } else if ("dice".equals(o.getType())) {
            int roll = rng.nextInt(20) + 1;
            int sk = getPlayer().skill(o.getSkill());
            boolean ok = (roll + sk) >= o.getDc();
            getCombatLog()
                    .add("Dice: " + roll + " + " + sk + " = " + (roll + sk) + " vs DC " + o.getDc() + " -> "
                            + (ok ? "SUCCESS" : "FAIL"));
            setCurNode(getCurDialog().findNode(ok ? o.getSuccess() : o.getFail()));
        } else if ("condition".equals(o.getType())) {
            parser.setPlayer(getPlayer());
            parser.setEngine(this);
            boolean ok = parser.evalCond(o.getCheck());
            setCurNode(getCurDialog().findNode(ok ? o.getSuccess() : o.getFail()));
        }
        if (getCurNode() == null) {
            endDialog(false);
            return;
        }

        if (getCurNode().getReward() != null && !getCurNode().getReward().isEmpty())
            parser.execute(getCurNode().getReward(), getPlayer(), this);

        if (getCurNode().isTriggerCombat()) {
            setCurEnemy(new Enemy());
            getCurEnemy().setName(getCurLoc().getNpcName());
            getCurEnemy().scale(getPlayer().getLevel());
            playerGuarding = false;
            getCombatLog().clear();
            setMode("COMBAT");
            return;
        }
        if (getCurNode().isEndDialog()) endDialog(getCurNode().isSuccess());
    }

    void endDialog(boolean success) {
        if (success && getCurLoc().getOnSuccess() != null)
            parser.execute(getCurLoc().getOnSuccess(), getPlayer(), this);
        else if (!success && getCurLoc().getOnFail() != null)
            parser.execute(getCurLoc().getOnFail(), getPlayer(), this);
        getPlayer().heal(getPlayer().getMaxHp() / 4);
        setTransitionMsg(success ? "Dialog succeeded." : "Dialog failed.");
        setMode("TRANSITION");
    }

    // ---- Combat ----
    public void pAttack() {
        int dmg = Math.max(1, getPlayer().atkDmg() - getCurEnemy().getDef());
        if (getCurEnemy().isGuarding()) dmg = Math.max(1, dmg / 2);
        getCurEnemy().setHp(getCurEnemy().getHp() - dmg);
        getCurEnemy().setGuarding(false);
        getCombatLog().add("You hit " + getCurEnemy().getName() + " for " + dmg + ".");
        if (checkCombatEnd()) return;
        enemyTurn();
    }

    public void pDefend() {
        playerGuarding = true;
        getCombatLog().add("You brace for impact.");
        enemyTurn();
    }

    public void pUseItem(Item it) {
        if ("consumable".equals(it.getType())) {
            getPlayer().heal(it.getValue());
            getPlayer().getInv().remove(it);
            getCombatLog().add("You use " + it.getName() + " (+HP " + it.getValue() + ").");
        }
        enemyTurn();
    }

    public void pFlee() {
        if (rng.nextInt(100) < getPlayer().fleeChance()) {
            getCombatLog().add("You escaped!");
            getPlayer().heal(getPlayer().getMaxHp() / 4);
            setTransitionMsg("Escaped successfully.");
            setMode("TRANSITION");
        } else {
            getCombatLog().add("Failed to escape!");
            enemyTurn();
        }
    }

    void enemyTurn() {
        if (getCurEnemy().getHp() <= 0 || getPlayer().getHp() <= 0) return;
        String act = getCurEnemy().chooseAction(getPlayer());
        if ("attack".equals(act)) {
            int dmg = Math.max(1, getCurEnemy().getDmg() - getPlayer().def());
            if (playerGuarding) {
                dmg = dmg / 2;
                if (rng.nextInt(100) < getPlayer().blockChance()) {
                    dmg = 0;
                    getCombatLog().add("You block completely!");
                } else getCombatLog().add("You reduce damage to " + dmg + ".");
            } else getCombatLog().add(getCurEnemy().getName() + " hits you for " + dmg + ".");
            getPlayer().damage(dmg);
        } else if ("defend".equals(act)) {
            getCurEnemy().setGuarding(true);
            getCombatLog().add(getCurEnemy().getName() + " guards.");
        } else if ("flee".equals(act)) {
            if (rng.nextInt(100) < 50) {
                getCombatLog().add(getCurEnemy().getName() + " flees!");
                getPlayer().gainExp(getCurEnemy().getMaxHp() / 2);
                getPlayer().heal(getPlayer().getMaxHp() / 4);
                setTransitionMsg(
                        getCurEnemy().getName() + " fled! +" + (getCurEnemy().getMaxHp() / 2) + " exp.");
                setMode("TRANSITION");
                return;
            } else getCombatLog().add(getCurEnemy().getName() + " tries to flee but fails!");
        }
        playerGuarding = false;
        checkCombatEnd();
    }

    boolean checkCombatEnd() {
        if (getPlayer().getHp() <= 0) {
            setMode("GAMEOVER");
            return true;
        }
        if (getCurEnemy().getHp() <= 0) {
            int exp = getCurEnemy().getMaxHp()
                    + (getPlayer().getMaxHp() - getPlayer().getHp());
            getPlayer().gainExp(exp);
            if (getCurLoc().getOnVictory() != null) parser.execute(getCurLoc().getOnVictory(), getPlayer(), this);
            getPlayer().heal(getPlayer().getMaxHp() / 4);
            setTransitionMsg("Victory! +" + exp + " exp.");
            setMode("TRANSITION");
            return true;
        }
        return false;
    }

    // ---- Exploration ----
    public void execAction(LocAct a) {
        String nav = parser.execute(a.getResult(), getPlayer(), this);
        if (getPlayer().getHp() <= 0) {
            setMode("GAMEOVER");
            return;
        }
        if (nav == null) nav = a.getNext();
        getPlayer().heal(getPlayer().getMaxHp() / 4);
        if ("nextFight".equals(nav)) genLocation("hostile");
        else if ("nextNPC".equals(nav)) genLocation("peaceful");
        else if ("nextSearch".equals(nav)) genLocation("exploratory");
        else genLocation(null);
        enterLoc();
    }

    public void continueAfterTransition() {
        genLocation(null);
        enterLoc();
    }

    public void genItem(String type) {
        if ("random".equals(type)) {
            String[] t = {"weapon", "armor", "consumable"};
            type = t[rng.nextInt(3)];
        }
        Item it;
        if ("weapon".equals(type)) {
            it = new Item(
                    pickName("weapon_names"),
                    "weapon",
                    rng.nextInt(3) + 1 + getPlayer().getLevel());
        } else if ("armor".equals(type)) {
            it = new Item(
                    pickName("armor_names"),
                    "armor",
                    rng.nextInt(2) + 1 + getPlayer().getLevel() / 2);
        } else {
            it = new Item(
                    pickName("consumable_names"),
                    "consumable",
                    rng.nextInt(10) + 5 + getPlayer().getLevel());
        }
        getPlayer().getInv().add(it);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public LocTemp getCurLoc() {
        return curLoc;
    }

    public void setCurLoc(LocTemp curLoc) {
        this.curLoc = curLoc;
    }

    public Enemy getCurEnemy() {
        return curEnemy;
    }

    public void setCurEnemy(Enemy curEnemy) {
        this.curEnemy = curEnemy;
    }

    public List<String> getCombatLog() {
        return combatLog;
    }

    public void setCombatLog(List<String> combatLog) {
        this.combatLog = combatLog;
    }

    public DialogData getCurDialog() {
        return curDialog;
    }

    public void setCurDialog(DialogData curDialog) {
        this.curDialog = curDialog;
    }

    public DialogNode getCurNode() {
        return curNode;
    }

    public void setCurNode(DialogNode curNode) {
        this.curNode = curNode;
    }

    public String getTransitionMsg() {
        return transitionMsg;
    }

    public void setTransitionMsg(String transitionMsg) {
        this.transitionMsg = transitionMsg;
    }
}
