package com.example.jason.simon;

import android.app.Activity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

/**
 * Created by Jason on 2/12/2015.
 */
    public class Player {
        Vector<Integer> sequence;

        Player(){
            this.sequence = new Vector<Integer>();
        }
        Player(Vector sequence){
            this.sequence = sequence;
        }

        Vector getSequence(){
            return sequence;
        }

        void storeChoice(int choice){
            sequence.add(choice);
        }

        int getRound(){
            return sequence.size();
        }

        int getChoice(int index){
            return sequence.elementAt(index);
        }

        boolean choiceExists(int index, int value){
            if(sequence.elementAt(index) == value){
                return true;
            }
            else{
                return false;
            }
        }



    }

