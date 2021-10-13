import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import gnu.trove.impl.sync.TSynchronizedShortByteMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for parsing an XML containing all the information about the starting bigraph
 * The file should be called "map.xml" but it can be renamed by modifying the FILENAME attribute.
 * To parse all the elements of the graph use parseMap(), parseAreas(), parseSections(), parseVehicles(), parseControlStations() in this order,
 * as the informations returned by previous functions is needed for the next.
 */

public class Parser {

    private static final String FILENAME = "src/map.xml";

    /**
     * Generates document for xml file
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document parseMap() throws ParserConfigurationException, SAXException, IOException {
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(FILENAME));

            doc.getDocumentElement().normalize();

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            return doc;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Parses the document for areas and starts building the initial abstract graph
     * @param doc the document to parse
     * @return a list of all the areas found
     */

    public List<Area> parseAreas(Document doc){
        List<Area> areaList = new ArrayList<>();

        NodeList list = doc.getElementsByTagName("GRIDAREA");
        String name;
        int latitude, longitude;

        for (int areaCounter = 0; areaCounter < list.getLength(); areaCounter++){
            Node areaNode = list.item(areaCounter);
            if (areaNode.getNodeType() == Node.ELEMENT_NODE) {
                Element areaElement = (Element) areaNode;

                name = areaElement.getElementsByTagName("ID").item(0).getTextContent();
                latitude = Integer.parseInt(areaElement.getElementsByTagName("LATITUDE").item(0).getTextContent());
                longitude = Integer.parseInt(areaElement.getElementsByTagName("LONGITUDE").item(0).getTextContent());
                areaList.add(new Area(name, latitude, longitude));
            }
        }

        return areaList;
    }

    /**
     * Parses a document for sections and adds them to the abstract graph
     * @param doc the document to parse
     * @param areaList a list of areas
     * @return a list with the sections found
     * @throws IncompatibleSectionType if two incompatible sections are found adjacent
     */
    public List<Section> parseSections(Document doc, List<Area> areaList) throws IncompatibleSectionType {
        List<Section> sectionList = new ArrayList<>();

        SectionFactory sf = new SectionFactory();

        NodeList list = doc.getElementsByTagName("SECTION");
        NodeList sList;
        String name, areaID, type;
        List<String> areaIDList = new ArrayList<>();
        List<List<String>> adjList = new ArrayList<>();

        //Parse for sections
        for (int secCounter = 0; secCounter < list.getLength(); secCounter++){
            Node secNode = list.item(secCounter);
            if (secNode.getNodeType() == Node.ELEMENT_NODE){
                List<String> aList = new ArrayList<>();
                Element secElement = (Element) secNode;

                name = secElement.getElementsByTagName("ID").item(0).getTextContent();
                areaID = secElement.getElementsByTagName("AREA").item(0).getTextContent();
                type = secElement.getElementsByTagName("TYPE").item(0).getTextContent();
                areaIDList.add(areaID);
                //parse adjacents
                sList = secElement.getElementsByTagName("ADJACENT");
                for (int i = 0; i < sList.getLength(); i++){
                    aList.add(sList.item(i).getTextContent());
                }

                adjList.add(aList);
                sectionList.add(sf.getSection(type, name));

            }

        }
        //add section info
        int secCount = 0;
        for (Section sec : sectionList){
            Section s;
            //Set area
            setArea(sec, areaList, areaIDList.get(secCount));
            //Set adjacents
            setAdjacents(sec, sectionList, adjList.get(secCount));

            secCount++;
        }

        return sectionList;
    }

    /**
     * Parses the document for vehicles and adds them to the abstract graph
     * @param doc the document to parse
     * @param sectionList the list of sections in the graph
     * @return a list of vehicles found
     * @throws IncompatibleVehicleType if a vehicle is found inside an incompatible section
     */
    public List<Vehicle> parseVehicles(Document doc, List<Section> sectionList) throws IncompatibleVehicleType {
        List<Vehicle> vehicleList = new ArrayList<>();

        VehicleFactory vf = new VehicleFactory();

        NodeList list = doc.getElementsByTagName("VEHICLE");
        NodeList sList;
        String name, sectionID, type;
        List<String> sectionIDList = new ArrayList<>();

        //Parse for sections
        for (int vecCounter = 0; vecCounter < list.getLength(); vecCounter++) {
            Node secNode = list.item(vecCounter);
            if (secNode.getNodeType() == Node.ELEMENT_NODE) {
                Element secElement = (Element) secNode;

                name = secElement.getElementsByTagName("ID").item(0).getTextContent();
                sectionID = secElement.getElementsByTagName("PARENTSECTION").item(0).getTextContent();
                type = secElement.getElementsByTagName("TYPE").item(0).getTextContent();
                sectionIDList.add(sectionID);
                Vehicle v = vf.getVehicle(type, name);
                setSection(v, sectionList, sectionID);
                vehicleList.add(v);
            }
        }
        return vehicleList;
    }

    /**
     * Parses the document for control stations and adds them to the graph
     * @param doc the document to parse
     * @param sectionList a list of sections inside the graph
     * @return the list of all the control stations found
     */
    public List<ControlStation> parseControlStations(Document doc, List<Section> sectionList){
        List<ControlStation> csList = new ArrayList<>();

        NodeList list = doc.getElementsByTagName("CONTROLSTATION");
        NodeList sList;
        String name, sectionID;
        List<String> sectionIDList = new ArrayList<>();

        //Parse for sections
        for (int csCounter = 0; csCounter < list.getLength(); csCounter++) {
            Node secNode = list.item(csCounter);
            if (secNode.getNodeType() == Node.ELEMENT_NODE) {
                Element secElement = (Element) secNode;

                name = secElement.getElementsByTagName("ID").item(0).getTextContent();
                sectionID = secElement.getElementsByTagName("PARENTSECTION").item(0).getTextContent();
                sectionIDList.add(sectionID);
                ControlStation cs = new ControlStation(name, findSection(sectionList, sectionID));
                csList.add(cs);
            }
        }
        return csList;
    }

    /**
     * Sets the area of a section
     * @param sec the section that will be added to the area
     * @param areaList the list of areas inside the graph
     * @param name the ID of the desired area
     */
    public void setArea(Section sec, List<Area> areaList, String name){
        for (Area a : areaList){
            if (a.getId().equalsIgnoreCase(name)){
                sec.setArea(a);
                a.addSection(sec);
                break;
            }
        }
    }

    /**
     * Sets the adjacent sections of a given section
     * @param sec the section
     * @param sectionList a list of all the sections inside the graph
     * @param nameList the IDs of all the sections adjacent to sec
     * @throws IncompatibleSectionType if two adjacents sections are incompatible
     */
    public void setAdjacents(Section sec, List<Section> sectionList, List<String> nameList) throws IncompatibleSectionType {
        for (String name : nameList){
            for (Section s : sectionList){
                if (s.getId().equalsIgnoreCase(name)){
                    sec.addAdjacent(s);
                    break;
                }
            }
        }
    }
    /**
     * Sets the sections of a vehicle
     * @param vec the vehicle that will be added to the section
     * @param sectionList the list of sections inside the graph
     * @param name the ID of the desired area
     */
    public void setSection(Vehicle vec, List<Section> sectionList, String name) throws IncompatibleVehicleType {
        for (Section s : sectionList){
            if (s.getId().equalsIgnoreCase(name)){
                vec.setSection(s);
                s.addVehicle(vec);
                break;
            }
        }
    }

    /**
     * Looks for a specific section inside a list
     * @param list the list in which to find the section
     * @param name the ID of the seciton
     * @return the desired section if found, null otherwise
     */
    public Section findSection(List<Section> list, String name){
        for (Section s : list){
            if (s.getId().equalsIgnoreCase(name)){
                return s;
            }
        }
        return null;
    }
}
