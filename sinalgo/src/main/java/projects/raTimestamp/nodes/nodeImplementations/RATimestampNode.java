package projects.raTimestamp.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.raTimestamp.nodes.messages.RATimestampMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.GraphPanel;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

import java.awt.*;

@Getter
@Setter
public class RATimestampNode extends LamportTimestampNode {

    Logging log = Logging.getLogger();

    /**
     * the neighbor with the smallest ID
     */
    private RATimestampNode next;

    @Getter
    private boolean waiting = false;
    
    private long token = 0L;
    private long validationToken = 0L;

    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox)
    {
        super.handleMessages(inbox);
        // RA Timestamp
        if (inbox.hasNext())
        {
            Message msg = inbox.next();
            if(msg instanceof RATimestampMessage)
            {
                RATimestampMessage m = (RATimestampMessage) msg;
                // Lamport TS Update
                this.setLamportTimestamp(Math.max(m.getTimestamp(), this.getLamportTimestamp()) + 1);
            }
        }
    }
    @Override
    public void preStep() {
    }

    @Override
    public void init() {
    }

    @Override
    public void neighborhoodChange() {
        this.setNext(null);
        for (Edge e : this.getOutgoingConnections()) {
            if (this.getNext() == null) {
                this.setNext((RATimestampNode) e.getEndNode());
            } else {
                if (e.getEndNode().compareTo(this.getNext()) < 0) {
                    this.setNext((RATimestampNode) e.getEndNode());
                }
            }
        }
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

    @NodePopupMethod(menuText="[App] Enter Region")
    public void appEnterRegion() {
    }

    @NodePopupMethod(menuText="[App] Exit Region")
    public void appExitRegion() {
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.draw(g, pt, highlight);
    }

}
