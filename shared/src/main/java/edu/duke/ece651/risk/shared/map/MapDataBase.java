package edu.duke.ece651.risk.shared.map;

import edu.duke.ece651.risk.shared.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

/**
 * @program: risk-map
 * @description: This is database which includes all maps we create
 * @author: Chengda Wu(cw402)
 * @create: 2020-03-09 10:20
 **/

public class MapDataBase<T extends Serializable> implements Serializable{
    private static final long serialVersionUID = 9L;

    Map<String, WorldMap<T>> mapHub;
    private String baseDirStr = "../config_file/MapDB_config/";


    public MapDataBase() throws IOException {
        mapHub = new HashMap<>();
        File baseDir = new File(baseDirStr);
        for (File subDir : baseDir.listFiles()) {
            String mapName = subDir.getName();

            String colorPath = Paths.get(baseDirStr,mapName, "color.txt").toString();
            String groupPath = Paths.get(baseDirStr,mapName, "group.txt").toString();
            String neighPath = Paths.get(baseDirStr,mapName, "neigh.txt").toString();
            String sizePath = Paths.get(baseDirStr,mapName, "size.txt").toString();
            String foodPath = Paths.get(baseDirStr,mapName, "food.txt").toString();
            String techPath = Paths.get(baseDirStr,mapName, "tech.txt").toString();


            Map<String,Set<String>> atlas = Utils.readNeighConfig(neighPath);
            List<String> colorList = Utils.readColorConfig(colorPath);
            Map<Set<String>,Boolean> groups = Utils.readGroupConfig(groupPath);
            Map<String, Integer> size = Utils.readSizeConfig(sizePath);
            Map<String, Integer> food = Utils.readBasicResourceConfig(foodPath);
            Map<String, Integer> tech = Utils.readSizeConfig(techPath);


            WorldMap<T> worldMap = new WorldMapImpl(atlas,colorList,groups,size,food,tech);
            worldMap.setName(mapName);
            mapHub.put(mapName,worldMap);
        }
    }
    public boolean containsMap(String inputName){
        if (null==inputName){
            return false;
        }else{
            String mapName = inputName.toLowerCase();
            return mapHub.containsKey(mapName);
        }
    }
    public WorldMap<T> getMap(String inputName){
        if (!containsMap(inputName)){
            throw new IllegalArgumentException("Input map name doesn't exist!");
        }
        String mapName = inputName.toLowerCase();
        return mapHub.get(mapName);
    }
    public Map<String, WorldMap<T>> getAllMaps(){
        return mapHub;
    }
}
