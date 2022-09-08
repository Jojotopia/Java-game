import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter; // Window Event
import java.awt.event.WindowEvent; // Window Event

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;

/**
 * @author PIAUD Joackim, TOUZINAUD Valentin
 *
 */
public class TPClient extends Frame {
    //grille du jeu
    byte [] etat = new byte [2*10*10];
    //numéro de l'equipe
    int team;
    //id du joueur
    int numero;
    //position x actuelle
    int x;
    //deplacement en x en attente
    int x_en_attente = 0;
    //position y actuelle
    int y;
    //deplacement en y en attente
    int y_en_attente = 0;
    //etat du joueur
    boolean en_vie = true;
    //etat de la partie
    boolean partie_finie = false;
    //port de la communication
    int port = 2000;
    //addresse du serveur peut etre changée en paramètre
    String serveur_addresse = "127.0.0.1";
    //Serveur
    Socket socket = null;
    //entrée: récéption des donnée du serveur
    InputStream in;
    //sortie: envoi des données vers le serveur
    DataOutputStream out;
    //Boutons du jeu
    TPPanel tpPanel;
    //Grille visuelle du jeu 
    TPCanvas tpCanvas;
    //Timer pour le refresh
    Timer timer;

    /** Constructeur */
    public TPClient()
    {
        setLayout(new BorderLayout());
        tpPanel = new TPPanel(this);
        add("North", tpPanel);
        tpCanvas = new TPCanvas(this.etat);
        add("Center", tpCanvas);

        timer = new Timer();
        timer.schedule ( new MyTimerTask (  ) , 500,500) ;

    }

    /**
     * fonction permettant de se connecter au serveur
     * 
     * @return id du joueur
     */
    public synchronized Integer connexionServeur(){
        try{
            InetAddress serveur = InetAddress.getByName(this.serveur_addresse);
            socket = new Socket(serveur, port);//Connexion au serveur
            in = socket.getInputStream();//Définition de l'entrée depuis le serveur
            out = new DataOutputStream(socket.getOutputStream());//Définition de la sortie vers serveur
            try{
                int number = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();//Récupération de l'id du joueur
                receiveEtat();//Récéption de l'état du jeu
                return number;
            }catch(Exception e){//En cas d'erreur afficher le message ci dessous
                System.out.println("La partie est terminée ou le serveur n'a pas été trouvée !");
                System.exit(0);
            }
            return null;
        }catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /** Action vers droit */
    public synchronized void droit()
    {
        try{//si le joueur n'est pas bloqué et qu'il n'y a ni mur ni joueur à droite
            if(this.en_vie && this.x+1 <= 9 && this.x_en_attente == 0 && this.y_en_attente == 0){
                out.writeInt(this.numero);//Demande de déplaceent au serveur
                out.writeInt(this.team);
                out.writeInt((this.y*10+this.x)*2);//position actuelle
                out.writeInt((this.y*10+this.x+1)*2);//position souhaité
                this.x_en_attente = 1;//On souhaite avancer sur x
                this.y_en_attente = 0;
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    /** Action vers gauche */
    public synchronized void gauche()
    {
        try{//si le joueur n'est pas bloqué et qu'il n'y a ni mur ni joueur à gauche
            if(this.en_vie && this.x-1 >= 0 && this.x_en_attente == 0 && this.y_en_attente == 0){
                out.writeInt(this.numero);//Demande de déplaceent au serveur
                out.writeInt(this.team);
                out.writeInt((this.y*10+this.x)*2);//position actuelle
                out.writeInt((this.y*10+this.x-1)*2);//position souhaité
                this.x_en_attente = -1;//On souhaite reculer sur x
                this.y_en_attente = 0;
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    /** Action vers haut */
    public synchronized void haut()
    {
        try{//si le joueur n'est pas bloqué et qu'il n'y a ni mur ni joueur en haut
            if(this.en_vie && this.y-1 >= 0 && this.x_en_attente == 0 && this.y_en_attente == 0){
                out.writeInt(this.numero);//Demande de déplaceent au serveur
                out.writeInt(this.team);
                out.writeInt((this.y*10+this.x)*2);//position actuelle
                out.writeInt(((this.y-1)*10+this.x)*2);//position souhaité
                this.x_en_attente = 0;
                this.y_en_attente = -1;//On souhaite reculer sur y
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /** Action vers bas */
    public synchronized void bas ()
    {
        try{//si le joueur n'est pas bloqué et qu'il n'y a ni mur ni joueur en bas
            if(this.en_vie && this.y+1 <= 9 && this.x_en_attente == 0 && this.y_en_attente == 0){
                out.writeInt(this.numero);
                out.writeInt(this.team);
                out.writeInt((this.y*10+this.x)*2);//position actuelle
                out.writeInt(((this.y+1)*10+this.x)*2);//position souhaité
                this.x_en_attente = 0;
                this.y_en_attente = 1;//On souhaite avancer sur y
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    /** Pour rafraichir la situation */
    public synchronized void refresh ()
    {
        tpCanvas.repaint();
    }
    /** Pour recevoir l'Etat */
    public void receiveEtat()
    {
        try{
            //Récuperation des données de la grille
            byte[] etatRecu = in.readNBytes(2*10*10);
            //Récupération de l'id de la personne concerné par le déplacement
            int number = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
            //Résultat de la demande de déplacement
            int result = new BigInteger(in.readNBytes(Integer.BYTES)).intValue();
            if(number == numero){//Si l'on est la personne concerné
                if(result == 1){//Si le resultat est positif
                    this.x += this.x_en_attente;//On se déplace comme demandé
                    this.y += this.y_en_attente;
                    this.x_en_attente = 0;//On remet à 0 les positions en attente
                    this.y_en_attente = 0;
                }else if(result == 0){//Si le résultat est nul on ne se déplace pas
                    this.x_en_attente = 0;//On remet à 0 les positions en attente
                    this.y_en_attente = 0;
                }else if(result == -1){//Si le résultat est négatif
                    this.en_vie = false;//On ne peut plus bouger
                }
            }else if(number == -1){//Si le joueur concerné est -1(tout le monde)
                if(result == -2){//Si le résultat est -2(fin de partie)
                    this.en_vie = false;//On ne peut plus bouger
                    this.partie_finie = true;//La partie est finie
                }
            }
            this.etat = etatRecu;//On actualise les données de la grille
            this.tpCanvas.etat = this.etat;//On actualise l'affichage de la grille
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    /** Initialisations */
    public void minit(int number, int pteam, int px, int py)
    {
        try{
            if(this.etat[(py*10+px)*2] == (byte)0){//Vérification que la position souhaité est libre
                this.numero = number;
                this.team = pteam;
                this.x = px;
                this.y = py;
                out.writeInt(this.numero);//Demande de déplacement à la position demandé
                out.writeInt(this.team);
                out.writeInt(-1);//-1 car ici le joueur n'est pas encore sur la grille
                out.writeInt((this.y*10+this.x)*2);
            }else{
                System.out.println("La position choisi est déjà occupé !");
                System.exit(0);
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 
     * @return l'état sous forme de string
     */
    public String etat()
    {
        String result = new String();
        int i = 0;
        for(byte b: this.etat){
            result += b;
            i++;
            if(i%20 == 0){
                result += "\n";
            }
        }
        return result;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        //Vérification du nombre d'arguments données
        if (args.length < 3 || args.length > 4 || Integer.parseInt(args[0]) > 2 || Integer.parseInt(args[0]) < 1) {
            System.out.println("Usage : java TPClient teamNumber(1 ou 2) positionX positionY <serveur_addresse: optionnel>");
            System.exit(0);
        }
        try {
            TPClient tPClient = new TPClient();//Création d'un joueur
            if(args.length == 4){
                tPClient.serveur_addresse = args[3];//Changement d'addresse serveur si donnée en paramètre
            }
            Integer number = tPClient.connexionServeur();//Connexion au serveur et récupération de l'id du joueur
            try{//Initialisation de la position du joueur
                tPClient.minit(number, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            }catch(Error e){
                e.printStackTrace();
                System.exit(0);
            }

            // Pour fermeture
            tPClient.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            // Create Panel back forward
            tPClient.pack();
            tPClient.setSize(1000, 1000+200);
            tPClient.setVisible(true);

            //Tant que le joueur ne ferme pas la fenetre il peut executer les actions via les boutons
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /** Pour rafraichir */
    class MyTimerTask extends TimerTask{
        public void run ()
        {//Tant que la partie n'est pas finie récupérer l'état de la grille et la mettre à jour à l'écran
            if(!partie_finie){
                receiveEtat();
                refresh();
            }
        }
    }
	
}
