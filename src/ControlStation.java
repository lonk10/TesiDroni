import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.List;
import java.util.stream.Collectors;

public class ControlStation {
    private List<Area> areas;

    //TODO graph inside ControlStation, or just areas under its control?
    //TODO message exchange between GCS/Drones

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

    private List<Vehicle> getVehicles(){
        return areas.stream().map(Area::getVehicles).flatMap(List::stream).collect(Collectors.toList());
    }

    public void removeLink(Section a, Section b) throws IncompatibleSectionType {
        a.removeSection(b);
        b.removeSection(a);
    }

    public void addLink(Section a, Section b) throws IncompatibleSectionType{
        if (a.getType().equals(b.getType())){
            //TODO do I really need to know cardinal directions?
        }
    }
}
