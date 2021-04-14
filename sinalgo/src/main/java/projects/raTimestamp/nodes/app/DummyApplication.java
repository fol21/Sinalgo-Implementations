package projects.raTimestamp.nodes.app;

import lombok.Getter;

public class DummyApplication implements IApplication {
    
    @Getter private boolean running = false;

    public ApplicationEvent enterRegion() {
        this.running = true;
        return ApplicationEvent.IN;
    }
    
    public ApplicationEvent exitRegion() {
        this.running = false;
        return ApplicationEvent.OFF;
    }
}
