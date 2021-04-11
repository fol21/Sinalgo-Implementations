package projects.tokenring2.nodes.app;

import lombok.Getter;

public class DummyApplication implements IApplication {
    
    @Getter private boolean running = false;

    public ApplicationEvent enterRegion() {
        this.running = true;
        return ApplicationEvent.ENTER_REGION;
    }
    
    public ApplicationEvent exitRegion() {
        this.running = false;
        return ApplicationEvent.EXIT_REGION;
    }
}
