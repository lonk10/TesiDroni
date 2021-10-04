import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class GroundLevelSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private GroundLevelSection north;
    private GroundLevelSection south;
    private GroundLevelSection east;
    private GroundLevelSection west;
    private List<AirSection> airs;
    private Area area;
    private String id;

    /**
     * Constructor with empty vehicle list
     */
    public GroundLevelSection(){
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.area = null;
        this.type = "";
    }

    public GroundLevelSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.area = null;
        this.type = "";
    }

    public GroundLevelSection(String name, List<Vehicle> v, Area ar, List<AirSection> as, GroundLevelSection n, GroundLevelSection s, GroundLevelSection e, GroundLevelSection w){
        this.id = name;
        this.vehicles = v;
        this.airs = as;
        this.north = n;
        this.south = s;
        this.east = e;
        this.west = w;
        this.type = "";
        this.area = ar;
    }

    @Override
    public List<Section> getAdjacents(){
        List<Section> sections = new ArrayList<>();
        sections.add(north);
        sections.add(south);
        sections.add(east);
        sections.add(west);
        sections.addAll(airs);
        return sections;
    }

    /**
     * Returns whether or not a section is adjacent to the current one.
     * @param sec the section to check
     * @return whether the section is adjacent to sec
     */
    @Override
    public Boolean isAdjacentTo(Section sec) throws AdjacencyException {
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
        if (v.getType().contains("air") || v.getType().contains("ground")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
        }
        else {
            throw new IncompatibleVehicleType("Ground Level Section only support Air and Ground vehicles.");
        }
    }

    /**
     * Removes a compatible vehicle from the section
     * @param v a vehicle
     */
    @Override
    public void removeVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("air") || v.getType().contains("ground")) {
            this.vehicles.remove(v);
            v.setSection(null);
            v.setArea(null);
        }
        else {
            throw new IncompatibleVehicleType("Ground Level Section only support Air and Ground vehicles.");
        }
    }

    /**
     * Adds a cardinal section
     * @param sec the section to add
     * @param p the cardinal point
     */
    public void addCardinal(Section sec, String p) throws IncompatibleSectionType {
        if (sec instanceof GroundLevelSection) {
            switch (p) {
                case "north":
                    this.north = (GroundLevelSection) sec;
                    break;
                case "south":
                    this.south = (GroundLevelSection) sec;
                    break;
                case "east":
                    this.east = (GroundLevelSection) sec;
                    break;
                case "west":
                    this.west = (GroundLevelSection) sec;
                    break;
            }
        } else {
            throw new IncompatibleSectionType("Ground Level section can only have Ground Level sections as cardinals. ");
        }
    }


    /**
     * Adds an adjacent section
     * @param sec the section to add
     */
    @Override
    public void addAdjacent(Section sec) throws IncompatibleSectionType{
        if (sec instanceof AirSection){
            this.airs.add((AirSection) sec);
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Ground Level section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            throw new IncompatibleSectionType("Ground Level section can't be adjacent to Water sections. ");
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground Level section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes a cardinal point
     * @param sec the section to remove
     */
    public void removeCardinal(GroundLevelSection sec){
        if (sec == this.north){
            this.north = null;
        } else if(sec == this.south){
            this.south = null;
        } else if(sec == this.east){
            this.east = null;
        } else if(sec == this.west){
            this.west = null;
        }
    }

    /**
     * Removes an adjacent section
     * @param sec the section to remove
     */
    @Override
    public void removeAdjacent(Section sec) throws IncompatibleSectionType {
        if (sec instanceof AirSection){
            this.airs.remove(sec);
        } else if (sec instanceof GroundLevelSection){
            this.removeCardinal((GroundLevelSection) sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground section can't be adjacent to Underwater sections. ");
        }
    }

    public void removeSection(Section sec) throws IncompatibleSectionType{
        if (sec instanceof GroundSection){
            this.removeCardinal((GroundLevelSection) sec);
        } else {
            this.removeAdjacent(sec);
        }
    }

    public List<Section> getAirSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.airs);
        return res;
    }

    public List<Section> getCardinals(){
        List<Section> res = new ArrayList<>();
        res.add(north);
        res.add(south);
        res.add(east);
        res.add(west);
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
    public String getId(){return this.id;}

    @Override
    public GroundLevelSection getNorth() {
        return north;
    }

    @Override
    public GroundLevelSection getSouth() {
        return south;
    }

    @Override
    public GroundLevelSection getEast() {
        return east;
    }

    @Override
    public GroundLevelSection getWest() {
        return west;
    }
}
