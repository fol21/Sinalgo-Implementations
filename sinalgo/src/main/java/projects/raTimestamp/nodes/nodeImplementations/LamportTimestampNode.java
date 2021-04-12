package projects.raTimestamp.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.raTimestamp.nodes.messages.LamportTimestampMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import java.awt.*;

@Getter
@Setter
public class LamportTimestampNode extends Node {
    Logging log = Logging.getLogger();

    /**
     * the neighbor with the smallest ID
     */
    private LamportTimestampNode next;

    private long lamportTimestamp = 0;

    @Override
    public void handleMessages(Inbox inbox)
    {
        if (inbox.hasNext())
        {
            Message msg = inbox.next();
            if(msg instanceof LamportTimestampMessage)
            {
                LamportTimestampMessage m = (LamportTimestampMessage) msg;
                this.lamportTimestamp = Math.max(m.getTimestamp(), this.lamportTimestamp) + 1;
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
                this.setNext((LamportTimestampNode) e.getEndNode());
            } else {
                if (e.getEndNode().compareTo(this.getNext()) < 0) {
                    this.setNext((LamportTimestampNode) e.getEndNode());
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

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawNodeAsSquareWithText(g, pt, highlight, Long.toString(this.getID()) + '(' + Long.toString(this.lamportTimestamp) + ')', 16, Color.WHITE);
    }

    // Actions
    @NodePopupMethod(menuText="Send Tick TS")
    public void sendTick() {
        Tools.getNodeSelectedByUser(n -> {
            if (n == null) {
                return; // the user aborted
            }
            LamportTimestampMessage msg = new LamportTimestampMessage(this.tick());
            if (Tools.isSimulationInAsynchroneMode()) {
                this.sendDirect(msg, n);
            } else {
                // we need to set a timer, such that the message is
                // sent during the next round, when this node performs its step.
                MessageTimer timer = new MessageTimer(msg, n);
                timer.startRelative(1.0, this);
            }
        }, "Select a node to which you want to send a tick Lamport Timestamp Message.");
    }
    
    @NodePopupMethod(menuText="Multicast Tick TS")
    public void multicastTick() {
        LamportTimestampMessage msg = new LamportTimestampMessage(this.tick());
        if (Tools.isSimulationInAsynchroneMode()) {
            this.broadcast(msg);
        } else {
            // we need to set a timer, such that the message is
            // sent during the next round, when this node performs its step.
            MessageTimer timer = new MessageTimer(msg);
            timer.startRelative(1.0, this);
        }
    }

    private long tick()
    {
        return ++this.lamportTimestamp;
    }
}
