import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import it.uniud.mads.jlibbig.core.std.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) throws IncompatibleSectionType, IncompatibleVehicleType {

        /*
        //Signature building
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        signatureBuilder.add(new Control("Area", true, 0));
        signatureBuilder.add(new Control("Air", true, 6));
        signatureBuilder.add(new Control("Ground", true, 6));
        signatureBuilder.add(new Control("Water", true, 7));
        signatureBuilder.add(new Control("Underwater", true, 5));
        signatureBuilder.add(new Control("GCS", true, 1));
        signatureBuilder.add(new Control("UAV", true, 2));
        signatureBuilder.add(new Control("UGV", true, 2));
        signatureBuilder.add(new Control("UUV", true, 3));
        signatureBuilder.add(new Control("Boat", true, 3));
        signatureBuilder.add(new Control("Float", true, 3));
        Signature signature = signatureBuilder.makeSignature();

        BigraphBuilder builder = new BigraphBuilder(signature);
        OuterName grid = builder.addOuterName("Grid");
        OuterName gcs = builder.addOuterName("GCS");
        Root root = builder.addRoot();
        Node air = builder.addNode("Air",root,grid);
        Node ground = builder.addNode("Ground",root,grid);
        Node ground2 = builder.addNode("Ground",root,grid);
        Node water = builder.addNode("Water",root,grid);
        Node underwater = builder.addNode("Underwater",root,grid);
        Node uav1 = builder.addNode("UAV",air,gcs);
        Node uav2 = builder.addNode("UAV",air,gcs);
        Node ugv = builder.addNode("UGV",ground2,gcs);


        //builder.addSite(root);
        //builder.addSite(printer);
        Bigraph bigraph = builder.makeBigraph();
        */


        //Create Areas
        Area area1 = new Area(0,0);
        Area area2 = new Area(1, 0);
        Area area3 = new Area(1,1);
        //Create sections
        AirSection air1 = new AirSection();
        AirSection air2 = new AirSection();
        AirSection air3 = new AirSection();
        GroundSection ground1 = new GroundSection();
        GroundSection ground2 = new GroundSection();
        GroundSection ground3 = new GroundSection();
        GroundSection ground4 = new GroundSection();
        WaterSection water1 = new WaterSection();
        WaterSection water2 = new WaterSection();
        UnderwaterSection under1 = new UnderwaterSection();
        //Create vehicles
        WaterVehicle wVehicle1 = new WaterVehicle();
        WaterVehicle wVehicle2 = new WaterVehicle();
        UnderwaterVehicle uwVehicle1 = new UnderwaterVehicle();
        UnderwaterVehicle uwVehicle2 = new UnderwaterVehicle();
        UnderwaterVehicle uwVehicle3 = new UnderwaterVehicle();


        //Add sections to areas
        area1.addAirSection(air1);
        area2.addAirSection(air2);
        area3.addAirSection(air3);
        area1.addGroundSection(ground1);
        area1.addGroundSection(ground2);
        area2.addGroundSection(ground3);
        area3.addGroundSection(ground4);
        area2.addWaterSection(water1);
        area3.addWaterSection(water2);
        area2.addUnderwaterSection(under1);

        //Create adjacencies
        air1.addAdjacent(air2);
        air1.addAdjacent(ground1);
        air1.addAdjacent(ground2);
        ground1.addAdjacent(ground2);
        ground2.addAdjacent(ground1);
        ground2.addAdjacent(ground3);

        air2.addAdjacent(air1);
        air2.addAdjacent(air3);
        air2.addAdjacent(ground3);
        air2.addAdjacent(water1);
        ground3.addAdjacent(air2);
        ground3.addAdjacent(ground2);
        ground3.addAdjacent(water1);
        ground3.addAdjacent(ground4);
        water1.addAdjacent(air2);
        water1.addAdjacent(ground3);
        water1.addAdjacent(water2);
        water1.addAdjacent(under1);
        under1.addAdjacent(water1);

        air3.addAdjacent(air2);
        air3.addAdjacent(ground4);
        air3.addAdjacent(water2);
        ground4.addAdjacent(ground3);
        ground4.addAdjacent(air3);
        ground4.addAdjacent(water2);
        water2.addAdjacent(air3);
        water2.addAdjacent(ground4);
        water2.addAdjacent(water1);

        //Place vehicles
        water1.addVehicle(wVehicle1);
        water1.addVehicle(wVehicle2);
        water1.addVehicle(uwVehicle3);
        under1.addVehicle(uwVehicle1);
        under1.addVehicle(uwVehicle2);

        //Gen uw conns
        wVehicle1.addToUWConnection(uwVehicle1);
        wVehicle1.addToUWConnection(uwVehicle2);
        uwVehicle1.addToUWConnection(wVehicle1);
        uwVehicle2.addToUWConnection(wVehicle1);

        //Create Graph
        List<Area> areas = new ArrayList<>();
        areas.add(area1);
        areas.add(area2);
        areas.add(area3);
        Graph graph = new Graph(areas);

        BigraphMaker mk = new BigraphMaker(graph);

        Bigraph bigraph = mk.makeBigraph();

        System.out.println(ANSI_YELLOW + "Vehicles in water1:" + ANSI_RESET);
        for (Vehicle v : water1.getVehicles()){
            System.out.println(v);
        }

        System.out.println(ANSI_YELLOW + "Vehicles in nodeMapList:" + ANSI_RESET);
        for (NodeMap n : mk.getNodeMapList()){
            if (n.getEntity() instanceof Vehicle){
                System.out.println(n.getNode() + " " + n.getEntity());
            }
        }

        System.out.println(ANSI_YELLOW + "Vehicles in uw conn with wVehicle1:" + ANSI_RESET);
        for (Vehicle v : wVehicle1.getUwConnection()){
            System.out.println(v);
        }
        System.out.println(bigraph);
    }
}
