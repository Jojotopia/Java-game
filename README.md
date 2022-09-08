#Java-game

Instruction:
	Le but du jeu est de bloquer les adversaires pour se faire vous devez les entourés avec votre équipe soit verticalement soit horizontalement.
	Pour vous deplacer appuyer simplement sur les boutons en haut de l'écran.
	La partie commencera dès que vous serez au moins un joueur dans chaque équipe mais d'autres joueurs pourront toujours arriver tant qu'aucune équipe n'a gagné.
	Pour bloquer vos adversaires vous avez deux possibilités:
		```Le bloquer contre un mur (ici les joueurs O gagnent):
			 _______   _______
			|X O _ _| |X _ _ _|
			|_ _ _ _| |O _ _ _|
			|_ _ _ _| |_ _ _ _|
			
		L'entourer (ici les joueurs X gagnent):
			 _______   _______
			|X O X _| |X _ _ _|
			|_ _ _ _| |O _ _ _|
			|_ _ _ _| |X _ _ _|
			|_ _ _ _| |_ _ _ _|```
			
	Une fois qu'un joueur est bloqué il ne peut plus bouger mais il peut encore servir pour bloquer les adversaires.
	Pour gagner vous devez bloqué tous les joueurs adverses. Lorsque vous gagner la grille se met à la couleur de votre équipe en faisant un petit sourir.

##Utilisation:
	Etape 1 (à chaque début de partie):
		- Compilation: javac *.java
		- Lancement du serveur: java TPServeur
		 
		- laisser le terminal ouvert, il s'agit du serveur

	Etape 2:
		Lancement du jeu pour un joueur: java TPClient teamNumber(1 ou 2) positionX positionY <serveur_addresse: optionnel>
			teamNumber: numéro de l'équipe soit 1 (bleu) soit 2 (rouge)
			positionX et positionY: coordonnées de départ entre 0 0 et 9 9 (Une erreur s'affichera si une personne occupe déjà la case demandé)
			serveur_addresse: Optionnel par défault "127.0.0.1"
		
		Exemple de commande: 
			java 1 0 0
			java 2 5 6
			java 1 3 7 192.168.56.23
