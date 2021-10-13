import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WaterSection extends GroundLevelSection{
    private String type;
    private List<Vehicle> vehicles;

    /**
     * Constructor with empty vehicle list
     */
    public WaterSection(){
        super();
        this.type = "water";
        this.vehicles = new ArrayList<>();
    }

    public WaterSection(String name){
        super(name);
        this.type = "water";
        this.vehicles = new ArrayList<>();
    }

    public WaterSection(String name, List<Vehicle> v, Area ar, List<Section> as){
        super(name, v, ar, as);
        this.type = "water";
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
            super.adjacents.add(sec);
        } else if (sec instanceof GroundSection){
            super.adjacents.add(sec);
        } else if (sec instanceof WaterSection){
            super.adjacents.add(sec);
        } else if (sec instanceof UnderwaterSection){
            super.adjacents.add(sec);
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        super.adjacents.remove(sec);
    }

    public List<Section> getUnderwaterSections(){
        return super.adjacents.stream().filter(c -> c instanceof UnderwaterSection).collect(Collectors.toList());
    }
    @Override
    public List<Vehicle> getVehicles(){
        return this.vehicles;
    }
}
