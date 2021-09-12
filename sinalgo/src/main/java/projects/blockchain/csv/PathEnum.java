package projects.blockchain.csv;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PathEnum {

    
    HISTORY_NODE_CHAIN_SIZE("C:\\Users\\usuario\\Downloads\\Blockchain\\history_chain_size"),
    HISTORY_CHAIN_SIZE("C:\\Users\\usuario\\Downloads\\Blockchain\\history_chain_size.csv"),

    TOTAL_ORPHANS("C:\\Users\\usuario\\Downloads\\Blockchain\\total_orphans.csv"),
    TOTAL_REPLACEMENTS("C:\\Users\\usuario\\Downloads\\Blockchain\\total_replacements.csv"),
    TOTAL_MESSAGES("C:\\Users\\usuario\\Downloads\\Blockchain\\total_messages.csv");

    private final String path;
}
