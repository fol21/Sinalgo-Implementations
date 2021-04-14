package projects.raTimestamp.nodes.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RATimestampReplyMessage extends LamportTimestampMessage {
    
    private int request = 0;

    public RATimestampReplyMessage(long timestamp)
    {
        super(timestamp);
    }

    @Override
    public RATimestampReplyMessage clone()
    {
        return new RATimestampReplyMessage(this.getTimestamp());
    }
}
