package projects.tokenring2.nodes.messages;

import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the TokenNodes in the tokenring2 projects. 
 *  
 */
@Getter
@Setter
public class TokenMessage extends Message {
    
    private boolean evaluation;

    private long data;

    private long targetId;


    public TokenMessage(boolean evaluation, long data, long targetId)
    {
        this.evaluation = evaluation;
        this.data = data;
        this.targetId = targetId;
    }
    
    public TokenMessage(long data, long targetId)
    {
        this.evaluation = false;
        this.data = data;
        this.targetId = targetId;
    }
    
    /**
     * Increments the value of the data by one.
     */
    public final void incrementData() {
        this.data++;
    }

    /**
     * Decreases the value of the data by one.
     */
    public final void decreaseData() {
        this.data--;
    }

    public final boolean isEvaluation() {
        return this.evaluation;
    }

    @Override
    public Message clone() {
        return new TokenMessage(this.evaluation, this.data, this.targetId);
    }
}
