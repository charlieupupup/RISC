package edu.duke.ece651.risk.shared.player;

import edu.duke.ece651.risk.shared.map.BasicResource;
import edu.duke.ece651.risk.shared.map.Territory;
import org.mongodb.morphia.annotations.Embedded;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import static edu.duke.ece651.risk.shared.Constant.*;

/**
 * @program: risk
 * @description: this is player class for evolution2&evolution3
 * @author: Chengda Wu
 * @create: 2020-03-28 20:16
 **/
@Embedded
public class PlayerV2<T> extends PlayerV1<T> implements Serializable { 
    private static final long serialVersionUID = 18L;
    @Embedded
    BasicResource tech;
    @Embedded
    BasicResource food;
    //this variable marks that this user have right to upgrade her maximum technology
    boolean upTechRight;
    int techLevel;

    private void initResource(){
        tech = new BasicResource(INITIAL_TECH_NUM);
        food = new BasicResource(INITIAL_FOOD_NUM);
        upTechRight = true;
        techLevel = 1;
    }

    public PlayerV2(InputStream in, OutputStream out) throws IOException {
        super(in, out);
        initResource();
    }

    //constructor for mongo
    public PlayerV2() {
        super();

    }
    //TODO add more testing for this method
    @Override
    public void updateState() {
        for (Territory territory : territories) {
            if (!territory.isRadiated()){
                territory.addBasicUnits(1);
                int foodYield = territory.getFoodYield();
                int techYield = territory.getTechYield();
                tech.addResource(techYield);
                food.addResource(foodYield);
            }
        }
        if (!upTechRight){//note that we only update the max tech level after a round of game
            this.upTechRight = true;
            techLevel++;
        }
        this.allyRequest = -1;
        this.actions = new ArrayList<>();
        isSpying = false;

    }

    @Override
    public int getFoodNum() {
        return food.getRemain();
    }

    @Override
    public int getTechNum() {
        return tech.getRemain();
    }

    @Override
    public void useFood(int foodUse) {
        if (getFoodNum()<foodUse){
            throw new IllegalArgumentException();
        }
        food.useResource(foodUse);

    }

    @Override
    public void useTech(int techUse) {
        if (getTechNum()<techUse){
            throw new IllegalArgumentException();
        }
        tech.useResource(techUse);

    }

    @Override
    public boolean canUpMaxTech() {
        if (upTechRight&&TECH_MAP.containsKey(techLevel)&&TECH_MAP.get(techLevel)<=this.getTechNum()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * this method should always be called after canUpTech
     */
    @Override
    public void upMaxTech() {
        if (!canUpMaxTech()){
            throw new IllegalArgumentException("Can't up tech now!");
        }
        this.useTech(TECH_MAP.get(techLevel));
        upTechRight = false;
    }

    @Override
    public int getTechLevel() {
        return this.techLevel;
    }

    @Override
    public void setId(int id) {
        if (this.id>0){
            throw new IllegalStateException("can't assign an id twice!");
        }
        super.setId(id);
    }
}
