import exceptions.AdjacencyException;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Graph {
    List<Area> areas;

    public Graph(){
        this.areas = new ArrayList<>();
    }

    public Graph(List<Area> areas) {
        this.areas = areas;
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

    /**
     * Adds an area to the graph
     * @param area the area to add
     */
    public void addArea(Area area){
        this.areas.add(area);
    }

    /**
     * Removes an area from the graph
     * @param area the area to remove
     */
    public void removeArea(Area area){
        this.areas.remove(area);
    }

    public List<Area> getAreas() { return areas; }

    public List<Section> getSections() {
        return areas.stream().map(Area::getSections).flatMap(List::stream).collect(Collectors.toList());
    }

    public void setAreas(List<Area> areas) { this.areas = areas; }
}