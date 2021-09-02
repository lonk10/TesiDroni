import exceptions.IncompatibleSectionType;
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

                System.out.println("Sec name: " + name);

                //parse cardinals
                //TODO continue implementing cardinal points
                sList = secElement.getElementsByTagName("CARDINAL");
                for (int i = 0; i < sList.getLength(); i++){
                    System.out.println("Card name: " + sList.item(i).getTextContent());
                    aList.add(sList.item(i).getTextContent());
                }
                //parse adjacents
                sList = secElement.getElementsByTagName("ADJACENT");
                for (int i = 0; i < sList.getLength(); i++){
                    System.out.println("Adj name: " + sList.item(i).getTextContent());
                    aList.add(sList.item(i).getTextContent());
                }

                adjList.add(aList);
                sectionList.add(sf.getSection(type, name));
                System.out.println(" ");

            }

        }
        //add section info
        int secCount = 0;
        for (Section sec : sectionList){
            //Set area
            setArea(sec, areaList, areaIDList.get(secCount));
            //Set adjacents
            setAdjacents(sec, sectionList, adjList.get(secCount));

            secCount++;
        }

        return sectionList;
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
        System.out.println("Sec: " + sec.getId());
        for (String name : nameList){
            System.out.println("Name: " + name);
            for (Section s : sectionList){
                if (s.getId().equalsIgnoreCase(name)){
                    sec.addAdjacent(s);
                    break;
                }
            }
        }
        System.out.println(" ");
    }
}
