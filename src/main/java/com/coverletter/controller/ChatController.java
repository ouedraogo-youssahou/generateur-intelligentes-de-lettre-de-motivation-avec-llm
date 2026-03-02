package com.coverletter.controller;

import com.coverletter.service.ConversationManager;
import com.coverletter.service.GroqApiService;
import com.coverletter.service.PdfExportService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;

/**
 * Contrôleur principal de l'interface chatbot.
 */
public class ChatController {

    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button generateButton;
    @FXML private Button copyButton;
    @FXML private Button downloadPdfButton;
    @FXML private Button printButton;
    @FXML private Button newChatButton;
    @FXML private TextArea letterPreview;
    @FXML private TextField apiKeyField;
    @FXML private Button saveApiKeyButton;
    @FXML private Label apiKeyStatus;

    private GroqApiService groqApiService;
    private ConversationManager conversationManager;
    private PdfExportService pdfExportService;
    private String lastGeneratedLetter = "";

    @FXML
    public void initialize() {
        groqApiService = new GroqApiService();
        conversationManager = new ConversationManager(groqApiService);
        pdfExportService = new PdfExportService();

        // Configuration des événements
        sendButton.setOnAction(e -> handleSendMessage());
        generateButton.setOnAction(e -> handleGenerateLetter());
        copyButton.setOnAction(e -> handleCopyLetter());
        downloadPdfButton.setOnAction(e -> handleDownloadPdf());
        printButton.setOnAction(e -> handlePrint());
        newChatButton.setOnAction(e -> handleNewChat());
        saveApiKeyButton.setOnAction(e -> handleSaveApiKey());

        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSendMessage();
            }
        });

        // État initial
        generateButton.setDisable(true);
        copyButton.setDisable(true);
        downloadPdfButton.setDisable(true);
        printButton.setDisable(true);
        letterPreview.setEditable(false);
        letterPreview.setWrapText(true);

        // Vérifier la clé API
        updateApiKeyStatus();

        // Démarrer la conversation si la clé API est configurée
        if (groqApiService.isApiKeyConfigured()) {
            startChat();
        } else {
            addBotMessage("Bienvenue ! Veuillez d'abord configurer votre clé API Groq dans le panneau ci-dessus pour commencer.");
        }
    }

    private void handleSaveApiKey() {
        String key = apiKeyField.getText().trim();
        if (!key.isEmpty()) {
            groqApiService.setApiKey(key);
            apiKeyField.clear();
            updateApiKeyStatus();

            if (chatMessages.getChildren().size() <= 1) {
                startChat();
            }
        }
    }

    private void updateApiKeyStatus() {
        if (groqApiService.isApiKeyConfigured()) {
            apiKeyStatus.setText("✓ Clé API configurée");
            apiKeyStatus.setStyle("-fx-text-fill: #27ae60;");
            messageInput.setDisable(false);
            sendButton.setDisable(false);
        } else {
            apiKeyStatus.setText("✗ Clé API non configurée");
            apiKeyStatus.setStyle("-fx-text-fill: #e74c3c;");
            messageInput.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    private void startChat() {
        setLoading(true);
        conversationManager.startConversation()
                .thenAccept(response -> Platform.runLater(() -> {
                    addBotMessage(response);
                    setLoading(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        addErrorMessage("Erreur : " + ex.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        messageInput.clear();
        setLoading(true);

        conversationManager.sendMessage(message)
                .thenAccept(response -> Platform.runLater(() -> {
                    addBotMessage(response);
                    setLoading(false);

                    // Activer le bouton de génération si la conversation avance bien
                    if (chatMessages.getChildren().size() > 4) {
                        generateButton.setDisable(false);
                    }

                    // Si une lettre a été détectée, l'afficher dans la prévisualisation
                    if (conversationManager.isLetterGenerated()) {
                        lastGeneratedLetter = cleanLetterContent(response);
                        letterPreview.setText(lastGeneratedLetter);
                        copyButton.setDisable(false);
                        downloadPdfButton.setDisable(false);
                        printButton.setDisable(false);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        addErrorMessage("Erreur : " + ex.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void handleGenerateLetter() {
        setLoading(true);
        addUserMessage("[Génération de la lettre de motivation...]");

        conversationManager.generateLetter()
                .thenAccept(response -> Platform.runLater(() -> {
                    addBotMessage(response);
                    lastGeneratedLetter = cleanLetterContent(response);
                    letterPreview.setText(lastGeneratedLetter);
                    copyButton.setDisable(false);
                    downloadPdfButton.setDisable(false);
                    printButton.setDisable(false);
                    setLoading(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        addErrorMessage("Erreur lors de la génération : " + ex.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void handleCopyLetter() {
        if (!lastGeneratedLetter.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(lastGeneratedLetter);
            Clipboard.getSystemClipboard().setContent(content);

            copyButton.setText("✓ Copié !");
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> copyButton.setText("Copier la lettre"));
            }).start();
        }
    }

    private void handleDownloadPdf() {
        if (lastGeneratedLetter.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la lettre de motivation");
        fileChooser.setInitialFileName("lettre_de_motivation.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(sendButton.getScene().getWindow());
        if (file != null) {
            try {
                pdfExportService.exportToPdf(lastGeneratedLetter, file);
                downloadPdfButton.setText("\u2713 Enregistré !");
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> downloadPdfButton.setText("Télécharger PDF"));
                }).start();
            } catch (Exception ex) {
                addErrorMessage("Erreur export PDF : " + ex.getMessage());
            }
        }
    }

    private void handlePrint() {
        if (lastGeneratedLetter.isEmpty()) return;

        try {
            File tempFile = File.createTempFile("lettre_motivation_", ".pdf");
            tempFile.deleteOnExit();
            pdfExportService.exportToPdf(lastGeneratedLetter, tempFile);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.PRINT)) {
                    desktop.print(tempFile);
                } else {
                    desktop.open(tempFile);
                }
            }
        } catch (Exception ex) {
            addErrorMessage("Erreur impression : " + ex.getMessage());
        }
    }

    private void handleNewChat() {
        chatMessages.getChildren().clear();
        conversationManager.reset();
        lastGeneratedLetter = "";
        letterPreview.clear();
        generateButton.setDisable(true);
        copyButton.setDisable(true);
        downloadPdfButton.setDisable(true);
        printButton.setDisable(true);

        if (groqApiService.isApiKeyConfigured()) {
            startChat();
        }
    }

    private void addUserMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.getStyleClass().add("user-message");

        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER_RIGHT);
        container.setPadding(new Insets(5, 10, 5, 50));

        chatMessages.getChildren().add(container);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.getStyleClass().add("bot-message");

        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(5, 50, 5, 10));

        chatMessages.getChildren().add(container);
        scrollToBottom();
    }

    private void addErrorMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.getStyleClass().add("error-message");

        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 10, 5, 10));

        chatMessages.getChildren().add(container);
        scrollToBottom();
    }

    private void setLoading(boolean loading) {
        sendButton.setDisable(loading);
        messageInput.setDisable(loading);
        generateButton.setDisable(loading || chatMessages.getChildren().size() <= 4);

        if (loading) {
            sendButton.setText("...");
        } else {
            sendButton.setText("Envoyer");
        }
    }

    private String cleanLetterContent(String content) {
        // Supprimer la section "Note :" ajoutée par l'IA à la fin
        int noteIndex = content.lastIndexOf("\nNote :");
        if (noteIndex == -1) noteIndex = content.lastIndexOf("\nNote:");
        if (noteIndex > 0) {
            content = content.substring(0, noteIndex).trim();
        }

        // Supprimer les préambules IA ("Voici la lettre...")
        String[] lines = content.split("\n");
        int start = 0;
        for (int i = 0; i < Math.min(3, lines.length); i++) {
            String lower = lines[i].trim().toLowerCase();
            if ((lower.contains("voici") && (lower.contains("lettre") || lower.contains("motivation")))
                    || lower.startsWith("bien s\u00fbr") || lower.startsWith("avec plaisir")) {
                start = i + 1;
                while (start < lines.length && lines[start].trim().isEmpty()) start++;
                break;
            }
        }
        if (start > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < lines.length; i++) {
                if (i > start) sb.append("\n");
                sb.append(lines[i]);
            }
            content = sb.toString();
        }

        return content;
    }

    private void scrollToBottom() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
}
