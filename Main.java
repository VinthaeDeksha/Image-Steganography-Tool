
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Main extends JFrame {

    private File selectedImage;

    private JLabel imageLabel;
    private JLabel fileNameLabel;
    private JLabel statusLabel;
    private JTextArea messageArea;

    public Main() {

        setTitle("Image Steganography Tool");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(new Color(240, 245, 255));
        setLayout(new BorderLayout(15, 15));

        // Heading
        JLabel heading = new JLabel(
                "IMAGE STEGANOGRAPHY TOOL",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 27));
        heading.setForeground(new Color(25, 65, 130));

        JLabel subtitle = new JLabel(
                "Securely Hide and Retrieve Secret Messages in Images",
                SwingConstants.CENTER
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setBackground(new Color(240, 245, 255));
        header.setBorder(new EmptyBorder(20, 10, 10, 10));
        header.add(heading);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(240, 245, 255));
        mainPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        // Left panel - Image
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(190, 205, 230), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel imageHeading = new JLabel(
                "Selected Image",
                SwingConstants.CENTER
        );
        imageHeading.setFont(new Font("Arial", Font.BOLD, 19));

        imageLabel = new JLabel(
                "No Image Selected",
                SwingConstants.CENTER
        );
        imageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        imageLabel.setForeground(Color.GRAY);

        fileNameLabel = new JLabel(
                "Choose an image to begin",
                SwingConstants.CENTER
        );

        fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        leftPanel.add(imageHeading, BorderLayout.NORTH);
        leftPanel.add(imageLabel, BorderLayout.CENTER);
        leftPanel.add(fileNameLabel, BorderLayout.SOUTH);

        // Right panel - Message
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(190, 205, 230), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel messageHeading = new JLabel(
                "Secret Message",
                SwingConstants.CENTER
        );
        messageHeading.setFont(new Font("Arial", Font.BOLD, 19));

        messageArea = new JTextArea();
        messageArea.setFont(new Font("Arial", Font.PLAIN, 16));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(messageArea);

        JLabel messageHint = new JLabel(
                "Enter a message to hide or view a decoded message",
                SwingConstants.CENTER
        );
        messageHint.setFont(new Font("Arial", Font.PLAIN, 11));

        rightPanel.add(messageHeading, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(messageHint, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 12, 12)
        );
        buttonPanel.setBackground(new Color(240, 245, 255));

        JButton selectButton = createButton("Select Image");
        JButton encodeButton = createButton("Encode Message");
        JButton decodeButton = createButton("Decode Message");
        JButton clearButton = createButton("Clear");
        JButton aboutButton = createButton("About Project");

        buttonPanel.add(selectButton);
        buttonPanel.add(encodeButton);
        buttonPanel.add(decodeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(aboutButton);

        // Status
        statusLabel = new JLabel(
                "Welcome! Select an image to get started.",
                SwingConstants.CENTER
        );
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setForeground(new Color(25, 65, 130));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 245, 255));
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        bottomPanel.setBorder(new EmptyBorder(5, 5, 15, 5));

        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        selectButton.addActionListener(e -> selectImage());
        encodeButton.addActionListener(e -> encodeMessage());
        decodeButton.addActionListener(e -> decodeMessage());
        clearButton.addActionListener(e -> clearAll());
        aboutButton.addActionListener(e -> showAbout());

        setVisible(true);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(35, 90, 170));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Select and preview image
    private void selectImage() {

        JFileChooser chooser = new JFileChooser();

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            selectedImage = chooser.getSelectedFile();

            try {

                BufferedImage image = ImageIO.read(selectedImage);

                if (image == null) {
                    throw new Exception("Invalid image file!");
                }

                Image scaledImage = image.getScaledInstance(
                        380, 350, Image.SCALE_SMOOTH
                );

                imageLabel.setText("");
                imageLabel.setIcon(new ImageIcon(scaledImage));

                fileNameLabel.setText(selectedImage.getName());

                statusLabel.setText("Image selected successfully.");

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to open image: " + ex.getMessage(),
                        "Image Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Encode message with password
    private void encodeMessage() {

        if (selectedImage == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an image first!"
            );
            return;
        }

        String message = messageArea.getText();

        if (message.trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a secret message!"
            );
            return;
        }

        JPasswordField passwordField = new JPasswordField();

        int option = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Create Password for Encryption",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        char[] password = passwordField.getPassword();

        if (password.length < 4) {

            Arrays.fill(password, '\0');

            JOptionPane.showMessageDialog(
                    this,
                    "Password must contain at least 4 characters!"
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(new File("encoded_image.png"));

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            Arrays.fill(password, '\0');
            return;
        }

        File outputFile = chooser.getSelectedFile();

        if (!outputFile.getName().toLowerCase().endsWith(".png")) {
            outputFile = new File(outputFile.getAbsolutePath() + ".png");
        }

        try {

            Steganography.encode(
                    selectedImage,
                    message,
                    password,
                    outputFile
            );

            messageArea.setText("");

            statusLabel.setText("Message encrypted and hidden successfully!");

            JOptionPane.showMessageDialog(
                    this,
                    "Message hidden successfully!\n\nSaved at:\n"
                            + outputFile.getAbsolutePath(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Encoding failed: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            Arrays.fill(password, '\0');
        }
    }

    // Decode message with password
    private void decodeMessage() {

        if (selectedImage == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select the encoded image first!"
            );
            return;
        }

        JPasswordField passwordField = new JPasswordField();

        int option = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Enter Password to Decode",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        char[] password = passwordField.getPassword();

        if (password.length == 0) {

            Arrays.fill(password, '\0');

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your password!"
            );
            return;
        }

        try {

            String message = Steganography.decode(
                    selectedImage,
                    password
            );

            messageArea.setText(message);

            statusLabel.setText("Secret message decoded successfully!");

            JOptionPane.showMessageDialog(
                    this,
                    "Secret message decoded successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Decoding failed. Check your password or image.\n"
                            + ex.getMessage(),
                    "Decoding Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            Arrays.fill(password, '\0');
        }
    }

    // Clear all fields
    private void clearAll() {

        selectedImage = null;

        imageLabel.setIcon(null);
        imageLabel.setText("No Image Selected");

        fileNameLabel.setText("Choose an image to begin");

        messageArea.setText("");

        statusLabel.setText("All fields cleared.");

    }

    // About project
    private void showAbout() {

        String about = """
                IMAGE STEGANOGRAPHY TOOL

                Project Description:
                This application hides secret messages inside images
                and retrieves them whenever required.

                Technologies Used:
                - Java
                - Java Swing
                - Java AWT
                - ImageIO

                Main Techniques:
                - Least Significant Bit (LSB)
                - Password-based encryption
                - Image processing

                Features:
                - Select and preview images
                - Hide secret messages
                - Password-protected encoding
                - Password-protected decoding
                - Save encoded images in PNG format
                - Clear and reset the application

                Project Type:
                Java Project-Based Learning
                """;

        JOptionPane.showMessageDialog(
                this,
                about,
                "About Project",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new Main());
    }
}
