package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Accept extends Message {
	public int CoordinatorCount;
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public Accept(Node sender, int CoordinatorCount) {
		this.sender = sender;
		this.CoordinatorCount = CoordinatorCount;
	}
}
