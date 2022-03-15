package com.moggendorf.solitaire;

import javax.swing.*;
import java.awt.*;

public class Canvas extends JPanel {
    public Canvas() {
        setLayout(new BorderLayout());
        Display display = new Display();
        InfoPanel info = new InfoPanel(display);

        add(display, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);

    }
}
