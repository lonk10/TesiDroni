public class SectionFactory {

    public Section getSection(String type, String name){
        if (type.equalsIgnoreCase("AIR")){
            return new AirSection(name);
        } else if (type.equalsIgnoreCase("GROUND")){
            return new GroundSection(name);
        } else if (type.equalsIgnoreCase("WATER")){
            return new WaterSection(name);
        } else if (type.equalsIgnoreCase("UNDERWATER")){
            return new UnderwaterSection(name);
        }
        return null;
    }
}
