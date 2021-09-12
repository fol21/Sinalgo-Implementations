package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYThere_answer extends Message {

	public Node sender;
	public boolean answer;
	public int CoordinatorCount;

	@Override
	public Message clone() {
		return this;
	}

	public AYThere_answer(Node sender, boolean answer, int CoordinatorCount) {
		this.sender = sender;
		this.answer = answer;
		this.CoordinatorCount = CoordinatorCount;
	}

}
