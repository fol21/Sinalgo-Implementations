package projects.blockchain.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
@AllArgsConstructor
public class ConsensusMessage extends AdressedMessage {
    
    private long chainSize;

    public ConsensusMessage(Node source, long chainSize)
    {
        this.source = source;
        this.chainSize = chainSize;
    }
    public ConsensusMessage clone()
    {
        return new ConsensusMessage(source, chainSize);
    }
}
