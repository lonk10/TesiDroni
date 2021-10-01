import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import it.uniud.mads.jlibbig.core.std.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BigraphMaker {

    private Graph graph;
    private BigraphBuilder builder;
    private List<NodeMap> nodeMapList;
    private Bigraph bigraph;

    public BigraphMaker(Graph g){
        this.graph = g;
        this.builder = null;
        this.nodeMapList = new ArrayList<>();
    }

    /**
     * Generates the signature for our bigraph
     * @return a signature
     */
    public Signature makeSignature(){
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        signatureBuilder.add(new Control("Area", true, 0));
        signatureBuilder.add(new Control("Air", true, 6));
        signatureBuilder.add(new Control("Ground", true, 6));
        signatureBuilder.add(new Control("Water", true, 7));
        signatureBuilder.add(new Control("Underwater", true, 5));
        signatureBuilder.add(new Control("ControlStation", true, 1));
        signatureBuilder.add(new Control("AirVehicle", true, 2));
        signatureBuilder.add(new Control("GroundVehicle", true, 2));
        signatureBuilder.add(new Control("UnderwaterVehicle", true, 3));
        signatureBuilder.add(new Control("WaterVehicle", true, 3));
        signatureBuilder.add(new Control("StaticWaterVehicle", true, 3));

        return signatureBuilder.makeSignature();
    }

    /**
     * Generates node for a generic Vehicle v
     * @param v Vehicle to generate
     * @param p parent of the node
     * @return the node generated
     */

    public Node generateVehicleNode(Vehicle v, Node p){
        Node vehicle;
        switch (v.getType()){
            case "air":
                vehicle = builder.addNode("AirVehicle", p);
                break;
            case "ground":
                vehicle = builder.addNode("GroundVehicle", p);
                break;
            case "water":
                vehicle = builder.addNode("WaterVehicle", p);
                break;
            case "underwater":
                vehicle = builder.addNode("UnderwaterVehicle", p);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getType());
        }

        this.nodeMapList.add(new NodeMap(v, vehicle));
        return vehicle;
    }

    /**
     * Generates node for a generic Section s and nodes for all the vehicles inside
     * @param s Section to generate
     * @param p parent of the node
     * @return the node generated
     */
    public Node generateSectionNode(Section s, Node p){
        Node section;
        if (s instanceof AirSection) {
            section = builder.addNode("Air", p);
        } else if (s instanceof GroundSection) {
            section = builder.addNode("Ground", p);
        } else if (s instanceof WaterSection) {
            section = builder.addNode("Water", p);
        } else if (s instanceof UnderwaterSection) {
            section = builder.addNode("Underwater", p);
        } else{
            throw new IllegalStateException("Unexpected value: " + s.getType());
        }
        //Generate vehicles inside section
        for (Vehicle v : s.getVehicles()){
            Node vehicle = generateVehicleNode(v, section);
        }

        if (s.getId().equalsIgnoreCase("Air 01")){
            System.out.println(section.toString());
        }

        this.nodeMapList.add(new NodeMap(s, section));
        return section;
    }

    public Node mapEntity(Object s){
        for (NodeMap nm : this.nodeMapList){
            if (s.equals(nm.getEntity())){
                return nm.getNode();
            }
        }
        return null;
    }
    public Point[] generateUWEdge(Vehicle v, List<Vehicle> vecList, int port){
        List<Point> pointlist = vecList
                .stream()
                .map(vec -> mapEntity(vec)
                        .getPort(port))
                .collect(Collectors.toList());

        Node n = mapEntity(v);
        pointlist.add(n.getPort(port));
        Point[] arr = new Point[pointlist.size()];
        arr = pointlist.toArray(arr);

        return arr;
    }

    public Point[] generateLinkArray(Node n, List<Section> sectionList, int sPort, int adjPort){
        List<Point> pointlist = sectionList
                .stream()
                .map(sec -> mapEntity(sec)
                        .getPort(adjPort))
                .collect(Collectors.toList());
        pointlist.add(n.getPort(sPort));
        Point[] arr = new Point[pointlist.size()];
        arr = pointlist.toArray(arr);
        return arr;
    }

    public void generateSectionLinks() throws IncompatibleSectionType {
        for (Section s : graph.getSections()){
            Node ns = mapEntity(s);
            Section north = s.getNorth();
            Section south = s.getSouth();
            Section east = s.getEast();
            Section west = s.getWest();
            if (north != null){
                this.builder.relink(ns.getPort(0), mapEntity(north).getPort(2));
            }
            if (east != null){
                this.builder.relink(ns.getPort(1), mapEntity(east).getPort(3));
            }
            if (south != null){
                this.builder.relink(ns.getPort(2), mapEntity(south).getPort(0));
            }
            if (west != null){
                this.builder.relink(ns.getPort(3), mapEntity(west).getPort(1));
            }
                //generate link s -- other type of section
            if (s instanceof AirSection) {
                if (!((AirSection) s).getGroundSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((AirSection) s).getGroundSections(), 4, 4));
                } if (!((AirSection) s).getWaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((AirSection) s).getWaterSections(), 5, 4));
                }
            } else if (s instanceof GroundSection) {
                if (!((GroundSection) s).getAirSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((GroundSection) s).getAirSections(), 4, 4));
                } if (!((GroundSection) s).getWaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((GroundSection) s).getWaterSections(), 5, 5));
                }
            } else if (s instanceof WaterSection) {
                if (!((WaterSection) s).getAirSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((WaterSection) s).getAirSections(), 4, 5));
                } if (!((WaterSection) s).getGroundSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((WaterSection) s).getGroundSections(), 5, 5));
                }
                if (!((WaterSection) s).getUnderwaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((WaterSection) s).getUnderwaterSections(), 6, 4));
                }
            } else if (s instanceof UnderwaterSection){
                if (!((UnderwaterSection) s).getWaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((UnderwaterSection) s).getWaterSections(), 4, 6));
                }
            }
        }
    }

    public void generateVehicleLinks(){
        for (Area a : graph.getAreas()){
            //generate local connections
            int localPort = 1;
            List <Point> pointlist = a.localConnection()
                                        .stream()
                                        .map(vec -> mapEntity(vec)
                                                .getPort(localPort))
                                        .collect(Collectors.toList());
            Point[] arr = new Point[pointlist.size()];
            arr = pointlist.toArray(arr);
            this.builder.relink(arr);

            //generate underwater connections
            int uwPort = 2;
            for (Section s : a.getWaterSections()){
                for (Vehicle v : s.getVehicles()){
                    if (v instanceof WaterVehicle) {
                        this.builder.relink(generateUWEdge(v, ((WaterVehicle) v).getUwConnection(), uwPort));
                    }
                }
            }
        }
        //TODO gcs links how?

    }

    public Bigraph makeBigraph() throws IncompatibleSectionType {
        //Generate BigraphBuilder with correct signature
        this.builder = new BigraphBuilder(makeSignature());
        //Generates nodes
        Root root = this.builder.addRoot();
        for (Area a : graph.getAreas()){
            //Generate areas
            Node area = builder.addNode("Area", root);
            this.nodeMapList.add(new NodeMap(a, area));
            //Generate sections
            for (Section sec : a.getSections()) {
                Node section = generateSectionNode(sec, area);
            }
        }
        
        generateSectionLinks();
        generateVehicleLinks();

        this.bigraph = this.builder.makeBigraph();
        return bigraph;
        //TODO generation of control station node
        //TODO gcs link generation
        //TODO check if mapping is ok
    }

    public List<NodeMap> getNodeMapList(){
        return this.nodeMapList;
    }

    public Section findSection(String name){
        for (NodeMap m : this.nodeMapList){
            if (m.getEntity() instanceof Section){
                Section sec = (Section) m.getEntity();
                if (sec.getId().equalsIgnoreCase(name)){
                    return sec;
                }
            }
        }
        return null;
    }

    public Vehicle findVehicle(String name){
        for (NodeMap m : this.nodeMapList){
            if (m.getEntity() instanceof Section){
                Vehicle vec = (Vehicle) m.getEntity();
                if (vec.getId().equalsIgnoreCase(name)){
                    return vec;
                }
            }
        }
        return null;
    }

    public void moveVehicle(String vehicle, String source, String destination) throws AdjacencyException, IncompatibleVehicleType {
        Vehicle vec = findVehicle(vehicle);
        Section sourceSec = findSection(source);
        Section destSec = findSection(destination);

        Node vecNode = mapEntity(vec);
        Node sourceNode = mapEntity(sourceSec);
        Node destNode = mapEntity(destSec);

        //graph.moveVehicle(vec, sourceSec, destSec);
        //TODO how to modify parent?????
        //either this or recreate the bigraph everytime
        //easier but seems less efficient, although complexity should stay the same
        //(EditableChild) vecNode.setParent();

    }
}
