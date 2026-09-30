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
import adventureconstructor.utils.AmbientPlayer;
import adventureconstructor.utils.JsonCommentStripper;
import adventureconstructor.utils.SupportFunctions;
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
    private Map<String, List<String>> imagePool = new HashMap<>();
    private Map<String, List<String>> ambientPool = new HashMap<>();
    private AmbientPlayer ambientPlayer = new AmbientPlayer();
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
    private String transitionNavigation;
    private boolean transitionNavigationFailed;

    private String lastAmbientKey = null;

    private GameOperationsController gameOperationsController;

    private static final String DATA_PARENT_FOLDER_NAME = "user.dir";

    public void setGameOperationsController(GameOperationsController controller) {
        this.gameOperationsController = controller;
    }

    private void syncPlayerToDb() {
        if (player == null || player.getId() == null || gameOperationsController == null) return;
        try {
            gameOperationsController.updatePlayer(player.toDb());
        } catch (Exception e) {
            System.err.println("Sync player to DB: " + e);
        }
    }

    private void incScore() {
        if (player != null) {
            player.setScore(player.getScore() + 1);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        JSONParser parser = new JSONParser();

        loadLocations(parser);
        loadDialogs(parser);
        loadNames(parser);
        loadImages(parser);
        loadAmbient(parser);
    }

    private void loadLocations(JSONParser parser) {
        try {
            Path locPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/locations.json")
                    .toFile()
                    .getAbsolutePath());
            String locText = new String(Files.readAllBytes(locPath));
            JSONObject root = (JSONObject) parser.parse(JsonCommentStripper.strip(locText));
            JSONArray locs = (JSONArray) root.get("locations");
            if (locs != null) {
                for (Object lo : locs) {
                    JSONObject lm = (JSONObject) lo;
                    LocTemp l = new LocTemp();
                    l.setId(jStr(lm, "id", ""));
                    l.setType(jStr(lm, "type", ""));
                    l.setName(jStr(lm, "name", ""));
                    l.setDesc(jStr(lm, "description", ""));
                    l.setImage(jStr(lm, "image", ""));
                    l.setAmbient(jStr(lm, "ambient", ""));
                    l.setAutoTransition(jBool(lm, "auto_transition", true));
                    l.setOnStart(jStr(lm, "on_start", ""));
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
                        l.setOnEnemyFlee(jStr(lm, "on_enemy_flee", ""));
                        l.setOnPlayerFlee(jStr(lm, "on_player_flee", jStr(lm, "on_flee", "")));
                        l.setScaleEnemy(jBool(lm, "scale_enemy", true));
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
    }

    private void loadDialogs(JSONParser parser) {
        try {
            Path dlgPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/dialogs.json")
                    .toFile()
                    .getAbsolutePath());
            String dlgText = new String(Files.readAllBytes(dlgPath));
            JSONObject root = (JSONObject) parser.parse(JsonCommentStripper.strip(dlgText));
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
    }

    private void loadNames(JSONParser parser) {
        try {
            Path nmPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/names.json")
                    .toFile()
                    .getAbsolutePath());
            String nmText = new String(Files.readAllBytes(nmPath));
            JSONObject root = (JSONObject) parser.parse(JsonCommentStripper.strip(nmText));
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

    private void loadImages(JSONParser parser) {
        try {
            Path imgPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/images.json")
                    .toFile()
                    .getAbsolutePath());
            if (Files.exists(imgPath)) {
                String imgText = new String(Files.readAllBytes(imgPath));
                JSONObject root = (JSONObject) parser.parse(JsonCommentStripper.strip(imgText));
                for (Object eo : root.entrySet()) {
                    Map.Entry<String, Object> en = (Map.Entry<String, Object>) eo;
                    if (en.getValue() instanceof JSONArray) {
                        List<String> sl = new ArrayList<>();
                        for (Object o : (JSONArray) en.getValue()) {
                            if (o != null) sl.add(o.toString());
                        }
                        imagePool.put(en.getKey(), sl);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Load images: " + e);
        }
    }

    private void loadAmbient(JSONParser parser) {
        try {
            Path ambPath = Path.of(Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data/ambient.json")
                    .toFile()
                    .getAbsolutePath());
            if (Files.exists(ambPath)) {
                String ambText = new String(Files.readAllBytes(ambPath));
                JSONObject root = (JSONObject) parser.parse(JsonCommentStripper.strip(ambText));
                for (Object eo : root.entrySet()) {
                    Map.Entry<String, Object> en = (Map.Entry<String, Object>) eo;
                    if (en.getValue() instanceof JSONArray) {
                        List<String> sl = new ArrayList<>();
                        for (Object o : (JSONArray) en.getValue()) {
                            if (o != null) sl.add(o.toString());
                        }
                        ambientPool.put(en.getKey(), sl);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Load ambient: " + e);
        }
    }

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

    public void genLocation(String forcedType) {
        List<LocTemp> pool = new ArrayList<>();
        for (LocTemp l : locPool) if (forcedType == null || l.getType().equals(forcedType)) pool.add(l);
        if (pool.isEmpty()) pool = locPool;

        if (locPool.isEmpty()) {
            SupportFunctions.showMessage("For the game to run correctly, locations of all types are required");
            return;
        }

        LocTemp cloned = LocTempCloner.clone(pool.get(rng.nextInt(pool.size())));

        prepareLocationAssets(cloned);
        setCurLoc(cloned);
    }

    private boolean genLocationById(String id) {
        for (LocTemp location : locPool) {
            if (id.equals(location.getId())) {
                LocTemp cloned = LocTempCloner.clone(location);
                prepareLocationAssets(cloned);
                setCurLoc(cloned);
                return true;
            }
        }
        return false;
    }

    private void prepareLocationAssets(LocTemp cloned) {
        if (cloned.getImage() == null || cloned.getImage().isEmpty()) {
            List<String> imgs = imagePool.get(cloned.getType());
            if (imgs != null && !imgs.isEmpty()) {
                cloned.setImage(imgs.get(rng.nextInt(imgs.size())));
            }
        }

        if (cloned.getAmbient() == null || cloned.getAmbient().isEmpty()) {
            List<String> ambs = ambientPool.get(cloned.getType());
            if (ambs != null && !ambs.isEmpty()) {
                cloned.setAmbient(ambs.get(rng.nextInt(ambs.size())));
            }
        }
    }

    public String getCurImagePath() {
        if (getCurLoc() == null) return null;
        String img = getCurLoc().getImage();
        if (img == null || img.isEmpty()) return null;
        return Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data", img)
                .toFile()
                .getAbsolutePath();
    }

    public void playAmbient(String type) {
        String path = null;

        if (getCurLoc() != null
                && getCurLoc().getAmbient() != null
                && !getCurLoc().getAmbient().isEmpty()) {
            path = Path.of(
                            System.getProperty(DATA_PARENT_FOLDER_NAME),
                            "data",
                            getCurLoc().getAmbient())
                    .toFile()
                    .getAbsolutePath();
        }

        if (path == null) {
            List<String> pool = ambientPool.get(type);
            if (pool != null && !pool.isEmpty()) {
                String rel = pool.get(rng.nextInt(pool.size()));
                path = Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data", rel)
                        .toFile()
                        .getAbsolutePath();
            }
        }

        ambientPlayer.play(path, true);
    }

    public void playAmbientForMode() {
        String key = mode + "_" + (curLoc != null ? System.identityHashCode(curLoc) : "null");

        if (key.equals(lastAmbientKey)) return;
        lastAmbientKey = key;

        switch (mode) {
            case "COMBAT":
                playAmbient("combat");
                break;
            case "GAMEOVER":
                ambientPlayer.play(getAmbientPath("gameover"), false);
                break;
            case "START":
            case "TRANSITION":
                ambientPlayer.stop();
                break;
            default:
                playAmbient(getCurLoc() != null ? getCurLoc().getType() : null);
                break;
        }
    }

    public void stopAmbient() {
        ambientPlayer.stop();
    }

    private String getAmbientPath(String type) {
        List<String> pool = ambientPool.get(type);
        if (pool == null || pool.isEmpty()) return null;
        String rel = pool.get(rng.nextInt(pool.size()));
        return Path.of(System.getProperty(DATA_PARENT_FOLDER_NAME), "data", rel)
                .toFile()
                .getAbsolutePath();
    }

    public void enterLoc() {
        if (getCurLoc() == null) {
            return;
        }

        getCombatLog().clear();
        transitionNavigationFailed = false;
        transitionNavigation = null;

        if ("peaceful".equals(getCurLoc().getType())) {
            DialogData d = dialogs.get(getCurLoc().getDialogId());
            if (d != null) {
                setCurDialog(d);
                setCurNode(d.findNode("start"));
                setMode("DIALOG");
            } else {
                setMode("EXPLORE");
            }
        } else if ("hostile".equals(getCurLoc().getType())) {
            setCurEnemy(new Enemy());
            getCurEnemy().setName(getCurLoc().getEnemyName());
            getCurEnemy().setHp(getCurLoc().getEnemyHp());
            getCurEnemy().setMaxHp(getCurLoc().getEnemyHp());
            getCurEnemy().setDmg(getCurLoc().getEnemyDmg());
            getCurEnemy().setDef(getCurLoc().getEnemyDef());
            if (getCurLoc().isScaleEnemy()) {
                getCurEnemy().scale(getPlayer().getLevel());
            }
            playerGuarding = false;
            getCombatLog()
                    .add(getCurEnemy().getName() + " (HP: " + getCurEnemy().getHp() + "/"
                            + getCurEnemy().getMaxHp() + ") appears!");
            setMode("COMBAT");
        } else {
            setMode("EXPLORE");
        }
        String onStart = getCurLoc().getOnStart();
        if (onStart != null && !onStart.trim().isEmpty()) {
            transitionNavigation = parser.execute(onStart, getPlayer(), this);
            if ("death".equals(transitionNavigation)) {
                getPlayer().setAlive(false);
                setMode("GAMEOVER");
            } else if (transitionNavigation != null
                    && !transitionNavigation.trim().isEmpty()) {
                setTransitionMsg("Location started.");
                setMode("TRANSITION");
            }
        }
        syncPlayerToDb();
    }

    public String formatText(String text) {
        if (text == null) return "";
        String r = text;
        r = r.replace("{gold}", String.valueOf(getPlayer().getGold()));
        r = r.replace("{hp}", String.valueOf(getPlayer().getHp()));
        r = r.replace("{maxHp}", String.valueOf(getPlayer().getMaxHp()));
        r = r.replace("{level}", String.valueOf(getPlayer().getLevel()));
        r = r.replace("{inventorySize}", String.valueOf(getPlayer().getInv().size()));
        r = r.replace("{item}", pickName("weapon_names"));
        r = r.replace("{char.str}", String.valueOf(getPlayer().getStr()));
        r = r.replace("{char.agi}", String.valueOf(getPlayer().getAgi()));
        r = r.replace("{char.intl}", String.valueOf(getPlayer().getIntl()));
        r = r.replace("{char.end}", String.valueOf(getPlayer().getEnd()));
        r = parser.formatVariables(r);
        return r;
    }

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
            boolean ok = parser.evalCondition(o.getCheck());
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
            getCurEnemy().setHp(getCurLoc().getEnemyHp());
            getCurEnemy().setMaxHp(getCurLoc().getEnemyHp());
            getCurEnemy().setDmg(getCurLoc().getEnemyDmg());
            getCurEnemy().setDef(getCurLoc().getEnemyDef());
            if (getCurLoc().isScaleEnemy()) {
                getCurEnemy().scale(getPlayer().getLevel());
            }
            playerGuarding = false;
            getCombatLog().clear();
            setMode("COMBAT");
            return;
        }
        if (getCurNode().isEndDialog()) endDialog(getCurNode().isSuccess());
    }

    void endDialog(boolean success) {
        transitionNavigationFailed = false;
        if (success && getCurLoc().getOnSuccess() != null)
            transitionNavigation = parser.execute(getCurLoc().getOnSuccess(), getPlayer(), this);
        else if (!success && getCurLoc().getOnFail() != null)
            transitionNavigation = parser.execute(getCurLoc().getOnFail(), getPlayer(), this);
        getPlayer().heal(getPlayer().getMaxHp() / 4);
        if ((transitionNavigation == null || transitionNavigation.trim().isEmpty())
                && !getCurLoc().isAutoTransition()) {
            incScore();
            syncPlayerToDb();
            setMode("DIALOG");
            return;
        }
        setTransitionMsg(success ? "Dialog succeeded." : "Dialog failed.");
        setMode("TRANSITION");
        incScore();
        syncPlayerToDb();
    }

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
            executeFleeEvent(getCurLoc().getOnPlayerFlee());
            getPlayer().heal(getPlayer().getMaxHp() / 4);
            setTransitionMsg("Escaped successfully.");
            setMode("TRANSITION");
            incScore();
            syncPlayerToDb();
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
                handleEnemyFlee();
                return;
            } else getCombatLog().add(getCurEnemy().getName() + " tries to flee but fails!");
        }
        playerGuarding = false;
        checkCombatEnd();
    }

    private void executeFleeEvent(String event) {
        transitionNavigation = null;
        if (event != null && !event.trim().isEmpty()) {
            transitionNavigation = parser.execute(event, getPlayer(), this);
        }
    }

    void handleEnemyFlee() {
        getCombatLog().add(getCurEnemy().getName() + " flees!");
        getPlayer().gainExp(getCurEnemy().getMaxHp() / 2);
        executeFleeEvent(getCurLoc().getOnEnemyFlee());
        getPlayer().heal(getPlayer().getMaxHp() / 4);
        setTransitionMsg(getCurEnemy().getName() + " fled! +" + (getCurEnemy().getMaxHp() / 2) + " exp.");
        setMode("TRANSITION");
        incScore();
        syncPlayerToDb();
    }

    boolean checkCombatEnd() {
        if (getPlayer().getHp() <= 0) {
            transitionNavigation = null;
            String onDefeat = getCurLoc().getOnDefeat();
            if (onDefeat != null && !onDefeat.trim().isEmpty()) {
                transitionNavigation = parser.execute(onDefeat, getPlayer(), this);
            }
            if (transitionNavigation != null
                    && !transitionNavigation.trim().isEmpty()
                    && !"death".equals(transitionNavigation)) {
                setTransitionMsg("Defeat.");
                setMode("TRANSITION");
                syncPlayerToDb();
                return true;
            }
            getPlayer().setAlive(false);
            setMode("GAMEOVER");
            syncPlayerToDb();
            return true;
        }
        if (getCurEnemy().getHp() <= 0) {
            int exp = getCurEnemy().getMaxHp()
                    + (getPlayer().getMaxHp() - getPlayer().getHp());
            getPlayer().gainExp(exp);
            if (getCurLoc().getOnVictory() != null)
                transitionNavigation = parser.execute(getCurLoc().getOnVictory(), getPlayer(), this);
            getPlayer().heal(getPlayer().getMaxHp() / 4);
            setTransitionMsg("Victory! +" + exp + " exp.");
            setMode("TRANSITION");
            incScore();
            syncPlayerToDb();
            return true;
        }
        return false;
    }

    public void execAction(LocAct a) {
        String nav = parser.execute(a.getResult(), getPlayer(), this);
        if (getPlayer().getHp() <= 0) {
            getPlayer().setAlive(false);
            setMode("GAMEOVER");
            syncPlayerToDb();
            return;
        }
        if ("death".equals(nav)) {
            getPlayer().setAlive(false);
            setMode("GAMEOVER");
            syncPlayerToDb();
            return;
        }
        if (nav == null) nav = a.getNext();
        getPlayer().heal(getPlayer().getMaxHp() / 4);
        incScore();
        if (nav != null && nav.startsWith("nextLocation:")) {
            String id = nav.substring("nextLocation:".length());
            if (genLocationById(id)) enterLoc();
            else {
                getCombatLog().add("Location id not found: " + id);
                syncPlayerToDb();
            }
            return;
        }
        if ((nav == null || nav.trim().isEmpty()) && !getCurLoc().isAutoTransition()) {
            syncPlayerToDb();
            return;
        }
        if ("nextFight".equals(nav)) genLocation("hostile");
        else if ("nextNPC".equals(nav)) genLocation("peaceful");
        else if ("nextSearch".equals(nav)) genLocation("exploratory");
        else if ("nextRandom".equals(nav)) genLocation(null);
        else genLocation(null);
        enterLoc();
    }

    public void continueAfterTransition() {
        if (transitionNavigationFailed) return;
        String nav = transitionNavigation;
        if ((nav == null || nav.trim().isEmpty()) && !getCurLoc().isAutoTransition()) return;
        transitionNavigation = null;
        if ("death".equals(nav)) {
            getPlayer().setAlive(false);
            setMode("GAMEOVER");
            syncPlayerToDb();
            return;
        }
        if (nav != null && nav.startsWith("nextLocation:")) {
            String id = nav.substring("nextLocation:".length());
            if (genLocationById(id)) enterLoc();
            else {
                getCombatLog().add("Location id not found: " + id);
                transitionNavigationFailed = true;
                syncPlayerToDb();
            }
            return;
        }
        genLocation("nextRandom".equals(nav) ? null : locationTypeForNavigation(nav));
        enterLoc();
    }

    public boolean canContinueAfterTransition() {
        return !transitionNavigationFailed
                && (transitionNavigation != null && !transitionNavigation.trim().isEmpty()
                        || getCurLoc() == null
                        || getCurLoc().isAutoTransition());
    }

    private String locationTypeForNavigation(String nav) {
        if ("nextFight".equals(nav)) return "hostile";
        if ("nextNPC".equals(nav)) return "peaceful";
        if ("nextSearch".equals(nav)) return "exploratory";
        return null;
    }

    public void genItem(String type) {
        genItem(type, null);
    }

    public void genItem(String type, String specifiedName) {
        if ("random".equals(type)) {
            String[] t = {"weapon", "armor", "consumable"};
            type = t[rng.nextInt(3)];
        }
        Item it;
        String itemName = specifiedName;
        if ("weapon".equals(type)) {
            if (itemName == null) itemName = pickName("weapon_names");
            it = new Item(itemName, "weapon", rng.nextInt(3) + 1 + getPlayer().getLevel());
        } else if ("armor".equals(type)) {
            if (itemName == null) itemName = pickName("armor_names");
            it = new Item(itemName, "armor", rng.nextInt(2) + 1 + getPlayer().getLevel() / 2);
        } else {
            if (itemName == null) itemName = pickName("consumable_names");
            it = new Item(
                    itemName, "consumable", rng.nextInt(10) + 5 + getPlayer().getLevel());
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
        parser.clearVariables();
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
