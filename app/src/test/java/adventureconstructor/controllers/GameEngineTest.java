package adventureconstructor.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adventureconstructor.models.Enemy;
import adventureconstructor.models.LocAct;
import adventureconstructor.models.LocTemp;
import adventureconstructor.models.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameEngineTest {
    private GameEngine engine;
    private Player player;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        player = new Player();
        engine.setPlayer(player);
    }

    @Test
    void onDefeatRunsAndCanNavigateToAnotherLocation() {
        LocTemp location = new LocTemp();
        location.setOnDefeat("log(\"Defeat; handler ran?\"); heal50; nextNPC");
        engine.setCurLoc(location);
        player.setHp(0);

        assertTrue(engine.checkCombatEnd());

        assertEquals(50, player.getHp());
        assertEquals("TRANSITION", engine.getMode());
        assertTrue(engine.canContinueAfterTransition());
        assertEquals(List.of("Defeat; handler ran?"), engine.getCombatLog());
    }

    @Test
    void onDefeatWithoutNavigationRunsThenKeepsDefaultGameOver() {
        LocTemp location = new LocTemp();
        location.setOnDefeat("gold+=5");
        engine.setCurLoc(location);
        player.setHp(0);

        assertTrue(engine.checkCombatEnd());

        assertEquals(5, player.getGold());
        assertEquals("GAMEOVER", engine.getMode());
        assertFalse(player.isAlive());
    }

    @Test
    void deathActionSetsGameOverInsteadOfTransitioning() {
        LocTemp location = new LocTemp();
        location.setType("exploratory");
        location.setAutoTransition(true);
        engine.setCurLoc(location);
        LocAct action = new LocAct();
        action.setResult("death");

        engine.execAction(action);

        assertEquals("GAMEOVER", engine.getMode());
        assertFalse(player.isAlive());
    }

    @Test
    void successfulFleeRunsConfiguredEventAndUsesItsNavigation() {
        LocTemp location = new LocTemp();
        location.setAutoTransition(false);
        location.setOnPlayerFlee("log(\"Player flee handler ran\"); nextSearch");
        engine.setCurLoc(location);
        player.setAgi(50);

        engine.pFlee();

        assertEquals("TRANSITION", engine.getMode());
        assertTrue(engine.canContinueAfterTransition());
        assertTrue(engine.getCombatLog().contains("Player flee handler ran"));
    }

    @Test
    void enemyFleeRunsConfiguredEventAndUsesItsNavigation() {
        LocTemp location = new LocTemp();
        location.setAutoTransition(false);
        location.setOnEnemyFlee("log(\"Enemy flee handler ran\"); nextNPC");
        engine.setCurLoc(location);
        Enemy enemy = new Enemy();
        enemy.setName("Test enemy");
        enemy.setMaxHp(20);
        enemy.setHp(1);
        engine.setCurEnemy(enemy);

        engine.handleEnemyFlee();

        assertEquals("TRANSITION", engine.getMode());
        assertTrue(engine.canContinueAfterTransition());
        assertTrue(engine.getCombatLog().contains("Enemy flee handler ran"));
        assertEquals(10, player.getExp());
    }
}
