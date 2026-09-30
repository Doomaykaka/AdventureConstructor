package adventureconstructor.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adventureconstructor.controllers.GameEngine;
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
}
