Projet Web API
Afin de mettre en place un nouveau jeu de type Gatcha, vous allez devoir gérer la mise en place
de différentes API pour rendre le système opérationnel.
Afin de rendre le jeu facile à déployer, vous devrez utiliser docker et des bases de données
mongodb.
Dans l’idéal, vous fournirez des fichiers JSON contenant les données à importer dans les
collections mongo afin d’avoir une base pour tester l’application.
Vous devrez fournir dans ce projet un readme expliquant les différentes étapes pour lancer le
projet avec l’aide d’un docker-compose.
Le projet COMPLET doit tourner sous docker, pas uniquement les bases mongo.
Concrètement le projet doit être simple à lancer sur n’importe quel ordinateur en récupérant juste
le code et les ressources associées.
L’utilisation de SpringBoot est fortement conseillée.
D’autre part, chaque API devra être couverte par des tests unitaires qui devront fonctionner (et
pas de assert(true).isTrue() sinon c’est 0).
Ce travail pourra être réalisé en groupes de 4 à 5 personnes maximum.
L’utilisation de git vous est conseillée pour ce genre de travail d’équipe.
API d’authentification
La première et la plus importante : une API pour gérer l’authentification. Elle prendra en :
- entrée :	 

	 	 identifiant
	 	 password
- sortie en cas de 200 :
	 	 token
L’objectif de cette API sera de générer un token d’authentification (fait maison).
Dans une base seront stockés les identifiants et mots de passe (pas nécessaire de s’embêter
avec du cryptage, on veut faire simple).
Lors d’un appel à l’API avec ces informations, si le mot de passe correspond à l’identifiant, on
retourne un token auto-généré qu’on va stocker en base en se basant sur les informations
d’authentification.
L’objectif est de générer un token encrypté constitué de : username-date(YYYY/MM/DD)-heure
(HH:mm:ss). Par exemple :
Ce token sera valable une heure, et sa date d’expiration devra donc être enregistrée dans la base.
A chaque appel d’une autre API du système, ce token devra être passé à et vérifié par l’API
d’authentification. Si le token est toujours valide, l’API d’authentification valide l’appel et retourne
le username lié au token, puis met à jour la date d’expiration à [maintenant + une heure].
Si le token est expiré, l’API renvoie une erreur 401, et demande donc une nouvelle authentification
à l’utilisateur.
Par exemple :
Je requête l’API du système permettant de gérer les différents monstres. Afin de lui signifier que je
suis connecté, je lui passe dans les headers le token que j’ai récupéré précédemment en appelant
l’API authentification avec mes identifiants. L’API monstres appelle alors l’API authentification
pour valider le token.
Cas 1 : mon token est valide -> l’API authentification retourne mon username pour l’API monstre
et celle-ci exécute ma requête.
Cas 2 : mon token est expiré -> l’API authentification retourne une erreur 401 et demande un
token valide. L’API monstre transfère cette réponse en retour.
API Joueur
L’API Joueur est utilisée pour gérer les infos de compte utilisateurs qui sont :
identifiant
level (va de 0 à 50)
experience (commence à 50 pas d’xp pour level up au niveau 1 pour passer niveau 2 puis
est multiplié par 1,1 à chaque niveau : donc il faudra 50 * 1,1 = 55 xp pour passer au niveau 3)
List<monstres> (taille conditionnée par le niveau = commence à 10 puis + 1 pour chaque
level)
Elle doit pouvoir gérer :
- la récupération de toutes les informations du profil
- la récupération de la liste de monstre
- la récupération du niveau du joueur
- un gain d’expérience (quantité passée en paramètre) et retourner le nouveau statut
utilisateur
- un gain de niveau (reset l’expérience, augmente le seuil de level up et augmente la taille
max de la liste de monstres) et retourner le nouveau statut utilisateur
- l’acquisition d’un nouveau monstre
- la suppression d’un monstre
