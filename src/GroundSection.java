import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class GroundSection extends GroundLevelSection{
    private List<Vehicle> vehicles;
    private String type;
    private GroundSection north;
    private GroundSection south;
    private GroundSection east;
    private GroundSection west;
    private List<AirSection> airs;
    private Area area;
    private String id;

    /**
     * Constructor with empty vehicle list
     */
    public GroundSection(){
        super();
        this.type = "ground";
        this.vehicles = new ArrayList<>();
    }

    public GroundSection(String name){
        super(name);
        this.type = "ground";
        this.vehicles = new ArrayList<>();
    }

    public GroundSection(String name, List<Vehicle> v, Area ar, List<AirSection> as, GroundLevelSection n, GroundLevelSection s, GroundLevelSection e, GroundLevelSection w){
        super(name, v, ar, as, n, s, e, w);
        this.type = "ground";
    }
}