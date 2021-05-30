package projects.blockchain.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;

@Getter
@Setter
public class BlockchainNode extends Node {
    
    @Override
    public void handleMessages(Inbox inbox) {
    }

    @Override
    public void preStep() {
    }

    @Override
    public void init() {
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void postStep() {
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Node(" + this.getID() + ") [");
        for (Edge e : this.getOutgoingConnections()) {
            Node n = e.getEndNode();
            s.append(n.getID()).append(" ");
        }
        return s + "]";
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }
}
