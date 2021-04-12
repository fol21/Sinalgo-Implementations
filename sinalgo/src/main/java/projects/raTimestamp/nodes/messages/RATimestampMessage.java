package projects.raTimestamp.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
public class RATimestampMessage extends LamportTimestampMessage {
    
    private int request = 0;

    public RATimestampMessage(int request, long timestamp)
    {
        super(timestamp);
        this.request = request;
    }

    @Override
    public RATimestampMessage clone()
    {
        return new RATimestampMessage(this.request, this.getTimestamp());
    }
}
