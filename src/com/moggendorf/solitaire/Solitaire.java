package com.moggendorf.solitaire;

import javax.swing.*;

public class Solitaire extends JFrame {
    public void run() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Const.F_WIDTH, Const.F_HEIGHT);
        setTitle(getClass().getSimpleName());

        add(new Canvas());

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Solitaire()::run);
    }
}
