package com.empatica.sample;

/**
 * Created by Rallmo on 2015-12-20.
 */
public class UserInput {
    private String textName;

    public UserInput(String textName){
        this.textName = textName;
    }
    public void setText(String inputName){
        this.textName = inputName;
    }
    public String getText(){
        return textName;
    }
}
