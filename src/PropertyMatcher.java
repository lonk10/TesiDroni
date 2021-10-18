import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;

public class PropertyMatcher extends Matcher {
    @Override
    protected boolean areMatchable(Bigraph agent, Node fromAgent, Bigraph redex, Node fromRedex){
        //System.out.println("Red: " + fromRedex.toString());
        //System.out.println("Agent: " + fromAgent.toString());
        if (fromAgent.getControl() != fromRedex.getControl()) {
            //System.out.println("Res: false");
            return false;
        }

        if (fromAgent.getProperties().size() != fromRedex.getProperties().size()) {
            //System.out.println("Res: false");
            return false;
        }

        if (fromAgent.getControl().getName().equals("Output") || fromRedex.getControl().getName().equals("Output")) {
            //System.out.println("Res: true");
            return true;
        }

        Property<Object> agentProp = fromAgent.getProperty("ID");
        Property<Object> redexProp = fromRedex.getProperty("ID");

        //System.out.println("Res: " + agentProp.equals(redexProp));
        return agentProp.get().equals(redexProp.get());
    }
}
