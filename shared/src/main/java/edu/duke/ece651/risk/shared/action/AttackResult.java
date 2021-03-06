package edu.duke.ece651.risk.shared.action;

import edu.duke.ece651.risk.shared.map.Territory;

import java.util.List;
import java.util.Objects;

/**
 * record the result of attack
 */
public class AttackResult {
    private static final long serialVersionUID = 3L;

    int attackerID;
    int defenderID;
    List<String> srcTerritories;
    String destTerritory;
    boolean isAttackerWin;

    @Override
    public boolean equals(Object o) {
        AttackResult that = (AttackResult) o;
        return getAttackerID() == that.getAttackerID() &&
                getDefenderID() == that.getDefenderID() &&
                isAttackerWin() == that.isAttackerWin() &&
                getSrcTerritories().equals(that.getSrcTerritories()) &&
                getDestTerritory().equals(that.getDestTerritory());
    }


    public AttackResult(int attackerID, int defenderID, List<String> srcTerritories, String destTerritory, boolean isAttackerWin) {
        this.attackerID = attackerID;
        this.defenderID = defenderID;
        this.srcTerritories = srcTerritories;
        this.destTerritory = destTerritory;
        this.isAttackerWin = isAttackerWin;

    }

    public int getAttackerID() {
        return attackerID;
    }

    public int getDefenderID() {
        return defenderID;
    }

    public boolean isAttackerWin() {
        return isAttackerWin;
    }

    public List<String> getSrcTerritories() {
        return srcTerritories;
    }

    public String getDestTerritory() {
        return destTerritory;
    }

}
