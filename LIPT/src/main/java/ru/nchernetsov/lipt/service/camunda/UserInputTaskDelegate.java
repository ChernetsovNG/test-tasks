package ru.nchernetsov.lipt.service.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.nchernetsov.lipt.service.data.DataService;
import ru.nchernetsov.lipt.service.data.GeoPoint;

@Component
public class UserInputTaskDelegate implements JavaDelegate {

    private final Logger log = LoggerFactory.getLogger(UserInputTaskDelegate.class);

    private final DataService dataService;

    public UserInputTaskDelegate(@Qualifier("daDataService") DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String userInputAddress = (String) execution.getVariable("FormField_Address");
        GeoPoint geoPoint = dataService.getAddressGeoPoint(userInputAddress);
        execution.setVariable("FormField_UserInput", userInputAddress);
        execution.setVariable("FormField_Coords", geoPoint.toText());
        execution.setVariable("FormField_Address", geoPoint.getCleanAddress());
    }
}
