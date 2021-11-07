import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class AirSection implements Section{
    private String id;
    private List<Vehicle> vehicles;
    private String type;
    private List<Section> adjacents;
    private Area area;

    /**
     * Constructor with empty vehicle list
     */
    public AirSection(){
        this.vehicles = new ArrayList<>();
        this.adjacents = new ArrayList<>();
        this.area = null;
        this.type = "Air";
    }
    public AirSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.adjacents = new ArrayList<>();
        this.area = null;
        this.type = "Air";
    }

    public AirSection(String name, List<Vehicle> v, Area ar, List<Section> adj){
        this.id = name;
        this.vehicles = v;
        this.adjacents = adj;
        this.type = "air";
        this.area  = ar;
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
    public Boolean isAdjacentTo(Section sec) throws AdjacencyException{
        Boolean res = (this.getAdjacents().contains(sec));
        if (res != sec.getAdjacents().contains(this)){
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
        if (v.getType().contains("Air")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
        }
        else {
            throw new IncompatibleVehicleType("Air Section only support Air vehicles.");
        }
    }

    /**
     * Removes a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void removeVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("Air")) {
            this.vehicles.remove(v);
            v.setSection(null);
            v.setArea(null);
        }
        else {
            throw new IncompatibleVehicleType("Air Section only support Air vehicles.");
        }
    }

    /**
     * Adds an adjacent section
     * @param sec the section to add
     */
    @Override
    public void addAdjacent(Section sec) throws IncompatibleSectionType{
        if (sec.getType().equals("Air")){
            this.adjacents.add(sec);
        } else if (sec.getType().equals("Ground")){
            this.adjacents.add(sec);
        } else if (sec.getType().equals("Water")){
            this.adjacents.add(sec);
        } else if (sec.getType().equals("Underwater")){
            throw new IncompatibleSectionType("Air section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec.getType().equals("Air")){
            this.adjacents.remove(sec);
        } else if (sec.getType().equals("Ground")){
            this.adjacents.remove(sec);
        } else if (sec.getType().equals("Water")){
            this.adjacents.remove(sec);
        } else if (sec.getType().equals("Underwater")){
            throw new IncompatibleSectionType("Air section can't be adjacent to Underwater sections. ");
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
