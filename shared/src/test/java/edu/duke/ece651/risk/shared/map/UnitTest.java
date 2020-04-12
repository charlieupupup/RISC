package edu.duke.ece651.risk.shared.map;

import edu.duke.ece651.risk.shared.Constant;
import edu.duke.ece651.risk.shared.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitTest {
    @Test
    void testConst(){
        assertDoesNotThrow(()->{new Unit(0,1);});
        assertThrows(IllegalArgumentException.class,()->{new Unit(0,1);});
        assertThrows(IllegalArgumentException.class,()->{new Unit(Utils.getMaxKey(Constant.UNIT_BONUS)+1,1);});
    }


    @Test
    void getLevel() {
        Unit unit = new Unit(0, 1);
        assertEquals(0,unit.getLevel());
    }

    @Test
    void getOwnerId() {
        Unit unit = new Unit(0, 1);
        assertEquals(1,unit.getOwnerId());
    }
}