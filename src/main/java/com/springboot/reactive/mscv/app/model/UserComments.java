package com.springboot.reactive.mscv.app.model;

import lombok.ToString;

@ToString
public class UserComments {
    
    private User user;
    private Comment comment;

    public UserComments(User user, Comment comment){
        this.user = user;
        this.comment = comment;
    }
}
