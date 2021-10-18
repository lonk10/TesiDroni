import exceptions.IncompatibleSectionType;
import exceptions.IncompatibleVehicleType;
import it.uniud.mads.jlibbig.core.attachedProperties.SimpleProperty;
import it.uniud.mads.jlibbig.core.std.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IncompatibleSectionType, IncompatibleVehicleType, ParserConfigurationException, IOException, SAXException {

        Test test = new Test();

        //test.testParsing();

        //test.testProperties();

        System.out.println(" TESTING MOVE VEC SEC 2 SEC REWRITING RULE \n");
        //test.testMoveVecSec2SecRewritingRule();

        System.out.println("\n \n \n TESTING ADD VEC TO SEC REWRITING RULE \n");
        test.testAddVecRewritingRule();

        System.out.println("\n \n \n TESTING RULES ON PARSED FILE \n");
       // test.testRules();

    }
}
