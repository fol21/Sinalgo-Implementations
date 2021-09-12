package projects.invite.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
public class AcceptAnswer extends Message {
	private Node sender;
	private boolean answer;

	@Override
	public Message clone() {
		return this;
	}

	public AcceptAnswer(Node sender, boolean answer) {
		this.sender = sender;
		this.answer = answer;
	}

}
