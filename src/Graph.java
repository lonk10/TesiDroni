import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Graph {
    List<Area> areas;
    List<ControlStation> controlStations;

    public Graph(){
        this.areas = new ArrayList<>();
    }

    public Graph(List<Area> areas) {
        this.areas = areas;
    }

    public Graph(List<Area> areas, List<ControlStation> cs) {
        this.controlStations = cs;
        this.areas = areas;
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

    public List<ControlStation> getControlStations(){ return this.controlStations; }
}