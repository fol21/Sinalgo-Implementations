package projects.blockchain.nodes.nodeImplementations;

import com.google.gson.Gson;

import lombok.Getter;
import projects.blockchain.nodes.blockchain.ambiental.AmbientalBlock;
import projects.blockchain.nodes.blockchain.ambiental.AmbientalBlockchain;
import projects.blockchain.nodes.messages.BlockMessage;
import projects.blockchain.nodes.messages.BlockchainMessage;
import projects.blockchain.nodes.messages.ConsensusMessage;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.*;


@Getter
public class AmbientalBlockchainNode extends BlockchainNode<AmbientalBlockchain> {
    
    private String savedTransaction = null;
    
    private double savedValor = -1;

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext())
        {
            this.setColor(Color.BLACK);
            //Stats
            
            // Message Handle
            Message msg = inbox.next();
            if(msg instanceof ConsensusMessage)
            {
                ConsensusMessage m = (ConsensusMessage) msg;
                this.log.logln(String.format("Node: %1$s Received Consensus message from: ", this.getID()));
                if(this.chain.size() > m.getChainSize())
                {
                    this.log.logln("Node: " + this.getID() + " " + "send(" + m.getSource().toString() + ", CHAIN, " + this.chain.size() + ")");
                    this.send(new BlockchainMessage(chain), m.getSource());
                }
            }
            if(msg instanceof BlockMessage)
            {
                BlockMessage m = (BlockMessage) msg;
                this.log.logln(String.format("Node: %1$s Received Block message from: ", this.getID()));
                if(m.getBlock().getIndex() < this.chain.size())
                {
                    this.log.logln(String.format("Node: %1$s Received outdated Block of index: %2$s", this.getID(), m.getBlock().getIndex()));
                } else {
                    this.log.logln(String.format("Node: %1$s Received Block of index: %2$s, adding to chain...", this.getID(), m.getBlock().getIndex()));
                    blockbuffer.add(m.getBlock());
                    this.chain.append((AmbientalBlock) m.getBlock());
                }
            }
            if(msg instanceof BlockchainMessage)
            {
                BlockchainMessage m = (BlockchainMessage) msg;
                this.log.logln(String.format("Node: %1$s Received Blockchain message from: ", this.getID()));
                this.log.logln(String.format("%1$s ", m.getChain()));
                if(m.getChain().size() > this.chain.size())
                {
                    this.log.logln("Node: " + this.getID() + " " + "Replacing for chain of size: " + m.getChain().size());
                    this.chain = (AmbientalBlockchain) m.getChain();
                }
            }
        }
    }

    @Override
    public void preStep() {
    }

    @Override
    public void init() {
        this.chain = new AmbientalBlockchain(5);
    }
    
    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void postStep() {
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {}

    
    /** Blockchain Actions */
    @Override
    public AmbientalBlock processBlock()
    {
        if(this.savedTransaction != null && this.savedValor >= 0)
        {
            AmbientalBlock block = this.chain.createBlock(this.savedTransaction, this.savedValor);
            if(block !=null)
            {
                this.log.logln(String.format("Node: %1$s Created Block:\n %2$s", this.getID(), (new Gson()).toJson(block.toDto())));
            }
            return block;
        }
        return null;
    }
    
    /** Node Actions */
    @NodePopupMethod(menuText="[Ambiental] Bypass")
    public void bypass()
    {

        this.chain
            .addSponsorship(this.chain.getSponsorshipTarget());
        this.log.logln(String.format("Node: %1$s  bypasses PoSp [%2$s]", this.getID(), this.chain.getSponsorships()));
    }

    @NodePopupMethod(menuText="[Ambiental] test")
    public void test()
    {
        this.savedTransaction = "add";
        this.savedValor = Tools.getRandomNumberGenerator().nextDouble();
        this.log.logln(String.format("Node: %1$s saved transaction: %2$s, valor: %3$s", this.getID(), this.savedTransaction, this.savedValor));
    }

    @NodePopupMethod(menuText="[Ambiental] Log Chain")
    public void logChain()
    {
        this.log.logln(String.format("Node: %1$s Chain:\n %2$s", this.getID(), this.chain.toJson()));
    }

    /** Presentation */
    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        long idx = this.chain.getLastBlock() != null ? this.chain.getLastBlock().getIndex() : -1;
        super.drawNodeAsSquareWithText(
            g,
            pt,
            highlight,
            String.format("%1$s(chain:%2$s |last:%3$s)", this.getID(), this.chain.size(), idx),
            16,
            Color.WHITE
        );
    }
}
