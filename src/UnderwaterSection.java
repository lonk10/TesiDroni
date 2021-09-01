import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class UnderwaterSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private List<UnderwaterSection> cardinals;
    private List<WaterSection> waters;
    private Area area;

    /**
     * Constructor with empty vehicle list
     */
    public UnderwaterSection(){
        this.vehicles = new ArrayList<>();
        this.cardinals = new ArrayList<>();
        this.waters = new ArrayList<>();
        this.type = "underwater";
        this.area = null;
    }

    public UnderwaterSection(List<Vehicle> v, Area ar, List<UnderwaterSection> us, List<WaterSection> ws){
        this.vehicles = v;
        this.cardinals = us;
        this.waters = ws;
        this.type = "underwater";
        this.area = ar;
    }

    @Override
    public List<Section> getAdjacents(){
        List<Section> sections = new ArrayList<>();
        sections.addAll(cardinals);
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
        if (v.getType().contains("underwater")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
        }
        else {
            throw new IncompatibleVehicleType("UnderWater Section only supports Underwater vehicles.");
        }
    }

    /**
     * Removes a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void removeVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("underwater")) {
            this.vehicles.remove(v);
            v.setSection(null);
            v.setArea(null);
        }
        else {
            throw new IncompatibleVehicleType("Underwater Section only supports Underwater vehicles.");
        }
    }


    /**
     * Adds an adjacent section
     * @param sec the section to add
     */
    @Override
    public void addAdjacent(Section sec) throws IncompatibleSectionType{
        if (sec instanceof AirSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Air sections. ");
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            this.waters.add((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            this.cardinals.add((UnderwaterSection) sec);
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec instanceof AirSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Air sections. ");
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            this.waters.remove(sec);
        } else if (sec instanceof UnderwaterSection){
            this.cardinals.remove(sec);
        }
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
    public List<Vehicle> getVehicles(){
        return this.vehicles;
    }

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
