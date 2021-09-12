package projects.blockchain.csv.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChainSizeEntry {
    public long ID;
    public int round;
    public int chainSize;
}
