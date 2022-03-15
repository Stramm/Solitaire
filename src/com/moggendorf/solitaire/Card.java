package com.moggendorf.solitaire;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Card {
    private static BufferedImage back;
    static {
        try {
            back = ImageIO.read(Card.class.getResource(Const.CARD_PATH + "back.gif"));
        } catch (IOException ignore) { }
    }

    private int x;
    private int y;
    private boolean faceUp;
    private int value;
    private SuitColor color;
    private BufferedImage image;

    public Card(int idx) {
        initCard(idx);
    }

    private void initCard(int idx) {
        try {
            image = ImageIO.read(Card.class.getResource(Const.CARD_PATH + (idx + 1) + ".gif"));
        } catch (IOException ignore) { }
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
        return color + " " + value;
    }
}
