import it.uniud.mads.jlibbig.core.std.*;

public class RewritingRules {
    Signature signature;

    public RewritingRules(){
        this.signature = this.makeSignature();
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

    public RewritingRule moveAirVehicleAirToAir(){

        //Reactum construction
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root redexRoot = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName("out1");
        OuterName redexOut2 = redexBuilder.addOuterName("out2");
        Node redexAir1 = redexBuilder.addNode("Air", redexRoot);
        Node outputRedexAir1 = redexBuilder.addNode("Output", redexAir1);
        redexBuilder.addSite(redexAir1); //add site
        Node redexAir2 = redexBuilder.addNode("Air", redexRoot);
        Node outputRedexAir2 = redexBuilder.addNode("Output", redexAir2);
        redexBuilder.addSite(redexAir2); //add site
        redexBuilder.relink(redexOut1, outputRedexAir1.getPort(0), redexAir2.getPort(0)); //link sections
        redexBuilder.relink(redexOut2, outputRedexAir2.getPort(0), redexAir1.getPort(0)); // ^
        //BigraphBuilder reactumBuilder = redexBuilder.clone()
        Bigraph redex = redexBuilder.makeBigraph();

        //Redex construction
        BigraphBuilder reactumBuilder = new BigraphBuilder(this.signature);
        Root reactumRoot = reactumBuilder.addRoot();
        OuterName reactumOut1 = reactumBuilder.addOuterName("out1");
        OuterName reactumOut2 = reactumBuilder.addOuterName("out2");
        Node reactumAir1 = reactumBuilder.addNode("Air", reactumRoot);
        Node outputReactumAir1 = reactumBuilder.addNode("Output", reactumAir1);
        reactumBuilder.addSite(reactumAir1); //add site
        Node reactumAir2 = reactumBuilder.addNode("Air", reactumRoot);
        Node outputReactumAir2 = reactumBuilder.addNode("Output", reactumAir2);
        reactumBuilder.addSite(reactumAir2); //add site
        reactumBuilder.relink(reactumOut1, outputReactumAir1.getPort(0), reactumAir2.getPort(0)); //link sections
        reactumBuilder.relink(reactumOut2, outputReactumAir2.getPort(0), reactumAir1.getPort(0)); // ^

        Bigraph reactum = reactumBuilder.makeBigraph();
        int[] map = {0, 1};
        RewritingRule rr = new RewritingRule(redex, reactum, new InstantiationMap(redex.getSites().size(), map));
        return rr;
    }

    private RewritingRule moveAirVehicleAirToGround(){
        return null;
    }

    private RewritingRule moveAirVehicleGroundToAir(){
        return null;
    }

    private RewritingRule moveAirVehicleAirToWater(){
        return null;
    }

    private RewritingRule moveAirVehicleWaterToAir(){
        return null;
    }
}
