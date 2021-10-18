import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class GroundLevelSection implements Section{
    protected List<Vehicle> vehicles;
    protected String type;
    protected List<Section> adjacents;
    protected Area area;
    protected String id;

    /**
     * Constructor with empty vehicle list
     */
    public GroundLevelSection(){
        this.vehicles = new ArrayList<>();
        this.adjacents = new ArrayList<>();
        this.area = null;
        this.type = "";
    }

    public GroundLevelSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.adjacents = new ArrayList<>();
        this.area = null;
        this.type = "";
    }

    public GroundLevelSection(String name, List<Vehicle> v, Area ar, List<Section> adj){
        this.id = name;
        this.vehicles = v;
        this.adjacents = adj;
        this.type = "";
        this.area = ar;
    }

    @Override
    public List<Section> getAdjacents(){
        return this.adjacents;
    }

    /**
     * Returns whether or not a section is adjacent to the current one.
     * @param sec the section to check
     * @return whether the section is adjacent to sec
     */
    @Override
    public Boolean isAdjacentTo(Section sec) throws AdjacencyException {
        Boolean res = this.getAdjacents().contains(sec);
        if (res != sec.isAdjacentTo(this)){
            throw new AdjacencyException("Only one section features the adjacency.");
        }
        return res;
    }


    /**
     * Adds a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void addVehicle(Vehicle v) throws IncompatibleVehicleType {
        if (v.getType().contains("Air") || v.getType().contains("Ground")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
        }
        else {
            throw new IncompatibleVehicleType("Ground Level Section only support Air and Ground vehicles.");
        }
    }

    /**
     * Removes a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void removeVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("Air") || v.getType().contains("Ground")) {
            this.vehicles.remove(v);
            v.setSection(null);
            v.setArea(null);
        }
        else {
            throw new IncompatibleVehicleType("Ground Level Section only support Air and Ground vehicles.");
        }
    }

    /**
     * Adds an adjacent section
     * @param sec the section to add
     */
    @Override
    public void addAdjacent(Section sec) throws IncompatibleSectionType{
        if (sec instanceof AirSection){
            this.adjacents.add(sec);
        } else if (sec instanceof GroundLevelSection){
            this.adjacents.add( sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground Level section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec instanceof AirSection){
            this.adjacents.remove(sec);
        } else if (sec instanceof GroundLevelSection){
            this.adjacents.remove(sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground section can't be adjacent to Underwater sections. ");
        }
    }

    @Override
    public List<Vehicle> getVehicles(){ return this.vehicles; }

    @Override
    public String getType(){
        return type;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
    public String getId(){return this.id;}
}
