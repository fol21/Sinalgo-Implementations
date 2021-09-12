package projects.blockchain.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Blockchain;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
@AllArgsConstructor
public class BlockchainMessage extends Message {
    
    private Blockchain chain;

    public BlockchainMessage clone()
    {
        return new BlockchainMessage(chain);
    }
}
