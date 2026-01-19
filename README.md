# Personalmanagement-Software: Anleitung

Dieses Dokument beschreibt die notwendigen Schritte zur Ausführung der Anwendung sowie weitere wichtige Hinweise zur Nutzung.

## Wichtiger Hinweis zur Datensicherheit

**Achtung:** Unter macOS sollte die Anwendung nicht über die Tastenkombination `Cmd + Q` oder die Menüleiste zwangsbeendet werden. Diese Aktion umgeht den regulären Schließvorgang, was zum Verlust nicht gespeicherter Daten führen kann.

Bitte verwenden Sie zum Beenden des Programms ausschließlich die Bedienelemente des Fensters (z. B. den Schließen-Button), um eine ordnungsgemäße Datenpersistenz zu gewährleisten.

## Anleitung zur Ausführung

Zur Kompilierung und zum Start der Anwendung sind eine Java-Laufzeitumgebung und Apache Maven erforderlich.

### 1. Systemvoraussetzungen

Öffnen Sie eine Kommandozeile und überprüfen Sie, ob die folgende Software installiert ist.

#### Apache Maven
Wird für die Verwaltung der Projektabhängigkeiten und den Build-Prozess benötigt.

*   **Überprüfung der Installation:**
    ```bash
    mvn -v
    ```
    Wenn eine Versionsnummer ausgegeben wird, ist Maven bereits korrekt installiert.

*   **Installation (falls erforderlich):**
    *   **macOS (mit Homebrew):** `brew install maven`
    *   **Windows:** Folgen Sie der offiziellen Anleitung auf [maven.apache.org](https://maven.apache.org/download.cgi). Dies umfasst das Herunterladen, Entpacken und Konfigurieren der `MAVEN_HOME`- und `PATH`-Umgebungsvariablen.

#### Java Development Kit (JDK)
Wird zur Kompilierung und Ausführung der Anwendung benötigt.

*   **Überprüfung der Installation:**
    ```bash
    java -version
    ```
    Die Anwendung erfordert **Java 17 oder eine neuere Version**.

*   **Installation (falls erforderlich):**
    Laden Sie ein JDK von einem vertrauenswürdigen Anbieter (z. B. Eclipse Temurin, OpenJDK) herunter und folgen Sie der plattformspezifischen Installationsanleitung.

### 2. Kompilierung und Start

Führen Sie die folgenden Befehle in der Kommandozeile aus:

1.  **Navigieren Sie in das Projektverzeichnis:**
    ```bash
    cd [PFAD_ZUM_PROJEKT]
    ```

2.  **Kompilieren und Verpacken des Projekts:**
    Dieser Befehl bereinigt vorherige Builds und erstellt eine ausführbare `.jar`-Datei.
    ```bash
    mvn clean package
    ```

3.  **Ausführen der Anwendung:**
    Die kompilierte Datei befindet sich im `target`-Verzeichnis. Starten Sie sie mit:
    ```bash
    java -jar target/personalmanagement-software-final.jar
    ```

## Zusätzliche Hinweise

### Anmeldedaten für Test-Benutzer

Beim Start der Anwendung werden die Anmeldedaten (Benutzername, Passwort, Berechtigungsstufe) aller initialen Mitarbeiter zu Demonstrationszwecken in der Konsole ausgegeben.

### Zurücksetzen der Datenbank

Um die Datenbank in ihren ursprünglichen Zustand zurückzusetzen, können die Datenbankdateien gelöscht werden. Die Datenbank wird dann beim nächsten Start aus den mitgelieferten `.json`-Dateien neu initialisiert.

**Warnung: Dieser Vorgang löscht alle bestehenden Daten unwiderruflich.**

1.  Stellen Sie sicher, dass die Anwendung vollständig beendet ist.
2.  Navigieren Sie zum Verzeichnis `~/h2_db_files/` in Ihrem Benutzerordner.
3.  Löschen Sie alle Dateien in diesem Verzeichnis.