======================================================
Client Android de LocalizeTeaPot version OpenStreetMap
======================================================

Installation
-------------

Le plus simple c'est d'installer le sdk tout en 1 (avd + eclipse spécialement bidouillé pour le développement sur android), à cette adresse:
http://developer.android.com/sdk/index.html#download

Une fois le téléchargement terminé, décompresser les fichiers et lancer Eclipse dans le répertoire (ex: adt-bundle-windows\eclipse).

Normalement le SDK de Android est chargé automatiquement, si ce n'est pas le cas, il faut allé dans le menu Window > Preferences > Android. Dans le champ "SDK Location", rentrer le répertoire ou vous avez décompressé le SDK (ex: adt-bundle-windows\sdk).

Ensuite, il faut installer une ou plusieurs versions de Android, pour ce faire, direction Window > Android SDK manager.
Reste plus qu'a cocher les versions avec lesquels vous voulez tester / développer l’application LocalizeTeaPot. 

Le développement de l'application a été fait avec la version 2.2 (Api 8), mais, a cause de la bibliotheque SherlockActionBar, il faut complié l'application en 4.1.2 donc, je vous conseille d'installer la version 4.1.2 a coté de celle plus ancienne. Ceci dit, le necessaire a été fait pour que LocalizeTeaPot tourne sur toutes les versions d’Android ! C'est juste la compilation qui demande a etre fait avec une API differente...

Enfin, il faut instancier un AVD, cette a dire, l'émulateur. Cela se passe par le menu Window > Android Virtual Device Manager. A vous donc de créer et configurer votre émulateur.

Si tout est bon, il ne reste plus qu'à lancer l'émulateur !



Problèmes connus
----------------

> Impossible d'afficher la carte, les marqueurs…

Un possible problème de connexion entre l'émulateur et le réseau.
  
Dans l'onglet target (quand vous cliquez sur la petite flèche au moment où vous lancez l'application), ajoutez la ligne: -dns-server VOTRE_IP_DNS

> Je n'arrive pas a installer SherlockActionBar

C'est assez pennible a installer cette bibliotheque car, ils n'ont pas fait de jar qui s'incruste d'un click dans notre application donc,
il faut l'important manuellement dans eclipse et l'inclure en tant que library dans le projet.

Il y a un site qui explique bien comment faire: www.necronet.info/2011/09/learn-to-use-sherlock-actionbar-on.html
Faire juste gaffe de bien cliqué sur la checkbox qui dit d'importer le projet en tant que library !

Une fois reussit a importer SherlockActionBar et la lié a notre projet LocalizeTeaPot, penser bien a faire:

- click droit sur le projet > Android Tools > Add support library
- et Menu eclipse Project > Clean > Choisir le projet + l'actionbarsherlock
- supprimer android-support-v4.jar du projet LocalizeTeaPot (jar + build reference...)

En general ces 2 etapes c'est pour eviter (ou supprimer) des erreurs assez genantes !