package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UITheme {
    public static final Color COLOR_ACCENT = new Color(125, 38, 205);
    public static final Color COLOR_BG_CONTENT = new Color(250, 250, 251);
    public static final Color COLOR_TEXT_HEADER = new Color(55, 65, 81);
    public static final Color COLOR_BORDER = new Color(230, 230, 230);
    public static final Color COLOR_HEADER_BG = new Color(245, 243, 255);
    public static final Color COLOR_TEXT_BODY = new Color(31, 41, 55);
    public static final Color COLOR_STATUS_GREEN = new Color(34, 197, 94);
    public static final Color COLOR_STATUS_RED = new Color(239, 68, 68);
    public static final Color COLOR_SELECTION = new Color(243, 232, 255);
    public static final Color COLOR_HOVER = new Color(243, 232, 255);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);

    private UITheme() {}

    public static JButton createStyledButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        if (primary) {
            btn.setBackground(COLOR_ACCENT);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(COLOR_TEXT_HEADER);
            btn.setBorder(new LineBorder(COLOR_BORDER, 1));
            btn.setBorderPainted(true);
        }
        return btn;
    }

    public static JPanel createModernCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_HEADER);
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    public static JTextField createModernTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }
}