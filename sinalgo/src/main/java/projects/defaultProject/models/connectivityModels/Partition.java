package projects.defaultProject.models.connectivityModels;

import projects.invite.nodes.nodeImplementations.InviteNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.tools.logging.Logging;

public class Partition extends ConnectivityModelHelper {
    private Logging logging = Logging.getLogger();

    @Override
    protected boolean isConnected(Node from, Node to) {
        this.logging.logln(String.format("from: %1$s to: %2$s", ((InviteNode) from).getCoordinatorGroup().getID(), ((InviteNode) to).getCoordinatorGroup().getID()));
        return (((InviteNode) from).getCoordinatorGroup().getID() == ((InviteNode) to).getCoordinatorGroup().getID());
    }
    
}
