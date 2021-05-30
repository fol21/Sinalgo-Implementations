package projects.blockchain.nodes.blockchain.ambiental;

import lombok.Getter;
import lombok.Setter;
import projects.blockchain.nodes.blockchain.Blockchain;

@Getter
@Setter
public class AmbientalBlockchain extends Blockchain {
    
    private int sponsorshipTarget = 0;
    @Getter
    private int sponsorships = 0;

    public AmbientalBlockchain(int sponsorshipTarget)
    {
        super();
        this.sponsorshipTarget = sponsorshipTarget;
    }

    public AmbientalBlockchain addSponsorship(int grade)
    {
        this.sponsorships++;
        return this;
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
