package projects.invite.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYCAnswer extends Message {
	public Node coord;
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public AYCAnswer(Node sender, Node coord) {
		this.sender = sender;
		if (coord == null)
			this.coord = sender;
		else
			this.coord = coord;
	}
}
