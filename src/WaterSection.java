import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class WaterSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private WaterSection north;
    private WaterSection south;
    private WaterSection east;
    private WaterSection west;
    private List<AirSection> airs;
    private List<GroundSection> grounds;
    private List<UnderwaterSection> underwaters;
    private Area area;
    private String id;

    /**
     * Constructor with empty vehicle list
     */
    public WaterSection(){
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.grounds = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.underwaters = new ArrayList<>();
        this.type = "water";
        this.area = null;
    }

    public WaterSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.grounds = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.underwaters = new ArrayList<>();
        this.type = "water";
        this.area = null;
    }

    public WaterSection(String name, List<Vehicle> v, Area ar, List<AirSection> as, List<GroundSection> gs, List<UnderwaterSection> us, WaterSection n, WaterSection s, WaterSection e, WaterSection w){
        this.id = name;
        this.vehicles = v;
        this.airs = as;
        this.grounds = gs;
        this.north = n;
        this.south = s;
        this.east = e;
        this.west = w;
        this.underwaters = us;
        this.type = "water";
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
        sections.addAll(grounds);
        sections.addAll(underwaters);
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
    public void addVehicle(Vehicle v) throws IncompatibleVehicleType{
        if (v.getType().contains("air") || v.getType().contains("water") || v.getType().contains("underwater")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
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
     * Adds a cardinal section
     * @param sec the section to add
     * @param p the cardinal point
     */
    public void addCardinal(Section sec, String p) throws IncompatibleSectionType {
        if (sec instanceof WaterSection) {
            switch (p) {
                case "north":
                    this.north = (WaterSection) sec;
                case "south":
                    this.south = (WaterSection) sec;
                case "east":
                    this.east = (WaterSection) sec;
                case "west":
                    this.west = (WaterSection) sec;
            }
        } else {
            throw new IncompatibleSectionType("Water section can only have Water sections as cardinals. ");
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
            this.grounds.add((GroundSection) sec);
        } else if (sec instanceof WaterSection){
            throw new IncompatibleSectionType("Water section can't be adjacent to Water sections. ");
        } else if (sec instanceof UnderwaterSection){
            this.underwaters.add((UnderwaterSection) sec);
        }
    }

    /**
     * Removes a cardinal point
     * @param sec the section to remove
     */
    public void removeCardinal(WaterSection sec){
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
        } else if (sec instanceof GroundSection){
            this.grounds.remove(sec);
        } else if (sec instanceof WaterSection){
            this.removeCardinal((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            this.underwaters.remove(sec);
        }
    }

    public List<Section> getAirSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.airs);
        return res;
    }

    public List<Section> getGroundSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.grounds);
        return res;
    }

    public List<Section> getUnderwaterSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.underwaters);
        return res;
    }

    public List<Section> getCardinals(){
        List<Section> res = new ArrayList<>();
        res.add(this.north);
        res.add(this.south);
        res.add(this.east);
        res.add(this.west);
        return res;
    }

    @Override
    public List<Vehicle> getVehicles(){ return this.vehicles; }

    @Override
    public String getType(){
        return this.type;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getId(){return this.id;}
}
