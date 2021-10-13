import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class GroundSection extends GroundLevelSection{
    private List<Vehicle> vehicles;
    private String type;

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

    public GroundSection(String name, List<Vehicle> v, Area ar, List<Section> adj){
        super(name, v, ar, adj);
        this.type = "ground";
    }
}