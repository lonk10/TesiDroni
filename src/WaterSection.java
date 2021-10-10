import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class WaterSection extends GroundLevelSection{
    private String type;
    private List<UnderwaterSection> underwaters;
    private List<Vehicle> vehicles;

    /**
     * Constructor with empty vehicle list
     */
    public WaterSection(){
        super();
        this.underwaters = new ArrayList<>();
        this.type = "water";
        this.vehicles = new ArrayList<>();
    }

    public WaterSection(String name){
        super(name);
        this.underwaters = new ArrayList<>();
        this.type = "water";
        this.vehicles = new ArrayList<>();
    }

    public WaterSection(String name, List<Vehicle> v, Area ar, List<AirSection> as, List<UnderwaterSection> us, GroundLevelSection n, GroundLevelSection s, GroundLevelSection e, GroundLevelSection w){
        super(name, v, ar, as, n, s, e, w);
        this.underwaters = us;
        this.type = "water";
    }

    @Override
    public List<Section> getAdjacents(){
        List<Section> sections = new ArrayList<>();
        sections.add(this.getNorth());
        sections.add(this.getSouth());
        sections.add(this.getEast());
        sections.add(this.getWest());
        sections.addAll(this.getAirSections());
        sections.addAll(underwaters);
        return sections;
    }

    /**
     * Adds a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void addVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("air") || v.getType().contains("water") || v.getType().contains("underwater")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.getArea());
        }
        else {
            throw new IncompatibleVehicleType("Water Section only support Air, Water and Underwater vehicles.");
        }
    }

    /**
     * Removes a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void removeVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("air") || v.getType().contains("water") || v.getType().contains("underwater")) {
            this.vehicles.remove(v);
            v.setSection(null);
            v.setArea(null);
        }
        else {
            throw new IncompatibleVehicleType("Water Section only support Air, Water and Underwater vehicles.");
        }
    }

    /**
     * Adds an adjacent section
     * @param sec the section to add
     */
    @Override
    public void addAdjacent(Section sec) throws IncompatibleSectionType{
        if (sec instanceof AirSection){
            this.getAirSections().add((AirSection) sec);
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Water section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            throw new IncompatibleSectionType("Water section can't be adjacent to Water sections. ");
        } else if (sec instanceof UnderwaterSection){
            this.underwaters.add((UnderwaterSection) sec);
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec instanceof AirSection){
            super.removeAdjacent(sec);
        } else if (sec instanceof GroundSection){
            this.removeCardinal((GroundSection) sec);
        } else if (sec instanceof WaterSection){
            this.removeCardinal((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            this.underwaters.remove(sec);
        }
    }

    public void removeSection(Section sec) throws IncompatibleSectionType{
        if (sec instanceof WaterSection){
            this.removeCardinal((WaterSection) sec);
        } else {
            this.removeAdjacent(sec);
        }
    }

    public List<Section> getUnderwaterSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.underwaters);
        return res;
    }
    @Override
    public List<Vehicle> getVehicles(){
        return this.vehicles;
    }
}
