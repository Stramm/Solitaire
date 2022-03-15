package com.moggendorf.solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoPanel extends JPanel {
    private Display canvas;
    private JButton newGame;
    private JButton quit;

    public InfoPanel(Display canvas) {
        this.canvas = canvas;
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(Const.F_WIDTH, 40));

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, Const.COLUMNS_STARTX, 5, Const.COLUMNS_STARTX));
        add(Box.createHorizontalGlue());

        ActionListener al = new InfoActionListener();

        newGame = new JButton("New Game");
        newGame.addActionListener(al);
        add(newGame);
        quit = new JButton("Quit");
        quit.addActionListener(al);
        add(quit);
    }

    class InfoActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == newGame) {
                canvas.startNewGame();
            } else if (evt.getSource() == quit) {
                System.exit(0);
            }
        }
    }
}
