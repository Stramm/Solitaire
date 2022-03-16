package com.moggendorf.solitaire;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageCache {
    private static BufferedImage[] images;
    private static BufferedImage back;

    static {
        images = new BufferedImage[Const.DECK_SIZE]; // deck and back
        try {
            for (int i = 0; i < Const.DECK_SIZE; i++)
                images[i] = ImageIO.read(Card.class.getResource(Const.CARD_PATH + (i + 1) + ".gif"));
                back = ImageIO.read(Card.class.getResource(Const.CARD_PATH + "back.gif"));
        } catch (IOException ignore) { }
    }

    public static BufferedImage[] getImages() {
        return images;
    }

    public static BufferedImage getBack() {
        return back;
    }
}
