package com.example.converter.model;


import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    private Long id;
    private String title;
    private String body;
    private List<Task> tasks;
    private Date date;
}
