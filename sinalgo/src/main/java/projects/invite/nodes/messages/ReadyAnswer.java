package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ReadyAnswer extends Message {
	public Node sender;
	public int CoordinatorCount;
	public boolean accept;

	@Override
	public Message clone() {
		return this;
	}

	public ReadyAnswer(Node node, int CoordinatorCount, boolean accept) {
		this.sender = node;
		this.CoordinatorCount = CoordinatorCount;
		this.accept = accept;
	}
}
