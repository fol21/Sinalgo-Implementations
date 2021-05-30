package projects.blockchain.nodes.blockchain;

import java.util.LinkedList;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.JSONSerializer;
import sinalgo.tools.storage.DoublyLinkedList;
import sinalgo.tools.storage.ReusableListIterator;

@Getter
@Setter
public class Blockchain extends DoublyLinkedList<Block> {
    
    @Override
    public boolean append(Block b)
    {  
        //Hash(previous)
        if(this.size() > 0)
        {
             b.setPrevious(DigestUtils.sha1Hex(JSONSerializer.serializeObject(super.elementAt(super.size() - 1).toDto())));
        }
        return super.append(b);
    }

    public Block getLastBlock()
    {
        return super.elementAt(super.size() - 1);
    }

    public boolean validate()
    {
        boolean valid = true;
        ReusableListIterator<Block> it = super.iterator();
        while(it.hasNext() && valid)
        {
            Block prev = it.next();
            if(it.hasNext()) {
                Block curr = it.next();
                valid = curr.getPrevious() == DigestUtils.sha1Hex(JSONSerializer.serializeObject(prev.toDto()));
            } 
        }
        return valid;
    }

}
