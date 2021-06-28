package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYThere extends Message {

	public Node sender;
	public int CoordinatorCount;

	@Override
	public Message clone() {
		return this;
	}

	public AYThere(Node sender, int CoordinatorCount) {
		this.sender = sender;
		this.CoordinatorCount = CoordinatorCount;
	}

}
