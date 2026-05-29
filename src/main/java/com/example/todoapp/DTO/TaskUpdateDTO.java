package com.example.todoapp.DTO;

public class TaskUpdateDTO {
    
    private String title; 
    private String description;
    private boolean done ;

    public TaskUpdateDTO(){}

    public TaskUpdateDTO (String title, String descrption, boolean done) {
        this.title = title;
        this.description = descrption;
        this.done = done;
    }
    public String  getTitle() {return title;}
    public void setTitle (String title){this.title = title;}

    public String getDescription() { return this.description;}
    public void setDescription (String description){this.description = description;}

    public boolean getDone() { return done;}
    public void setDone (boolean done){this.done = done;}

}
