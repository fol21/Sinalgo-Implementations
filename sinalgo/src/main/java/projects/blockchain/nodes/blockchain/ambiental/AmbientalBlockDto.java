package projects.blockchain.nodes.blockchain.ambiental;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.BlockDto;

@Getter
@Setter
@AllArgsConstructor
public class AmbientalBlockDto extends BlockDto
{
    private String transaction;
    private double valor;


    public AmbientalBlockDto(long index, UUID id, long timestamp, String transaction, double valor)
    {
        this.index = index;
        this.id = id;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.valor = valor;
    }

    public AmbientalBlockDto(long index, UUID id, long timestamp, String transaction, double valor, String previous)
    {
        this.index = index;
        this.id = id;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.valor = valor;
        this.previous = previous;
    }    
}
