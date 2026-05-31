package com.example.todoapp.DTO;

public class TaskCreateDTO {
    private String title; 
    private String description;
    public TaskCreateDTO(){}

    public TaskCreateDTO (String title, String descrption) {
        this.title=title;
        this.description=descrption;
    }
    public String  getTitle() {return this.title;}
    public void setTitle (String title){this.title = title;}

    public String getDescription() { return this.description;}
    public void setDescription (String description){this.description = description;}
}
