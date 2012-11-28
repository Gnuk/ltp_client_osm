======================================================
Client Android de LocalizeTeaPot version OpenStreetMap
======================================================

Installation
-------------

Le plus simple c'est d'installer le sdk tout en 1 (avd + eclipse sp�cialement bidouill� pour le d�veloppement sur android), � cette adresse:
http://developer.android.com/sdk/index.html#download

Une fois le t�l�chargement termin�, d�compresser les fichiers et lancer Eclipse dans le r�pertoire (ex: adt-bundle-windows\eclipse).

Normalement le SDK de Android est charg� automatiquement, si ce n'est pas le cas, il faut all� dans le menu Window > Preferences > Android. Dans le champ "SDK Location", rentrer le r�pertoire ou vous avez d�compress� le SDK (ex: adt-bundle-windows\sdk).

Ensuite, il faut installer une ou plusieurs versions de Android, pour ce faire, direction Window > Android SDK manager.
Reste plus qu'a cocher les versions avec lesquels vous voulez tester / d�velopper l�application LocalizeTeaPot. 

Le d�veloppement de l'application a �t� fait avec la version 2.2 (Api 8), mais, a cause de la bibliotheque SherlockActionBar, il faut compli� l'application en 4.1.2 donc, je vous conseille d'installer la version 4.1.2 a cot� de celle plus ancienne. Ceci dit, le necessaire a �t� fait pour que LocalizeTeaPot tourne sur toutes les versions d�Android ! C'est juste la compilation qui demande a etre fait avec une API differente...

Enfin, il faut instancier un AVD, cette a dire, l'�mulateur. Cela se passe par le menu Window > Android Virtual Device Manager. A vous donc de cr�er et configurer votre �mulateur.

Si tout est bon, il ne reste plus qu'� lancer l'�mulateur !



Probl�mes connus
----------------

> Impossible d'afficher la carte, les marqueurs�

Un possible probl�me de connexion entre l'�mulateur et le r�seau.
  
Dans l'onglet target (quand vous cliquez sur la petite fl�che au moment o� vous lancez l'application), ajoutez la ligne: -dns-server VOTRE_IP_DNS

> Je n'arrive pas a installer SherlockActionBar

C'est assez pennible a installer cette bibliotheque car, ils n'ont pas fait de jar qui s'incruste d'un click dans notre application donc,
il faut l'important manuellement dans eclipse et l'inclure en tant que library dans le projet.

Il y a un site qui explique bien comment faire: www.necronet.info/2011/09/learn-to-use-sherlock-actionbar-on.html
Faire juste gaffe de bien cliqu� sur la checkbox qui dit d'importer le projet en tant que library !

Une fois reussit a importer SherlockActionBar et la li� a notre projet LocalizeTeaPot, penser bien a faire:

- click droit sur le projet > Android Tools > Add support library
- et Menu eclipse Project > Clean > Choisir le projet + l'actionbarsherlock
- supprimer android-support-v4.jar du projet LocalizeTeaPot (jar + build reference...)

En general ces 2 etapes c'est pour eviter (ou supprimer) des erreurs assez genantes !