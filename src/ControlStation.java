import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.List;
import java.util.stream.Collectors;

public class ControlStation {
    private List<Area> areas;
    private String ID;
    private Section parent;
    //TODO graph inside ControlStation, or just areas under its control?
    //TODO message exchange between GCS/Drones
    public ControlStation(){
        this.ID = null;
        this.parent = null;
    }

    public ControlStation(String name){
        this.ID = name;
    }

    public ControlStation(String name, Section sec){
        this.ID = name;
        this.parent = sec;
    }
    /**
     * Moves a vehicle from a section to another
     * @param vehicle the vehicle to move
     * @param source section the vehicle is currently in
     * @param dest section the vehicle wants to move into
     * @throws IncompatibleVehicleType if vehicle type is not compatible with source or dest section
     */

    public void moveVehicle(Vehicle vehicle, Section source, Section dest) throws IncompatibleVehicleType, AdjacencyException {
        try {
            if (source.isAdjacentTo(dest)) {
                source.removeVehicle(vehicle);
                dest.addVehicle(vehicle);
            } else {
                throw new AdjacencyException("Sections are not adjacent.");
            }
        } catch (IncompatibleVehicleType | AdjacencyException e){
            System.out.println(e.getMessage() + "Movement was not possible.");
        }
    }
    public List<Vehicle> getVehicles(){
        return areas.stream().map(Area::getVehicles).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Removes link between two adjacent sections
     * @param a the first section
     * @param b the second section
     * @throws IncompatibleSectionType if the two section can't be adjacent (eg. Ground and Underwater sections)
     */
    public void removeLink(Section a, Section b) throws IncompatibleSectionType {
        a.removeSection(b);
        b.removeSection(a);
    }
    /**
     * Restores/Adds a link between two adjacent sections
     * @param a the first section
     * @param b the second section
     * @throws IncompatibleSectionType if the two section can't be adjacent (eg. Ground and Underwater sections)
     */
    public void addLink(Section a, Section b) throws IncompatibleSectionType{
        if (a.getType().equals(b.getType())){
            //TODO do I really need to know cardinal directions?
        }
    }

    public Section getSection(){
        return this.parent;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }
}
