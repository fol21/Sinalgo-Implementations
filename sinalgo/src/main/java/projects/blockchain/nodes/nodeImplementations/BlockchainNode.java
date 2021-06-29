package projects.blockchain.nodes.nodeImplementations;

import java.util.ArrayList;


import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Block;
import projects.blockchain.nodes.blockchain.Blockchain;
import projects.blockchain.nodes.messages.BlockMessage;
import projects.blockchain.nodes.messages.ConsensusMessage;
import projects.defaultProject.nodes.timers.MessageTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

@Getter
@Setter
public abstract class BlockchainNode<T extends Blockchain> extends Node {
    
    protected T chain;

    protected ArrayList<Block> blockbuffer = new ArrayList<Block>();
    
    protected ArrayList<Block> orphans = new ArrayList<Block>();

    Logging log = Logging.getLogger();
    Logging chainLog = Logging.getLogger("blockchain/chain.txt");
    Logging blocksLog = Logging.getLogger("blockchain/blocks.txt");
    Logging orphansLog = Logging.getLogger("blockchain/orphans.txt");

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Node(" + this.getID() + ") [");
        for (Edge e : this.getOutgoingConnections()) {
            Node n = e.getEndNode();
            s.append(n.getID()).append(" ");
        }
        return s + "]";
    }


    public void randomBroadcast(Message m, double maxTime)
    {
        for (Edge e : this.getOutgoingConnections()) {
            Node n = e.getEndNode();
            MessageTimer timer = new MessageTimer(m, n);
            timer.startRelative(maxTime * Tools.getRandomNumberGenerator().nextDouble(), this);
        }
    }

    /** Blockchain Actions */

    protected abstract Block processBlock();
    
    /** Node Actions */
    @NodePopupMethod(menuText="[BC] Consensus")
    public void consensus()
    {
        this.log.logln(String.format("Node: %1$s requested consensus", this.getID()));
        int span = 100;
        if(Tools.isSimulationRunning())
        {
            this.randomBroadcast(new ConsensusMessage(this, chain.size()), span);
        }
        else
        {
            if (Tools.isSimulationInAsynchroneMode()) {
                this.randomBroadcast(new ConsensusMessage(this, chain.size()), span);
            } else {
                // we need to set a timer, such that the message is
                // sent during the next round, when this node performs its step.
                this.randomBroadcast(new ConsensusMessage(this, chain.size()), span);
            }
        }
    }
    @NodePopupMethod(menuText="[BC] Add Block")
    public void addBlock()
    {
        this.log.logln(String.format("Node: %1$s is broadcasting block...", this.getID()));
        Block block = this.processBlock();
        if(block !=null)
            this.chain.append(block);
    }
    @NodePopupMethod(menuText="[BC] Broadcast Block")
    public void broadcastBlock()
    {
        this.log.logln(String.format("Node: %1$s is broadcasting block...", this.getID()));
        Block block = this.processBlock();
        if(block !=null)
            this.chain.append(block);
        int span = 1000;
        if(block !=null)
        {
            if(Tools.isSimulationRunning())
            {
                    this.randomBroadcast(new BlockMessage(block), span);
            }
            else
            {
                if (Tools.isSimulationInAsynchroneMode()) {
                    this.randomBroadcast(new BlockMessage(block), span);
                } else {
                    // we need to set a timer, such that the message is
                    // sent during the next round, when this node performs its step.
                    
                    this.randomBroadcast(new BlockMessage(block), span);
                }
            }   
        }
    }
}
