package projects.raTimestamp.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.raTimestamp.nodes.app.ApplicationEvent;
import projects.raTimestamp.nodes.messages.RATimestampReplyMessage;
import projects.raTimestamp.nodes.messages.RATimestampRequestMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.storage.ReusableListIterator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    @Getter
    @Setter
    private long requestTimestamp = -1;

    private ApplicationEvent appEvent = ApplicationEvent.OFF;

    private int pending = -1;

    private List<Long> replyDeferred = new ArrayList<Long>();
    

    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox)
    {
        // RA Timestamp
        if (inbox.hasNext())
        {
            Message msg = inbox.next();
            if(msg instanceof RATimestampReplyMessage)
            {
                RATimestampReplyMessage m = (RATimestampReplyMessage) msg;
                this.log.logln("Node: " + this.getID() + " " + "recv(REPLY, " + m.getTimestamp() + ")");
                // Lamport TS Update
                this.setLamportTimestamp(Math.max(m.getTimestamp(), this.getLamportTimestamp()) + 1);
                
                this.pending--;
                if(this.pending == 0)
                {
                    this.appEvent = ApplicationEvent.IN;
                    this.setColor(Color.RED);
                }
            }
            if(msg instanceof RATimestampRequestMessage)
            {
                RATimestampRequestMessage m = (RATimestampRequestMessage) msg;
                this.log.logln("Node: " + this.getID() + " " + "recv(" + m.getNodeId() + ", REQ, " + m.getTimestamp() + ")");
                // Lamport TS Update
                this.setLamportTimestamp(Math.max(m.getTimestamp(), this.getLamportTimestamp()) + 1);

                if(this.appEvent == ApplicationEvent.OFF || requestTimestamp > m.getTimestamp())
                {
                    this.send(new RATimestampReplyMessage(this.getLamportTimestamp()), this.findEndNodeById(m.getNodeId()));
                }
                else {
                    this.replyDeferred.add(m.getNodeId());
                    this.log.logln("Replies to Defer:"  + this.replyDeferred.toString());
                }
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
        if(this.appEvent == ApplicationEvent.OFF)
        {
            this.requestTimestamp = this.getLamportTimestamp();
            this.appEvent = ApplicationEvent.HOLD;
            this.pending = this.getOutgoingConnections().size();
            this.broadcast(new RATimestampRequestMessage(this.getID(), (this.requestTimestamp)));
        } else
        {
            this.log.logln("Already In Application or in error.");
        }
    }

    @NodePopupMethod(menuText="[App] Exit Region")
    public void appExitRegion() {
        try {
            if(this.appEvent == ApplicationEvent.IN)
            {
                this.appEvent = ApplicationEvent.OFF;
                this.pending = -1;
                this.setColor(Color.BLACK);
                if(this.replyDeferred.size() > 0)
                {
                    this.send(new RATimestampReplyMessage(this.getLamportTimestamp()), this.findEndNodeById(this.replyDeferred.get(0)));
                    this.replyDeferred.remove(0);
                }

            } else {
                this.log.logln("Already Off Application or in error.");
            }
        } catch (Exception e) {
            this.log.logln(e.getMessage());

        }
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.draw(g, pt, highlight);
    }

    private Node findEndNodeById(long ID)
    {
        ReusableListIterator<Edge> it = this.getOutgoingConnections().iterator();
        while (it.hasNext())
        {
            Node n = it.next().getEndNode();
            if(n.getID() == ID) {
                return n;
            }
        }
        return null;
    }

}