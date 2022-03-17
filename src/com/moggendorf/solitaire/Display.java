package com.moggendorf.solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Stack;

public class Display extends JPanel {
    private Deck deck;
    private Layout layout;
    private Stack<Layout> previousStates;
    private LinkedList<Card> draggedCards;
    private boolean continuousRedo; // the first redo action needs to take two elements from the stack


    public Display() {
        initComponent();
    }

    private void initComponent() {
        layout = new Layout();
        previousStates = new Stack<>();

        MyMouseListener listener = new MyMouseListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);

        draggedCards = new LinkedList<>();

        startNewGame();
    }

    public void startNewGame() {
        deck = new Deck();
        layout.setDeck(deck);
        layout.initLayout();

        previousStates.clear();
        draggedCards.clear();
        saveState();
        continuousRedo = false;
        repaint();
    }

    public void endGame() {
        System.exit(0);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // shape of the foundation
        layout.drawFoundationShape(g2);
        // shape for the base
        layout.drawBaseShape(g2);
        // columns cards
        for (LinkedList<Card> cards : layout.getColumns())
            for (Card card : cards)
                card.drawCard(g2);
        // foundation cards
        for (LinkedList<Card> cards : layout.getFoundation())
            if (!cards.isEmpty())
                cards.getLast().drawCard(g2); // just draw the upper card
        // base
        if (!layout.getBase().isEmpty())
            layout.getBase().getLast().drawCard(g2);

        if (!layout.getOpenBase().isEmpty())
            layout.getOpenBase().getLast().drawCard(g2);

        // the draggedCards... taken from its list and added to a temp list that's painted last... that way they stay on top
        for (Card card : draggedCards)
            card.drawCard(g2);
    }
    private void saveState() {
        continuousRedo = false;
        try {
            previousStates.push((Layout)layout.clone());
        } catch (CloneNotSupportedException e) {
            previousStates.clear();
        }
    }

    public void restorePrevState() {
        if (previousStates.isEmpty())
            return;

        if (!continuousRedo)
            previousStates.pop();
        continuousRedo = true;

        if (!previousStates.isEmpty()) {
            layout = previousStates.pop();
            draggedCards.clear();
        }

        if (previousStates.isEmpty())
            saveState();

        repaint();
    }

    class MyMouseListener extends MouseAdapter {
        private boolean selected;
        private int dx;
        private int dy;
        private int origin;

        private Source source;


        @Override
        public void mousePressed(MouseEvent e) {
            // for flipping the cards on the base (top right)
            if (e.getButton() == MouseEvent.BUTTON1 && e.getY() > Const.FOUNDATION_STARTY && e.getY() < Const.FOUNDATION_STARTY + Const.CARD_HEIGHT
                    && e.getX() > Const.BASE_STARTX && e.getX() < Const.BASE_STARTX + Const.CARD_WIDTH) {
                // a click inside the base area... now check more precise
                if (!layout.getBase().isEmpty()) {
                    Card card = layout.getBase().pollLast();
                    card.setFaceUp(true);
                    card.setX(Const.BASE_OPEN_STARTX);
                    layout.getOpenBase().add(card);
                } else {
                    // base is empty... check if open base has elements and put them back to the base, face down
                    while (!layout.getOpenBase().isEmpty()) {
                        Card card = layout.getOpenBase().pollLast();
                        card.setX(Const.BASE_STARTX);
                        card.setFaceUp(false);
                        layout.getBase().add(card);
                    }
                }
                saveState();
                repaint();

                // all rmb actions from here on: 1. open base for quick drop to foundation
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                // rmb on open base
                if (e.getY() > Const.BASE_OPEN_STARTY && e.getY() < Const.BASE_OPEN_STARTY + Const.CARD_HEIGHT
                        && e.getX() > Const.BASE_OPEN_STARTX && e.getX() < Const.BASE_OPEN_STARTX + Const.CARD_WIDTH) {

                    draggedCards.add(layout.getOpenBase().pollLast());
                    source = Source.BASE;
                    if (!tryFitsOnFoundation() && !tryFitsOnColumn()) {
                        backToOrigin();
                        repaint();
                    }
                } else { // 2. rmb on columns for quick drop to foundation or move to other columns
                    for (int idx = 0; idx < layout.getColumns().size(); idx++) {
                        // if the current column is empty, go to the next
                        LinkedList<Card> column = getColumn(idx);
                        if (column.isEmpty())
                            continue;

                        // get the index of the clicked card (if valid)
                        int idy = clickCardIndexInColumn(column, e.getX(), e.getY());

                        if (idy != -1) {
                            // all cards from the selected on copied to draggedCards list
                            selectToDrag(getColumn(idx), idy);
                            origin = idx;
                            source = Source.COLUMN;

                            if (draggedCards.size() == 1) { // if only one card is selected we try to place it on found. and columns
                                if (!tryFitsOnFoundation() && !tryFitsOnColumn()) {
                                    backToOrigin();
                                    repaint();
                                }
                            } else { // more cards selected, try to fit on only on another colum
                                if (!tryFitsOnColumn()) {
                                    backToOrigin();
                                    repaint();
                                    return;
                                }
                            }

                        }
                    } // end for idx;
                }
            }
        }
        // checking here if the drag destination is valid

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selected) {
                selected = false;
                // released on a column?
                if (e.getY() > Const.COLUMNS_STARTY) {
                    for (int idx = 0; idx < layout.getColumns().size(); idx++) {
                        //Card currCard = layout.getColumns().get(idx).getLast();
                        int currColumnX = getColumnX(idx);

                        if (e.getX() > currColumnX && e.getX() < currColumnX + Const.CARD_WIDTH
                                && e.getY() > Const.COLUMNS_STARTY && e.getY() < Const.F_HEIGHT) {
                            // yes, column detected, is it empty, then just drop there
                            int startY = 0;
                            Card source = draggedCards.getFirst();
                            if (getColumn(idx).isEmpty()) {
                                // is it a king? then dropping is allowed
                                if (source.getValue() != 13) {
                                    backToOrigin();
                                    return;
                                }
                                startY = Const.COLUMNS_STARTY;
                            } else { // add to a not empty column
                                // check if last card is opposite color and value of it is 1 less
                                Card target = getColumn(idx).getLast();
                                if (source.getValue() + 1 != target.getValue()
                                        || !source.getColor().isOppositeColor(target.getColor())) {

                                    backToOrigin();
                                    return;
                                }
                                startY = getColumn(idx).getLast().getY() + Const.CARD_DIST;
                            }
                            // now we have x (currColumnX) and y (startY) for the first card in draggedCards.
                            updateDraggedPosition(currColumnX, startY);
                            moveToDestColumn(idx);
                            // faceUp if there's still a card on the origin column
                            faceUp();

                            saveState();
                            repaint();
                            return;
                        }
                    }
                } else {
                    // check if released onto foundation, drop on the entire foundation is OK, code looks for fitting column
                    if (e.getY() > Const.FOUNDATION_STARTY && e.getY() < Const.FOUNDATION_STARTY + Const.CARD_HEIGHT
                            && e.getX() > Const.FOUNDATION_STARTX && e.getX() < Const.FOUNDATION_STARTX + 4 * (Const.CARD_WIDTH + Const.FOUNDATION_DIST)
                            && draggedCards.size() == 1) { // we only can drag one card here and check the foundation entire area
                        // go through all foundation columns and look at the upper card
                        for (int idx = 0; idx < layout.getFoundation().size(); idx++) {
                            // is same color and value +1 or, if foundation is empty, an ace?
                            Card probedCard = draggedCards.getFirst(); // the probed card
                            // get the value of the card on the foundation
                            // 0, if empty or the value of the upper card
                            int targetValue = layout.getFoundation().get(idx).isEmpty() ? 0 : layout.getFoundation().get(idx).getLast().getValue();
                            if ((layout.getFoundation().get(idx).isEmpty() || // if there's no card on the foundation the color does not matter
                                    layout.getFoundation().get(idx).getFirst().getColor() == probedCard.getColor()) // otherwise check for same color
                                    && probedCard.getValue() == targetValue + 1) { // and if the values match (probed card is next value)

                                updateDraggedPosition(getFoundationX(idx), Const.FOUNDATION_STARTY);
                                moveToDestFoundation(idx);

                                // faceUp if there's still a card on the origin column
                                faceUp();

                                saveState();
                                repaint();
                                return;
                            }
                        }
                    }
                }

                // not dropped on a valid target...
                backToOrigin();
            }
        }
        // here getting the cards to drag

        @Override
        public void mouseDragged(MouseEvent e) {
            // if the selected boolean is set, we have a filled draggedCards list
            // so we just need to update the position of these selected cards and repaint the canvas till mouse released
            if (selected) { // drag
                updateDraggedPosition(e.getX() - dx, e.getY() - dy);
                repaint();
                return;
            }

            // a mouse press... now check if the click happened on a valid card
            // first check the columns
            // go through all of them and check if the mouse is over a face up card.
            // if so, move this and all cards below that card to another temp list (dragged cards) and dragged them
            // around in the above if condition
            if (e.getY() > Const.COLUMNS_STARTY) { // the columns
                for (int idx = 0; idx < layout.getColumns().size(); idx++) {
                    // if the current column is empty, go to the next
                    LinkedList<Card> column = getColumn(idx);
                    if (column.isEmpty())
                        continue;

                    // go through the cards in the current column (from the last (upper) to the first)
                    int idy = clickCardIndexInColumn(column, e.getX(), e.getY());

                    if (idy != -1) {
                        Card currCard = column.get(idy);
                        // we have a hit, that's the card we want to drag. The boolean saves that found condition
                        selected = true;
                        source = Source.COLUMN;
                        origin = idx; // save the origin columns index
                        // move all selected cards to the draggedCards list (removing from the column)
                        selectToDrag(column, idy); // saved in draggedCards
                        dx = e.getX() - currCard.getX(); // and save the cursor to card edge difference for a smooth drag start
                        dy = e.getY() - currCard.getY();
                        return; // and end the execution of this method
                    }
                }
                // checking a drag on the foundation
            } else if (e.getY() > Const.FOUNDATION_STARTY && e.getY() < Const.FOUNDATION_STARTY + Const.CARD_HEIGHT
                    && e.getX() < Const.BASE_OPEN_STARTX) {
                for (int idx = 0; idx < layout.getFoundation().size(); idx++) {
                    LinkedList<Card> foundation = getFoundation(idx);
                    if (foundation.isEmpty())
                        continue;

                    Card currCard = foundation.getLast();
                    // finally we can check if the mouse cursor is inside the current cards dimensions
                    if (e.getX() > currCard.getX() && e.getX() < currCard.getX() + Const.CARD_WIDTH) {
                        // we have a hit, that's the card we want to drag. The boolean saves that found condition
                        selected = true;
                        source = Source.FOUNDATION;
                        origin = idx; // save the origin foundations index
                        // move all selected cards to the draggedCards list (removing from the column)
                        selectToDrag(foundation, foundation.size() - 1); // saved in draggedCards
                        dx = e.getX() - currCard.getX(); // and save the cursor to card edge difference for a smooth drag start
                        dy = e.getY() - currCard.getY();
                        return; // and end the execution of this method
                    }
                }
                // and finally check if dragging from the open base
            } else if (e.getX() > Const.BASE_OPEN_STARTX && e.getX() < Const.BASE_OPEN_STARTX + Const.CARD_WIDTH) {
                LinkedList<Card> base = layout.getOpenBase();
                if (base.isEmpty())
                    return;

                Card currCard = base.getLast();

                // we have a hit, that's the card we want to drag. The boolean saves that found condition
                selected = true;
                source = Source.BASE;
                // move all selected cards to the draggedCards list (removing from the column)
                selectToDrag(base, base.size() - 1); // saved in draggedCards
                // origin = 0; // save the origin foundations index
                dx = e.getX() - currCard.getX(); // and save the cursor to card edge difference for a smooth drag start
                dy = e.getY() - currCard.getY();
                return;

            }
        }
        // helper methods for the Adapter class

        private boolean tryFitsOnFoundation() {
            // now x, go through all foundation elements
            for (int idf = 0; idf < layout.getFoundation().size(); idf++) {
                int currFoundationX = getFoundationX(idf);

                // is same color and value +1 or, if foundation is empty, an ace?
                Card source = draggedCards.getFirst();
                if (source == null)
                    return false;
                // 0, if empty or the value of the upper card
                int targetValue = layout.getFoundation().get(idf).isEmpty() ? 0 : layout.getFoundation().get(idf).getLast().getValue();
                if ((layout.getFoundation().get(idf).isEmpty() ||
                        layout.getFoundation().get(idf).getFirst().getColor() == source.getColor()) // same color
                        && source.getValue() == targetValue + 1) { // and next value

                    updateDraggedPosition(currFoundationX, Const.FOUNDATION_STARTY);
                    moveToDestFoundation(idf);

                    // faceUp if there's still a card on the origin column
                    faceUp();
                    saveState();
                    repaint();
                    return true;
                }
            }
            return false;
        }

        private boolean tryFitsOnColumn() {
            // now x, go through all foundation elements
            for (int idc = 0; idc < Const.COLUMNS; idc++) {

                if (source == Source.COLUMN && idc == origin) // don't test same column
                    continue;

                boolean move = false;
                int currColumnX = getColumnX(idc);
                int dy = Const.COLUMNS_STARTY;
                // is same color and value +1 or, if foundation is empty, an ace?
                Card source = draggedCards.getFirst();

                if (layout.getColumns().get(idc).isEmpty()) {
                    if (source.getValue() == 13) { // we can move a card on an empty list, if it is a king
                        move = true;
                    }
                } else if (layout.getColumns().get(idc).getLast().getColor().isOppositeColor(source.getColor()) // same color
                        && source.getValue() + 1 == layout.getColumns().get(idc).getLast().getValue()) {
                    move = true;
                    dy = layout.getColumns().get(idc).getLast().getY() + Const.CARD_DIST;
                }

                if (move) {
                    updateDraggedPosition(currColumnX, dy);
                    moveToDestColumn(idc);

                    // faceUp if there's still a card on the origin column
                    faceUp();
                    saveState();
                    repaint();
                    return true;
                }
            }
            return false;
        }

        private int clickCardIndexInColumn(LinkedList<Card> column, int clickX, int clickY) {
            for (int idy = column.size() - 1; idy >= 0; idy--) {
                Card currCard = column.get(idy);

                // if the card is not faceup, then this is not a valid selection
                if (!currCard.isFaceUp())
                    return -1; // to next column

                if (clickX > currCard.getX() && clickX < currCard.getX() + Const.CARD_WIDTH
                        && clickY > currCard.getY() && clickY < currCard.getY() + Const.CARD_HEIGHT) {
                    // found a click on a face up card in the column, return its index
                    return idy;
                }
            }
            return -1;
        }

        private void faceUp() {
            if (!getColumn(origin).isEmpty())
                getColumn(origin).getLast().setFaceUp(true);

            testWin();
        }

        private int getColumnX(int idx) {
            return Const.COLUMNS_STARTX + (idx * (Const.CARD_WIDTH + Const.COLUMNS_DIST));
        }

        private int getColumnY(int idx) {
            return getColumn(idx).isEmpty() ? Const.COLUMNS_STARTY : getColumn(idx).getLast().getY() + Const.CARD_DIST;
        }

        private int getFoundationX(int idx) {
            return Const.FOUNDATION_STARTX + (idx * (Const.CARD_WIDTH + Const.FOUNDATION_DIST));
        }

        private void backToOrigin() {
            switch (source) {
                case COLUMN :
                    updateDraggedPosition(getColumnX(origin), getColumnY(origin));
                    moveToDestColumn(origin);
                    break;
                case FOUNDATION :
                    updateDraggedPosition(getColumnX(origin), Const.FOUNDATION_STARTY);
                    moveToDestFoundation(origin);
                    break;
                case BASE :
                    updateDraggedPosition(Const.BASE_OPEN_STARTX, Const.BASE_OPEN_STARTY);
                    moveToOpenBase();
                    break;
            }
            repaint();
        }

        private LinkedList<Card> getColumn(int idx) {
            return layout.getColumns().get(idx);
        }

        private LinkedList<Card> getFoundation(int idx) {
            return layout.getFoundation().get(idx);
        }

        private void selectToDrag(LinkedList<Card> column, int index) {
            while (column.size() > index)
                draggedCards.add(column.remove(index));
        }

        private void moveToDestColumn(int idx) {
            getColumn(idx).addAll(draggedCards);
            draggedCards.clear();
            testWin();
        }

        private void moveToDestFoundation(int idx) {
            layout.getFoundation().get(idx).addAll(draggedCards);
            draggedCards.clear();
            testWin();
        }

        private void moveToOpenBase() {
            layout.getOpenBase().addAll(draggedCards);
            draggedCards.clear();

        }

        // todo: add nicer restart and win animation
        private boolean testWin() {
            if (!layout.getOpenBase().isEmpty() || !layout.getBase().isEmpty())
                return false; // base has to be empty for a win

            // if all columns are in series... -> win
            for (LinkedList<Card> column : layout.getColumns()) {
                int prev = 0; // card values start at 1
                for (Card card : column) {
                    if (!card.isFaceUp())
                        return false;
                    if (prev >= card.getValue())
                        return false;
                }
            }
            allColumnToFoundation();

            int newGame = JOptionPane.showConfirmDialog(null, "You won this game! Start a new one?", "You won", JOptionPane.YES_NO_OPTION);
            if (newGame == JOptionPane.YES_OPTION)
                startNewGame();
            else
                endGame();

            return true;
        }

        // if win detected in testWin... move all column cards to foundation...
        // todo: add animation moving cards to foundation
        private void allColumnToFoundation() {
            for (int val = 1; val <= 13; val++) {
                for (int i = 0; i < Const.COLUMNS; i++) {

                    if (!getColumn(i).isEmpty() && getColumn(i).getLast().getValue() == val) {
                        Card source = getColumn(i).removeLast();
                        for (int idf = 0; idf < layout.getFoundation().size(); idf++) {

                            int targetValue = layout.getFoundation().get(idf).isEmpty() ? 0 : layout.getFoundation().get(idf).getLast().getValue();
                            if ((layout.getFoundation().get(idf).isEmpty() ||
                                    layout.getFoundation().get(idf).getFirst().getColor() == source.getColor()) // same color
                                    && source.getValue() == targetValue + 1) { // and next value


                                source.setX(getFoundationX(idf));
                                source.setY(Const.FOUNDATION_STARTY);

                                layout.getFoundation().get(idf).add(source);

                                repaint();
                            }
                        }
                    }
                }
            } // end outer for
        }

        private void updateDraggedPosition(int dx, int dy) {
            for (Card card : draggedCards) {
                card.setX(dx);
                card.setY(dy);
                dy += Const.CARD_DIST;
            }
        }

    } // end MouseAdapter

}

