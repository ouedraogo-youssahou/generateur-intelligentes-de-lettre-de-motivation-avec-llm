# Générateur de Lettres de Motivation - AI

Une application JavaFX moderne pour générer des lettres de motivation intelligentes via l'API Groq AI.

## 🎯 Fonctionnalités

- **Interface Chatbot** : Discutez naturellement avec l'IA pour affiner votre lettre
- **Génération Automatique** : Créez des lettres de motivation sur mesure en quelques secondes
- **Prévisualisation en Temps Réel** : Visualisez immédiatement le résultat
- **Export PDF** : Téléchargez votre lettre au format professionnel
- **Impression Directe** : Imprimez directement depuis l'application
- **Copie Presse-papiers** : Copiez facilement le texte généré
- **Gestion des Conversations** : Nouvelle conversation, historique, etc.

## 🚀 Installation

### Prérequis

- **Java JDK 21** ou supérieur
- **Apache Maven 3.9+**
- **Clé API Groq** (gratuite)

### 1. Installation de Java

Téléchargez et installez JDK 21 depuis [Adoptium](https://adoptium.net/) :

```bash
# Vérifiez l'installation
java -version
javac -version
```

### 2. Installation de Maven

Téléchargez Maven depuis [apache.org](https://maven.apache.org/download.cgi) et configurez votre PATH.

### 3. Configuration de l'Environnement

#### Windows
```cmd
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
set PATH="%JAVA_HOME%\bin;C:\Users\votre_nom\tools\apache-maven-3.9.12\bin;%PATH%"
```

#### Linux/Mac
```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"
```

## 🏃‍♂️ Lancement de l'Application

### Méthode 1 : Directement avec Maven

```bash
# Depuis le répertoire du projet
mvn javafx:run
```

### Méthode 2 : Avec configuration complète

#### Windows (cmd)
```cmd
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot" && "C:\Users\votre_nom\tools\apache-maven-3.9.12\bin\mvn" -f pom.xml javafx:run
```

#### Windows (PowerShell)
```powershell
$jdkPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:JAVA_HOME = $jdkPath
$env:PATH = "$jdkPath\bin;C:\Users\votre_nom\tools\apache-maven-3.9.12\bin;" + $env:PATH
mvn -f pom.xml javafx:run
```

#### Linux/Mac
```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
mvn javafx:run
```

## 📝 Utilisation

### 1. Configuration de l'API

1. **Obtenez votre clé API Groq** :
   - Créez un compte sur [Groq Cloud](https://console.groq.com/)
   - Générez une clé API dans la section "API Keys"
   - La clé commence généralement par `gsk_`

2. **Entrez votre clé** :
   - Lancez l'application
   - Saisissez votre clé API dans le champ prévu
   - Cliquez sur "Sauvegarder"

### 2. Génération d'une Lettre

1. **Démarrez la conversation** :
   - L'application vous accueille automatiquement
   - Décrivez le poste visé, votre expérience, etc.

2. **Affinez avec le chat** :
   - Posez des questions à l'IA
   - Demandez des modifications
   - Obtenez des conseils

3. **Générez la lettre** :
   - Cliquez sur "Générer la lettre"
   - La lettre apparaît dans la colonne de droite

4. **Export/Traitement** :
   - **Copier** : Copie le texte dans le presse-papiers
   - **Télécharger PDF** : Enregistre au format PDF
   - **Imprimer** : Impression directe

### 3. Exemples de Messages

```text
"Je cherche un poste de développeur Java senior avec 5 ans d'expérience"
"Adapte la lettre pour une startup tech innovante"
"Ajoute une section sur mes compétences en leadership"
"Rends le ton plus formel/professionnel"
```

## 🏗️ Architecture Technique

### Structure du Projet

```
src/
├── main/
│   ├── java/com/coverletter/
│   │   ├── App.java                    # Point d'entrée principal
│   │   ├── controller/
│   │   │   └── ChatController.java     # Gestion de l'interface
│   │   ├── service/
│   │   │   ├── GroqApiService.java     # Communication avec Groq
│   │   │   ├── ConversationManager.java # Gestion des échanges
│   │   │   └── PdfExportService.java   # Export PDF
│   │   └── model/
│   │       └── UserProfile.java        # Données utilisateur
│   └── resources/com/coverletter/
│       ├── chat-view.fxml              # Interface graphique
│       └── styles.css                  # Style CSS
└── target/                             # Fichiers compilés
```

### Technologies Utilisées

- **JavaFX 21** : Interface graphique
- **Groq API** : Intelligence artificielle
- **Gson** : JSON parsing
- **Apache PDFBox** : Génération PDF
- **Maven** : Gestion des dépendances

### Flux de Données

1. **Utilisateur** → Message dans le chat
2. **ChatController** → Envoi à ConversationManager
3. **ConversationManager** → Appel GroqApiService
4. **GroqApiService** → Requête HTTP à Groq
5. **Groq** → Réponse IA
6. **Retour** → Affichage dans l'interface

## 🔧 Configuration Avancée

### Personnalisation CSS

Modifiez `src/main/resources/com/coverletter/styles.css` pour changer l'apparence :

```css
/* Couleurs principales */
.root-pane { -fx-background-color: #f8f9fa; }
.app-title { -fx-text-fill: #2c3e50; }

/* Style des messages */
.user-message { -fx-background-color: #3498db; }
.bot-message { -fx-background-color: #ecf0f1; }
```

### Configuration Groq

Les clés API sont stockées temporairement en mémoire. Pour une version plus avancée :

```java
// Dans GroqApiService.java
private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
private static final String DEFAULT_MODEL = "llama3-8b-8192";
```

## 🐛 Dépannage

### Problèmes Courants

#### 1. Erreur "JAVA_HOME not set"
```bash
# Solution Windows
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"

# Solution Linux/Mac
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
```

#### 2. Maven non reconnu
```bash
# Vérifiez l'installation
mvn --version

# Ajoutez au PATH si nécessaire
export PATH="$PATH:/chemin/vers/maven/bin"
```

#### 3. Erreur de clé API
- Vérifiez que la clé commence par `gsk_`
- Assurez-vous qu'elle n'a pas expiré
- Vérifiez les quotas d'utilisation sur Groq Cloud

#### 4. Problèmes de dépendances
```bash
# Nettoyez et reconstruisez
mvn clean compile
```

### Logs et Debug

Activez les logs détaillés :
```bash
mvn javafx:run -X
```

## 📋 Exigences Système

- **Système d'exploitation** : Windows 10+, macOS 10.14+, Linux
- **Mémoire RAM** : 4GB minimum, 8GB recommandé
- **Espace disque** : 100MB pour les dépendances
- **Connexion Internet** : Requise pour l'API Groq

## 🤝 Contribuer

1. Fork le projet
2. Créez une branche : `git checkout -b feature/nouvelle-fonctionnalite`
3. Commit vos changements : `git commit -m 'Ajout de la fonctionnalité X'`
4. Push vers la branche : `git push origin feature/nouvelle-fonctionnalite`
5. Ouvrez une Pull Request

## 📄 License

Ce projet est sous license MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- **Groq** pour leur excellente API d'IA
- **OpenJFX** pour le framework JavaFX
- **Apache PDFBox** pour la génération PDF

## 📞 Support

Pour toute question ou problème :

1. Vérifiez d'abord cette documentation
2. Consultez les logs de l'application
3. Ouvrez une issue sur GitHub avec :
   - Description du problème
   - Logs d'erreur
   - Version de Java/Maven
   - Système d'exploitation

---

**✨ Bonne chance dans votre recherche d'emploi !**