package com.moggendorf.solitaire;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Layout {
    private Deck deck;
    private List<LinkedList<Card>> columns;
    private List<LinkedList<Card>> foundation;
    private LinkedList<Card> base;
    private LinkedList<Card> openBase;

    public Layout() {
        initStructure();
    }

    private void initStructure() {
        // foundation: 4 lists for the 4 suits
        foundation = new ArrayList<>();
        for (int i = 0; i < Const.SUITS; i++) {
            foundation.add(new LinkedList<>());
        }

        // columns: 7 lists
        columns = new ArrayList<>();
        for (int i = 0; i < Const.COLUMNS; i++) {
            columns.add(new LinkedList<>());
        }

        // the base: all the remaining cards go here
        base = new LinkedList<>();
        openBase = new LinkedList<>();
    }

    /**
     * create a new start setup
     * before that a deck has to be set
     */
    public void initLayout() {
        clearListsIn(columns);
        clearListsIn(foundation);
        base.clear();
        openBase.clear();

        // deal the cards and add them to the columns
        for (int i = 0; i < Const.COLUMNS; i++) {
            for (int j = 0; j <= i ; j++) {
                if (!deck.isEmpty()) {
                    Card card = deck.deal();
                    card.setY(Const.COLUMNS_STARTY + j * Const.CARD_DIST);
                    card.setX(Const.COLUMNS_STARTX + i * (Const.CARD_WIDTH + Const.COLUMNS_DIST));
                    columns.get(i).add(card);
                }
            }
        }

        // set upper card of each column face up
        for (LinkedList<Card> cards : columns)
            cards.getLast().setFaceUp(true);

        // deal the remaining cards from the deck to the base
        while (!deck.isEmpty()) {
            Card card = deck.deal();
            card.setY(Const.BASE_STARTY);
            card.setX(Const.BASE_STARTX);
            base.add(card);
        }

    }

    private void clearListsIn(List<LinkedList<Card>> list) {
        for (List<Card> cards : list)
            cards.clear();
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public List<LinkedList<Card>> getColumns() {
        return columns;
    }

    public boolean isColumnsEmpty() {
        for (List<Card> column : columns)
            if (!column.isEmpty())
                return false;
        return true;
    }

    public List<LinkedList<Card>> getFoundation() {
        return foundation;
    }

    public LinkedList<Card> getBase() {
        return base;
    }

    public LinkedList<Card> getOpenBase() {
        return openBase;
    }

    public void drawFoundationShape(Graphics2D g2) {
        for (int i = 0; i < Const.SUITS; i++) {
            g2.drawRect(Const.FOUNDATION_STARTX - 2 + i * (Const.CARD_WIDTH + Const.FOUNDATION_DIST), Const.FOUNDATION_STARTY - 2,
                    Const.CARD_WIDTH + 4, Const.CARD_HEIGHT + 4);
        }
    }
}

