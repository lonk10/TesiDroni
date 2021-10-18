import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicatingProperty;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;

public class PropertyMatcher extends Matcher {
    @Override
    protected boolean areMatchable(Bigraph agent, Node fromAgent, Bigraph redex, Node fromRedex){
        if (fromAgent.getControl() != fromRedex.getControl()){
            return false;
        }
        if (fromAgent.getProperties().size() != fromRedex.getProperties().size()){
            return false;
        }
        Property<Object> agentProp = fromAgent.getProperty("ID");
        Property<Object> redexProp = fromRedex.getProperty("ID");

        return agentProp.equals(redexProp);
    }
}
