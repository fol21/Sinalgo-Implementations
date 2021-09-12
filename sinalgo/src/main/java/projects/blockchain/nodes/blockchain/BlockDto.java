package projects.blockchain.nodes.blockchain;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BlockDto {
    
    protected long index;
    protected UUID id;
    protected long timestamp;
    protected String previous = null;
}
