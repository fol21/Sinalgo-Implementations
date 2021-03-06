package projects.blockchain.nodes.nodeImplementations;

import com.google.gson.Gson;

import lombok.AccessLevel;
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
import java.util.LinkedHashMap;
import java.util.Random;


@Getter
public class AmbientalBlockchainNode extends BlockchainNode<AmbientalBlockchain> {
    
    private String savedTransaction = null;
    
    private double savedValor = -1;

    @Getter(AccessLevel.PRIVATE)
    private Random random = new Random();

    // KPis
    private int totalChainReplacements = 0;
    private int totalMessages = 0;

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext())
        {
            this.setColor(Color.BLACK);
            //Stats
            this.totalMessages++;
            // Message Handle
            Message msg = inbox.next();
            if(msg instanceof ConsensusMessage)
            {
                ConsensusMessage m = (ConsensusMessage) msg;
                this.log.logln(String.format("Node: %1$s Received Consensus message from: ", this.getID()));
                if(this.chain.size() > m.getChainSize())
                {
                    this.log.logln(String.format("Node: %1$s send(%2$s, CHAIN, %3$s)", this.getID(), m.getSource().getID(), this.chain.size()));
                    this.chainLog.logln(String.format("Node: %1$s send(%2$s, CHAIN, %3$s)", this.getID(), m.getSource().getID(), this.chain.size()));
                    this.send(new BlockchainMessage(chain), m.getSource());
                }
            }
            if(msg instanceof BlockMessage)
            {
                BlockMessage m = (BlockMessage) msg;
                if(m.getBlock() != null)
                {
                    this.log.logln(String.format("Node: %1$s Received Block message of Index %2$s: ", this.getID(), m.getBlock().getIndex()));
                    if(m.getBlock().getIndex() < this.chain.size())
                    {
                        this.log.logln(String.format("Node: %1$s Received outdated Block of index: %2$s", this.getID(), m.getBlock().getIndex()));
                    } else if(m.getBlock().getIndex() == this.chain.size() && this.chain.validateLast((AmbientalBlock) m.getBlock())) 
                    {
                        this.log.logln(String.format("Node: %1$s Received Block of index: %2$s, adding to chain...", this.getID(), m.getBlock().getIndex()));
                        this.blocksLog.logln(String.format("Node: %1$s Received Block of index: %2$s, adding to chain...", this.getID(), m.getBlock().getIndex()));
                        // this.blockbuffer.add(m.getBlock());
                        this.chain.append((AmbientalBlock) m.getBlock());
                    } else {
                        this.log.logln(String.format("Node: %1$s Incoming block is from a chain ahead, index: incoming %2$s last: %3$s", 
                            this.getID(), m.getBlock().getIndex(), this.chain.getLastBlock().getIndex()));
                    }
                }
            }
            if(msg instanceof BlockchainMessage)
            {
                BlockchainMessage m = (BlockchainMessage) msg;
                this.log.logln(String.format("Node: %1$s Received Blockchain message from: ", this.getID()));
                AmbientalBlockchain incoming = (AmbientalBlockchain) m.getChain();
                if(incoming.size() > this.chain.size())
                {
                    //Orphans Handling
                    int beforeDiffIndex = this.chain.size() - 1;
                    this.log.logln(String.format("Node: %1$s incoming: %2$s, current: %3$s, diff: %4$s", this.getID(), incoming.size(), this.chain.size(), beforeDiffIndex));
                    boolean indexIsDifferent = beforeDiffIndex >= 0 ? incoming.elementAt(beforeDiffIndex) != this.chain.elementAt(beforeDiffIndex) : false;
                    while(indexIsDifferent && beforeDiffIndex >= 0)
                    {
                        this.orphans.add(this.chain.elementAt(beforeDiffIndex));
                        this.log.logln(String.format("Node: %1$s Adding Orphan from index: %2$s", this.getID(), this.chain.elementAt(beforeDiffIndex).getId()));
                        this.orphansLog.logln(String.format("Node: %1$s Adding Orphan from index: %2$s", this.getID(), this.chain.elementAt(beforeDiffIndex).getId()));
                        indexIsDifferent = --beforeDiffIndex >= 0 ? incoming.elementAt(beforeDiffIndex) != this.chain.elementAt(beforeDiffIndex) : false;
                    }

                    this.log.logln(String.format("Node: %1$s Replacing for chain of size: %2$s", this.getID(), incoming.size()));
                    this.chainLog.logln(String.format("Node: %1$s Replacing for chain of size: %2$s", this.getID(), incoming.size()));
                    this.totalChainReplacements++;
                    this.chain = incoming;
                    
                }
            }
        }
    }

    @Override
    public void init() {
        this.chain = new AmbientalBlockchain(5);
    }
    
    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void preStep() {
        if(Tools.getGlobalTime() % 100 == 0 && random.nextDouble() >= .5)
        {
            this.testTransaction();
            this.bypass();
            // this.addBlock();
            this.broadcastBlock();
        }
        else if (Tools.getGlobalTime() % 500 == 0 && random.nextDouble() >= .5)
        {
            this.consensus();
        }
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

    public void testTransaction(String transaction, double valor)
    {
        this.savedTransaction = transaction;
        this.savedValor = valor;
        this.log.logln(String.format("Node: %1$s saved transaction: %2$s, valor: %3$s", this.getID(), this.savedTransaction, this.savedValor));
    }
    
    /** Node Actions */
    @NodePopupMethod(menuText="[Ambiental] Bypass")
    public void bypass()
    {

        this.chain
            .addSponsorship(this.chain.getSponsorshipTarget());
        this.log.logln(String.format("Node: %1$s  bypasses PoSp [%2$s]", this.getID(), this.chain.getSponsorships()));
    }

    @NodePopupMethod(menuText="[Ambiental] test transaction")
    public void testTransaction()
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
        super.drawNodeAsDiskWithText(
            g,
            pt,
            highlight,
            String.format("%1$s(chain:%2$s |last:%3$s)", this.getID(), this.chain.size(), idx),
            12,
            Color.WHITE
        );
    }

    //#region utils

    //#endregion
}
