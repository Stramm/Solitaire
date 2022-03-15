package com.moggendorf.solitaire;

public enum SuitColor {
    SPADES(0), HEARTS(1), CLUBS(2), DIAMONDS(3);

    int id;

    SuitColor(int id) {
        this.id = id;
    }

    public boolean isOppositeColor(SuitColor that) {
        return this.id % 2 != that.id % 2;
    }

    public static SuitColor getForID(int id) {
        return values()[id];
    }

}
