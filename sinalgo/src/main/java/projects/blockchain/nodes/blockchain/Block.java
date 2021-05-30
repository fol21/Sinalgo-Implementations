package projects.blockchain.nodes.blockchain;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import sinalgo.tools.storage.DoublyLinkedListEntry;

@Getter
@Setter
public abstract class Block implements DoublyLinkedListEntry {
    
    protected long index;
    protected UUID id;
    protected long timestamp;
    protected String previous = null;
    
    private DLLFingerList doublyLinkedListFinger = new DLLFingerList();

    public abstract Block clone();

    public abstract BlockDto toDto();

}
