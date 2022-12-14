package ru.currency_pair.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDTO {

    private String name;
    private Double value;
    private String time;
    private String date;

}
