# Client Android de LocalizeTeaPot version OpenStreetMap

## Installation du projet sous eclipse

### Prérequis

- un eclipse configuré avec le greffon egit et TOUT les SDKs Android de google.

### Lien du projet

- https://github.com/Gnuk/ltp_client_osm.git

### Installation

- lancer eclipse
- aller dans "File"->"Import...", puis sélectionner "Projects from Git" dans l'onglet "Git"
- sélectionner "URI", puis copier le lien du projet dans le champ "URI" de la partie "Location"
- une fois le projet importé, des erreurs doivent apparaître, pour les résoudre, il faut importer la bibliothéque "ActionBarSherlock" dans eclipse
- aller dans "File"->"Import...", puis sélectionner "Existing Android Code Into Workspace" dans l'onglet "Android"
- choisir le chemin de la bibliothéque dans le champ "Root Directory", la bibliothéque se trouve dans le dossier "libs/ActionBarSherlock" du projet
- le projet est maintenant importé

- Ajouter une target Android : Clic droit sur le nom du projet puis clic sur Android puis ajouter une target Android 4.2.

### Erreurs éventuelles

Mauvaise importation de la bibliothèque "ActionBarSherlock". Afin de résoudre cette erreur, il faut aller dans les configurations du projet.

- sélectionner le projet et faire un clique-droit dessus, puis choisir le menu "Properties"
- aller dans l'onglet "Android" et vérifier que "Is Library" est décoché
- une coche verte doit appaître à coté de "libs/ActionBarSherlock"
