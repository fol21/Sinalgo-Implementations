package projects.tokenring2.nodes.timers;

import projects.tokenring2.nodes.app.IApplication;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class CriticalSessionTimer extends Timer {
   
    public IApplication app;
    
    public CriticalSessionTimer(IApplication app)
    {
        this.app = app;
    }

    public final void startApplicationRelative(double relativeTime, Node n) {
        app.enterRegion();
        super.startRelative(relativeTime, n);
    }

    public final void startApplicationAbsolute(double absoluteTime, Node n) {
        app.enterRegion();
        super.startAbsolute(absoluteTime, n);
    }

    public void fire() {
        //Acess Critical Session and then Leave
        app.exitRegion();
    }
}
