import exceptions.AdjacencyException;
import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.std.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BigraphManager {

    private Graph graph;
    private BigraphBuilder builder;
    private List<NodeMap> nodeMapList;
    private List<Node> nodeList;
    private Bigraph bigraph;
    private RewritingRules rewritingRules;
    private Signature signature;

    public BigraphManager(Graph g){
        makeSignature();
        this.graph = g;
        this.builder = null;
        this.nodeList = new ArrayList<>();
        //this.nodeMapList = new ArrayList<>();
        this.rewritingRules = new RewritingRules(this.signature);
    }

    /**
     * Generates the signature for our bigraph
     * @return a signature
     */
    public void makeSignature(){
        SignatureBuilder signatureBuilder = new SignatureBuilder();
        signatureBuilder.add(new Control("Area", true, 0));
        signatureBuilder.add(new Control("Air", true, 1));
        signatureBuilder.add(new Control("Ground", true, 1));
        signatureBuilder.add(new Control("Water", true, 1));
        signatureBuilder.add(new Control("Underwater", true, 1));
        signatureBuilder.add(new Control("ControlStation", true, 1));
        signatureBuilder.add(new Control("AirVehicle", true, 2));
        signatureBuilder.add(new Control("GroundVehicle", true, 2));
        signatureBuilder.add(new Control("UnderwaterVehicle", true, 3));
        signatureBuilder.add(new Control("WaterVehicle", true, 3));
        signatureBuilder.add(new Control("StaticWaterVehicle", true, 3));
        signatureBuilder.add(new Control("Output", true, 1));

        this.signature = signatureBuilder.makeSignature();
    }

    public Signature getSignature(){ return this.signature;}

    /**
     * Generates node for a generic Vehicle v
     * @param v Vehicle to generate
     * @param p parent of the node
     * @return the node generated
     */

    private Node generateVehicleNode(Vehicle v, Node p){
        Node vehicle;
        switch (v.getType()){
            case "AirVehicle":
                vehicle = builder.addNode("AirVehicle", p);
                break;
            case "GroundVehicle":
                vehicle = builder.addNode("GroundVehicle", p);
                break;
            case "WaterVehicle":
                vehicle = builder.addNode("WaterVehicle", p);
                break;
            case "UnderwaterVehicle":
                vehicle = builder.addNode("UnderwaterVehicle", p);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getType());
        }

        vehicle.attachProperty(new ReplicatingProperty<String>("ID", v.getId()));
        this.nodeList.add(vehicle);
        //this.nodeMapList.add(new NodeMap(v, vehicle));
        return vehicle;
    }

    /**
     * Generates node for a generic Section s and nodes for all the vehicles inside
     * @param s Section to generate
     * @param p parent of the node
     * @return the node generated
     */
    private Node generateSectionNode(Section s, Node p){
        Node section;
        if (s.getType().equals("Air")) {
            section = builder.addNode("Air", p);
        } else if (s.getType().equals("Ground")) {
            section = builder.addNode("Ground", p);
        } else if (s.getType().equals("Water")) {
            section = builder.addNode("Water", p);
        } else if (s.getType().equals("Underwater")) {
            section = builder.addNode("Underwater", p);
        } else{
            throw new IllegalStateException("Unexpected value: " + s.getType());
        }
        //Generate vehicles inside section
        for (Vehicle v : s.getVehicles()){
            Node vehicle = generateVehicleNode(v, section);
        }

        section.attachProperty(new ReplicatingProperty<String>("ID", s.getId()));
        this.nodeList.add(section);
        //this.nodeMapList.add(new NodeMap(s, section));
        return section;
    }

    /**
     * Maps an entity s to its corresponding node inside the bigraph
     * @param s the object to map
     * @return the corresponding node
     */
    Node mapEntity(String s){
        for (Node m : this.nodeList) {
            if (m.getProperty("ID").get().equals(s)) {
                return m;
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
    private Point[] generateUWEdge(Vehicle v, List<Vehicle> vecList, int port){
        List<Point> pointlist = vecList
                .stream()
                .map(vec -> mapEntity(vec.getId())
                        .getPort(port))
                .collect(Collectors.toList());

        Node n = mapEntity(v.getId());
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
    private Point[] generateLinkArray(Node n, List<Section> sectionList, int sPort, int adjPort){
        List<Point> pointlist = sectionList
                .stream()
                .map(sec -> mapEntity(sec.getId())
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

    private void generateSectionLinks() throws IncompatibleSectionType {
        for (Section s : graph.getSections()){
            Node ns = mapEntity(s.getId());
            for (Section ss : s.getAdjacents()){
                Node output = this.builder.addNode("Output", ns);
                Handle h = mapEntity(ss.getId()).getPort(0).getHandle();
                List<Point> pointList = new ArrayList<>(h.getPoints());
                pointList.add(output.getPort(0));
                builder.relink(pointList);
            }
        }
    }

    /**
     * Generates the links between all the vehicles inside the same area, the links between the GCS and vehicles and the underwater links
     * @param a the area in which the connection is effective
     */
    private void generateLocalConn(Area a) {
        int localPort = 1;
        List<Point> pointlist = a.localConnection()
                .stream()
                .map(vec -> mapEntity(vec.getId())
                        .getPort(localPort))
                .collect(Collectors.toList());
        Point[] arr = new Point[pointlist.size()];
        arr = pointlist.toArray(arr);
        this.builder.relink(arr);
    }

    /**
     * Generates the underwater connection for a given area
     * @param a the area in which the connection is effective
     */
    private void generateUnderwaterConn(Area a){
        int uwPort = 2;
        for (Section s : a.getWaterSections()) {
            for (Vehicle v : s.getVehicles()) {
                if (v.getType().equals("WaterVehicle") && !(v.getId().contains("Unknown")) && !(v.getId().contains("Enemy"))) {
                    this.builder.relink(generateUWEdge(v, ((WaterVehicle) v).getUwConnection(), uwPort));
                }
            }
        }
    }

    /**
     * Generates the local and underwater connections for every area
     */
    private void generateVehicleLinks(){
        for (Area a : graph.getAreas()) {
            //generate local connections
            generateLocalConn(a);

            //generate underwater connections
            generateUnderwaterConn(a);
        }
    }

    /**
     * Generates the hyperarc between a GCS and its vehicles
     */
    private void generateCSLinks(){
        for (ControlStation cs : graph.getControlStations()){
            List<Point> pointlist = cs.getVehicles()
                    .stream()
                    .filter(sec -> !sec.getSection().getType().equals("Underwater"))
                    .map(sec -> mapEntity(sec.getId())
                            .getPort(0))
                    .collect(Collectors.toList());
            pointlist.add(mapEntity(cs.getID()).getPort(0));
            Point[] arr = new Point[pointlist.size()];
            arr = pointlist.toArray(arr);
            this.builder.relink(arr);
        }
    }

    /**
     * Generates the bigraph
     * @return the initialized bigraph
     * @throws IncompatibleSectionType if incompatible sections are linked together
     */
    public Bigraph makeBigraph() throws IncompatibleSectionType {
        //Generate BigraphBuilder with correct signature
        this.builder = new BigraphBuilder(this.signature);
        //Generates nodes
        Root root = this.builder.addRoot();
        for (Area a : graph.getAreas()){
            //Generate areas
            Node area = builder.addNode("Area", root);
            area.attachProperty(new ReplicatingProperty<String>("ID", a.getId()));
            //System.out.println(area.getProperty("ID").get());
            this.nodeList.add(area);
            //Generate sections
            for (Section sec : a.getSections()) {
                //System.out.println(sec.getId());
                Node section = generateSectionNode(sec, area);
            }
        }

        for (ControlStation cs : graph.getControlStations()){
            Node csn;
            if (cs.getSection() != null){
                csn = builder.addNode("ControlStation", mapEntity(cs.getSection().getId()));
            } else {
                csn = builder.addNode("ControlStation", root);
            }
            csn.attachProperty(new ReplicatingProperty<String>("ID", cs.getID()));
            this.nodeList.add(csn);
        }

        generateSectionLinks();
        generateVehicleLinks();
        generateCSLinks();

        this.bigraph = this.builder.makeBigraph();
        return bigraph;
    }

    /**
     * Finds a given section inside the entity-node mappings
     * @param name the name of the entity (e.g. "Ground 01")
     * @return the Section entity
     */

    public Section findSection(String name){
        for (Section sec : this.graph.getSections()){
            if (sec.getId().equalsIgnoreCase(name)){
                return sec;
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
        for (Vehicle vec : this.graph.getVehicles()){
            if (vec.getId().equalsIgnoreCase(name)) {
                return vec;
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

        Node vecNode = mapEntity(vec.getId());
        Node sourceNode = mapEntity(sourceSec.getId());
        Node destNode = mapEntity(destSec.getId());

        RewritingRule rr = this.rewritingRules.moveVehicleSectionToSection(vec.getType(), vecNode.getProperty("ID"),
                sourceSec.getType(), sourceNode.getProperty("ID"), destSec.getType(), destNode.getProperty("ID"));

        boolean rewSuccess = false;

        for (Bigraph mid : rr.apply(this.bigraph)) {
            this.graph.moveVehicle(vec, sourceSec, destSec);
            this.bigraph = mid;
            this.nodeList = bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList());
            System.out.println("Moved vehicle " + vehicle + " from section " + source + " to section " + destination + ".");
            rewSuccess = true;
            break;
        }

        //Connect vehicle to local connection of destination area
        List<Vehicle> areaVecs = destSec.getArea().localConnection();
        areaVecs.remove(vec);
        if (!areaVecs.isEmpty() && !destSec.getType().equals("Underwater") && rewSuccess) {
            Vehicle vecConn = areaVecs.get(0);
            Node vecConnNode = mapEntity(vecConn.getId());
            System.out.println("Attempting to connect the vehicle " + vecConn.getId() + "...");
            RewritingRule relink = this.rewritingRules.linkToLocalConn(vec.getType(), vecNode.getProperty("ID"), vecConn.getType(), vecConnNode.getProperty("ID"));
            for (Bigraph mid : relink.apply(this.bigraph)) {
                this.bigraph = mid;
                this.nodeList = bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList());
                System.out.println("Connected vehicle " + vehicle + " to local connection of vehicle " + vecConn.getId() + ".");
            }
        }
        //Generate new UW connection if vehicle is submerging
        if (destSec.getType().equals("Underwater") && rewSuccess){
            Vehicle vecConn = null;
            for (Vehicle v : areaVecs){
                if (v.getType().equals("WaterVehicle")){
                    vecConn = v;
                    System.out.println("Attempting to connect to the vehicle " + vecConn.getId() + "...");
                }
            }
            if (vecConn != null){
                Node vecConnNode = mapEntity(vecConn.getId());
                RewritingRule relink = this.rewritingRules.linktoUWConn(vecNode.getProperty("ID"), vecConnNode.getProperty("ID"));
                for (Bigraph mid : relink.apply(this.bigraph)) {
                    this.bigraph = mid;
                    this.nodeList = bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList());
                    System.out.println("Connected vehicle " + vehicle + " to uw connection of vehicle " + vecConn.getId() + ".");
                    break;
                }
            }
        }

        if (!rewSuccess){
            System.out.println("Could not move vehicle " + vehicle + " from section " + source + " to section " + destination + ".");
        }

    }

    /**
     * Moves a vehicle on a given path
     * @param vehicle the vehicle to move
     * @param sectionIDList ordered list of the IDs of the sections to move between
     * @throws AdjacencyException if two subsequent sections aren't adjacent
     * @throws IncompatibleVehicleType if the vehicle is moved inside an incompatible section
     */
    public void moveVehicleOnPath(String vehicle, List<String> sectionIDList) throws AdjacencyException, IncompatibleVehicleType {
        Iterator<String> tt = sectionIDList.iterator();
        String source = tt.next();
        String dest;
        while (tt.hasNext()){
            dest = tt.next();
            moveVehicle(vehicle, source, dest);
            source = dest;
        }
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
        Node secNode = mapEntity(decSec.getId());

        Vehicle decVehicle = new VehicleFactory().getVehicle(type, name);

        decSec.addVehicle(decVehicle);
        RewritingRule rule = this.rewritingRules.addNewVehicle(type, name, secNode.getControl().getName(), secNode.getProperty("ID"));

        //Bigraph mid;
        for (Bigraph mid : rule.apply(this.bigraph)) {
            this.bigraph = mid;
            this.nodeList = new ArrayList<>(bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList()));
            System.out.println("Added vehicle " + name + " to section " + section + ".");
        }

    }

    /**
     * Unlinks two linked sections
     * @param sectionOne
     * @param sectionTwo
     * @throws IncompatibleSectionType if the two sections are not compatible
     */

    public void unlinkSections(String sectionOne, String sectionTwo) throws IncompatibleSectionType {
        Section sec1 = findSection(sectionOne);
        Section sec2 = findSection(sectionTwo);

        Node nodeSec1 = mapEntity(sectionOne);
        Node nodeSec2 = mapEntity(sectionTwo);

        RewritingRule rule = this.rewritingRules.unlinkSections(nodeSec1.getControl().getName(), nodeSec1.getProperty("ID"),
                                                                nodeSec2.getControl().getName(), nodeSec2.getProperty("ID"));


        for (Bigraph value : rule.apply(this.bigraph)) {
            this.bigraph = value;
            this.nodeList = new ArrayList<>(bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList()));
            sec1.removeAdjacent(sec2);
            sec2.removeAdjacent(sec1);
            System.out.println("Unlinked sections " + sectionOne + " and " + sectionTwo + ".");
        }

    }

    /**
     * Links two linked sections
     * @param sectionOne
     * @param sectionTwo
     * @throws IncompatibleSectionType if the two sections are not compatible
     */

    public void linkSections(String sectionOne, String sectionTwo) throws IncompatibleSectionType, AdjacencyException {
        Section sec1 = findSection(sectionOne);
        Section sec2 = findSection(sectionTwo);

        if (!sec1.isAdjacentTo(sec2)) {
            Node nodeSec1 = mapEntity(sectionOne);
            Node nodeSec2 = mapEntity(sectionTwo);

            RewritingRule rule = this.rewritingRules.linkSections(nodeSec1.getControl().getName(), nodeSec1.getProperty("ID"),
                    nodeSec2.getControl().getName(), nodeSec2.getProperty("ID"));


            for (Bigraph value : rule.apply(this.bigraph)) {
                this.bigraph = value;
                this.nodeList = new ArrayList<>(bigraph.getNodes().stream().filter(n -> !n.getControl().getName().equals("Output")).collect(Collectors.toList()));
                sec1.removeAdjacent(sec2);
                sec2.removeAdjacent(sec1);
                System.out.println("Unlinked sections " + sectionOne + " and " + sectionTwo + ".");
            }
        } else {
            throw new AdjacencyException("Sections are already adjacent");
        }

    }

    /**
     * @return the current bigraph
     */
    public Bigraph getBigraph(){ return this.bigraph; }
}
