package com.example.todoapp.DTO;

public class ErrorDTO {
    private String title;
    private String description;

    public ErrorDTO(String title, String description) {
        this.title = title;
        this.description =  description;
    }
    public String getTitle() { return title;}
    public String getDescription() { return description;}
}
