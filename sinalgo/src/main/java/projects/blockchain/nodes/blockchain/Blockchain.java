package projects.blockchain.nodes.blockchain;

import java.util.ArrayList;

import com.google.gson.Gson;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.JSONSerializer;
import sinalgo.tools.storage.DoublyLinkedList;
import sinalgo.tools.storage.ReusableListIterator;

@Getter
@Setter
public abstract class Blockchain<T extends Block> extends DoublyLinkedList<T> {
    
    @Override
    public boolean append(T b)
    {  
        //Hash(previous)
        if(this.size() > 0)
        {
             b.setPrevious(DigestUtils.sha1Hex((new Gson()).toJson(super.elementAt(super.size() - 1).toDto())));
        }
        return super.append(b);
    }

    public T getLastBlock()
    {
        return super.size() > 0 ? super.elementAt(super.size() - 1) : null;
    }

    public boolean validate()
    {
        boolean valid = true;
        ReusableListIterator<T> it = super.iterator();
        while(it.hasNext() && valid)
        {
            T prev = it.next();
            if(it.hasNext()) {
                T curr = it.next();
                valid = curr.getPrevious() == DigestUtils.sha1Hex((new Gson()).toJson(prev.toDto()));
            } 
        }
        return valid;
    }

    public boolean validateLast()
    {      
        return this.size() >= 2 ?
            this.getLastBlock().getPrevious() == DigestUtils.sha1Hex((new Gson()).toJson(super.elementAt(super.size() - 2).toDto())) :
            true;
    }

    public String toJson()
    {
        ArrayList<BlockDto> list = new ArrayList<BlockDto>();
        ReusableListIterator<T> it = super.iterator();
        while(it.hasNext())
        {
            list.add(it.next().toDto());
        }
        return (new Gson()).toJson(list);
    }

    @Override
    public String toString()
    {
        return String.format("Chain: {size: %1$s, genesis: %2$s, last: %3$s}", this.size(), this.elementAt(0).getId(), this.getLastBlock().getId());
    }

}
