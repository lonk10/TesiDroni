import java.util.ArrayList;
import java.util.List;

public class WaterVehicle extends Vehicle {
    private List<Vehicle> uwConnection;
    public WaterVehicle(){
        super();
        super.setType("WaterVehicle");
        uwConnection = new ArrayList<>();
    }

    public WaterVehicle(String name){
        super(name);
        super.setType("WaterVehicle");
        uwConnection = new ArrayList<>();
    }

    public void addToUWConnection(Vehicle v){
        if (!this.uwConnection.contains(v) && v instanceof UnderwaterVehicle) {
            this.uwConnection.add(v);
            ((UnderwaterVehicle) v).addToUWConnection(this);
        }
    }

    public void removeFromUWConnection(Vehicle v){
        this.uwConnection.remove(v);
    }

    public List<Vehicle> getUwConnection() {
        return uwConnection;
    }

    public void setUwConnection(List<Vehicle> uwConnection) {
        this.uwConnection = uwConnection;
    }
}
