package projects.blockchain.nodes.nodeImplementations;

import java.util.ArrayList;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Block;
import projects.blockchain.nodes.blockchain.Blockchain;
import projects.blockchain.nodes.messages.BlockMessage;
import projects.blockchain.nodes.messages.ConsensusMessage;
import projects.defaultProject.nodes.timers.MessageTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

@Getter
@Setter
public abstract class BlockchainNode<T extends Blockchain> extends Node {
    
    protected T chain;

    protected ArrayList<Block> blockbuffer = new ArrayList<Block>();

    Logging log = Logging.getLogger();
    Logging chainLog = Logging.getLogger("blockchain/chain.txt");

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Node(" + this.getID() + ") [");
        for (Edge e : this.getOutgoingConnections()) {
            Node n = e.getEndNode();
            s.append(n.getID()).append(" ");
        }
        return s + "]";
    }

    /** Blockchain Actions */

    protected abstract Block processBlock();
    
    /** Node Actions */
    @NodePopupMethod(menuText="[BC] Consensus")
    public void consensus()
    {
        this.log.logln(String.format("Node: %1$s requested consensus", this.getID()));
        if(Tools.isSimulationRunning())
        {
            this.broadcast(new ConsensusMessage(chain.size()));
        }
        else
        {
            if (Tools.isSimulationInAsynchroneMode()) {
                this.broadcast(new ConsensusMessage(chain.size()));
            } else {
                // we need to set a timer, such that the message is
                // sent during the next round, when this node performs its step.

                MessageTimer timer = new MessageTimer(new ConsensusMessage(chain.size()));
                timer.startRelative(1.0, this);
            }
        }
    }
    @NodePopupMethod(menuText="[BC] Add Block")
    public void broadcastBlock()
    {
        this.log.logln(String.format("Node: %1$s is broadcasting block...", this.getID()));
        Block block = this.processBlock();
        this.chain.append(block);
        if(block !=null)
        {
            if(Tools.isSimulationRunning())
            {
                    this.broadcast(new BlockMessage(block));
            }
            else
            {
                if (Tools.isSimulationInAsynchroneMode()) {
                    this.broadcast(new BlockMessage(block));
                } else {
                    // we need to set a timer, such that the message is
                    // sent during the next round, when this node performs its step.
                    
                    MessageTimer timer = new MessageTimer(new BlockMessage(block));
                    timer.startRelative(1.0, this);
                }
            }   
        }
    }
}
