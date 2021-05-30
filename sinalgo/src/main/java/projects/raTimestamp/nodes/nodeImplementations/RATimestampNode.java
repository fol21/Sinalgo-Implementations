package projects.raTimestamp.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.raTimestamp.nodes.app.ApplicationEvent;
import projects.raTimestamp.nodes.messages.RATimestampReplyMessage;
import projects.raTimestamp.nodes.messages.RATimestampRequestMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.DataSeries;
import sinalgo.tools.storage.ReusableListIterator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RATimestampNode extends LamportTimestampNode {

    Logging log = Logging.getLogger();
    Logging lamportLog = Logging.getLogger("lamport/lamport.txt");
    Logging statsLog = Logging.getLogger("lamport/stats.txt");

    /**
     * the neighbor with the smallest ID
     */
    private RATimestampNode next;

    @Getter
    private boolean waiting = false;

    @Getter
    @Setter
    private long requestTimestamp = -1;

    private ApplicationEvent appEvent = ApplicationEvent.OUT;

    private int pending = -1;

    private List<Long> replyDeferred = new ArrayList<Long>();
    
    //Stats
    private int totalMessages = 0;
    private double messageRate = 0;
    private int totalReplies = 0;
    private double repliesRate = 0;
    private int totalRequests = 0;
    private double requestsRate = 0;

    private DataSeries timesInRegionPerEvent = new DataSeries();
    private DataSeries timesHoldRegionPerEvent = new DataSeries();

    // a flag to prevent all nodes from sending messages
    @Getter
    @Setter
    private static boolean isSending = true;

    @Override
    public void handleMessages(Inbox inbox)
    {
        // RA Timestamp
        while (inbox.hasNext())
        {

            //Stats
            this.messageRate = ++this.totalMessages / Tools.getGlobalTime();
        
            Message msg = inbox.next();
            if(msg instanceof RATimestampReplyMessage)
            {
                this.repliesRate = ++this.totalReplies / Tools.getGlobalTime();

                RATimestampReplyMessage m = (RATimestampReplyMessage) msg;
                this.pending--;
                this.log.logln("Node: " + this.getID() + " " + "recv(" + m.getNodeId() + ", REPLY, " + m.getTimestamp() + ") " + this.pending + " pending(s) remaining.");
                // Lamport TS Update
                this.setLamportTimestamp(Math.max(m.getTimestamp(), this.getLamportTimestamp()) + 1);
                
                if(this.pending == 0)
                {
                    this.timesHoldRegionPerEvent.addSample(this.getLamportTimestamp() - this.requestTimestamp);

                    this.appEvent = ApplicationEvent.IN;
                    this.setColor(Color.RED);
                    this.log.logln("Node: " + this.getID() + " In App TS: " + this.getLamportTimestamp());
                }
            }
            if(msg instanceof RATimestampRequestMessage)
            {
                this.requestsRate = ++this.totalRequests / Tools.getGlobalTime();

                RATimestampRequestMessage m = (RATimestampRequestMessage) msg;
                this.log.logln("Node: " + this.getID() + " " + "recv(" + m.getNodeId() + ", REQ, " + m.getTimestamp() + ")");
                // Lamport TS Update
                this.setLamportTimestamp(Math.max(m.getTimestamp(), this.getLamportTimestamp()) + 1);

                Node sendToNode = this.findEndNodeById(m.getNodeId());
                boolean tiebreak = 
                    this.appEvent == ApplicationEvent.HOLD &&
                    requestTimestamp == m.getTimestamp() &&
                    this.getID() > sendToNode.getID();
                    
                if(
                    this.appEvent == ApplicationEvent.OUT || 
                    (this.appEvent == ApplicationEvent.HOLD && requestTimestamp > m.getTimestamp()) || 
                    tiebreak
                )
                {
                    this.send(new RATimestampReplyMessage(this.getLamportTimestamp(), this.getID()), sendToNode); 
                }

                else {
                    this.replyDeferred.add(m.getNodeId());
                    this.log.logln("Node " + this.getID() + ": Replies to Defer: "  + this.replyDeferred.toString());
                }
            }

        }
        if(Math.random() * 100 >= 98){
            if(this.appEvent == ApplicationEvent.OUT){
                this.appEnterRegion();
            }
        }
    }
 
    @Override
    public void init() {
    }

    
    @Override
    public void preStep() {

        this.requestTimestamp = this.getLamportTimestamp();
        if(Math.random() * 100 >= 75){
            if(this.appEvent == ApplicationEvent.IN){
                this.appExitRegion();
            }
            if(this.appEvent == ApplicationEvent.HOLD){
                this.appGiveUp();
            }
        }
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
        //Stats
        if((int) Math.floor(Tools.getGlobalTime()) % 5000 == 0)
        {
            this.log.logln("***** " + "[Stats] Node " + this.getID() + " Round " + Global.getCurrentTime() + "*****");
            
            this.log.logln("[Stats] Node " + this.getID() + " Message Total: " +  this.getTotalMessages() + "messages");
            this.log.logln("[Stats] Node " + this.getID() + " Replies Total: " +  this.getTotalReplies() + "messages");
            this.log.logln("[Stats] Node " + this.getID() + " Requests Total: " +  this.getTotalRequests() + "messages");
            this.log.logln("[Stats] Node " + this.getID() + " Message Rate: " +  this.getMessageRate() + "messages/s");
            this.log.logln("[Stats] Node " + this.getID() + " Replies Rate: " +  this.getRepliesRate() + "messages/s");
            this.log.logln("[Stats] Node " + this.getID() + " Requests Rate: " +  this.getRequestsRate() + "messages/s");
            this.log.logln("[Stats] Node " + this.getID() + " Average time on Hold: " +  this.getTimesHoldRegionPerEvent().getMean() + " events");
            this.log.logln("[Stats] Node " + this.getID() + " Average time in Application: " +  this.getTimesInRegionPerEvent().getMean() + " events");


            this.statsLog.logln("***** " + "[Stats] Node " + this.getID() + " Round " + Global.getCurrentTime() + "*****");

            this.statsLog.logln("[Stats] Node " + this.getID() + " Message Total: " +  this.getTotalMessages() + "messages");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Replies Total: " +  this.getTotalReplies() + "messages");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Requests Total: " +  this.getTotalRequests() + "messages");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Message Rate: " +  this.getMessageRate() + "messages/s");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Replies Rate: " +  this.getRepliesRate() + "messages/s");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Requests Rate: " +  this.getRequestsRate() + "messages/s");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Average time on Hold: " +  this.getTimesHoldRegionPerEvent().getMean() + " events");
            this.statsLog.logln("[Stats] Node " + this.getID() + " Average time in Application: " +  this.getTimesInRegionPerEvent().getMean() + " events");
        }
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
        if(this.appEvent == ApplicationEvent.OUT)
        {
            this.setColor(Color.ORANGE);
            this.requestTimestamp = this.getLamportTimestamp();
            this.appEvent = ApplicationEvent.HOLD;
            this.pending = this.getOutgoingConnections().size();
            if(Tools.isSimulationRunning())
            {
                this.broadcast(new RATimestampRequestMessage(this.getID(), (this.requestTimestamp)));
            }
            else
            {
                if (Tools.isSimulationInAsynchroneMode()) {
                    this.broadcast(new RATimestampRequestMessage(this.getID(), (this.requestTimestamp)));
                } else {
                    // we need to set a timer, such that the message is
                    // sent during the next round, when this node performs its step.
                    MessageTimer timer = new MessageTimer(new RATimestampRequestMessage(this.getID(), (this.requestTimestamp)));
                    timer.startRelative(1.0, this);
                }
            }
            this.log.logln("Node " + this.getID() + " is " + "in HOLD.");
        } else
        {
            this.log.logln("Already In Application or in error.");
        }
    }

    @NodePopupMethod(menuText="[App] Exit Region")
    public void appExitRegion() {
        try {
            if(this.appEvent == ApplicationEvent.IN || this.appEvent == ApplicationEvent.HOLD)
            {
                this.timesInRegionPerEvent.addSample(this.getLamportTimestamp() - this.requestTimestamp);

                this.appEvent = ApplicationEvent.OUT;
                this.pending = -1;
                this.setColor(Color.BLACK);
                this.log.logln("Node " + this.getID() + " is " + "OFF.");
                if(this.replyDeferred.size() > 0)
                {
                    for (Long l : this.replyDeferred) {
                        this.log.logln("Node " + this.getID() + ": Deferring: "  + l);
                        this.send(new RATimestampReplyMessage(this.getLamportTimestamp(), this.getID()), this.findEndNodeById(l));              
                    }
                    this.replyDeferred.clear();
                }

            } else {
                this.log.logln("Already Off Application or in error.");
            }
        } catch (Exception e) {
            this.log.logln(e.getMessage());
        }
    }

    @NodePopupMethod(menuText="[App] Enter Region")
    public void appGiveUp()
    {
        try {
            if(this.appEvent == ApplicationEvent.HOLD)
            {
                this.timesInRegionPerEvent.addSample(this.getLamportTimestamp() - this.requestTimestamp);

                this.appEvent = ApplicationEvent.OUT;
                this.pending = -1;
                this.setColor(Color.BLACK);
                this.log.logln("Node " + this.getID() + " is " + "OFF.");
                if(this.replyDeferred.size() > 0)
                {
                    for (Long l : this.replyDeferred) {
                        this.log.logln("Node " + this.getID() + ": Deferring: "  + l);
                        this.send(new RATimestampReplyMessage(this.getLamportTimestamp(), this.getID()), this.findEndNodeById(l));              
                    }
                    this.replyDeferred.clear();
                }
            } else if(this.appEvent == ApplicationEvent.IN)
            {
                this.log.logln("Already In Application or in error.");
            } else
            {
                this.log.logln("Already Out of Application or in error.");
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