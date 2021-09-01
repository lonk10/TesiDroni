import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class AirSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private List<AirSection> cardinals;
    private List<GroundSection> grounds;
    private List<WaterSection> waters;
    private Area area;

    /**
     * Constructor with empty vehicle list
     */
    public AirSection(){
        this.vehicles = new ArrayList<>();
        this.cardinals = new ArrayList<>();
        this.waters = new ArrayList<>();
        this.grounds = new ArrayList<>();
        this.area = null;
        this.type = "air";
    }

    public AirSection(List<Vehicle> v, Area ar, List<AirSection> as, List<GroundSection> gs, List<WaterSection> ws){
        this.vehicles = v;
        this.cardinals = as;
        this.grounds = gs;
        this.waters = ws;
        this.type = "air";
        this.area  = ar;
    }
    @Override
    public List<Section> getAdjacents(){
        List<Section> sections = new ArrayList<>();
        sections.addAll(cardinals);
        sections.addAll(grounds);
        sections.addAll(waters);
        return sections;
    }

    /**
     * Returns whether or not a section is adjacent to the current one.
     * @param sec the section to check
     * @return whether the section is adjacent to sec
     */
    @Override
    public Boolean isAdjacentTo(Section sec) throws AdjacencyException{
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
        if (v.getType().contains("air")) {
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
        if (v.getType().contains("air")) {
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
        if (sec instanceof AirSection){
            this.cardinals.add((AirSection) sec);
        } else if (sec instanceof GroundSection){
            this.grounds.add((GroundSection) sec);
        } else if (sec instanceof WaterSection){
            this.waters.add((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Air section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec instanceof AirSection){
            this.cardinals.remove(sec);
        } else if (sec instanceof GroundSection){
            this.grounds.remove(sec);
        } else if (sec instanceof WaterSection){
            this.waters.remove(sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Air section can't be adjacent to Underwater sections. ");
        }
    }

    public List<Section> getGroundSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.grounds);
        return res;
    }

    public List<Section> getWaterSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.waters);
        return res;
    }

    public List<Section> getCardinals(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.cardinals);
        return res;
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
}
