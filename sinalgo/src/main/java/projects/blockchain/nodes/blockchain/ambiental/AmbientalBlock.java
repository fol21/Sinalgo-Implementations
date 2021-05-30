package projects.blockchain.nodes.blockchain.ambiental;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Block;

@Getter
@Setter
public class AmbientalBlock extends Block
{
    
    private String transaction;
    private double valor;


    public AmbientalBlock(long index, UUID id, long timestamp, String transaction, double valor)
    {
        this.index = index;
        this.id = id;
        this.timestamp=timestamp;
        this.transaction = transaction;
        this.valor = valor;
    }

    @Override
    public AmbientalBlock clone()
    {
        return new AmbientalBlock(index, id, timestamp, transaction, valor);
    }

    @Override
    public  AmbientalBlockDto toDto()
    {
        return new AmbientalBlockDto(index, id, timestamp, transaction, valor, previous);
    }
}
