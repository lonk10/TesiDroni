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

public class Parser {

    private static final String FILENAME = "src/map.xml";

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

    public List<Section> parseSections(Document doc, List<Area> areaList) throws IncompatibleSectionType {
        List<Section> sectionList = new ArrayList<>();

        SectionFactory sf = new SectionFactory();

        NodeList list = doc.getElementsByTagName("SECTION");
        NodeList sList;
        String name, areaID, type;
        List<String> areaIDList = new ArrayList<>();
        List<List<String>> adjList = new ArrayList<>();
        List<String[]> cardList = new ArrayList<>();

        //Parse for sections
        for (int secCounter = 0; secCounter < list.getLength(); secCounter++){
            Node secNode = list.item(secCounter);
            if (secNode.getNodeType() == Node.ELEMENT_NODE){
                List<String> aList = new ArrayList<>();
                String[] cList;
                cList = new String[4];
                Element secElement = (Element) secNode;

                name = secElement.getElementsByTagName("ID").item(0).getTextContent();
                areaID = secElement.getElementsByTagName("AREA").item(0).getTextContent();
                type = secElement.getElementsByTagName("TYPE").item(0).getTextContent();
                areaIDList.add(areaID);

                //parse cardinals
                sList = secElement.getElementsByTagName("NORTH");
                if (sList.getLength() == 1){
                    cList[0] = sList.item(0).getTextContent();
                }

                sList = secElement.getElementsByTagName("SOUTH");
                if (sList.getLength() == 1){
                    cList[1] = sList.item(0).getTextContent();
                }

                sList = secElement.getElementsByTagName("WEST");
                if (sList.getLength() == 1){;
                    cList[2] = sList.item(0).getTextContent();
                }

                sList = secElement.getElementsByTagName("EAST");
                if (sList.getLength() == 1){
                    cList[3] = sList.item(0).getTextContent();
                }
                //parse adjacents
                sList = secElement.getElementsByTagName("ADJACENT");
                for (int i = 0; i < sList.getLength(); i++){
                    aList.add(sList.item(i).getTextContent());
                }

                adjList.add(aList);
                cardList.add(cList);
                sectionList.add(sf.getSection(type, name));

            }

        }
        //add section info
        int secCount = 0;
        for (Section sec : sectionList){
            Section s;
            //Set area
            setArea(sec, areaList, areaIDList.get(secCount));

            //Set cardinals
            String[] cList = cardList.get(secCount);
            //Set north
            if (cList[0] != null) {
                s = findSection(sectionList, cList[0]);
                sec.addCardinal(s, "north");
            }
            //Set south
            if (cList[1] != null) {
                s = findSection(sectionList, cList[1]);
                sec.addCardinal(s, "south");
            }
            //Set west
            if (cList[2] != null) {
                s = findSection(sectionList, cList[2]);
                sec.addCardinal(s, "west");
            }
            //Set east
            if (cList[3] != null) {
                s = findSection(sectionList, cList[3]);
                sec.addCardinal(s, "east");
            }
            //Set adjacents
            setAdjacents(sec, sectionList, adjList.get(secCount));

            secCount++;
        }

        return sectionList;
    }

    public List<Vehicle> parseVehicles(Document doc, List<Section> sectionList) throws IncompatibleVehicleType {
        List<Vehicle> vehicleList = new ArrayList<>();

        VehicleFactory vf = new VehicleFactory();

        NodeList list = doc.getElementsByTagName("VEHICLE");
        NodeList sList;
        String name, sectionID, type;
        List<String> sectionIDList = new ArrayList<>();
        List<List<String>> adjList = new ArrayList<>();
        List<String[]> cardList = new ArrayList<>();

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

    public void setArea(Section sec, List<Area> areaList, String name){
        for (Area a : areaList){
            if (a.getId().equalsIgnoreCase(name)){
                sec.setArea(a);
                a.addSection(sec);
                break;
            }
        }
    }

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

    public void setSection(Vehicle vec, List<Section> sectionList, String name) throws IncompatibleVehicleType {
        for (Section s : sectionList){
            if (s.getId().equalsIgnoreCase(name)){
                vec.setSection(s);
                s.addVehicle(vec);
                break;
            }
        }
    }

    public Section findSection(List<Section> list, String name){
        for (Section s : list){
            if (s.getId().equalsIgnoreCase(name)){
                return s;
            }
        }
        return null;
    }
}
