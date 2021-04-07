package projects.tokenring2.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.tokenring2.nodes.messages.TokenMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;


@Getter
@Setter
public class TokenNode2 extends Node {
    
    Logging log = Logging.getLogger("token_node_log");

    /**
     * the neighbor with the smallest ID
     */
    private TokenNode2 next;

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
            if (msg instanceof TokenMessage) {
                TokenMessage m = (TokenMessage) msg;
                if(!(m.isEvaluation())) {
                    if (this.getNext() != null) {
                        m.incrementData();
                        //Update Tokens
                    }
                } else {
                    //update Validation Token
                    if (this.getNext() != null) {
                        m.decreaseData();
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

    @NodePopupMethod(menuText="Cast Token")
    public void castTokenpMethod() {
        _castToken(false);
    }
    @NodePopupMethod(menuText="Cast Validation Token")
    public void castValidationTokenpMethod() {
        _castToken(true);
    }

    private void _castToken(boolean validation) {
        TokenMessage msg = new TokenMessage(validation, 2000L);
        MessageTimer timer = new MessageTimer(msg);
        timer.startRelative(1, this);
    }
}
