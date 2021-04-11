package projects.tokenring2.nodes.app;

import lombok.Getter;

public interface IApplication {

    public ApplicationEvent enterRegion();
    public ApplicationEvent exitRegion();
}
