import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.json.JSONObject;

public class ModernQuizGUI {
    private JFrame frame;
    private JLabel questionLabel, scoreLabel, progressLabel;
    private JButton[] optionButtons = new JButton[3];
    private int currentIndex = 0;
    private int score = 0;

    public ModernQuizGUI() {
        frame = new JFrame("Java-Python Quiz");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel atas: progress & score
        JPanel topPanel = new JPanel(new BorderLayout());
        progressLabel = new JLabel("Question: 1");
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(progressLabel, BorderLayout.WEST);
        topPanel.add(scoreLabel, BorderLayout.EAST);

        // Panel tengah: pertanyaan
        questionLabel = new JLabel("Question will appear here");
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.add(questionLabel, BorderLayout.CENTER);

        // Panel bawah: jawaban
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        for (int i = 0; i < 3; i++) {
            optionButtons[i] = new JButton("Option " + (i+1));
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 16));
            optionButtons[i].setBackground(new Color(135, 206, 250));
            optionButtons[i].setFocusPainted(false);
            optionsPanel.add(optionButtons[i]);
            int index = i;
            optionButtons[i].addActionListener(e -> checkAnswer(optionButtons[index].getText()));
        }

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(questionPanel, BorderLayout.CENTER);
        frame.add(optionsPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null); // tengah layar
        frame.setVisible(true);

        loadQuestion(currentIndex);
    }

    private void loadQuestion(int index) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "quiz.py", "get_question", String.valueOf(index));
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String jsonText = reader.readLine();
            JSONObject obj = new JSONObject(jsonText);

            if (obj.has("question") && obj.getString("question").equals("Finished")) {
                JOptionPane.showMessageDialog(frame, "Quiz Finished!\nYour score: " + score);
                frame.dispose();
                return;
            }

            questionLabel.setText("<html><body style='text-align:center'>" + obj.getString("question") + "</body></html>");
            for (int i = 0; i < 3; i++) {
                optionButtons[i].setText(obj.getJSONArray("options").getString(i));
            }

            progressLabel.setText("Question: " + obj.getInt("index") + "/" + obj.getInt("total"));
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
        SwingUtilities.invokeLater(() -> new ModernQuizGUI());
    }
}
