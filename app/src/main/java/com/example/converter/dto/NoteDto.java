package com.example.converter.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {

    private Long id;
    private String title;
    private String body;
    private Date date;
    private Integer countAll;
    private Integer countCompleted;
}
