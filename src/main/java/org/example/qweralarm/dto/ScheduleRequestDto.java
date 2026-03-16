package org.example.qweralarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {
    private Long id;
    private String date;
    private String content;
    private String time;
    private boolean completed;
    private String color;
}
