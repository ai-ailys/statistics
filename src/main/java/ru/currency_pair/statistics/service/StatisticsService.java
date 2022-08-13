package ru.currency_pair.statistics.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.currency_pair.statistics.config.StatisticsConfig;
import ru.currency_pair.statistics.dto.HistoryDTO;
import ru.currency_pair.statistics.model.EnumOHLC;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Getter
public class StatisticsService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    WebClient client = WebClient.create("http://localhost:8080/api/v1");

    @Autowired
    StatisticsConfig statisticsConfig;

    Map<String, Map<Integer, Map<String, Map<EnumOHLC, Double>>>> statisticsByName = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void startOHLC(){

        String startTime =  history(statisticsConfig.getCurrencyPairs().get(0),
                LocalTime.MIN.format(DateTimeFormatter.ISO_LOCAL_TIME),
                LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)).get(0).getTime();//
        LocalTime startLocalTime1 = LocalTime.parse(startTime);
        startLocalTime1 = startLocalTime1.minusMinutes(startLocalTime1.getMinute()).minusSeconds(startLocalTime1.getSecond());
        //System.out.println(startLocalTime);
        LocalTime endLocalTime = LocalTime.now();

        for (String name: statisticsConfig.getCurrencyPairs()) {

            Map<Integer, Map<String, Map<EnumOHLC, Double>>> statisticsByTimeframe = new HashMap<>();

            for (int timeframe: statisticsConfig.getTimeframe()){

                Map<String, Map<EnumOHLC, Double>> mapOfTimeAndOHLC = addMap(endLocalTime, startLocalTime1, name, timeframe);
                statisticsByTimeframe.put(timeframe, mapOfTimeAndOHLC);

            }
            statisticsByName.put(name, statisticsByTimeframe);
        }
    }

    @Scheduled(initialDelayString = "PT030S", fixedDelayString = "PT05M")
    private void addOHLC(){

        LocalTime endTime = LocalTime.now();
        endTime = endTime.minusSeconds(endTime.getSecond()).minusNanos(endTime.getNano());
        LocalTime startTime = endTime.minusMinutes(5);
        startTime = startTime.minusMinutes(startTime.getMinute());

        for (String name: statisticsConfig.getCurrencyPairs()){
            Map<Integer, Map<String, Map<EnumOHLC, Double>>> statisticsByTimeframe = statisticsByName.get(name);
            for (int timeframe: statisticsConfig.getTimeframe()){

                Map<String, Map<EnumOHLC, Double>> mapOfTimeAndOHLC = statisticsByTimeframe.get(timeframe);
                Map<String, Map<EnumOHLC, Double>> addMapOfTimeAndOHLC = addMap(endTime, startTime, name, timeframe);;
                mapOfTimeAndOHLC.putAll(addMapOfTimeAndOHLC);

            }
        }
    }

    private Map<String, Map<EnumOHLC, Double>> addMap(LocalTime endTime, LocalTime startTime, String name, int timeframe) {
        Map<String, Map<EnumOHLC, Double>> addMapOfTimeAndOHLC = new LinkedHashMap<>();

        LocalTime startLocalTime = startTime;

        while(true){

            String startTime_ = startLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
            startLocalTime = startLocalTime.plusMinutes(timeframe);
            String endTime_ = startLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME);


            List<HistoryDTO> historyFrom0ToCurrentTime = history(name, startTime_, endTime_);
            assert historyFrom0ToCurrentTime != null;
            if (historyFrom0ToCurrentTime.size() == 0) {
                continue;
            }

            Map<EnumOHLC, Double> dictOHLC = dictOHLC(historyFrom0ToCurrentTime);

            addMapOfTimeAndOHLC.put(startTime_, dictOHLC);

            if (startLocalTime.compareTo(endTime) >= 0) {
                return addMapOfTimeAndOHLC;
                //break;
            }
        }
    }


    private List<HistoryDTO> history(String name, String start, String end){
        return client
                .get()
                .uri("/history/findAllBy?name=" + name + "&startTime=" + start + "&endTime=" + end)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                .block(REQUEST_TIMEOUT);
    }

    private Map<EnumOHLC, Double> dictOHLC(List<HistoryDTO> historyFrom0ToCurrentTime){

        Map<EnumOHLC, Double> dictOHLC = new HashMap<>();

        dictOHLC.put(EnumOHLC.OPEN, historyFrom0ToCurrentTime.get(0).getValue());
        dictOHLC.put(EnumOHLC.LOW, historyFrom0ToCurrentTime.get(0).getValue());
        dictOHLC.put(EnumOHLC.HIGH, historyFrom0ToCurrentTime.get(0).getValue());
        dictOHLC.put(EnumOHLC.CLOSE, historyFrom0ToCurrentTime.get(historyFrom0ToCurrentTime.size()-1).getValue());

        for ( HistoryDTO historyDTO : historyFrom0ToCurrentTime) {
            if(dictOHLC.get(EnumOHLC.LOW) > historyDTO.getValue()) {
                dictOHLC.put(EnumOHLC.LOW, historyDTO.getValue());
            }
            if(dictOHLC.get(EnumOHLC.HIGH) < historyDTO.getValue()) {
                dictOHLC.put(EnumOHLC.HIGH, historyDTO.getValue());
            }
        }
        return dictOHLC;
    }




}
