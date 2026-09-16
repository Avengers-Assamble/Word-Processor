import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;

public class Main {

    static JTextPane textPane;

    static JComboBox<String> fontBox;
    static JComboBox<Integer> sizeBox;

    static JToggleButton boldButton;
    static JToggleButton italicButton;
    static JToggleButton underlineButton;

    static textrun currentRun = new textrun("");

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Word Processor");

            frame.setSize(1000, 700);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // =================================================
            // TOP PANEL
            // =================================================

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

            // =================================================
            // FILE TOOLBAR
            // =================================================

            JPanel filePanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT)
            );

            JButton newButton = new JButton("New");
            JButton openButton = new JButton("Open");
            JButton saveButton = new JButton("Save");
            JButton clearButton = new JButton("Clear");

            filePanel.add(newButton);
            filePanel.add(openButton);
            filePanel.add(saveButton);
            filePanel.add(clearButton);

            // =================================================
            // FORMATTING TOOLBAR
            // =================================================

            JPanel formatPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT)
            );

            // Font
            fontBox = new JComboBox<>(new String[]{
                    "Arial",
                    "Times New Roman",
                    "Calibri",
                    "Serif",
                    "SansSerif",
                    "Monospaced"
            });

            fontBox.setPreferredSize(
                    new Dimension(160, 30)
            );

            // Font size
            sizeBox = new JComboBox<>(new Integer[]{
                    8, 10, 12, 14, 16, 18,
                    20, 24, 28, 32, 36, 40
            });

            sizeBox.setSelectedItem(16);

            sizeBox.setPreferredSize(
                    new Dimension(70, 30)
            );

            // Formatting buttons
            boldButton = new JToggleButton("B");
            italicButton = new JToggleButton("I");
            underlineButton = new JToggleButton("U");

            boldButton.setFont(
                    new Font("Arial", Font.BOLD, 14)
            );

            italicButton.setFont(
                    new Font("Arial", Font.ITALIC, 14)
            );

            underlineButton.setFont(
                    new Font("Arial", Font.PLAIN, 14)
            );

            formatPanel.add(new JLabel("Font:"));
            formatPanel.add(fontBox);

            formatPanel.add(new JLabel("Size:"));
            formatPanel.add(sizeBox);

            formatPanel.add(boldButton);
            formatPanel.add(italicButton);
            formatPanel.add(underlineButton);

            // Add both toolbars
            topPanel.add(filePanel);
            topPanel.add(formatPanel);

            // =================================================
            // TEXT AREA
            // =================================================

            textPane = new JTextPane();

            textPane.setFont(
                    FontManager.getFont(currentRun)
            );

            textPane.setMargin(
                    new Insets(20, 20, 20, 20)
            );

            JScrollPane scrollPane =
                    new JScrollPane(textPane);

            // =================================================
            // NEW BUTTON
            // =================================================

            newButton.addActionListener(e -> {

                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Create a new document?",
                        "New Document",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {

                    textPane.setText("");

                    currentRun = new textrun("");

                    fontBox.setSelectedItem("Arial");
                    sizeBox.setSelectedItem(16);

                    boldButton.setSelected(false);
                    italicButton.setSelected(false);
                    underlineButton.setSelected(false);

                    textPane.setFont(
                            FontManager.getFont(currentRun)
                    );
                }
            });

            // =================================================
            // CLEAR BUTTON
            // =================================================

            clearButton.addActionListener(e -> {

                textPane.setText("");
            });

            // =================================================
            // OPEN BUTTON
            // =================================================

            openButton.addActionListener(e -> {

                JFileChooser fileChooser =
                        new JFileChooser();

                int result =
                        fileChooser.showOpenDialog(frame);

                if (result ==
                        JFileChooser.APPROVE_OPTION) {

                    File file =
                            fileChooser.getSelectedFile();

                    try {

                        BufferedReader reader =
                                new BufferedReader(
                                        new FileReader(file)
                                );

                        StringBuilder text =
                                new StringBuilder();

                        String line;

                        while ((line =
                                reader.readLine()) != null) {

                            text.append(line);
                            text.append("\n");
                        }

                        reader.close();

                        textPane.setText(
                                text.toString()
                        );

                        JOptionPane.showMessageDialog(
                                frame,
                                "File opened successfully!"
                        );

                    } catch (IOException ex) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Error opening file!"
                        );
                    }
                }
            });

            // =================================================
            // SAVE BUTTON
            // =================================================

            saveButton.addActionListener(e -> {

                JFileChooser fileChooser =
                        new JFileChooser();

                int result =
                        fileChooser.showSaveDialog(frame);

                if (result ==
                        JFileChooser.APPROVE_OPTION) {

                    File file =
                            fileChooser.getSelectedFile();

                    try {

                        BufferedWriter writer =
                                new BufferedWriter(
                                        new FileWriter(file)
                                );

                        writer.write(
                                textPane.getText()
                        );

                        writer.close();

                        JOptionPane.showMessageDialog(
                                frame,
                                "File saved successfully!"
                        );

                    } catch (IOException ex) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Error saving file!"
                        );
                    }
                }
            });

            // =================================================
            // FONT
            // =================================================

            fontBox.addActionListener(e -> {

                String fontName =
                        (String) fontBox.getSelectedItem();

                currentRun.setFontName(fontName);

                applyFont();
            });

            // =================================================
            // FONT SIZE
            // =================================================

            sizeBox.addActionListener(e -> {

                int size =
                        (Integer) sizeBox.getSelectedItem();

                currentRun.setFontsize(size);

                applyFontSize();
            });

            // =================================================
            // BOLD
            // =================================================

            boldButton.addActionListener(e -> {

                currentRun.setBold(
                        boldButton.isSelected()
                );

                applyBold();
            });

            // =================================================
            // ITALIC
            // =================================================

            italicButton.addActionListener(e -> {

                currentRun.setItalic(
                        italicButton.isSelected()
                );

                applyItalic();
            });

            // =================================================
            // UNDERLINE
            // =================================================

            underlineButton.addActionListener(e -> {

                currentRun.setUnderline(
                        underlineButton.isSelected()
                );

                applyUnderline();
            });

            // =================================================
            // FRAME
            // =================================================

            frame.setLayout(
                    new BorderLayout()
            );

            frame.add(
                    topPanel,
                    BorderLayout.NORTH
            );

            frame.add(
                    scrollPane,
                    BorderLayout.CENTER
            );

            frame.setVisible(true);
        });
    }

    // =====================================================
    // APPLY FONT
    // =====================================================

    static void applyFont() {

        SimpleAttributeSet attributes =
                new SimpleAttributeSet();

        StyleConstants.setFontFamily(
                attributes,
                currentRun.getFontName()
        );

        applyAttributes(attributes);
    }

    // =====================================================
    // APPLY FONT SIZE
    // =====================================================

    static void applyFontSize() {

        SimpleAttributeSet attributes =
                new SimpleAttributeSet();

        StyleConstants.setFontSize(
                attributes,
                currentRun.getFontsize()
        );

        applyAttributes(attributes);
    }

    // =====================================================
    // APPLY BOLD
    // =====================================================

    static void applyBold() {

        SimpleAttributeSet attributes =
                new SimpleAttributeSet();

        StyleConstants.setBold(
                attributes,
                currentRun.isBold()
        );

        applyAttributes(attributes);
    }

    // =====================================================
    // APPLY ITALIC
    // =====================================================

    static void applyItalic() {

        SimpleAttributeSet attributes =
                new SimpleAttributeSet();

        StyleConstants.setItalic(
                attributes,
                currentRun.isItalic()
        );

        applyAttributes(attributes);
    }

    // =====================================================
    // APPLY UNDERLINE
    // =====================================================

    static void applyUnderline() {

        SimpleAttributeSet attributes =
                new SimpleAttributeSet();

        StyleConstants.setUnderline(
                attributes,
                currentRun.isUnderline()
        );

        applyAttributes(attributes);
    }

    // =====================================================
    // APPLY ATTRIBUTES TO SELECTED TEXT
    // =====================================================

    static void applyAttributes(
            SimpleAttributeSet attributes) {

        int start =
                textPane.getSelectionStart();

        int end =
                textPane.getSelectionEnd();

        int length = end - start;

        if (length > 0) {

            textPane.getStyledDocument()
                    .setCharacterAttributes(
                            start,
                            length,
                            attributes,
                            false
                    );
        }
    }
}