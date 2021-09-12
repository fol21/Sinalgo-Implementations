package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYCoord extends Message {
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public AYCoord(Node sender) {
		this.sender = sender;
	}

}
