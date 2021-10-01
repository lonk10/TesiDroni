import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;

public class UnderwaterSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private UnderwaterSection north;
    private UnderwaterSection south;
    private UnderwaterSection east;
    private UnderwaterSection west;
    private List<WaterSection> waters;
    private Area area;
    private String id;

    /**
     * Constructor with empty vehicle list
     */
    public UnderwaterSection(){
        this.vehicles = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.waters = new ArrayList<>();
        this.type = "underwater";
        this.area = null;
    }

    public UnderwaterSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.waters = new ArrayList<>();
        this.type = "underwater";
        this.area = null;
    }

    public UnderwaterSection(String name, List<Vehicle> v, Area ar, List<WaterSection> ws, UnderwaterSection n, UnderwaterSection s, UnderwaterSection e, UnderwaterSection w){
        this.id = name;
        this.vehicles = v;
        this.north = n;
        this.south = s;
        this.east = e;
        this.west = w;
        this.waters = ws;
        this.type = "underwater";
        this.area = ar;
    }

    @Override
    public List<Section> getAdjacents(){
        List<Section> sections = new ArrayList<>();
        sections.add(north);
        sections.add(south);
        sections.add(east);
        sections.add(west);
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
     * Adds a cardinal section
     * @param sec the section to add
     * @param p the cardinal point
     */
    public void addCardinal(Section sec, String p) throws IncompatibleSectionType {
        if (sec instanceof UnderwaterSection) {
            switch (p) {
                case "north":
                    this.north = (UnderwaterSection) sec;
                    break;
                case "south":
                    this.south = (UnderwaterSection) sec;
                    break;
                case "east":
                    this.east = (UnderwaterSection) sec;
                    break;
                case "west":
                    this.west = (UnderwaterSection) sec;
                    break;
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
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Air sections. ");
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            this.waters.add((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes a cardinal point
     * @param sec the section to remove
     */
    public void removeCardinal(UnderwaterSection sec){
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
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Air sections. ");
        } else if (sec instanceof GroundSection){
            throw new IncompatibleSectionType("Underwater section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            this.waters.remove(sec);
        } else if (sec instanceof UnderwaterSection){
            this.removeCardinal((UnderwaterSection) sec);
        }
    }

    public void removeSection(Section sec) throws IncompatibleSectionType{
        if (sec instanceof UnderwaterSection){
            this.removeCardinal((UnderwaterSection) sec);
        } else {
            this.removeAdjacent(sec);
        }
    }

    public List<Section> getWaterSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.waters);
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

    public String getId(){return this.id;}

    @Override
    public UnderwaterSection getNorth() {
        return north;
    }

    @Override
    public UnderwaterSection getSouth() {
        return south;
    }

    @Override
    public UnderwaterSection getEast() {
        return east;
    }

    @Override
    public UnderwaterSection getWest() {
        return west;
    }
}
