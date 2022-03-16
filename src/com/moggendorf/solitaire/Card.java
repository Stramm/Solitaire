package com.moggendorf.solitaire;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Card implements Cloneable {
    private int x;
    private int y;
    private int value;
    private boolean faceUp;
    private SuitColor color;
    private BufferedImage back; // saving a ref to the back here if maybe different backs are necessary one time
    private BufferedImage image;

    public Card(int idx, ImageCache cache) {
        initCard(idx, cache);
    }

    private void initCard(int idx, ImageCache cache) {
        back = ImageCache.getBack();
        image = ImageCache.getImages()[idx];
        value = idx % 13 + 1;
        color = SuitColor.getForID(idx / 13);
    }

    public void drawCard(Graphics2D g2) {
        g2.drawImage(faceUp ? image : back, x, y, Const.CARD_WIDTH, Const.CARD_HEIGHT, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public int getValue() {
        return value;
    }

    public SuitColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return color + " " + value + "(" + x +", " + y +")";
    }

    // for redo
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
