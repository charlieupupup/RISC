package edu.duke.ece651.risk.shared.map;

import org.mongodb.morphia.annotations.Embedded;

import java.io.Serializable;

/**
 * @program: risk
 * @description: this is class to represent resource for evolution2
 * @author: Chengda Wu
 * @create: 2020-03-28 19:01
 **/
@Embedded
public class BasicResource implements Serializable {
    private static final long serialVersionUID = 8L;

    int totalNum;

    public BasicResource(int totalNum) {
        this.totalNum = totalNum;
    }

    public BasicResource() {
        totalNum = 0;
    }

    public void addResource(int addNum){
        this.totalNum += addNum;
    }

    /**
     * @return the current remain value of resources
     */
    public int getRemain(){
        return this.totalNum;
    }

    /**
     * this method should be called after getRemain()
     * @param useNum
     */
    public void useResource(int useNum){
        if (getRemain()<useNum){
            throw new IllegalArgumentException("There isn't enough resources for this operation!");
        }
        totalNum -= useNum;
    }
}
