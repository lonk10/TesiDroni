import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.graph.G;

import java.util.ArrayList;
import java.util.List;

public class GroundSection implements Section{
    private List<Vehicle> vehicles;
    private String type;
    private GroundSection north;
    private GroundSection south;
    private GroundSection east;
    private GroundSection west;
    private List<AirSection> airs;
    private List<WaterSection> waters;
    private Area area;
    private String id;

    /**
     * Constructor with empty vehicle list
     */
    public GroundSection(){
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.waters = new ArrayList<>();
        this.area = null;
        this.type = "Ground";
    }

    public GroundSection(String name){
        this.id = name;
        this.vehicles = new ArrayList<>();
        this.airs = new ArrayList<>();
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.waters = new ArrayList<>();
        this.area = null;
        this.type = "Ground";
    }

    public GroundSection(String name, List<Vehicle> v, Area ar, List<AirSection> as, List<WaterSection> ws, GroundSection n, GroundSection s, GroundSection e, GroundSection w){
        this.id = name;
        this.vehicles = v;
        this.airs = as;
        this.north = n;
        this.south = s;
        this.east = e;
        this.west = w;
        this.waters = ws;
        this.type = "Ground";
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
        if (v.getType().contains("air") || v.getType().contains("ground")) {
            this.vehicles.add(v);
            v.setSection(this);
            v.setArea(this.area);
        }
        else {
            throw new IncompatibleVehicleType("Ground Section only support Air and Ground vehicles.");
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
            throw new IncompatibleVehicleType("Ground Section only support Air and Ground vehicles.");
        }
    }

    /**
     * Adds a cardinal section
     * @param sec the section to add
     * @param p the cardinal point
     */
    public void addCardinal(Section sec, String p) throws IncompatibleSectionType {
        if (sec instanceof GroundSection) {
            switch (p) {
                case "north":
                    this.north = (GroundSection) sec;
                case "south":
                    this.south = (GroundSection) sec;
                case "east":
                    this.east = (GroundSection) sec;
                case "west":
                    this.west = (GroundSection) sec;
            }
        } else {
            throw new IncompatibleSectionType("Ground section can only have Ground sections as cardinals. ");
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
            throw new IncompatibleSectionType("Ground section can't be adjacent to Ground sections. ");
        } else if (sec instanceof WaterSection){
            this.waters.add((WaterSection) sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground section can't be adjacent to Underwater sections. ");
        }
    }

    /**
     * Removes a cardinal point
     * @param sec the section to remove
     */
    public void removeCardinal(GroundSection sec){
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
            this.removeCardinal((GroundSection) sec);
        } else if (sec instanceof WaterSection){
            this.waters.remove(sec);
        } else if (sec instanceof UnderwaterSection){
            throw new IncompatibleSectionType("Ground section can't be adjacent to Underwater sections. ");
        }
    }

    public List<Section> getAirSections(){
        List<Section> res = new ArrayList<>();
        res.addAll(this.airs);
        return res;
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
}