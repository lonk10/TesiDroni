import exceptions.IncompatibleVehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Area {
    private String id;
    private int latitude;
    private int longitude;
    private List<AirSection> airSections;
    private List<GroundSection> groundSections;
    private List<WaterSection> waterSections;
    private List<UnderwaterSection> uwSections;

    public Area(String id, int lat, int lon) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lon;
        this.airSections = new ArrayList<>();
        this.groundSections = new ArrayList<>();
        this.waterSections = new ArrayList<>();
        this.uwSections = new ArrayList<>();
    }

    //TODO vertical movements inside area or the graph?
    /**
     * Lands an aerial vehicle onto a ground section
     * @param vehicle the vehicle to land
     * @param air the air section where the vehicle is currently in
     * @param ground the ground section where to land
     */
    public void landVehicle(Vehicle vehicle, AirSection air, GroundSection ground) throws IncompatibleVehicleType {
        try {
            air.removeVehicle(vehicle);
            ground.addVehicle(vehicle);
        } catch(IncompatibleVehicleType e){
            System.out.println(e.getMessage() + "Landing was not possible.");
        }
    }
    /**
     * Lifts off an aerial vehicle
     * @param vehicle the vehicle to lift off
     * @param air the air section where the lift off
     * @param ground the ground section where the vehicle is currently in
     */
    public void liftOffVehicle(Vehicle vehicle, AirSection air, GroundSection ground) throws IncompatibleVehicleType {
        try {
            ground.removeVehicle(vehicle);
            air.addVehicle(vehicle);
        } catch(IncompatibleVehicleType e){
            System.out.println(e.getMessage() + "Lifting off was not possible.");
        }
    }

    public void submergeVehicle(Vehicle vehicle, WaterSection water, UnderwaterSection under) throws IncompatibleVehicleType {
        try{
            water.removeVehicle(vehicle);
            under.addVehicle(vehicle);
        } catch(IncompatibleVehicleType e){
            System.out.println(e.getMessage() + "Submerging was not possible.");
        }
    }

    public void emergeVehicle(Vehicle vehicle, WaterSection water, UnderwaterSection under) throws IncompatibleVehicleType {
        try {
            under.removeVehicle(vehicle);
            water.addVehicle(vehicle);
        } catch(IncompatibleVehicleType e){
            System.out.println(e.getMessage() + "Emerging was not possible.");
        }
    }

    /**
     * TODO: might be better as an attribute / vehicles connect automatically?
     * @return a list of all vehicles locally connected
     */
    public List<Vehicle> localConnection(){
        List<Vehicle> conn = new ArrayList<>();
        airSections.forEach(section -> conn.addAll(section.getVehicles()
                .stream()
                .filter(ve -> (!ve.getId().contains("Enemy")) && !(ve.getId().contains("Unknown"))).collect(Collectors.toList())));
        groundSections.forEach(section -> conn.addAll(section.getVehicles()
                .stream()
                .filter(ve -> (!ve.getId().contains("Enemy")) && !(ve.getId().contains("Unknown"))).collect(Collectors.toList())));
        waterSections.forEach(section -> conn.addAll(section.getVehicles()
                .stream()
                .filter(ve -> (!ve.getId().contains("Enemy")) && !(ve.getId().contains("Unknown"))).collect(Collectors.toList())));
        return conn;
    }
    /**
     * Adds an air section to the area
     * @param air
     */
    public void addAirSection(AirSection air){
        this.airSections.add(air);
        air.setArea(this);
    }

    /**
     * Adds an air section to the area
     * @param ground
     */
    public void addGroundSection(GroundSection ground){
        this.groundSections.add(ground);
        ground.setArea(this);
    }

    /**
     * Adds an air section to the area
     * @param water
     */
    public void addWaterSection(WaterSection water){
        this.waterSections.add(water);
        water.setArea(this);
    }

    /**
     * Adds an air section to the area
     * @param uw
     */
    public void addUnderwaterSection(UnderwaterSection uw){
        this.uwSections.add(uw);
        uw.setArea(this);
    }

    public void addSection(Section sec){
        if (sec.getType().equals("Air")){
            this.addAirSection((AirSection) sec);
        } else if (sec.getType().equals("Ground")){
            this.addGroundSection((GroundSection) sec);
        } else if (sec.getType().equals("Water")){
            this.addWaterSection((WaterSection) sec);
        } else if (sec.getType().equals("Underwater")){
            this.addUnderwaterSection((UnderwaterSection) sec);
        }
    }

    /**
     * @return All the sections inside the area;
     */

    public List<Section> getSections(){
        List<Section> sections = new ArrayList<>();
        sections.addAll(this.airSections);
        sections.addAll(this.groundSections);
        sections.addAll(this.waterSections);
        sections.addAll(this.uwSections);

        return sections;
    }

    public List<Vehicle> getVehicles(){
        List<Vehicle> res = new ArrayList<>();
        res.addAll(airSections.stream().map(Section::getVehicles).flatMap(List::stream).collect(Collectors.toList()));
        res.addAll(groundSections.stream().map(Section::getVehicles).flatMap(List::stream).collect(Collectors.toList()));
        res.addAll(waterSections.stream().map(Section::getVehicles).flatMap(List::stream).collect(Collectors.toList()));
        res.addAll(uwSections.stream().map(Section::getVehicles).flatMap(List::stream).collect(Collectors.toList()));
        return res;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public List<AirSection> getAirSections() {
        return airSections;
    }

    public void setAirSections(List<AirSection> airSections) {
        this.airSections = airSections;
    }

    public List<GroundSection> getGroundSections() {
        return groundSections;
    }

    public void setGroundSections(List<GroundSection> groundSections) {
        this.groundSections = groundSections;
    }

    public List<WaterSection> getWaterSections() {
        return waterSections;
    }

    public void setWaterSections(List<WaterSection> waterSections) {
        this.waterSections = waterSections;
    }

    public List<UnderwaterSection> getUwSections() {
        return uwSections;
    }

    public void setUwSections(List<UnderwaterSection> uwSections) {
        this.uwSections = uwSections;
    }

    public String getId(){return this.id;}
}
