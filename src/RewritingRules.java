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
        Root redexRoot = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName("out1"); // outer names
        OuterName redexOut2 = redexBuilder.addOuterName("out2"); //
        Node redexSourceSection = redexBuilder.addNode(sourceSectionType, redexRoot);
        redexSourceSection.attachProperty(sourceProp);
        Node outputredexSourceSection = redexBuilder.addNode("Output", redexSourceSection);
        //redexSourceSection.attachProperty(sourceProperty);
        redexBuilder.addSite(redexSourceSection); //add site
        Node redexDestSection = redexBuilder.addNode(destSectionType, redexRoot);
        redexDestSection.attachProperty(destProp);
        Node redexVec = redexBuilder.addNode(vehicleType, redexSourceSection);
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
        Root reactumRoot = reactumBuilder.addRoot();
        OuterName reactumOut1 = reactumBuilder.addOuterName("out1");
        OuterName reactumOut2 = reactumBuilder.addOuterName("out2");
        Node reactumSourceSection = reactumBuilder.addNode(sourceSectionType, reactumRoot);
        reactumSourceSection.attachProperty(sourceProp);
        Node outputreactumSourceSection = reactumBuilder.addNode("Output", reactumSourceSection);
        reactumBuilder.addSite(reactumSourceSection); //add site
        Node reactumDestSection = reactumBuilder.addNode(destSectionType, reactumRoot);
        reactumDestSection.attachProperty(destProp);
        Node reactumVec = reactumBuilder.addNode(vehicleType, reactumDestSection);
        reactumVec.attachProperty(vecProp);
        Node outputreactumDestSection = reactumBuilder.addNode("Output", reactumDestSection);
        reactumBuilder.addSite(reactumDestSection); //add site
        reactumBuilder.relink(reactumOut1, outputreactumSourceSection.getPort(0), reactumDestSection.getPort(0)); //link sections
        reactumBuilder.relink(reactumOut2, outputreactumDestSection.getPort(0), reactumSourceSection.getPort(0)); // ^

        Bigraph reactum = reactumBuilder.makeBigraph();
        int[] map = {0, 1};
        RewritingRule rr = new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), map));
        return rr;
    }

    RewritingRule addNewVehicle(String vehicleType, String vecID, String sectionType, Property<Object> secProp){
        //Redex construction
        BigraphBuilder bigBuilder = new BigraphBuilder(this.signature);
        Root redexRoot = bigBuilder.addRoot();
        OuterName redexOut = bigBuilder.addOuterName("out");
        Node redexSection = bigBuilder.addNode(sectionType, redexRoot);
        redexSection.attachProperty(secProp);
        //Node outputredexSourceSection = bigBuilder.addNode("Output", redexSection);
        bigBuilder.addSite(redexSection); //add site
        bigBuilder.relink(redexOut, redexSection.getPort(0)); //link section to outer interface
        Bigraph redex = bigBuilder.makeBigraph();

        //Reactum construction from redex's
        Node vec = bigBuilder.addNode(vehicleType, redexSection);
        vec.attachProperty(new ReplicatingProperty<>("ID", vecID));
        Bigraph reactum = bigBuilder.makeBigraph();

        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), 0));
    }

    RewritingRule unlinkSections(String fstSection, String sndSection){
        //Reactum construction
        BigraphBuilder bigBuilder = new BigraphBuilder(this.signature);
        Root root = bigBuilder.addRoot();
        OuterName outer1 = bigBuilder.addOuterName("out1");
        OuterName outer2 = bigBuilder.addOuterName("out2");
        Node section1 = bigBuilder.addNode(fstSection, root);
        Node section2 = bigBuilder.addNode(sndSection, root);
        bigBuilder.relink(outer1, section1.getPort(0));
        bigBuilder.relink(outer2, section2.getPort(0));
        Bigraph reactum = bigBuilder.makeBigraph();

        //Redex construction
        bigBuilder.unlink(section1.getPort(0));
        bigBuilder.unlink(section2.getPort(0));
        Node output1 = bigBuilder.addNode("Output", section1);
        Node output2 = bigBuilder.addNode("Output", section2);
        bigBuilder.relink(outer1, section1.getPort(0), output2.getPort(0));
        bigBuilder.relink(outer2, section2.getPort(0), output1.getPort(0));
        Bigraph redex = bigBuilder.makeBigraph();

        return new RewritingRule(this.matcher, redex, reactum, new InstantiationMap(redex.getSites().size(), 0));
    }
}
