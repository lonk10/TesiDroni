import it.uniud.mads.jlibbig.core.std.*;

public class NodeMap {
    private Object entity;
    private Node node;

    public NodeMap(){
        this.entity = null;
        this.node = null;
    }

    public NodeMap(Object o, Node n){
        this.entity = o;
        this.node = n;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
