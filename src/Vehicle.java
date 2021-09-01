import java.util.List;

public abstract class Vehicle{
    private double latitude;
    private double longitude;
    private ControlStation gcs;
    private String type;
    private Area area;
    private Section section;

    //TODO vehicle builder (container, types)
    //TODO vehicle type as list? How to implement hybrids?

    public Vehicle(){
        latitude = 0;
        longitude = 0;
        gcs = null;
        type = "";
        area = null;
        section = null;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public String getType() { return type; }

    public void setType(String s) { this.type = s; }

    public ControlStation getControlStation() {
        return gcs;
    }

    public Area getArea() { return area; }

    public void setArea(Area area) { this.area = area; }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }
}
