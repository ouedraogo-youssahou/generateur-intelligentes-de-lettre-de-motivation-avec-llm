package com.coverletter.model;

/**
 * Modèle contenant les informations de l'utilisateur
 * collectées par le chatbot pour générer la lettre de motivation.
 */
public class UserProfile {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String formation;
    private String competences;
    private String experiences;
    private String posteVise;
    private String entrepriseCible;
    private String motivations;

    public UserProfile() {}

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public String getCompetences() { return competences; }
    public void setCompetences(String competences) { this.competences = competences; }

    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }

    public String getPosteVise() { return posteVise; }
    public void setPosteVise(String posteVise) { this.posteVise = posteVise; }

    public String getEntrepriseCible() { return entrepriseCible; }
    public void setEntrepriseCible(String entrepriseCible) { this.entrepriseCible = entrepriseCible; }

    public String getMotivations() { return motivations; }
    public void setMotivations(String motivations) { this.motivations = motivations; }
}
