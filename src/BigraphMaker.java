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
        signatureBuilder.add(new Control("Air", true, 5));
        signatureBuilder.add(new Control("Ground", true, 5));
        signatureBuilder.add(new Control("Water", true, 6));
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

        this.nodeMapList.add(new NodeMap(s, section));
        return section;
    }

    /**
     * Maps an entity s to its corresponding node inside the bigraph
     * @param s the object to map
     * @return the corresponding node
     */
    public Node mapEntity(Object s){
        for (NodeMap nm : this.nodeMapList){
            if (s.equals(nm.getEntity())){
                return nm.getNode();
            }
        }
        return null;
    }

    /**
     * Generates the hyperedge for a underwater connection, which is a connection between water and underwater vehicles, where the water vehicles acts as intermediary between the GCS and uw vehicles.
     * @param v the water vehicle acting as intermediary
     * @param vecList a list of underwater vehicles to connect
     * @param port node port
     * @return an array of points for linking
     */
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

    /**
     * Generates the hyperedge for connecting a section to its adjacents
     * @param n the node for the section to connect
     * @param sectionList the list of the adjacents to connect
     * @param sPort port for the initial section
     * @param adjPort port for the list of sections
     * @return the hyperedge
     */
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

    /**
     * Generates all the links between section inside the bigraph during the initialization phase
     * @throws IncompatibleSectionType if two incompatible sections are linked together
     */

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
            if (s instanceof AirSection) {
                List<Section> glsList = new ArrayList<>();
                glsList.addAll(((AirSection) s).getGroundSections());
                glsList.addAll(((AirSection) s).getWaterSections());
                if (!glsList.isEmpty()){
                    this.builder.relink(generateLinkArray(ns, glsList, 4, 4));
                }

            } else if (s instanceof GroundSection) {
                /*
                if (!((GroundSection) s).getAirSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((GroundSection) s).getAirSections(), 4, 4));
                }
                */
            } else if (s instanceof WaterSection) {
                /*
                if (!((WaterSection) s).getAirSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((WaterSection) s).getAirSections(), 4, 5));
                }
                 */
                if (!((WaterSection) s).getUnderwaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((WaterSection) s).getUnderwaterSections(), 5, 4));
                }
            } else if (s instanceof UnderwaterSection){
                /*
                if (!((UnderwaterSection) s).getWaterSections().isEmpty()) {
                    this.builder.relink(generateLinkArray(ns, ((UnderwaterSection) s).getWaterSections(), 4, 5));
                }
                 */
            }
        }
    }

    /**
     * Generates the links between all the vehicles inside the same area, the links between the GCS and vehicles and the underwater links
     */

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

    /**
     * Generates the bigraph
     * @return the initialized bigraph
     * @throws IncompatibleSectionType if incompatible sections are linked together
     */
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

    /**
     * @return the list of entity-node mappings
     */

    public List<NodeMap> getNodeMapList(){
        return this.nodeMapList;
    }

    /**
     * Finds a given section inside the entity-node mappings
     * @param name the name of the entity (e.g. "Ground 01")
     * @return the Section entity
     */

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

    /**
     * Finds a given vehicle inside the entity-node mappings
     * @param name the name of the entity (e.g. "Ground 01")
     * @return the Vehicle entity
     */

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

    /**
     * Moves a vehicle from one section to another
     * @param vehicle the id of the vehicle to move
     * @param source the id of the source section
     * @param destination the id of the destination section
     * @throws AdjacencyException if the two section are not adjacent
     * @throws IncompatibleVehicleType if the vehicle is being moved in an incompatible section
     */

    public void moveVehicle(String vehicle, String source, String destination) throws AdjacencyException, IncompatibleVehicleType {
        Vehicle vec = findVehicle(vehicle);
        Section sourceSec = findSection(source);
        Section destSec = findSection(destination);

        Node vecNode = mapEntity(vec);
        Node sourceNode = mapEntity(sourceSec);
        Node destNode = mapEntity(destSec);

        //graph.moveVehicle(vec, sourceSec, destSec);
        //TODO how to modify parent????? use rewriting rules
        //either this or recreate the bigraph everytime
        //easier but seems less efficient, although complexity should stay the same

    }

    /**
     * Adds a new vehicle inside the vehicle after it has been detected by a patrol drone
     * @param type the type of the vehicle detected
     * @param name the id to give to the vehicle
     * @param section the section where the vehicles was detected in
     * @throws IncompatibleVehicleType if the vehicle is detected in an incompatible section (e.g. uw vehicle found in air section)
     */
    public void addDetectedVehicle(String type, String name, String section) throws IncompatibleVehicleType {
        Section decSec = findSection(section);

        Vehicle decVehicle = new VehicleFactory().getVehicle(type, name);

        decSec.addVehicle(decVehicle);

        Node vecNode = this.builder.addNode(type, mapEntity(decSec));
        this.nodeMapList.add(new NodeMap(decVehicle, vecNode));
    }
}
