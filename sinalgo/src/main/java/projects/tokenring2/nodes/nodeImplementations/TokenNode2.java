package projects.tokenring2.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.tokenring2.nodes.app.ApplicationEvent;
import projects.tokenring2.nodes.app.DummyApplication;
import projects.tokenring2.nodes.messages.CriticalSessionMessage;
import projects.tokenring2.nodes.messages.TokenMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

import java.awt.*;


@Getter
@Setter
public class TokenNode2 extends Node {
    
    Logging log = Logging.getLogger();

    /**
     * the neighbor with the smallest ID
     */
    private TokenNode2 next;

    @Getter
    private boolean waiting = false;
    
    private DummyApplication app = new DummyApplication();

    private long token = 0L;
    private long validationToken = 0L;

    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox) {
        if (!isSending()) { // don't even look at incoming messages
            return;
        }
        if (inbox.hasNext()) {
            Message msg = inbox.next();
            if(msg instanceof CriticalSessionMessage)
            {
                CriticalSessionMessage m = (CriticalSessionMessage) msg;
                this.log.logln("[Region]Target ID: " + m.getNodeId() + ", Node Id: " + this.getID());
                if(m.getNodeId() == this.getID())
                {
                    this.log.logln("[Region] Found Id");
                    if(m.getEvent() == ApplicationEvent.ENTER_REGION)
                    {
                        this.app.exitRegion();
                        this.setColor(Color.BLACK);
                        this.broadcast(new TokenMessage(this.token, m.getNodeId()));
                    }
                } 
            }
            if (msg instanceof TokenMessage)
            {
                TokenMessage m = (TokenMessage) msg;
                this.log.logln("[Token] Target ID: " + m.getTargetId() + ", Node Id: " + this.getID());
                if(m.getTargetId() == this.getID())
                {
                    this.log.logln("[Token] Found Id");
                    if(this.waiting)
                    {
                        this.app.enterRegion();
                        this.waiting = false;
                        if(!m.isEvaluation()) {
                            this.token = m.getData();
                            this.setColor(Color.RED);
                        }
                    }
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
                this.setNext((TokenNode2) e.getEndNode());
            } else {
                if (e.getEndNode().compareTo(this.getNext()) < 0) {
                    this.setNext((TokenNode2) e.getEndNode());
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
        this.broadcast(new CriticalSessionMessage(this.getID(), ApplicationEvent.ENTER_REGION));
    }

    @NodePopupMethod(menuText="[App] Exit Region")
    public void appExitRegion() {
        this.broadcast(new CriticalSessionMessage(this.getID(), ApplicationEvent.EXIT_REGION));
    }

    @NodePopupMethod(menuText="Cast Token")
    public void castTokenpMethod() {
        _castToken(false);
    }
    @NodePopupMethod(menuText="Cast Validation Token")
    public void castValidationTokenpMethod() {
        _castToken(true);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawNodeAsSquareWithText(g, pt, highlight, Long.toString(this.getID()), 16, Color.WHITE);
    }

    private void _castToken(boolean validation) {
        java.util.Random rand = sinalgo.tools.Tools.getRandomNumberGenerator();

        TokenMessage msg = new TokenMessage(validation, rand.nextLong(), 0L);
        MessageTimer timer = new MessageTimer(msg);
        timer.startRelative(1, this);
    }
}
