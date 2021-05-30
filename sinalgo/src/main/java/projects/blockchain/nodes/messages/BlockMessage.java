package projects.blockchain.nodes.messages;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Block;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
@AllArgsConstructor
public class BlockMessage extends Message {

    private Block block;

    public BlockMessage clone() {
        return new BlockMessage(this.block);
    }
}
