import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.std.*;

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

    RewritingRule moveVehicleSectionToSection(String vehicleType, Property<Object> vecProp, String sourceSectionType, Property<Object> sourceProp, String destSectionType, Property<Object> destProp){

        //ReplicatingProperty<String> sourceProperty = new ReplicatingProperty<String>("ID", sourceID);
        //ReplicatingProperty<String> destProperty = new ReplicatingProperty<String>("ID", destID);
        //ReplicatingProperty<String> vecProperty = new ReplicatingProperty<String>("ID", vehicleID);

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
        //redexSourceSection.attachProperty(sourceProperty);
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
        //redexVec.attachProperty(vecProperty);
        Node outputredexDestSection = redexBuilder.addNode("Output", redexDestSection);
        //redexDestSection.attachProperty(destProperty);
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

        Node reactumVec;
        if (vehicleType.equals("UnderwaterVehicle")){ // Generation of vehicle if underwater vehicle
            OuterName reactumVecOut3 = reactumBuilder.addOuterName("vecout3");
            if ((sourceSectionType.equals("Water") && destSectionType.equals("Underwater")) || (sourceSectionType.equals("Underwater") && destSectionType.equals("Water")) ){
                reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection);
                reactumBuilder.relink(reactumVecOut3, reactumVec.getPort(2));
            } else if (sourceSectionType.equals("Water") && destSectionType.equals("Water")){
                reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection, reactumVecOut1, reactumVecOut2, reactumVecOut3);
            } else { //Underwater -> Underwater
                reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection);
            }
        } else {
            reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection);
            reactumBuilder.relink(reactumVecOut1, reactumVec.getPort(0));
        }
        reactumVec.attachProperty(vecProp);

        Bigraph reactum = reactumBuilder.makeBigraph();
        int[] map = {0, 1};
        RewritingRule rr = new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
        return rr;
    }

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
}
