package adventureconstructor.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adventureconstructor.controllers.GameEngine;
import adventureconstructor.models.Item;
import adventureconstructor.models.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyntaxParserTest {
    private Player player;
    private GameEngine engine;
    private SyntaxParser parser;

    @BeforeEach
    void setUp() {
        player = new Player();
        engine = new GameEngine();
        engine.setPlayer(player);
        parser = new SyntaxParser();
        parser.setPlayer(player);
        parser.setEngine(engine);
    }

    @Test
    void evaluatesComparisonsAndArithmetic() {
        assertTrue(parser.evalCondition("hp==50 and 2+3*4==14"));
        assertTrue(parser.evalCondition("level>=1 or gold>0"));
        assertFalse(parser.evalCondition("hp<50 or gold!=0"));
    }

    @Test
    void respectsBooleanPrecedenceParenthesesAndNegation() {
        assertTrue(parser.evalCondition("1==2 or 2==2 and not 3==4"));
        assertFalse(parser.evalCondition("(1==2 or 2==3) and not 3==4"));
        assertFalse(parser.evalCondition("not (1==1 or 2==2)"));
    }

    @Test
    void updatesAndComparesUserVariables() {
        parser.execute("setVar(\"count\", 8); setVar(\"count\", getVar(\"count\")+4)", player, engine);
        parser.execute("setVar(\"name\", \"Ada\"); appendVar(\"name\", \" Lovelace\")", player, engine);

        assertTrue(parser.evalCondition("getVar(\"count\")==12"));
        assertTrue(parser.evalCondition("varEquals(\"name\", \"Ada Lovelace\")==1"));
        assertTrue(parser.evalCondition("varContains(\"name\", \"Lovelace\")==1"));
    }

    @Test
    void preservesDelimitersInsideStringsAndNestedTernary() {
        parser.execute(
                "1==1 ? 1==1 ? log(\"yes: question? equals =; semicolon\") : log(\"inner failure\") : log(\"outer failure\")",
                player,
                engine);

        assertEquals(List.of("yes: question? equals =; semicolon"), engine.getCombatLog());
    }

    @Test
    void returnsNavigationForExplicitLocationId() {
        assertEquals("nextLocation:test_search", parser.execute("nextLocation(\"test_search\")", player, engine));
    }

    @Test
    void createsNamedWeaponArmorAndConsumable() {
        parser.execute(
                "itemWeapon(\"Тестовый меч\"); itemArmor(\"Тестовая броня\"); " + "itemConsumable(\"Тестовое зелье\")",
                player,
                engine);

        assertEquals(3, player.getInv().size());
        assertEquals("Тестовый меч", player.getInv().get(0).getName());
        assertEquals("weapon", player.getInv().get(0).getType());
        assertEquals("Тестовая броня", player.getInv().get(1).getName());
        assertEquals("armor", player.getInv().get(1).getType());
        assertEquals("Тестовое зелье", player.getInv().get(2).getName());
        assertEquals("consumable", player.getInv().get(2).getType());
    }

    @Test
    void namedItemFunctionsAcceptStringExpressions() {
        parser.execute(
                "setVar(\"weaponName\", \"Имя из переменной\"); itemWeapon(getVar(\"weaponName\"))", player, engine);

        Item weapon = player.getInv().get(0);
        assertEquals("Имя из переменной", weapon.getName());
        assertEquals("weapon", weapon.getType());
    }
}
