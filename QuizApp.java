import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.awt.*;
import java.io.*;

public class QuizApp extends JFrame {
    private JLabel headerLabel;
    private JTextArea questionArea;
    private JButton[] optionButtons;
    private int currentIndex = 0;
    private int score = 0;
    private int totalQuestions = 0;
    
    // XML Configuration
    private QuizConfig config;
    
    private Color BACKGROUND;
    private Color HEADER_BG;
    private Color BUTTON_BG;
    private Color BUTTON_HOVER;
    private Color CORRECT_COLOR;
    private Color WRONG_COLOR;
    
    public QuizApp() {
        loadConfiguration();
        setupUI();
        loadQuestion();
    }
    
    private void loadConfiguration() {
        try {
            File xmlFile = new File("quiz_config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            config = new QuizConfig(doc);
            
            // Load colors from XML
            BACKGROUND = parseColor(config.getBackgroundColor());
            HEADER_BG = parseColor(config.getHeaderBackground());
            BUTTON_BG = parseColor(config.getButtonBackground());
            BUTTON_HOVER = parseColor(config.getButtonHover());
            CORRECT_COLOR = parseColor(config.getCorrectColor());
            WRONG_COLOR = parseColor(config.getWrongColor());
            
            System.out.println("✓ Konfigurasi XML berhasil dimuat!");
            
        } catch (Exception e) {
            System.err.println("⚠ Gagal membaca XML, menggunakan default!");
            e.printStackTrace();
            
            // Fallback to default colors
            BACKGROUND = new Color(245, 247, 250);
            HEADER_BG = new Color(79, 70, 229);
            BUTTON_BG = new Color(99, 102, 241);
            BUTTON_HOVER = new Color(79, 70, 229);
            CORRECT_COLOR = new Color(34, 197, 94);
            WRONG_COLOR = new Color(239, 68, 68);
            
            config = new QuizConfig(); // Default config
        }
    }
    
    private Color parseColor(String hex) {
        hex = hex.replace("#", "");
        return new Color(
            Integer.valueOf(hex.substring(0, 2), 16),
            Integer.valueOf(hex.substring(2, 4), 16),
            Integer.valueOf(hex.substring(4, 6), 16)
        );
    }
    
    private void setupUI() {
        setTitle(config.getTitle());
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND);
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setPreferredSize(new Dimension(config.getWindowWidth(), config.getHeaderHeight()));
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        headerLabel = new JLabel("Soal 1 dari ?");
        headerLabel.setFont(config.getHeaderFont());
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JLabel scoreLabel = new JLabel("Skor: 0");
        scoreLabel.setFont(config.getHeaderFont());
        scoreLabel.setForeground(Color.WHITE);
        headerPanel.add(scoreLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Question Panel
        JPanel questionPanel = new JPanel();
        questionPanel.setBackground(BACKGROUND);
        questionPanel.setLayout(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));
        
        questionArea = new JTextArea();
        questionArea.setFont(config.getQuestionFont());
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
        button.setFont(config.getButtonFont());
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(600, config.getButtonHeight()));
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
            if (config.isShowFeedback()) {
                showPopup(config.getCorrectAnswer(), config.getCorrectDetail(), CORRECT_COLOR);
            }
        } else {
            optionButtons[answerIndex].setBackground(WRONG_COLOR);
            if (config.isShowFeedback()) {
                showPopup(config.getWrongAnswer(), config.getWrongDetail(), WRONG_COLOR);
            }
        }
        
        // Wait and move to next question
        Timer timer = new Timer(config.getNextQuestionDelay(), e -> {
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
        
        Timer timer = new Timer(config.getFeedbackDelay(), e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
        
        dialog.setVisible(true);
    }
    
    private void showFinalScore() {
        JDialog dialog = new JDialog(this, config.getTitle(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setBackground(HEADER_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel titleLabel = new JLabel(config.getQuizComplete());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel scoreLabel = new JLabel(config.getYourScore() + " " + score + " / " + totalQuestions);
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
        
        // Passing score indicator
        panel.add(Box.createVerticalStrut(10));
        String passStatus = percentage >= config.getPassingScore() ? "✓ LULUS" : "✗ TIDAK LULUS";
        JLabel passLabel = new JLabel(passStatus);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passLabel.setForeground(percentage >= config.getPassingScore() ? CORRECT_COLOR : WRONG_COLOR);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JButton closeButton = new JButton(config.getCloseButton());
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

// ============ QuizConfig Class ============
class QuizConfig {
    private String title = "Quiz Interaktif";
    private int timePerQuestion = 0;
    private boolean showFeedback = true;
    private boolean shuffleQuestions = false;
    private int passingScore = 60;
    
    private String backgroundColor = "#F5F7FA";
    private String headerBackground = "#4F46E5";
    private String buttonBackground = "#6366F1";
    private String buttonHover = "#4F46E5";
    private String correctColor = "#22C55E";
    private String wrongColor = "#EF4444";
    
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 20);
    private Font questionFont = new Font("Segoe UI", Font.PLAIN, 22);
    private Font buttonFont = new Font("Segoe UI", Font.PLAIN, 18);
    
    private int windowWidth = 700;
    private int windowHeight = 550;
    private int headerHeight = 80;
    private int buttonHeight = 60;
    
    private String correctAnswer = "Benar! ✓";
    private String wrongAnswer = "Salah! ✗";
    private String correctDetail = "Jawaban Anda benar!";
    private String wrongDetail = "Jawaban Anda kurang tepat.";
    private String quizComplete = "🎉 Quiz Selesai! 🎉";
    private String yourScore = "Skor Anda:";
    private String closeButton = "Tutup";
    
    private int feedbackDelay = 1000;
    private int nextQuestionDelay = 1500;
    
    public QuizConfig() {
        // Default constructor with default values
    }
    
    public QuizConfig(Document doc) {
        try {
            // Settings
            title = getTagValue("title", doc);
            timePerQuestion = Integer.parseInt(getTagValue("timePerQuestion", doc));
            showFeedback = Boolean.parseBoolean(getTagValue("showFeedback", doc));
            shuffleQuestions = Boolean.parseBoolean(getTagValue("shuffleQuestions", doc));
            passingScore = Integer.parseInt(getTagValue("passingScore", doc));
            
            // Theme colors
            backgroundColor = getTagValue("backgroundColor", doc);
            headerBackground = getTagValue("headerBackground", doc);
            buttonBackground = getTagValue("buttonBackground", doc);
            buttonHover = getTagValue("buttonHover", doc);
            correctColor = getTagValue("correctColor", doc);
            wrongColor = getTagValue("wrongColor", doc);
            
            // Fonts
            NodeList headerFontNodes = doc.getElementsByTagName("headerFont");
            if (headerFontNodes.getLength() > 0) {
                Element hf = (Element) headerFontNodes.item(0);
                String family = hf.getElementsByTagName("family").item(0).getTextContent();
                int size = Integer.parseInt(hf.getElementsByTagName("size").item(0).getTextContent());
                String style = hf.getElementsByTagName("style").item(0).getTextContent();
                headerFont = new Font(family, getFontStyle(style), size);
            }
            
            NodeList questionFontNodes = doc.getElementsByTagName("questionFont");
            if (questionFontNodes.getLength() > 0) {
                Element qf = (Element) questionFontNodes.item(0);
                String family = qf.getElementsByTagName("family").item(0).getTextContent();
                int size = Integer.parseInt(qf.getElementsByTagName("size").item(0).getTextContent());
                String style = qf.getElementsByTagName("style").item(0).getTextContent();
                questionFont = new Font(family, getFontStyle(style), size);
            }
            
            NodeList buttonFontNodes = doc.getElementsByTagName("buttonFont");
            if (buttonFontNodes.getLength() > 0) {
                Element bf = (Element) buttonFontNodes.item(0);
                String family = bf.getElementsByTagName("family").item(0).getTextContent();
                int size = Integer.parseInt(bf.getElementsByTagName("size").item(0).getTextContent());
                String style = bf.getElementsByTagName("style").item(0).getTextContent();
                buttonFont = new Font(family, getFontStyle(style), size);
            }
            
            // Dimensions
            windowWidth = Integer.parseInt(getTagValue("windowWidth", doc));
            windowHeight = Integer.parseInt(getTagValue("windowHeight", doc));
            headerHeight = Integer.parseInt(getTagValue("headerHeight", doc));
            buttonHeight = Integer.parseInt(getTagValue("buttonHeight", doc));
            
            // Messages
            correctAnswer = getTagValue("correctAnswer", doc);
            wrongAnswer = getTagValue("wrongAnswer", doc);
            correctDetail = getTagValue("correctDetail", doc);
            wrongDetail = getTagValue("wrongDetail", doc);
            quizComplete = getTagValue("quizComplete", doc);
            yourScore = getTagValue("yourScore", doc);
            closeButton = getTagValue("closeButton", doc);
            
            // Timing
            feedbackDelay = Integer.parseInt(getTagValue("feedbackDelay", doc));
            nextQuestionDelay = Integer.parseInt(getTagValue("nextQuestionDelay", doc));
            
        } catch (Exception e) {
            System.err.println("Error parsing XML, using defaults: " + e.getMessage());
        }
    }
    
    private String getTagValue(String tag, Document doc) {
        NodeList nodeList = doc.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
    
    private int getFontStyle(String style) {
        switch (style.toUpperCase()) {
            case "BOLD": return Font.BOLD;
            case "ITALIC": return Font.ITALIC;
            case "BOLD_ITALIC": return Font.BOLD | Font.ITALIC;
            default: return Font.PLAIN;
        }
    }
    
    // Getters
    public String getTitle() { return title; }
    public int getTimePerQuestion() { return timePerQuestion; }
    public boolean isShowFeedback() { return showFeedback; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public int getPassingScore() { return passingScore; }
    
    public String getBackgroundColor() { return backgroundColor; }
    public String getHeaderBackground() { return headerBackground; }
    public String getButtonBackground() { return buttonBackground; }
    public String getButtonHover() { return buttonHover; }
    public String getCorrectColor() { return correctColor; }
    public String getWrongColor() { return wrongColor; }
    
    public Font getHeaderFont() { return headerFont; }
    public Font getQuestionFont() { return questionFont; }
    public Font getButtonFont() { return buttonFont; }
    
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public int getHeaderHeight() { return headerHeight; }
    public int getButtonHeight() { return buttonHeight; }
    
    public String getCorrectAnswer() { return correctAnswer; }
    public String getWrongAnswer() { return wrongAnswer; }
    public String getCorrectDetail() { return correctDetail; }
    public String getWrongDetail() { return wrongDetail; }
    public String getQuizComplete() { return quizComplete; }
    public String getYourScore() { return yourScore; }
    public String getCloseButton() { return closeButton; }
    
    public int getFeedbackDelay() { return feedbackDelay; }
    public int getNextQuestionDelay() { return nextQuestionDelay; }
}
