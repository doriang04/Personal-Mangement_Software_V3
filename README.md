# Personalmanagement-Software: Anleitung

Dieses Dokument beschreibt die notwendigen Schritte zur Ausführung der Anwendung.

## Wichtiger Hinweis zur Datensicherheit

**Achtung:** Unter macOS sollte die Anwendung nicht über die Tastenkombination `Cmd + Q` oder die Menüleiste zwangsbeendet werden. Diese Aktion umgeht den regulären Schließvorgang der Anwendung, was dazu führen kann, dass Änderungen nicht persistent in der Datenbank gespeichert werden.

Bitte verwenden Sie zum Beenden des Programms ausschließlich die dafür vorgesehenen Bedienelemente der Fenster. Nur so kann eine ordnungsgemäße Datenpersistenz gewährleistet werden.

## Anleitung zur Ausführung über die Kommandozeile

Um die Anwendung zu kompilieren und zu starten, sind eine Java-Laufzeitumgebung und Apache Maven erforderlich.

### 1. Voraussetzungen und erstmalige Einrichtung

Öffnen Sie eine Kommandozeile und überprüfen Sie die folgenden Installationen.

#### 1.1 Apache Maven

Maven wird für die Verwaltung der Projektabhängigkeiten und den Build-Prozess benötigt.

*   **Überprüfung der Installation:**
    ```bash
    mvn -v
    ```
    Wenn dieser Befehl eine Versionsnummer ausgibt, ist Maven bereits korrekt installiert.

*   **Installation (falls erforderlich):**
    *   **macOS (mit Homebrew):**
        ```bash
        brew install maven
        ```
    *   **Windows:**
        1. Laden Sie die aktuelle Binärdistribution von der offiziellen Webseite herunter: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
        2. Entpacken Sie das Archiv in ein stabiles Verzeichnis (z. B. `C:\Program Files\maven`).
        3. Setzen Sie die Umgebungsvariable `MAVEN_HOME` auf das Verzeichnis, in das Sie Maven entpackt haben.
        4. Fügen Sie den Pfad zum `bin`-Verzeichnis von Maven (`%MAVEN_HOME%\bin`) zur `PATH`-Umgebungsvariable hinzu.
        5. Starten Sie die Kommandozeile neu, damit die Änderungen wirksam werden.

#### 1.2 Java Development Kit (JDK)

Für die Kompilierung und Ausführung der Anwendung ist ein JDK erforderlich.

*   **Überprüfung der Installation:**
    ```bash
    java -version
    ```
    Die Anwendung erfordert **Java Version 25**.

*   **Installation (falls erforderlich):**
    Falls keine kompatible Java-Version installiert ist, laden Sie bitte ein JDK von einem vertrauenswürdigen Anbieter herunter.

    Die Installationsanleitungen sind auf den Webseiten der jeweiligen Anbieter zu finden und plattformspezifisch.

### 2. Starten der Anwendung

Nachdem die Voraussetzungen erfüllt sind, führen Sie die folgenden Befehle in Ihrer Kommandozeile aus.

1.  **Navigieren Sie in das Projektverzeichnis:**
    Ersetzen Sie `[PFAD_ZUM_PROJEKT]` durch den tatsächlichen Pfad zu diesem Projektordner.
    ```bash
    cd [PFAD_ZUM_PROJEKT]
    ```

2.  **Kompilieren und Verpacken des Projekts:**
    Dieser Befehl bereinigt vorherige Builds, kompiliert den Quellcode und packt die Anwendung in eine ausführbare `.jar`-Datei.
    ```bash
    mvn clean package
    ```

3.  **Ausführen der Anwendung:**
    Die kompilierte Datei befindet sich im `target`-Verzeichnis. Starten Sie sie mit folgendem Befehl:
    ```bash
    java -jar target/personalmanagement-software-final.jar
    ```

Die Anwendung sollte nun starten und die grafische Benutzeroberfläche anzeigen.

Alle Mitarbeiter, als welche sich angemeldet werden können, können mit Systemberechtigung, Benutzernamen und Passwort
in der Konsole eingesehen werden bei jedem Start des Programmes.

Falls die Datenbank auf seinen Startzustand gebracht werden soll, dann muss die Datei gelöscht werden,
welche unter `~/h2_db_files/` gefunden werden kann. Alle Dateien aus diesem Ordner müssen gelöscht werden, während das
Programm nicht läuft, damit aus den `.json` Dateien der Standard gezogen werden kann.