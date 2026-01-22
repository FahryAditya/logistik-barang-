import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SimpleQuizGUI {
    private JFrame frame;
    private JLabel questionLabel, scoreLabel, progressLabel;
    private JButton[] optionButtons = new JButton[3];
    private int currentIndex = 0;
    private int score = 0;

    public SimpleQuizGUI() {
        frame = new JFrame("Java-Python Quiz");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel atas
        JPanel topPanel = new JPanel(new BorderLayout());
        progressLabel = new JLabel("Question: 1");
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(progressLabel, BorderLayout.WEST);
        topPanel.add(scoreLabel, BorderLayout.EAST);

        // Panel pertanyaan
        questionLabel = new JLabel("Question will appear here");
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.add(questionLabel, BorderLayout.CENTER);

        // Panel jawaban
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        for (int i = 0; i < 3; i++) {
            optionButtons[i] = new JButton("Option " + (i+1));
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 16));
            optionButtons[i].setBackground(new Color(135, 206, 250));
            optionButtons[i].setFocusPainted(false);
            optionsPanel.add(optionButtons[i]);
            int idx = i;
            optionButtons[i].addActionListener(e -> checkAnswer(optionButtons[idx].getText()));
        }

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(questionPanel, BorderLayout.CENTER);
        frame.add(optionsPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadQuestion(currentIndex);
    }

    private void loadQuestion(int index) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "quiz.py", "get_question", String.valueOf(index));
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            if (line.equals("FINISHED")) {
                JOptionPane.showMessageDialog(frame, "Quiz Finished!\nYour score: " + score);
                frame.dispose();
                return;
            }

            // Pisahkan string
            String[] parts = line.split("\\|");
            questionLabel.setText("<html><body style='text-align:center'>" + parts[0] + "</body></html>");
            for (int i = 0; i < 3; i++) {
                optionButtons[i].setText(parts[i+1]);
            }

            progressLabel.setText("Question: " + parts[4] + "/" + parts[5]);
            scoreLabel.setText("Score: " + score);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAnswer(String selected) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "quiz.py", "check_answer", String.valueOf(currentIndex), selected);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();

            if(result.equals("correct")) score++;

            JOptionPane.showMessageDialog(frame, result.equals("correct") ? "Correct!" : "Wrong!");
            currentIndex++;
            loadQuestion(currentIndex);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleQuizGUI());
    }
}
