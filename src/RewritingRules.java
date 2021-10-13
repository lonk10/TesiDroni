import it.uniud.mads.jlibbig.core.std.*;

public class RewritingRules {
    Signature signature;

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

    private RewritingRule moveAirVehicleAirToAir(){
        //Redex construction
        BigraphBuilder redexBuilder = new BigraphBuilder(this.signature);
        Root redexRoot = redexBuilder.addRoot();
        OuterName redexOut1 = redexBuilder.addOuterName();
        OuterName redexOut2 = redexBuilder.addOuterName();
        Node redexAir1 = redexBuilder.addNode("Air", redexRoot, redexOut1);
        Node outputRedexAir1 = redexBuilder.addNode("Output", redexAir1);
        redexBuilder.addNode("AirVehicle", redexAir1); //add vehicle
        redexBuilder.addSite(outputRedexAir1); //add site
        Node redexAir2 = redexBuilder.addNode("Air", redexRoot);
        Node outputRedexAir2 = redexBuilder.addNode("Output", redexAir2, redexOut2);
        redexBuilder.addSite(outputRedexAir2); //add site
        redexBuilder.relink(outputRedexAir1.getPort(0), redexAir2.getPort(0)); //link sections
        redexBuilder.relink(outputRedexAir2.getPort(0), redexAir1.getPort(0)); // ^

        Bigraph redex = redexBuilder.makeBigraph();

        //Reactum construction
        BigraphBuilder reactumBuilder = new BigraphBuilder(this.signature);
        Root reactumRoot = reactumBuilder.addRoot();

        Bigraph reactum = reactumBuilder.makeBigraph();

        return null;
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
