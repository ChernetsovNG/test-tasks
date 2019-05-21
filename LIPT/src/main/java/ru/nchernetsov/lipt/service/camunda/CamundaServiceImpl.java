package ru.nchernetsov.lipt.service.camunda;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class CamundaServiceImpl implements CamundaService {

    private static final String PROCESS_KEY = "get_coordinates_process";

    private final RuntimeService runtimeService;

    public CamundaServiceImpl(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @EventListener
    public void processPostDeploy(PostDeployEvent event) {
        runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    }
}
