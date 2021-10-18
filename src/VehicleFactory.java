public class VehicleFactory {

    public Vehicle getVehicle(String type, String name){
        if (type.equalsIgnoreCase("AIRVEHICLE")){
            return new AirVehicle(name);
        } else if (type.equalsIgnoreCase("GROUNDVEHICLE")){
            return new GroundVehicle(name);
        } else if (type.equalsIgnoreCase("WATERVEHICLE")){
            return new WaterVehicle(name);
        } else if (type.equalsIgnoreCase("UNDERWATERVEHICLE")){
            return new UnderwaterVehicle(name);
        }
        return null;
    }
}