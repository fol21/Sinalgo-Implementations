package projects.blockchain.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
public abstract class AdressedMessage extends Message {
    protected Node source;
}
