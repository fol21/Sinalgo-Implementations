package projects.tokenring2.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.tokenring2.nodes.app.ApplicationEvent;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the TokenNodes in the tokenring2 projects. 
 *  
 */
@Getter
@Setter
public class CriticalSessionMessage extends Message {
    
    private ApplicationEvent event;
    private long nodeId;
    
    public CriticalSessionMessage(long nodeId, ApplicationEvent event)
    {
        this.nodeId = nodeId;
        this.event = event;
    }


    @Override
    public Message clone() {
        return new CriticalSessionMessage(this.nodeId,this.event);
    }
}
