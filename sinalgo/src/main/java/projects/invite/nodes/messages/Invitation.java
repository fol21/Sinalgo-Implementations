package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Invitation extends Message {
	public Node sender;
	public int CoordinatorCount;

	@Override
	public Message clone() {
		return this;
	}

	public Invitation(Node Coordinator, int CoordinatorCount) {
		this.sender = Coordinator;
		this.CoordinatorCount = CoordinatorCount;
	}

}