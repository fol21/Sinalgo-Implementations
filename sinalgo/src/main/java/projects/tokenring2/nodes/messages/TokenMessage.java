package projects.tokenring2.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the TokenNodes in the tokenring2 projects. 
 *  
 */
@Getter
@Setter
@AllArgsConstructor
public class TokenMessage extends Message {
    
    private boolean evaluation;

    private long data;

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
        return new TokenMessage(this.evaluation, this.data);
    }
}
