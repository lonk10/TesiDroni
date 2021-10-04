public class VehicleFactory {

    public Vehicle getVehicle(String type, String name){
        if (type.equalsIgnoreCase("AIR")){
            return new AirVehicle(name);
        } else if (type.equalsIgnoreCase("GROUND")){
            return new GroundVehicle(name);
        } else if (type.equalsIgnoreCase("WATER")){
            return new WaterVehicle(name);
        } else if (type.equalsIgnoreCase("UNDERWATER")){
            return new UnderwaterVehicle(name);
        }
        return null;
    }
}