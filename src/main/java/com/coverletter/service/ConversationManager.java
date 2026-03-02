package com.coverletter.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Gère le flux de conversation entre l'utilisateur et le chatbot.
 * Maintient l'historique des messages et le system prompt.
 */
public class ConversationManager {

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant spécialisé dans la rédaction de lettres de motivation professionnelles en français.

            TON RÔLE :
            1. Accueillir chaleureusement l'utilisateur et lui expliquer que tu vas l'aider à créer sa lettre de motivation.
            2. Collecter les informations nécessaires en posant des questions claires et engageantes, UNE ou DEUX questions à la fois maximum.
            3. Les informations à collecter sont :
               - Prénom et nom
               - Coordonnées (email, téléphone, adresse)
               - Formation et diplômes
               - Compétences clés
               - Expériences professionnelles pertinentes
               - Poste visé / intitulé du poste
               - Nom de l'entreprise cible
               - Motivations personnelles pour ce poste/cette entreprise
            4. Si l'utilisateur oublie des informations essentielles (nom, poste visé, entreprise), tu DOIS les lui demander avant de générer la lettre.
            5. Quand tu as suffisamment d'informations, propose de générer la lettre en disant exactement :
               "J'ai maintenant toutes les informations nécessaires ! Voulez-vous que je génère votre lettre de motivation ?"
            6. Quand l'utilisateur confirme, génère une lettre de motivation complète, professionnelle et structurée avec :
               - En-tête avec coordonnées
               - Lieu et date
               - Objet
               - Formule d'appel
               - Introduction accrocheuse
               - Corps argumenté (compétences, expériences, motivation)
               - Conclusion avec demande d'entretien
               - Formule de politesse
               - Signature

            RÈGLES :
            - Sois conversationnel et encourageant
            - Ne pose pas toutes les questions d'un coup
            - Adapte-toi au niveau de détail fourni par l'utilisateur
            - La lettre générée doit être en français, professionnelle et personnalisée
            - Utilise un ton formel mais chaleureux dans la lettre
            """;

    private final GroqApiService groqApiService;
    private final List<Map<String, String>> conversationHistory;
    private boolean letterGenerated;

    public ConversationManager(GroqApiService groqApiService) {
        this.groqApiService = groqApiService;
        this.conversationHistory = new ArrayList<>();
        this.letterGenerated = false;

        // Ajouter le system prompt
        conversationHistory.add(createMessage("system", SYSTEM_PROMPT));
    }

    /**
     * Envoie un message utilisateur et obtient la réponse du chatbot.
     */
    public CompletableFuture<String> sendMessage(String userMessage) {
        conversationHistory.add(createMessage("user", userMessage));

        return groqApiService.chat(conversationHistory)
                .thenApply(response -> {
                    conversationHistory.add(createMessage("assistant", response));

                    // Détecter si la lettre a été générée (contient des éléments typiques)
                    if (response.contains("Objet :") || response.contains("Madame, Monsieur")) {
                        letterGenerated = true;
                    }

                    return response;
                });
    }

    /**
     * Demande au chatbot de démarrer la conversation.
     */
    public CompletableFuture<String> startConversation() {
        return groqApiService.chat(conversationHistory)
                .thenApply(response -> {
                    conversationHistory.add(createMessage("assistant", response));
                    return response;
                });
    }

    /**
     * Demande explicitement la génération de la lettre.
     */
    public CompletableFuture<String> generateLetter() {
        String generatePrompt = "Génère maintenant la lettre de motivation complète et professionnelle " +
                "avec toutes les informations que je t'ai fournies. " +
                "Structure-la de manière formelle avec en-tête, objet, corps et formule de politesse.";

        return sendMessage(generatePrompt);
    }

    public boolean isLetterGenerated() {
        return letterGenerated;
    }

    /**
     * Réinitialise la conversation.
     */
    public void reset() {
        conversationHistory.clear();
        conversationHistory.add(createMessage("system", SYSTEM_PROMPT));
        letterGenerated = false;
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
