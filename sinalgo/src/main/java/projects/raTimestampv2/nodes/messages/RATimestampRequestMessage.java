package projects.raTimestampv2.nodes.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RATimestampRequestMessage extends LamportTimestampMessage {
    
    private long nodeId;

    public RATimestampRequestMessage(long nodeId, long timestamp)
    {
        super(timestamp);
        this.nodeId = nodeId;
    }

    @Override
    public RATimestampRequestMessage clone()
    {
        return new RATimestampRequestMessage(this.nodeId, this.getTimestamp());
    }
}
