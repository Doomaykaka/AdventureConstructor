package adventureconstructor.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class PlayerTest {
    @Test
    void healingAndDamageStayWithinHealthBounds() {
        Player player = new Player();

        player.damage(20);
        player.heal(100);
        assertEquals(player.getMaxHp(), player.getHp());

        player.damage(1000);
        assertEquals(0, player.getHp());
    }

    @Test
    void gainingEnoughExperienceLevelsUpAndRestoresHealth() {
        Player player = new Player();
        player.damage(30);

        player.gainExp(100);

        assertEquals(2, player.getLevel());
        assertEquals(0, player.getExp());
        assertEquals(3, player.getSp());
        assertEquals(player.getMaxHp(), player.getHp());
    }

    @Test
    void equippingReplacementReturnsOldItemToInventory() {
        Player player = new Player();
        Item first = new Item("First sword", "weapon", 4);
        Item second = new Item("Second sword", "weapon", 8);
        player.getInv().add(first);
        player.getInv().add(second);

        player.equip(first);
        player.equip(second);

        assertSame(second, player.getWeapon());
        assertEquals(1, player.getInv().size());
        assertSame(first, player.getInv().get(0));
    }
}
