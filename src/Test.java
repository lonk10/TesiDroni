import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.attachedProperties.SimpleProperty;
import it.uniud.mads.jlibbig.core.std.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Test {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Test for rewriting rules
     */
    public void testRewritingRule(){

        //signature
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        signatureBuilder.add(new Control("Area", true, 0));
        signatureBuilder.add(new Control("Air", true, 5));
        signatureBuilder.add(new Control("Ground", true, 5));
        signatureBuilder.add(new Control("Water", true, 6));
        signatureBuilder.add(new Control("Underwater", true, 5));
        signatureBuilder.add(new Control("GCS", true, 1));
        signatureBuilder.add(new Control("UAV", true, 2));
        signatureBuilder.add(new Control("UGV", true, 2));
        signatureBuilder.add(new Control("UUV", true, 3));
        signatureBuilder.add(new Control("Boat", true, 3));
        signatureBuilder.add(new Control("Float", true, 3));
        Signature signature = signatureBuilder.makeSignature();

        //build redex
        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        Node area = builder.addNode("Area", root);
        builder.addSite(area);
        OuterName outRedex1 = builder.addOuterName();
        OuterName outRedex2 = builder.addOuterName();
        Node ground1 = builder.addNode("Ground", area);
        ground1.attachProperty(new SimpleProperty<String>("Node name", "Ground 01"));
        Node ground2 = builder.addNode("Ground", area);
        ground2.attachProperty(new SimpleProperty<String>("Node name", "Ground 02"));
        builder.relink(ground1.getPort(0), ground2.getPort(0));
        builder.relink(outRedex1, ground1.getPort(1));
        builder.relink(outRedex2, ground2.getPort(1));

        Bigraph big1 = builder.makeBigraph();

        //build reactum
        builder.unlink(ground1.getPort(0));
        builder.unlink(ground2.getPort(0));

        Bigraph big2 = builder.makeBigraph();

        //build bigraph
        BigraphBuilder builder3 = new BigraphBuilder(signature);
        Root root3 = builder3.addRoot();
        Node area3 = builder3.addNode("Area", root3);
        Node air = builder3.addNode("Air", area3);
        Node ground13 = builder3.addNode("Ground", area3);
        ground13.attachProperty(new SimpleProperty<String>("Node name", "Ground 01"));
        Node ground23 = builder3.addNode("Ground", area3);
        ground23.attachProperty(new SimpleProperty<String>("Node name", "Ground 02"));
        builder3.relink(ground13.getPort(0), ground23.getPort(0));
        builder3.relink(ground13.getPort(1), air.getPort(0), ground23.getPort(1));

        Bigraph big3 = builder3.makeBigraph();


        //make expanded version of redex for testing
        //make rr
        InstantiationMap im = new InstantiationMap(1, 0);
        RewritingRule rr = new RewritingRule(big1, big2, im);
        System.out.println(ANSI_YELLOW + big1 + ANSI_RESET);
        System.out.println(big3);
        //Bigraph ff = rr.getMatcher().match(big3, big1).iterator().next().getRedex();
        System.out.println(ANSI_GREEN + big2 + ANSI_RESET);
        System.out.println(ANSI_CYAN + rr.apply(big3).iterator().next() + ANSI_RESET);
        //System.out.println(ff);
    }

    public void testMoveVecSec2SecRewritingRule(){
        RewritingRules rrs = new RewritingRules();
        BigraphBuilder builder = new BigraphBuilder(rrs.getSignature());
        Root root = builder.addRoot();
        Node air1 = builder.addNode("Air", root);
        air1.attachProperty(new ReplicatingProperty<>("ID", "Air 01"));
        Node outputAir1 = builder.addNode("Output", air1);
        Node ground = builder.addNode("Ground", root);
        ground.attachProperty(new ReplicatingProperty<>("ID", "Ground 01"));
        Node output1G = builder.addNode("Output", air1);
        Node output1GG = builder.addNode("Output", air1);
        Node outputA1 = builder.addNode("Output", ground);
        Node outputA2 = builder.addNode("Output", ground);
        Node gcs = builder.addNode("ControlStation", root);
        Node airvec = builder.addNode("AirVehicle", air1);
        Node airvec2 = builder.addNode("AirVehicle", air1);
        airvec.attachProperty(new ReplicatingProperty<>("ID", "UAV 01"));
        Node air2 = builder.addNode("Air", root);
        air2.attachProperty(new ReplicatingProperty<>("ID", "Air 02"));
        //System.out.println(air2.getPropertyNames());
        Node outputAir2 = builder.addNode("Output", air2);
        Node output2G = builder.addNode("Output", air2);
        builder.relink(outputA2.getPort(0), outputAir1.getPort(0), air2.getPort(0)); //link sections
        builder.relink(outputA1.getPort(0), outputAir2.getPort(0), air1.getPort(0)); // ^
        builder.relink(airvec.getPort(0), gcs.getPort(0));
        builder.relink(airvec.getPort(1), airvec2.getPort(1));

        Bigraph big = builder.makeBigraph();
        RewritingRule rr = rrs.moveVehicleSectionToSection("AirVehicle", airvec.getProperty("ID"), "Air", air1.getProperty("ID"),  "Air", air2.getProperty("ID"));
        System.out.println(big);
        Iterator<Bigraph> tt = rr.apply(big).iterator();
        Bigraph u = null;
        while (tt.hasNext()) {
            u = tt.next();
            System.out.println(ANSI_RED + u + ANSI_RESET);
        }
        //System.out.println(ANSI_GREEN + u.getNodes().contains(air1) + ANSI_RESET);
    }

    public void testAddVecRewritingRule(){
        RewritingRules rrs = new RewritingRules();
        BigraphBuilder builder = new BigraphBuilder(rrs.getSignature());
        //generate nodes
        Root root = builder.addRoot();
        Node air1 = builder.addNode("Air", root);
        Node air2 = builder.addNode("Air", root);
        Node air3 = builder.addNode("Air", root);
        //generate output nodes
        Node outputAir12 = builder.addNode("Output", air1);
        Node outputAir13 = builder.addNode("Output", air1);
        Node outputAir123 = builder.addNode("Output", air1);
        Node outputAir21 = builder.addNode("Output", air2);
        Node outputAir23 = builder.addNode("Output", air2);
        Node outputAir31 = builder.addNode("Output", air3);
        Node outputAir32 = builder.addNode("Output", air3);
        //attach properties
        air1.attachProperty(new ReplicatingProperty<>("ID", "Air 01"));
        air2.attachProperty(new ReplicatingProperty<>("ID", "Air 02"));
        air3.attachProperty(new ReplicatingProperty<>("ID", "Air 03"));
        //link sections
        builder.relink(air1.getPort(0), outputAir21.getPort(0), outputAir31.getPort(0));
        builder.relink(air2.getPort(0), outputAir12.getPort(0), outputAir32.getPort(0));
        builder.relink(air3.getPort(0), outputAir13.getPort(0), outputAir23.getPort(0));

        Bigraph big = builder.makeBigraph();


        System.out.println(big);
        //generate rule
        RewritingRule rr = rrs.addNewVehicle("AirVehicle", "Enemy UAV", "Air", air1.getProperty("ID"));

        System.out.println(rr.getRedex());
        System.out.println(rr.getRedex().getEdges());
        //apply rule
        Iterable<Bigraph> tt = rr.apply(big);
        System.out.println(tt.toString());

        Bigraph u = null;
        tt.iterator().next();
        while (tt.iterator().hasNext()) {
            u = tt.iterator().next();
            System.out.println(ANSI_RED + u + ANSI_RESET);
        }
    }

    public void testUnlinkSection(){
        RewritingRules rrs = new RewritingRules();
        BigraphBuilder bigBuilder = new BigraphBuilder(rrs.getSignature());

        Root root = bigBuilder.addRoot();
        Node section1 = bigBuilder.addNode("Air", root);
        Node section2 = bigBuilder.addNode("Air", root);
        Node section3 = bigBuilder.addNode("Ground", root);
        Node section4 = bigBuilder.addNode("Ground", root);

        section1.attachProperty(new ReplicatingProperty<>("ID", "Air 01"));
        section2.attachProperty(new ReplicatingProperty<>("ID", "Water 01"));
        section3.attachProperty(new ReplicatingProperty<>("ID", "Ground 01"));
        section4.attachProperty(new ReplicatingProperty<>("ID", "Ground 02"));

        Node output12 = bigBuilder.addNode("Output", section1);
        Node output21 = bigBuilder.addNode("Output", section2);
        Node output31 = bigBuilder.addNode("Output", section3);
        Node output42 = bigBuilder.addNode("Output", section4);
        Node output1 = bigBuilder.addNode("Output", section1);
        Node output2 = bigBuilder.addNode("Output", section2);
        Node uav = bigBuilder.addNode("AirVehicle", section1);
        Node uav1 = bigBuilder.addNode("GroundVehicle", section2);

        bigBuilder.relink(section1.getPort(0), output21.getPort(0), output31.getPort(0));
        bigBuilder.relink(section2.getPort(0), output12.getPort(0), output42.getPort(0));
        bigBuilder.relink(section3.getPort(0), output1.getPort(0));
        bigBuilder.relink(section4.getPort(0), output2.getPort(0));

        RewritingRule rr = rrs.unlinkSections("Air", section1.getProperty("ID"), "Air", section2.getProperty("ID"));

        Bigraph big = bigBuilder.makeBigraph();

        Bigraph u = null;
        Iterable<Bigraph> tt = rr.apply(big);

        System.out.println(rr.getRedex());
        System.out.println(big);

        while (tt.iterator().hasNext()) {
            u = tt.iterator().next();
            System.out.println(ANSI_RED + u + ANSI_RESET);
            break;
        }
    }

    public BigraphManager testParsing() throws ParserConfigurationException, IOException, SAXException, IncompatibleSectionType, IncompatibleVehicleType {
        Parser parser = new Parser();
        Document doc = parser.parseMap();
        List<Area> areaList = parser.parseAreas(doc);
        List<Section> secList = parser.parseSections(doc, areaList);
        parser.parseVehicles(doc, secList);

        Graph graphP = new Graph(areaList, parser.parseControlStations(doc, secList));
        //Since we use only a single gcs, we can set areas like this
        graphP.getControlStations().iterator().next().setAreas(graphP.getAreas());
        BigraphManager mkP = new BigraphManager(graphP);

        Bigraph bigraphP = mkP.makeBigraph();

        return mkP;
    }

    public void testDemo() throws ParserConfigurationException, IOException, SAXException, IncompatibleSectionType, IncompatibleVehicleType, AdjacencyException {
        Parser parser = new Parser();
        Document doc = parser.parseMap();
        List<Area> areaList = parser.parseAreas(doc);
        List<Section> secList = parser.parseSections(doc, areaList);
        parser.parseVehicles(doc, secList);
        Graph graphP = new Graph(areaList, parser.parseControlStations(doc, secList));
        graphP.getControlStations().iterator().next().setAreas(graphP.getAreas());

        BigraphManager manager = new BigraphManager(graphP);
        manager.makeBigraph();
        System.out.println(manager.getBigraph());
        manager.moveVehicle("UUV 01", "Water 01", "Underwater 01");
        System.out.println(ANSI_RED + manager.getBigraph() + ANSI_RESET);
        manager.moveVehicle("UUV 01", "Underwater 01", "Water 01");
        //manager.addDetectedVehicle("AirVehicle", "Enemy UAV", "Air 02");
        //System.out.println(ANSI_RED + manager.getBigraph() + ANSI_RESET);
        //manager.unlinkSections("Ground 02", "Ground 01");
        //manager.unlinkSections("Ground 02", "Ground 03");
        System.out.println(ANSI_BLUE + manager.getBigraph() + ANSI_RESET);
    }

    public void testRules() throws IncompatibleSectionType, ParserConfigurationException, IOException, IncompatibleVehicleType, SAXException {
        BigraphManager bmk = testParsing();

        bmk.addDetectedVehicle("AirVehicle", "Enemy UAV", "Air 01");

        System.out.println(ANSI_YELLOW + bmk.getBigraph() + ANSI_RESET);

        System.out.println(bmk.mapEntity("Air 01").getProperty("ID"));

        System.out.println(bmk.mapEntity("Enemy UAV").getProperty("ID") + " " + bmk.mapEntity("Enemy UAV"));
        System.out.println(bmk.mapEntity("Enemy UAV").getProperty("Owner"));

    }

    public void test1() throws IncompatibleSectionType, IncompatibleVehicleType {
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



        //Create Areas
        Area area1 = new Area("Area 01", 0,0);
        Area area2 = new Area("Area 02", 1, 0);
        Area area3 = new Area("Area 03", 1,1);
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

        BigraphManager mk = new BigraphManager(graph);

        Bigraph bigraph = mk.makeBigraph();

        System.out.println(ANSI_YELLOW + "Vehicles in water1:" + ANSI_RESET);
        for (Vehicle v : water1.getVehicles()){
            System.out.println(v);
        }
        /*
        System.out.println(ANSI_YELLOW + "Vehicles in nodeMapList:" + ANSI_RESET);
        for (NodeMap n : mk.getNodeMapList()){
            if (n.getEntity() instanceof Vehicle){
                System.out.println(n.getNode() + " " + n.getEntity());
            }
        }*/

        System.out.println(ANSI_YELLOW + "Vehicles in uw conn with wVehicle1:" + ANSI_RESET);
        for (Vehicle v : wVehicle1.getUwConnection()){
            System.out.println(v);
        }
        System.out.println(bigraph);
    }

    public void testProperties(){
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        signatureBuilder.add(new Control("Area", true, 0));
        signatureBuilder.add(new Control("Air", true, 5));
        signatureBuilder.add(new Control("Ground", true, 5));
        signatureBuilder.add(new Control("Water", true, 6));
        signatureBuilder.add(new Control("Underwater", true, 5));
        signatureBuilder.add(new Control("GCS", true, 1));
        signatureBuilder.add(new Control("UAV", true, 2));
        signatureBuilder.add(new Control("UGV", true, 2));
        signatureBuilder.add(new Control("UUV", true, 3));
        signatureBuilder.add(new Control("Boat", true, 3));
        signatureBuilder.add(new Control("Float", true, 3));
        Signature signature = signatureBuilder.makeSignature();

        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        Node m = builder.addNode("Area", root);
        m.attachProperty(new SimpleProperty<String>("ID", "Area 01"));
        System.out.println(m.getProperties());
        System.out.println(m.getProperty("ID").get().equals("Area 01"));
    }
}
