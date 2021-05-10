package projects.raTimestampv2.nodes.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RATimestampReplyMessage extends LamportTimestampMessage {
    
    private int request = 0;
    private long nodeId = 0;

    public RATimestampReplyMessage(long timestamp, long nodeId)
    {
        super(timestamp);
        this.nodeId = nodeId;
    }

    @Override
    public RATimestampReplyMessage clone()
    {
        return new RATimestampReplyMessage(this.getTimestamp(), this.getNodeId());
    }
}
