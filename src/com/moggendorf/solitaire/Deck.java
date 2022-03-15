package com.moggendorf.solitaire;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> deck;

    public Deck() {

        initDeck();
    }

    private void initDeck() {
        deck = new ArrayList<>();
        for (int idx = 0; idx < Const.DECK_SIZE; idx++) {
            deck.add(new Card(idx));
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(deck);
    }

    public boolean isEmpty() {
        return deck.isEmpty();
    }

    public Card deal() {
        return deck.isEmpty() ? null : deck.remove(0);
    }
}
