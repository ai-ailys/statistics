package ru.currency_pair.statistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.currency_pair.statistics.StatisticsApplication;
import ru.currency_pair.statistics.model.EnumOHLC;
import ru.currency_pair.statistics.service.StatisticsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "api/v1/statistics")
public class StatisticsController {

    static final Logger log = LoggerFactory.getLogger(StatisticsApplication.class);

    @Autowired
    StatisticsService service;

    @GetMapping
    public Map<String, Map<Integer, Map<String, Map<EnumOHLC, Double>>>> getOHCL(){
        return service.getStatisticsByName();
    }

    @GetMapping(value = "/findBy")
    public Map<Integer, Map<String, Map<EnumOHLC, Double>>> getOHCLbyNameAndTimeframe(@RequestParam String name,
                                                                                      @RequestParam(required = false) Integer timeframe){
        try{
            if (timeframe == null) {
                log.debug("Method 'getOHLCbyNameAndTimeframe' was called with name = " + name);
                return service.getStatisticsByName().get(name);
            }
            else {
                Map<String, Map<EnumOHLC, Double>> mapByTimeframe = service.getStatisticsByName().get(name).get(timeframe);
                if (mapByTimeframe == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Timeframe Not Found");
                }
                else{
                    log.debug("Method 'getOHLCbyNameAndTimeframe' was called with name = " + name + ", timeframe = " + timeframe);
                    Map<Integer, Map<String, Map<EnumOHLC, Double>>> mapByName = new HashMap<>();
                    mapByName.put(timeframe, mapByTimeframe);
                    return mapByName;
                }
            }
        } catch (NullPointerException exception){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Name Not Found");
        }

    }


}
