import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class QuizApp extends JFrame {
    private JLabel headerLabel;
    private JTextArea questionArea;
    private JButton[] optionButtons;
    private int currentIndex = 0;
    private int score = 0;
    private int totalQuestions = 0;
    
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color HEADER_BG = new Color(79, 70, 229);
    private static final Color BUTTON_BG = new Color(99, 102, 241);
    private static final Color BUTTON_HOVER = new Color(79, 70, 229);
    private static final Color CORRECT_COLOR = new Color(34, 197, 94);
    private static final Color WRONG_COLOR = new Color(239, 68, 68);
    
    public QuizApp() {
        setupUI();
        loadQuestion();
    }
    
    private void setupUI() {
        setTitle("Quiz Interaktif");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND);
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setPreferredSize(new Dimension(700, 80));
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        headerLabel = new JLabel("Soal 1 dari ?");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JLabel scoreLabel = new JLabel("Skor: 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        headerPanel.add(scoreLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Question Panel
        JPanel questionPanel = new JPanel();
        questionPanel.setBackground(BACKGROUND);
        questionPanel.setLayout(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));
        
        questionArea = new JTextArea();
        questionArea.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);
        questionArea.setBackground(BACKGROUND);
        questionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        questionPanel.add(questionArea, BorderLayout.CENTER);
        
        add(questionPanel, BorderLayout.CENTER);
        
        // Options Panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(3, 1, 0, 15));
        optionsPanel.setBackground(BACKGROUND);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        
        optionButtons = new JButton[3];
        for (int i = 0; i < 3; i++) {
            final int answerIndex = i;
            optionButtons[i] = createOptionButton("");
            optionButtons[i].addActionListener(e -> checkAnswer(answerIndex));
            optionsPanel.add(optionButtons[i]);
        }
        
        add(optionsPanel, BorderLayout.SOUTH);
    }
    
    private JButton createOptionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(600, 60));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_BG);
            }
        });
        
        return button;
    }
    
    private void loadQuestion() {
        String result = callPython("get_question", String.valueOf(currentIndex));
        
        if (result.equals("END")) {
            showFinalScore();
            return;
        }
        
        String[] parts = result.split("\\|");
        if (parts.length >= 6) {
            String question = parts[0];
            totalQuestions = Integer.parseInt(parts[5]);
            
            questionArea.setText(question);
            
            for (int i = 0; i < 3; i++) {
                optionButtons[i].setText((char)('A' + i) + ". " + parts[i + 1]);
            }
            
            updateHeader();
        }
    }
    
    private void checkAnswer(int answerIndex) {
        // Disable all buttons
        for (JButton btn : optionButtons) {
            btn.setEnabled(false);
        }
        
        String result = callPython("check_answer", String.valueOf(currentIndex), String.valueOf(answerIndex));
        
        boolean isCorrect = result.equals("correct");
        
        if (isCorrect) {
            score++;
            optionButtons[answerIndex].setBackground(CORRECT_COLOR);
            showPopup("Benar! ✓", "Jawaban Anda benar!", CORRECT_COLOR);
        } else {
            optionButtons[answerIndex].setBackground(WRONG_COLOR);
            showPopup("Salah! ✗", "Jawaban Anda kurang tepat.", WRONG_COLOR);
        }
        
        // Wait and move to next question
        Timer timer = new Timer(1500, e -> {
            currentIndex++;
            resetButtons();
            loadQuestion();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void resetButtons() {
        for (JButton btn : optionButtons) {
            btn.setEnabled(true);
            btn.setBackground(BUTTON_BG);
        }
    }
    
    private void updateHeader() {
        headerLabel.setText("Soal " + (currentIndex + 1) + " dari " + totalQuestions);
        
        // Update score in header
        Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && ((JLabel)comp).getText().startsWith("Skor:")) {
                ((JLabel)comp).setText("Skor: " + score);
            }
        }
    }
    
    private void showPopup(String title, String message, Color color) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        
        dialog.add(panel);
        
        Timer timer = new Timer(1000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
        
        dialog.setVisible(true);
    }
    
    private void showFinalScore() {
        JDialog dialog = new JDialog(this, "Quiz Selesai!", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setBackground(HEADER_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel titleLabel = new JLabel("🎉 Quiz Selesai! 🎉");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel scoreLabel = new JLabel("Skor Anda: " + score + " / " + totalQuestions);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scoreLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        double percentage = (double)score / totalQuestions * 100;
        JLabel percentLabel = new JLabel(String.format("(%.1f%%)", percentage));
        percentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        percentLabel.setForeground(Color.WHITE);
        percentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(percentLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JButton closeButton = new JButton("Tutup");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(HEADER_BG);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> System.exit(0));
        panel.add(closeButton);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private String callPython(String... args) {
        try {
            String[] command = new String[args.length + 2];
            command[0] = "python";
            command[1] = "quiz_backend.py";
            System.arraycopy(args, 0, command, 2, args.length);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            
            process.waitFor();
            return result != null ? result : "";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            QuizApp app = new QuizApp();
            app.setVisible(true);
        });
    }
}
