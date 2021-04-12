package projects.raTimestamp.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

@Getter
@Setter
public class LamportTimestampMessage extends Message{
    
    private long timestamp;

    public LamportTimestampMessage(long timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public LamportTimestampMessage clone()
    {
        return new LamportTimestampMessage(this.timestamp);
    }
}
