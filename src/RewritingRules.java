import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.std.*;
import org.chocosolver.solver.expression.discrete.relational.BiReExpression;

public class RewritingRules {
    private Signature signature;
    private PropertyMatcher matcher;

    public RewritingRules(){
        //Signature init
        this.signature = this.makeSignature();
        this.matcher = new PropertyMatcher();
    }
    public RewritingRules(Signature sig){
        //Signature init
        this.signature = sig;
        this.matcher = new PropertyMatcher();
    }

    public Signature getSignature(){
        return this.signature;
    }

    /**
     * Generates the signature
     * @return the signature
     */

    private Signature makeSignature(){
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

        return signatureBuilder.makeSignature();
    }

    /**
     * General method per generating a rewriting rule that moves a vehicle of a determined type from a type A section to a type B section
     * @param vehicleType the vehicle's type
     * @param vecProp the ID property of the vehicle
     * @param sourceSectionType the source section's type
     * @param sourceProp the ID property of the source section
     * @param destSectionType the destination section's type
     * @param destProp the ID property of the destination section
     * @return the rewriting rule
     */
    RewritingRule moveVehicleSectionToSection(String vehicleType, Property<Object> vecProp, String sourceSectionType, Property<Object> sourceProp, String destSectionType, Property<Object> destProp){

        //redex construction
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root redexRoot1 = redexBuilder.addRoot();
        Root redexRoot2 = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName("out1"); // outer names
        OuterName redexOut2 = redexBuilder.addOuterName("out2"); //
        OuterName redexVecOut1 = redexBuilder.addOuterName("vecout1");
        OuterName redexVecOut2 = redexBuilder.addOuterName("vecout2");
        Node redexSourceSection = redexBuilder.addNode(sourceSectionType, redexRoot1);
        redexSourceSection.attachProperty(sourceProp);
        Node outputredexSourceSection = redexBuilder.addNode("Output", redexSourceSection);
        redexBuilder.addSite(redexSourceSection); //add site
        Node redexDestSection = redexBuilder.addNode(destSectionType, redexRoot2);
        redexDestSection.attachProperty(destProp);
        Node redexVec;
        if (vehicleType.equals("UnderwaterVehicle") || vehicleType.equals("WaterVehicle")) {
            OuterName redexVecOut3 = redexBuilder.addOuterName("vecout3");
            redexVec = redexBuilder.addNode(vehicleType, redexSourceSection, redexVecOut1, redexVecOut2, redexVecOut3);
        } else {
            redexVec = redexBuilder.addNode(vehicleType, redexSourceSection, redexVecOut1, redexVecOut2);
        }
        redexVec.attachProperty(vecProp);
        Node outputredexDestSection = redexBuilder.addNode("Output", redexDestSection);
        redexBuilder.addSite(redexDestSection); //add site
        redexBuilder.relink(redexOut1, outputredexSourceSection.getPort(0), redexDestSection.getPort(0)); //link sections
        redexBuilder.relink(redexOut2, outputredexDestSection.getPort(0), redexSourceSection.getPort(0)); // ^
        Bigraph redex = redexBuilder.makeBigraph();

        //Reactum construction
        BigraphBuilder reactumBuilder = new BigraphBuilder(this.signature);
        Root reactumRoot1 = reactumBuilder.addRoot();
        Root reactumRoot2 = reactumBuilder.addRoot();
        OuterName reactumOut1 = reactumBuilder.addOuterName("out1");
        OuterName reactumOut2 = reactumBuilder.addOuterName("out2");
        OuterName reactumVecOut1 = reactumBuilder.addOuterName("vecout1");
        OuterName reactumVecOut2 = reactumBuilder.addOuterName("vecout2");
        Node reactumSourceSection = reactumBuilder.addNode(sourceSectionType, reactumRoot1);
        reactumSourceSection.attachProperty(sourceProp);
        Node outputreactumSourceSection = reactumBuilder.addNode("Output", reactumSourceSection);
        reactumBuilder.addSite(reactumSourceSection); //add site
        Node reactumDestSection = reactumBuilder.addNode(destSectionType, reactumRoot2);
        reactumDestSection.attachProperty(destProp);
        Node outputreactumDestSection = reactumBuilder.addNode("Output", reactumDestSection);
        reactumBuilder.addSite(reactumDestSection); //add site
        reactumBuilder.relink(reactumOut1, outputreactumSourceSection.getPort(0), reactumDestSection.getPort(0)); //link sections
        reactumBuilder.relink(reactumOut2, outputreactumDestSection.getPort(0), reactumSourceSection.getPort(0)); // ^

        if (vehicleType.equals("UnderwaterVehicle") || vehicleType.equals("WaterVehicle")) {
            OuterName reactumVecOut3 = reactumBuilder.addOuterName("vecout3");
        }
        Node reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection, reactumVecOut1);
        reactumVec.attachProperty(vecProp);

        Bigraph reactum = reactumBuilder.makeBigraph();
        int[] map = {0, 1};
        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
    }

    /**
     * Generates a rewriting rule for connection a type A vehicle to the local connection of a vehicle B
     * @param vehicle1Type the type of the vehicle to connect
     * @param vec1Prop the ID property of the vehicle to connect
     * @param vehicle2Type the type of the vehicle to connect to
     * @param vec2Prop the ID property of the vehicle to connect to
     * @return the rewriting rule
     */
    RewritingRule linkToLocalConn(String vehicle1Type, Property<Object> vec1Prop, String vehicle2Type, Property<Object> vec2Prop){
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root redexRoot1 = redexBuilder.addRoot();
        Root redexRoot2 = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName("gcs");
        OuterName redexOut2 = redexBuilder.addOuterName("local1");
        OuterName redexOut3 = redexBuilder.addOuterName("gcs2");
        OuterName redexOut4 = redexBuilder.addOuterName("local2");
        Node vec1, vec2;
        if (vehicle1Type.equals("WaterVehicle") || vehicle1Type.equals("UnderwaterVehicle")){
            OuterName redexOutUW1 = redexBuilder.addOuterName("uw1");
            vec1 = redexBuilder.addNode(vehicle1Type, redexRoot1, redexOut1, redexOut2, redexOutUW1);
        } else {
            vec1 = redexBuilder.addNode(vehicle1Type, redexRoot1, redexOut1, redexOut2);
        }
        vec1.attachProperty(vec1Prop);
        if (vehicle2Type.equals("WaterVehicle") || vehicle2Type.equals("UnderwaterVehicle")){
            OuterName redexOutUW2 = redexBuilder.addOuterName("uw2");
            vec2 = redexBuilder.addNode(vehicle2Type, redexRoot2, redexOut3, redexOut4, redexOutUW2);
        } else {
            vec2 = redexBuilder.addNode(vehicle2Type, redexRoot2, redexOut3, redexOut4);
        }
        vec2.attachProperty(vec2Prop);
        Bigraph redex = redexBuilder.makeBigraph();

        redexBuilder.relink(redexOut4, vec1.getPort(1), vec2.getPort(1));
        Bigraph reactum = redexBuilder.makeBigraph();

        return new RewritingRule(this.matcher, redex, reactum);
    }

    /**
     * Generates the rewriting rule for connecting an underwater vehicle A to the uw connection of a water vehicle B
     * @param subProp the ID property of the vehicle to connect
     * @param boatProp the ID property of the vehicle to connect to
     * @return the rewriting rule
     */

    RewritingRule linktoUWConn(Property<Object> subProp, Property <Object> boatProp){
        BigraphBuilder bigBuilder = new BigraphBuilder(this.signature);
        Root root1 = bigBuilder.addRoot();
        Root root2 = bigBuilder.addRoot();
        OuterName gcsOut1 = bigBuilder.addOuterName("gcs1");
        OuterName localOut1 = bigBuilder.addOuterName("local1");
        OuterName uwOut1 = bigBuilder.addOuterName("uw1");
        OuterName uwOut2 = bigBuilder.addOuterName("uw2");
        OuterName gcsOut2 = bigBuilder.addOuterName("gcs2");
        OuterName localOut2 = bigBuilder.addOuterName("local2");

        Node sub = bigBuilder.addNode("UnderwaterVehicle", root2);
        Node boat = bigBuilder.addNode("WaterVehicle", root1, gcsOut1, localOut1);
        sub.attachProperty(subProp);
        boat.attachProperty(boatProp);
        bigBuilder.relink(uwOut1, sub.getPort(2), boat.getPort(2));
        Bigraph reactum = bigBuilder.makeBigraph();

        bigBuilder.relink(uwOut1, boat.getPort(2));
        bigBuilder.relink(gcsOut2, sub.getPort(0));
        bigBuilder.relink(localOut2, sub.getPort(1));
        bigBuilder.relink(uwOut2, sub.getPort(2));
        Bigraph redex = bigBuilder.makeBigraph();

        return new RewritingRule(this.matcher, redex, reactum);
    }


    /**
     * Rewriting rule for adding a new vehicle node inside a section node
     * @param vehicleType control for the new vehicle
     * @param vecID id of the new vehicle
     * @param sectionType control for the section
     * @param secProp ID property of the section
     * @return the rewriting rule
     */
    RewritingRule addNewVehicle(String vehicleType, String vecID, String sectionType, Property<Object> secProp){
        //Redex construction
        BigraphBuilder bigBuilder = new BigraphBuilder(this.signature);
        Root redexRoot = bigBuilder.addRoot(); //add root
        OuterName redexOut = bigBuilder.addOuterName("out"); //generate outer int
        Node redexSection = bigBuilder.addNode(sectionType, redexRoot, redexOut); //generate section, connected to outer int
        redexSection.attachProperty(secProp); // attach property
        bigBuilder.addSite(redexSection); // add site
        Bigraph redex = bigBuilder.makeBigraph();

        //Reactum construction from redex's
        Node vec = bigBuilder.addNode(vehicleType, redexSection); //add vehicle node
        vec.attachProperty(new ReplicatingProperty<>("ID", vecID)); //attach property
        Bigraph reactum = bigBuilder.makeBigraph();

        int[] map = {0};
        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
    }

    /**
     * General method for generating a rewriting rule to unlink a type A section and a type B section
     * @param fstSection the type of the first section
     * @param fstProp the ID property of the first section
     * @param sndSection the type of the second section
     * @param sndProp the ID property of the second section
     * @return the rewriting rule
     */
    RewritingRule unlinkSections(String fstSection, Property<Object> fstProp,  String sndSection, Property<Object> sndProp){
        //Reactum construction
        BigraphBuilder reactumBuilder = new BigraphBuilder(this.signature);
        Root root1 = reactumBuilder.addRoot();
        Root root2 = reactumBuilder.addRoot();
        OuterName outer1Reactum = reactumBuilder.addOuterName("out1"); //generate outer interfaces
        OuterName outer2Reactum = reactumBuilder.addOuterName("out2");
        Node section1Reactum = reactumBuilder.addNode(fstSection, root1); //generate nodes
        Node section2Reactum = reactumBuilder.addNode(sndSection, root2);
        reactumBuilder.addSite(section1Reactum); //add sites
        reactumBuilder.addSite(section2Reactum);
        section1Reactum.attachProperty(fstProp); //attach properties
        section2Reactum.attachProperty(sndProp);
        reactumBuilder.relink(outer1Reactum, section1Reactum.getPort(0)); //link to outer interfaces
        reactumBuilder.relink(outer2Reactum, section2Reactum.getPort(0));
        Bigraph reactum = reactumBuilder.makeBigraph();

        //Redex construction
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root redexRoot1 = redexBuilder.addRoot();
        Root redexRoot2 = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName("out1");
        OuterName redexOut2 = redexBuilder.addOuterName("out2");
        Node section1Redex = redexBuilder.addNode(fstSection, redexRoot1);
        Node section2Redex = redexBuilder.addNode(sndSection, redexRoot2);
        section1Redex.attachProperty(fstProp); //attach properties
        section2Redex.attachProperty(sndProp);
        redexBuilder.addSite(section1Redex); //add sites
        redexBuilder.addSite(section2Redex);
        Node output1 = redexBuilder.addNode("Output", section1Redex); //generate output nodes
        Node output2 = redexBuilder.addNode("Output", section2Redex);
        redexBuilder.relink(redexOut1, section1Redex.getPort(0), output2.getPort(0)); //generate links
        redexBuilder.relink(redexOut2, section2Redex.getPort(0), output1.getPort(0));
        Bigraph redex = redexBuilder.makeBigraph();

        int[] map = {0, 1};
        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
    }

    /**
     * General method for generating a rewriting rule to link a type A section and a type B section
     * @param fstSection the type of the first section
     * @param fstProp the ID property of the first section
     * @param sndSection the type of the second section
     * @param sndProp the ID property of the second section
     * @return the rewriting rule
     */

    RewritingRule linkSections(String fstSection, Property<Object> fstProp,  String sndSection, Property<Object> sndProp){
        //Reactum construction
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root root1 = redexBuilder.addRoot();
        Root root2 = redexBuilder.addRoot();
        OuterName outer1Redex = redexBuilder.addOuterName("out1"); //generate outer interfaces
        OuterName outer2Redex = redexBuilder.addOuterName("out2");
        Node section1Redex = redexBuilder.addNode(fstSection, root1); //generate nodes
        Node section2Redex = redexBuilder.addNode(sndSection, root2);
        redexBuilder.addSite(section1Redex); //add sites
        redexBuilder.addSite(section2Redex);
        section1Redex.attachProperty(fstProp); //attach properties
        section2Redex.attachProperty(sndProp);
        redexBuilder.relink(outer1Redex, section1Redex.getPort(0)); //link to outer interfaces
        redexBuilder.relink(outer2Redex, section2Redex.getPort(0));
        Bigraph redex = redexBuilder.makeBigraph();

        //Redex construction
        BigraphBuilder reactumBuilder = new BigraphBuilder(this.signature);
        Root reactumRoot1 = reactumBuilder.addRoot();
        Root reactumRoot2 = reactumBuilder.addRoot();
        OuterName reactumOut1 = reactumBuilder.addOuterName("out1");
        OuterName reactumOut2 = reactumBuilder.addOuterName("out2");
        Node section1Reactum = reactumBuilder.addNode(fstSection, reactumRoot1);
        Node section2Reactum = reactumBuilder.addNode(sndSection, reactumRoot2);
        section1Reactum.attachProperty(fstProp); //attach properties
        section2Reactum.attachProperty(sndProp);
        reactumBuilder.addSite(section1Reactum); //add sites
        reactumBuilder.addSite(section2Reactum);
        Node output1 = reactumBuilder.addNode("Output", section1Reactum); //generate output nodes
        Node output2 = reactumBuilder.addNode("Output", section2Reactum);
        reactumBuilder.relink(reactumOut1, section1Reactum.getPort(0), output2.getPort(0)); //generate links
        reactumBuilder.relink(reactumOut2, section2Reactum.getPort(0), output1.getPort(0));
        Bigraph reactum = reactumBuilder.makeBigraph();

        int[] map = {0, 1};
        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
    }
}
