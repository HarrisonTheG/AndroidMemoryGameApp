package iss.project.t11memorygame.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChosenImage implements Serializable {
    private ArrayList<Integer> choices;

    public ChosenImage(){
        this.choices=new ArrayList<>();
    }

    public ArrayList<Integer> getChoices() {
        return choices;
    }

    public void setChoices(ArrayList<Integer> choices) {
        this.choices = choices;
    }
}
