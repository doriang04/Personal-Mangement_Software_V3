# Important Notice

Do not force-quitt this application on Mac via `Cmd + Q`, as this does not save your changes to the database.

# How to run this programm via your CLI

## Before running it for the first time

- Open your Terminal
- Check if Maven is installed:
  - `mvn -v`
  - If not installed:
    - Mac (mit homebrew):
      - `brew install maven`
    - Windows:
      - Maven von https://maven.apache.org/download.cgi
        herunterladen 
      - Entpacken
      - `MAVEN_HOME` setzen
      - `%MAVEN_HOME%\bin` zur `PATH`-Variable hinzufügen
      - Terminal neu öffnen
- Check if Java is installed:
  - `java -version`
  - If not installed:
    - just google it man, not gonna explain that one...

## Commands to actually start the programm

- `cd [PATH TO THIS PROJECT]`
- `mvn clean package`
- `java -jar target/personalmanagement-software-final.jar`