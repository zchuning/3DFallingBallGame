/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import java.awt.*;
import javax.swing.*;

class Game implements Runnable {
    public void run() {

        final JFrame frame = new JFrame("How Far Can You Go?");
        frame.setLocation(300, 300);

        // Instructions
        JOptionPane.showMessageDialog(frame, "Welcome to \"How Far Can You Go?\"\n" +
                "The game is simple, avoid going over the ceiling or falling through the floor.\n" +
                "And go as far as you can!\n\n" +
                "Controls:\n" +
                "Use A and D to move left and right;\n" +
                "Use SPACE to jump;\n" +
                "And don't forget to try LEFTARROW and RIGHTARROW. They're beyond your imagination...\n",
                "Welcome", JOptionPane.PLAIN_MESSAGE);

        // Score panel
        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.NORTH);
        final JLabel status = new JLabel("Score: ");
        status_panel.add(status);

        // Main display
        final Display disp = new Display(status);
        disp.setBackground(Color.BLACK);
        frame.add(disp, BorderLayout.CENTER);

        // Control Panel
        final JPanel control_panel = new JPanel();
        frame.add(control_panel, BorderLayout.SOUTH);

        // Reset button
        final JButton reset = new JButton("Reset");
        reset.addActionListener(e -> disp.reset());
        control_panel.add(reset);

        // Pause button
        final JButton pause = new JButton("Pause");
        pause.addActionListener(e -> disp.pause());
        control_panel.add(pause);

        // Resume button
        final JButton resume = new JButton("Resume");
        resume.addActionListener(e -> disp.resume());
        control_panel.add(resume);


        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // THE GAME BEGINS
        disp.init();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}