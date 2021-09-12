package projects.blockchain.nodes.blockchain.ambiental;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import lombok.AccessLevel;
import projects.blockchain.nodes.blockchain.Blockchain;

@Getter
@Setter
public class AmbientalBlockchain extends Blockchain<AmbientalBlock> {
    
    @Setter(AccessLevel.PRIVATE)
    private int sponsorshipTarget = 0;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private int sponsorships = 0;

    public AmbientalBlockchain(int sponsorshipTarget)
    {
        super();
        this.sponsorshipTarget = sponsorshipTarget;
    }

    public AmbientalBlockchain addSponsorship(int grade)
    {
        this.sponsorships += grade;
        return this;
    }
    public AmbientalBlockchain addSponsorship()
    {
        return this.addSponsorship(1);
    }

    public AmbientalBlock createBlock(String transaction, double valor)
    {
        AmbientalBlock block = new AmbientalBlock(this.size(), UUID.randomUUID(), Instant.now().getEpochSecond() * 1000L, transaction, valor);
        return this.proofOfSponsorship() ? block : null;
    }

    public AmbientalBlockchain removeSponsorship(int grade)
    {
        this.sponsorships -= grade;
        return this;
    }
    public AmbientalBlockchain removeSponsorship()
    {
        return this.removeSponsorship(1);
    }

    public boolean proofOfSponsorship()
    {
        if(this.sponsorships >= this.sponsorshipTarget)
        {
            this.sponsorships -= this.sponsorshipTarget;
            return true;
        }
        return false;
    }

    @Override
    public AmbientalBlock getLastBlock()
    {
        return (AmbientalBlock) super.getLastBlock();
    }
}
