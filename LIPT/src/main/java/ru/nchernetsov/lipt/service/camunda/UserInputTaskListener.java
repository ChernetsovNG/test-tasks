package ru.nchernetsov.lipt.service.camunda;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.nchernetsov.lipt.service.data.DataService;
import ru.nchernetsov.lipt.service.data.GeoPoint;

@Component
public class UserInputTaskListener implements TaskListener {

    private final Logger log = LoggerFactory.getLogger(UserInputTaskListener.class);

    private final DataService dataService;

    public UserInputTaskListener(@Qualifier("mockDataService") DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Object variable = delegateTask.getVariable("FormField_Address");
        String variableStr = (String) variable;
        GeoPoint coords = dataService.getAddressCoords(variableStr);
        log.info(coords.toString());
        delegateTask.setVariable("FormField_Coords", coords.toString());
    }
}
