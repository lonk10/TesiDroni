import java.util.ArrayList;
import java.util.List;

public class UnderwaterVehicle extends WaterVehicle{
    private List<Vehicle> uwConnection;

    public UnderwaterVehicle(){
        super();
        super.setType("underwater");
    }

    public UnderwaterVehicle(String name){
        super(name);
        super.setType("underwater");
    }

    public void addToUwConnection(Vehicle v){
        if ( !this.uwConnection.contains(v)  ){
            this.uwConnection.add(v);
        }
    }
}