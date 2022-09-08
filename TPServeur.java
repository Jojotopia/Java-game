/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author PIAUD Joackim, TOUZINAUD Valentin
 * 
 */
public class TPServeur extends Thread{
    //port de communication
    final static int port = 2000;
    //client pour chaque sous-serveur
    Socket socket;
    //entrée depuis le client
    InputStream in;
    //Sortie vers le client
    DataOutputStream out;
    //nombre de serveur au total(1 par joueur)
    static int nbThread = 1;
    //nombre de joueurs(permet de donné un id à chaque joueurs)
    static int nbJoueurs = 1;
    //id du sous-serveur
    Integer id = 0;
    //nombre de joueurs non bloqué de l'équipe bleu
    static Integer nbJoueursEnVieBleu = 0;
    //nombre de joueurs non bloqué de l'équipe rouge
    static Integer nbJoueursEnVieRouge = 0;
    //la partie est elle commencé
    static boolean partie_commence = false;
    //la partie est elle finie
    static boolean partie_finie = false;
    //donnée de la grille du jeu commune à tout les sous-serveurs
    static byte[] etatServeur = new byte [2*10*10];
    //tableau de tous les sous-serveurs
    static ArrayList<TPServeur> serveurs = new ArrayList<TPServeur>();
    //tableau des joueurs éliminés
    static ArrayList<Integer> joueurs_elimines = new ArrayList<Integer>();

    public static void main(String[] args) {
        try {//Lancement du serveur
            ServerSocket socketServeur = new ServerSocket(port);
            while (!partie_finie) {//Tant que la partie n'est pas finie
                //Attendre un joueur
                Socket socketClient = socketServeur.accept();
                if(!partie_finie){//Si la partie n'est pas finie
                    TPServeur serveur = new TPServeur(socketClient, nbThread++);//Création d'un sous-serveur pour le joueur
                    serveur.start();
                    serveurs.add(serveur);//Ajout du sous-serveur à la liste des sous-serveurs
                }else{
                    socketClient.close();//Si la partie est finie mais qu'un joueur rejoint il ne sera pas accepter
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructeur d'un sous-serveur
     */
    public TPServeur(Socket socket, Integer id) {
        this.socket = socket;
        this.id = id;
    }

    /**
     * Lancement d'un sous-serveur(fonction obligatoire car fonctionne comme un Thread)
     */
    public void run() {
        traitements();
    }
    
    /**
     * Envoi de l'état du jeu au joueur
     * @param numero: numéro du joueur concerné
     * @param result: résultat de sa demande
     */
    public void sendEtat(int numero, int result){
        try{
            out.write(etatServeur);
            out.writeInt(numero);
            out.writeInt(result);
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Vérification des potentiels perdant
     */
    public void perdant(){
        for(int i=0; i<etatServeur.length; i++){//Parcours de toute la grille
            //Si un joueur est trouvé et qu'il n'est pas déjà eliminé
            if(i%2 == 0 && etatServeur[i] != 0 && !joueurs_elimines.contains((Integer)(int)etatServeur[i])){
                /*Grosse condition pour être bloqué:
                    Test horizontal:
                            Si la case à la droite du joueur n'existe pas(derniere case)
                            ou si la case est sur une autre ligne(mur)
                            ou si la case est occupé et est occupé par un joueur d'une autre équipe
                        ET
                            Si la case à la gauche du joueur n'existe pas(premiere case)
                            ou si la case est sur une autre ligne(mur)
                            ou si la case est occupé et est occupé par un joueur d'une autre équipe
                    OU
					Test vertical:
                            Si la case en dessous du joueur n'existe pas(mur)
                            ou si la case est occupé et est occupé par un joueur d'une autre équipe
                        ET
                            Si la case au dessus du joueur n'existe pas(mur)
                            ou si la case est occupé et est occupé par un joueur d'une autre équipe
                
                    Si les deux conditions du test vertical ou les deux conditions du test horizontal
                    sont vraies alors le joueur est bloqué
                */
                if(
                    (	
                        (i+3>200 || (Math.floor((i+3)/20) != Math.floor(i/20))
                            || (etatServeur[i+3] != 0 && etatServeur[i+3] != etatServeur[i+1]))
                        && (i-1<0 ||(Math.floor((i-1)/20) != Math.floor(i/20)) 
                            || (etatServeur[i-1] != 0 && etatServeur[i-1] != etatServeur[i+1])))
                    ||(
                        (i+21 > 200 || (etatServeur[i+21] != 0 && etatServeur[i+21] != etatServeur[i+1]))
                        && (i-19 < 0 || (etatServeur[i-19] != 0 && etatServeur[i-19] != etatServeur[i+1])))
                ){
                    joueurs_elimines.add((Integer)(int)etatServeur[i]);//Ajout du joueur à la liste des éliminés
                    if(etatServeur[i+1] == 1){//1 joueur en moins dans son équipe
                    	nbJoueursEnVieBleu -= 1;
                    }else{
                    	nbJoueursEnVieRouge -= 1;
                    }
                    for(TPServeur s: serveurs){
                        s.sendEtat(etatServeur[i], -1);//Envoi du nouvelle état à tous les joueurs en indiquant le numéro du joueur éliminé
                    }
                }
            }
        }
        if(nbJoueursEnVieBleu == 0 || nbJoueursEnVieRouge == 0){//Si tous les rouges ou tous les bleus sont éliminés
            System.out.println("Fin");
            partie_finie = true;//Fin de la patie
            afficherGagnant();//Affichage de l'équipe gagnante
        }
    }
	
    /**
     * Affichage du gagnant
     */
    public void afficherGagnant(){
        int couleurGagnante = 0;
        if(nbJoueursEnVieBleu == 0){//Si tous les bleus sont éliminés 
            couleurGagnante = 2;//Alors les rouges gagnent
        }else{
            couleurGagnante = 1;
        }
        //Coloration de toute la grille aux couleurs du gagnant avec un smiley dessus
        for(int i=0; i<etatServeur.length; i++){
            //Liste permettant de dessiner un smiley sur la grille de tous les joueurs
            ArrayList<Integer> smiley = new ArrayList<Integer>(Arrays.asList(24, 26, 32, 34, 44, 46, 52, 54, 64, 66, 72, 74, 122, 124, 126, 128, 130, 132, 134, 136, 144, 146, 148, 150, 152, 154, 166, 168, 170, 172));
            if(i%2 == 0){
                etatServeur[i] = 1;
            }else if(smiley.contains(i) || (i > 0 && smiley.contains(i-1))){
                etatServeur[i] = 0;
            }else{
                etatServeur[i] = (byte)couleurGagnante;
            }
        }
        //Envoi du dessin à tous les joueurs
        for(TPServeur s: serveurs){
            s.sendEtat(-1, -2);
        }
		System.exit(0);//Fermeture du serveur et des sous-serveurs à la fin de la partie
    }

    /**
     * Fonction principale de chaque sous-serveurs
     */
    public void traitements() {
        Boolean running = true;
        while (running) {
            try {
                //Definition des entrees sortie du joueur
                in = this.socket.getInputStream();
                out = new DataOutputStream(this.socket.getOutputStream());
                out.writeInt(nbJoueurs++);//Envoi au joueur de son id
                sendEtat(nbJoueurs, 1);//Envoi de l'état du jeu au joueur
                try{
                    while(running){
                        //Demande de déplacement reçu par le joueur
                        //Id du joueur
                        int numero = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
                        //Equipe du joueur
                        int team = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
                        //Position actuelle du joueur
                        int origine = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
                        //Position souhaité par le joueur
                        int destination = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
                        //Résultat de sa demande
                        int result = 0;
                        //Si la case souhaité est non utilisé
                        if(etatServeur[destination] == 0){
                            if(origine != -1){//Si le joueur est déjà sur la grille
                                etatServeur[origine] = 0;//On le supprime de sa position actuelle
                                etatServeur[origine+1] = 0;
                            }else{//Si le joueur n'était pas encore sur la grille
                                //Ajout d'un joueur non bloqué dans la bonne equipe
                                if(team == 1){
                                    nbJoueursEnVieBleu += 1;
                                }else{
                                    nbJoueursEnVieRouge += 1;
                                }
                                //La partie peut commencer lorsqu'il y a plus d'un joueur dans chaque équipe
                                if(nbJoueursEnVieBleu >= 1 && nbJoueursEnVieRouge >= 1){
                                    partie_commence = true;
                                }
                            }
                            etatServeur[destination] = (byte)numero;//Ajout du joueur à la position qu'il souhaite
                            etatServeur[destination+1] = (byte)team;
                            result = 1;//Demande acceptée
                        }
                        try{
                            if(partie_commence){
                                perdant();//Si la partie à commencer vérifier les potentiels perdant
                            }
                            if(!partie_finie){//Si la partie n'est pas finie envoi de l'état à tous les joueurs
                                for(TPServeur s: serveurs){
                                    s.sendEtat(numero, result);
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    this.socket.close();//Deconnexion avec le client
                }catch(Exception e){//Lorsqu'un joueur quitte le serveur
                    System.out.println("Client déconnecté !");
                    serveurs.remove(this);//Suppression de ce sous-serveur de la liste
                    int i = 0;
                    for(byte b: etatServeur){//Recherche du joueur déconnecté
                        if(i%2 == 0 && b == this.id.byteValue()){
                            //Retrait du joueur dasn son équipe si il n'a pas déjà été éliminé
							if(!joueurs_elimines.contains((Integer)(int)etatServeur[i])){
								if(etatServeur[i+1] == 1){
									nbJoueursEnVieBleu -= 1;
								}else{
									nbJoueursEnVieRouge -= 1;
								}
							}
                            //Suppression de son pion
                            etatServeur[i] = 0;
                            etatServeur[i+1] = 0;
                        }
                        i++;
                    }
					if(partie_commence){
						perdant();//Si la partie à commencer vérifier les potentiels perdant
					}
                    //Envoi des nouvelles données à tous les joueurs
                    for(TPServeur s: serveurs){
                        s.sendEtat(this.id, 1);
                    }
                    running = false;//Fin de ce sous-serveur
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
