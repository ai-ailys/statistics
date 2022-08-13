package ru.currency_pair.statistics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.currency_pair.statistics.model.EnumOHLC;
import ru.currency_pair.statistics.service.StatisticsService;

import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/statistics")
public class StatisticsController {

    @Autowired
    StatisticsService service;

    @GetMapping
    public Map<String, Map<Integer, Map<String, Map<EnumOHLC, Double>>>> getOHCL(){
        return service.getStatisticsByName();
    }

    @GetMapping(value = "/findBy")
    public Object getOHCLbyNameAndTimeframe(@RequestParam String name, @RequestParam(required = false) Integer timeframe){
        if (timeframe == null) {
            return service.getStatisticsByName().get(name);
        }
        else return service.getStatisticsByName().get(name).get(timeframe);
    }


}
