package com.unciv.civinfo;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Predicate;
import com.unciv.game.HexMath;
import com.unciv.models.LinqCollection;
import com.unciv.models.LinqHashMap;
import com.unciv.models.gamebasics.GameBasics;
import com.unciv.models.gamebasics.ResourceType;
import com.unciv.models.gamebasics.Terrain;
import com.unciv.models.gamebasics.TileResource;

public class TileMap{

    private  LinqHashMap<String, TileInfo> tiles = new LinqHashMap<String, TileInfo>();

    public TileMap(){} // for json parsing, we need to have a default constructor

    public TileMap(int distance) {
        for(Vector2 vector : HexMath.GetVectorsInDistance(Vector2.Zero,distance)) addRandomTile(vector);
    }


    private void addRandomTile(Vector2 position) {
        final TileInfo tileInfo = new TileInfo();
        tileInfo.position = position;
        LinqCollection<Terrain> Terrains = GameBasics.Terrains.linqValues();

        final Terrain baseTerrain = Terrains.where(new Predicate<Terrain>() {
            @Override
            public boolean evaluate(Terrain arg0) {
                return arg0.Type.equals("baseTerrain") && !arg0.Name.equals("Lakes");
            }
        }).getRandom();
        tileInfo.baseTerrain = baseTerrain.Name;

        if (baseTerrain.CanHaveOverlay) {
            if (Math.random() > 0.7f) {
                Terrain SecondaryTerrain = Terrains.where(new Predicate<Terrain>() {
                    @Override
                    public boolean evaluate(Terrain arg0) {
                        return arg0.Type.equals("terrainFeature") && arg0.OccursOn.contains(baseTerrain.Name);
                    }
                }).getRandom();
                if (SecondaryTerrain != null) tileInfo.terrainFeature = SecondaryTerrain.Name;
            }
        }

        LinqCollection<TileResource> TileResources = GameBasics.TileResources.linqValues();

        // Resources are placed according to terrainFeature, if exists, otherwise according to BaseLayer.
        TileResources = TileResources.where(new Predicate<TileResource>() {
            @Override
            public boolean evaluate(TileResource arg0) {
                return arg0.TerrainsCanBeFoundOn.contains(tileInfo.getLastTerrain().Name);
            }
        });

        TileResource resource = null;
        if (Math.random() < 1 / 5f) {
            resource = GetRandomResource(TileResources, ResourceType.Bonus);
        } else if (Math.random() < 1 / 7f) {
            resource = GetRandomResource(TileResources, ResourceType.Strategic);
        } else if (Math.random() < 1 / 10f) {
            resource = GetRandomResource(TileResources, ResourceType.Luxury);
        }
        if (resource != null) tileInfo.resource = resource.Name;

        tiles.put(position.toString(),tileInfo);
    }

    public boolean contains(Vector2 vector){ return tiles.containsKey(vector.toString());}

    public TileInfo get(Vector2 vector){return tiles.get(vector.toString());}

    public LinqCollection<TileInfo> getTilesInDistance(Vector2 origin, int distance){
        LinqCollection<TileInfo> tiles = new LinqCollection<TileInfo>();

        for(Vector2 vector : HexMath.GetVectorsInDistance(origin, distance))
            if(contains(vector))
                tiles.add(get(vector));

        return tiles;
    }

    public LinqCollection<TileInfo> values(){return tiles.linqValues();}

    public TileResource GetRandomResource(LinqCollection<TileResource> resources, final ResourceType resourceType) {
        return resources.where(new Predicate<TileResource>() {
            @Override
            public boolean evaluate(TileResource arg0) {
                return arg0.ResourceType.equals(resourceType);
            }
        }).getRandom();
    }

}
