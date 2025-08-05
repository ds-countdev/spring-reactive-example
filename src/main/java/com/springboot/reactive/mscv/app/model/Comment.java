package com.springboot.reactive.mscv.app.model;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

@ToString
public class Comment {
    
    private List<String> comments;

    public Comment(){
        this.comments = new ArrayList<>();
    }

    public Comment addComments(String comment){
        comments.add(comment);
        return this;
    }
}
