package adventureconstructor.controllers;

import adventureconstructor.dao.ItemsDAO;
import adventureconstructor.dao.PlayersDAO;
import adventureconstructor.models.db.Item;
import adventureconstructor.models.db.Player;
import adventureconstructor.utils.Constants;
import adventureconstructor.utils.Logger;
import adventureconstructor.utils.SupportFunctions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameOperationsController {
    private PlayersDAO playersDAO;
    private ItemsDAO itemsDAO;

    public GameOperationsController(PlayersDAO playersDAO, ItemsDAO itemsDAO) {
        this.playersDAO = playersDAO;
        this.itemsDAO = itemsDAO;
    }

    public List<Player> getAllPlayers() {
        Logger.getInstance().info("Get all players");
        return this.playersDAO.getAll();
    }

    public Player getPlayer(Long id) {
        Logger.getInstance().info("Get player by id: " + id);
        return this.playersDAO.get(id);
    }

    public Player createNewPlayer(String name) {
        Player newPlayer = new Player();
        newPlayer.setName(name != null ? name : "Hero");
        Logger.getInstance().info("Create player: " + newPlayer.getName());
        this.playersDAO.create(newPlayer);
        return newPlayer;
    }

    public void savePlayer(Player player) {
        Logger.getInstance().info("Save player: " + player.getName());

        this.playersDAO.create(player);

        for (Item item : player.getInventory()) {
            item.setPlayerId(player.getId());
            this.itemsDAO.update(item);
        }
    }

    public void updatePlayer(Player player) {
        Logger.getInstance().info("Update player: " + player.getName());

        this.playersDAO.update(player);
    }

    public boolean removePlayer(Long id) {
        Player playerToRemove = this.playersDAO.get(id);

        if (playerToRemove == null) {
            Logger.getInstance().info("Player not found: " + id);
            return false;
        }
        Logger.getInstance().info("Remove player: " + playerToRemove.getName());
        return this.playersDAO.remove(playerToRemove);
    }

    public void exportPlayer(Player player, File fileToSave) {
        Logger.getInstance().info("Export player: " + player.getName());

        JSONObject userObj = new JSONObject();

        userObj.put("name", player.getName());
        userObj.put("creationDate", player.getCreationDate().toInstant().getEpochSecond());
        userObj.put("hp", player.getHp());
        userObj.put("maxHp", player.getMaxHp());
        userObj.put("gold", player.getGold());
        userObj.put("level", player.getLevel());
        userObj.put("exp", player.getExp());
        userObj.put("expNext", player.getExpNext());
        userObj.put("sp", player.getSp());
        userObj.put("str", player.getStr());
        userObj.put("agi", player.getAgi());
        userObj.put("intl", player.getIntl());
        userObj.put("end", player.getEnd());
        userObj.put("weaponId", player.getWeaponId());
        userObj.put("armorId", player.getArmorId());
        userObj.put("currentLocationId", player.getCurrentLocationId());
        userObj.put("score", player.getScore());
        userObj.put("isAlive", player.isAlive());

        JSONArray itemsArray = new JSONArray();
        for (Item item : player.getInventory()) {
            JSONObject itemObj = new JSONObject();
            itemObj.put("name", item.getName());
            itemObj.put("type", item.getType());
            itemObj.put("value", item.getValue());
            itemObj.put("equipped", item.isEquipped());
            itemObj.put("slot", item.getSlot());
            itemsArray.add(itemObj);
        }
        userObj.put("inventory", itemsArray);

        StringWriter stringWriter = new StringWriter();
        try {
            userObj.writeJSONString(stringWriter);
        } catch (IOException e) {
            Logger.getInstance().warning("Export write error: " + e.getMessage());
        }

        SupportFunctions.writeContentInNewFile(
                fileToSave.getParentFile(), fileToSave.getName(), List.of(stringWriter.toString()));
    }

    public Player importPlayer(File fileToLoad) {
        Logger.getInstance().info("Import player from: " + fileToLoad.getName());

        List<String> fileContent = List.of();
        try {
            fileContent = SupportFunctions.readFileContent(new FileReader(fileToLoad));
        } catch (FileNotFoundException e) {
            Logger.getInstance().warning("Import file not found: " + e.getMessage());
        }

        if (fileContent.isEmpty()) {
            return null;
        }

        String jsonStr = fileContent.get(0);
        JSONParser parser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) parser.parse(jsonStr);

            Player player = new Player();
            player.setName((String) obj.get("name"));
            player.setCreationDate(new Date(((Long) obj.get("creationDate")) * Constants.SECONDS_TO_MILLIS_MULTIPLIER));
            player.setHp(obj.get("hp") != null ? ((Long) obj.get("hp")).intValue() : 100);
            player.setMaxHp(obj.get("maxHp") != null ? ((Long) obj.get("maxHp")).intValue() : 100);
            player.setGold(obj.get("gold") != null ? ((Long) obj.get("gold")).intValue() : 0);
            player.setLevel(obj.get("level") != null ? ((Long) obj.get("level")).intValue() : 1);
            player.setExp(obj.get("exp") != null ? ((Long) obj.get("exp")).intValue() : 0);
            player.setExpNext(obj.get("expNext") != null ? ((Long) obj.get("expNext")).intValue() : 100);
            player.setSp(obj.get("sp") != null ? ((Long) obj.get("sp")).intValue() : 0);
            player.setStr(obj.get("str") != null ? ((Long) obj.get("str")).intValue() : 5);
            player.setAgi(obj.get("agi") != null ? ((Long) obj.get("agi")).intValue() : 5);
            player.setIntl(obj.get("intl") != null ? ((Long) obj.get("intl")).intValue() : 5);
            player.setEnd(obj.get("end") != null ? ((Long) obj.get("end")).intValue() : 5);
            player.setWeaponId(obj.get("weaponId") != null ? (Long) obj.get("weaponId") : null);
            player.setArmorId(obj.get("armorId") != null ? (Long) obj.get("armorId") : null);
            player.setCurrentLocationId((String) obj.get("currentLocationId"));
            player.setScore(obj.get("score") != null ? ((Long) obj.get("score")).intValue() : 0);
            player.setAlive(obj.get("isAlive") != null ? (Boolean) obj.get("isAlive") : true);

            JSONArray itemsArray = (JSONArray) obj.get("inventory");
            if (itemsArray != null) {
                List<Item> inventory = new ArrayList<>();
                for (Object io : itemsArray) {
                    JSONObject itemObj = (JSONObject) io;
                    Item item = new Item();
                    item.setName((String) itemObj.get("name"));
                    item.setType((String) itemObj.get("type"));
                    item.setValue(itemObj.get("value") != null ? ((Long) itemObj.get("value")).intValue() : 0);
                    item.setEquipped(itemObj.get("equipped") != null ? (Boolean) itemObj.get("equipped") : false);
                    item.setSlot((String) itemObj.get("slot"));
                    item.setPlayer(player);
                    inventory.add(item);
                }
                player.setInventory(inventory);
            }

            return player;

        } catch (ParseException e) {
            Logger.getInstance().warning("Import parse error: " + e.getMessage());
            return null;
        }
    }
}
