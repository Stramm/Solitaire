package com.moggendorf.solitaire;

public class Const {
    public static final String CARD_PATH = "cards/";
    public static final int SUITS = 4;
    public static final int COLUMNS = 7;

    public static final int F_WIDTH = 800;
    public static final int F_HEIGHT = 700;


    public static final int CARD_WIDTH = 80;
    public static final int CARD_HEIGHT = 108;
    public static final int CARD_DIST = 24;
    public static final int DECK_SIZE = 52;


    public static final int FOUNDATION_STARTX = 30;
    public static final int FOUNDATION_STARTY = 30;
    public static final int FOUNDATION_DIST = 28;
    public static final int COLUMNS_STARTX = 30;
    public static final int COLUMNS_STARTY = 180;
    public static final int COLUMNS_DIST = 28;

    public static final int BASE_STARTX = FOUNDATION_STARTX + ((COLUMNS - 1) * (CARD_WIDTH + FOUNDATION_DIST));
    public static final int BASE_STARTY = FOUNDATION_STARTY;
    public static final int BASE_OPEN_STARTX = BASE_STARTX - CARD_WIDTH - FOUNDATION_DIST;
    public static final int BASE_OPEN_STARTY = FOUNDATION_STARTY;;
}
