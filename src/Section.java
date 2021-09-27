import exceptions.*;

import java.util.List;

public interface Section {
    /**
     * Adds a vehicle inside the section
     * @param v a vehicle
     */
    public void addVehicle (Vehicle v) throws IncompatibleVehicleType;

    /**
     * Remove a vehicle from the section
     * @param v a vehicle
     */
    public void removeVehicle (Vehicle v) throws IncompatibleVehicleType;

    public List<Vehicle> getVehicles();

    public List<Section> getAdjacents();

    public Boolean isAdjacentTo(Section sec) throws AdjacencyException;

    public String getType();

    public String getId();

    public void setArea(Area area);

    public void addAdjacent(Section sec) throws IncompatibleSectionType;

    public void removeAdjacent(Section sec) throws  IncompatibleSectionType;

    public List<Section> getCardinals();

    public Section getNorth();

    public Section getSouth();

    public Section getWest();

    public Section getEast();

    public void addCardinal(Section s, String p) throws IncompatibleSectionType;
}
