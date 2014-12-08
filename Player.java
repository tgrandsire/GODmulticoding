import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int nbrJoueur = in.nextInt(); // number of players in the game (2 to 4 players) (P)
        int ME_id = in.nextInt(); // ID of your player (0, 1, 2, or 3)
        int nbrDronePerJoueur = in.nextInt(); // number of drones in each team (3 to 11) (D)
        int nbrZonesInMap = in.nextInt(); // number of zones on the map (4 to 8) (Z)
        
        //initializing board with param number of player
        GameHelper.setGameVars(nbrJoueur, nbrDronePerJoueur, nbrZonesInMap, ME_id);
        Board board = new Board(nbrJoueur, nbrDronePerJoueur, nbrZonesInMap, ME_id);
        GameHelper.setBoard(board);
        GameHelper.setDefaultDestination(new Coordinate(2000, 900));
        Joueur ME = board.getJoueur(ME_id);
        
        for (int zoneId = 0; zoneId < nbrZonesInMap; zoneId++) {
            int X = in.nextInt(); // corresponds to the position of the center of a zone. A zone is a circle with a radius of 100 units.
            int Y = in.nextInt();
            // initialization of targetted zones
            board.addZone(new Zone(zoneId, new Coordinate(X, Y)));
        }
        
        // GAME LOOP !!!!!
        while (true) {
            
            for (int i = 0; i < nbrZonesInMap; i++) {
                int TID = in.nextInt(); // ID of the team controlling the zone (0, 1, 2, or 3) or -1 if it is not controlled. The zones are given in the same order as in the initialization.
                // Assignment of the owner for this zone
                board.getZone(i).setOwner(board.getJoueur(TID));
                //System.err.println(board.getZonesList().get(i).getOwner());
                
            }
            // i: joueur ... j: drone
            for (int i = 0; i < nbrJoueur; i++) {
                for (int j = 0; j < nbrDronePerJoueur; j++) {
                    int DX = in.nextInt(); // The first D lines contain the coordinates of drones of a player with the ID 0, the following D lines those of the drones of player 1, and thus it continues until the last player.
                    int DY = in.nextInt();
                    Joueur joueur = board.getJoueur(i);
                    Drone drone = joueur.getDrone(j);
                    drone.setCoordinates(new Coordinate(DX, DY));
                }
            }
            // END INITIALIZATION
            //System.err.println(board.getZone(0).getCoordinates());
            //System.err.println(Coordinate.computeDistance(ME.getDrone(0).getCoordinates(), board.getZone(0).getCoordinates()));
            
            
            board.computeRiskPoints();
            board.zonesAllocatesDrones();
            
            //GameHelper.droneControlTour();
            
            //ANSWER LOOP !!!
            for (int droneId = 0; droneId < nbrDronePerJoueur; droneId++) {
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                
                System.out.println(ME.getDrone(droneId).getDestination().toAnswerString());//.);
            }
        }
    }
    
    
    
    /***********************************************/
    /******************CLASSES**********************/
    
    public static class GameHelper{
        private static Board board;
        private static int nbrZones;
        private static int nbrDrones;
        private static int nbrJoueurs;
        private static int ME_id;
        private static Coordinate defaultDestination = new Coordinate(2000, 900);
        
        public static void setBoard(Board _board){
            board = _board;
        }
        
        public static void setGameVars(int _nbrJoueurs, int _nbrDrones, int _nbrZones, int _ME_id){
            nbrJoueurs = _nbrJoueurs;
            nbrDrones = _nbrDrones;
            nbrZones = _nbrZones;
            ME_id = _ME_id;
        }
        
        public static Board getBoard(){
            return board;
        }
        
        public static int getNbrJoueurs(){
            return nbrJoueurs;
        }
        
        public static int getNbrDrones(){
            return nbrDrones;
        }
        
        public static int getNbrZones(){
            return nbrZones;
        }
        
        public static int getMyOwnId(){
            return ME_id;
        }
        
        public static void setDefaultDestination(Coordinate coord){
            defaultDestination = coord;
        }
        
        public static Coordinate getDefaultDestination(){
            return defaultDestination;
        }
        
        public static void droneControlTour(){
            for (int droneId = 0; droneId < Board.nbrDrone; droneId++){
                Drone drone = board.getJoueur(Board.ME_id).getDrone(droneId);
                if (drone != null){
                    System.err.println("Drone "+ droneId +": "+ drone.toString());
                    System.err.println(((drone.isAllocated())? "Alloué ": "Non alloué ")+((drone.getAllocatedZone() != null)? "à la zone "+drone.getAllocatedZone().getId(): "à aucune zone"));
                    
                }
            }
            for (int zoneId = 0; zoneId < Board.nbrZonesInMap; zoneId++){
                Zone zone = board.getZone(zoneId);
                if (zone != null){
                    System.err.println("Zone "+ zoneId +": "+ zone.toString());
                    int i = 0;
                    for (int riskPoint: zone.getRiskPoints()){
                        System.err.println("Risques pour joueur"+i+" :"+ riskPoint);
                        i++;
                    }
                    System.err.println("Drones:");
                    for (Drone dedicatedDrone: zone.getAllocatedDrones()){
                        System.err.println(dedicatedDrone.toString() +" - id: "+ dedicatedDrone.getId());
                    }
                    
                    //System.err.println()
                }
            }
        }
        
    }
    
    
    public static class Board{
        
        private ArrayList<Zone> zones;
        private ArrayList<Joueur> joueurs;
        private static int nbrJoueur;
        private static int nbrZonesInMap;
        private static int nbrDrone;
        private static int ME_id;
        
        public Board(int nbrJoueur, int nbrDrone, int nbrZonesInMap){
            this.zones = new ArrayList<Zone>();
            this.joueurs = new ArrayList<Joueur>();
            this.nbrJoueur = nbrJoueur;
            this.nbrZonesInMap = nbrZonesInMap;
            this.nbrDrone = nbrDrone;
            this.initJoueurs(nbrJoueur, nbrDrone);
        }
        
        public Board(int nbrJoueur, int nbrDrone, int nbrZonesInMap, int ME_id){
            this(nbrJoueur, nbrDrone, nbrZonesInMap);
            this.ME_id = ME_id;
        }
        
        private void initJoueurs(int nbrJoueur, int nbrDrone){
            for (int i = 0; i < nbrJoueur; i++ ){
                Joueur joueur = new Joueur(i, nbrDrone);
                this.joueurs.add(joueur);
            }
        }
        
        public void addJoueur(Joueur joueur){
            this.joueurs.add(joueur);
        }
        
        public ArrayList<Joueur> getJoueursList(){
            return this.joueurs;
        }
        
        public Joueur getJoueur(int id){
            return (id < 0)? null: (Joueur) this.joueurs.get(id);
        }
        
        public void addZone(Zone zone){
            this.zones.add(zone);
        }
        
        public ArrayList<Zone> getZonesList(){
            return this.zones;
        }
        
        public Zone getZone(int id){
            return (Zone) this.zones.get(id);
        }
        
        public boolean zoneIsMine(Zone zone){
            return (boolean) (zone.getOwner() == this.getJoueur(GameHelper.getMyOwnId()));
        }
        
        public void zonesAllocatesDrones(){
            
            for (int zoneId = 0; zoneId < GameHelper.getNbrZones(); zoneId++){
                Zone zone = this.getZone(zoneId);
                
                //System.err.println("Zone"+zone.getId()+"("+zoneId+"): alloue "+zone.getAllocatedDrones().size()+" drones.");
                //System.err.println("Pour un score de risque de "+zone.getRiskScore());
                
                
                while (zone.getAllocatedDrones().size() < zone.getRiskScore()){
                    Drone drone = zone.getNearestFreeDrone();
                    if (drone != null){
                        zone.allocateDrone(drone);
                        System.err.println("Zone"+zone.getId()+"("+zoneId+"): alloue "+zone.getAllocatedDrones().size()+" drones.");
                    } else
                        break;
                }
                
                while (zone.getAllocatedDrones().size() > zone.getRiskScore()){
                    Drone drone = (zone.getAllocatedDronesInZone().size() > 0)? zone.getAllocatedDronesInZone().get(0): null;
                    if (drone != null && zone.getOwner() == GameHelper.getBoard().getJoueur(GameHelper.getMyOwnId())){
                        zone.freeDrone(drone);
                        System.err.println("Zone"+zone.getId()+"("+zoneId+"): libère et alloue "+zone.getAllocatedDrones().size()+" drones.");
                        
                    } else
                        break;
                }
                    
                
            }
        }
        
        
        public void computeRiskPoints(){
            for (int zoneId = 0; zoneId < GameHelper.getNbrZones(); zoneId++){
                Zone zone = this.getZone(zoneId);
                Coordinate coordZone = zone.getCoordinates();
                zone.clearRiskPoints();
                
                if(!zoneIsMine(zone)){
                    zone.addRiskPointForAll(1);
                }
                
                for (int joueurId = 0; joueurId < GameHelper.getNbrJoueurs(); joueurId++){
                    Joueur joueur = this.getJoueur(joueurId);
                    for (int droneId = 0; droneId < GameHelper.getNbrDrones(); droneId++){
                        
                        Drone drone = this.getJoueur(joueurId).getDrone(droneId);
                        Coordinate coordDrone = drone.getCoordinates();
                        
                        
                        if (Coordinate.computeDistance(coordDrone, coordZone) < 100){
                            if (joueurId == GameHelper.getMyOwnId()){
                                zone.setRiskPoints(joueurId, zone.getDronesInZone().size());
                            } else {
                                zone.addRiskPoint(joueurId, 1);
                            }
                            
                        }
                    }
                }
            }
        }
        
    }
    // end Board
    
    
    public static class Zone{
        
        private int id;
        private Coordinate XY;
        private Joueur owner;
        private ArrayList<Drone> allocatedDrones;
        private ArrayList<Integer> riskPoints;
        
        public Zone(int id, Coordinate XY){
            this.id = id;
            this.XY = XY;
            this.allocatedDrones = new ArrayList<Drone>();
            this.riskPoints = new ArrayList<Integer>();
            this.clearRiskPoints();
        }
        
        public int getId(){
            return this.id;
        }
        
        public void setOwner(Joueur joueur){
            this.owner = joueur;
        }
        
        public Joueur getOwner(){
            return (Joueur) this.owner;
        }
        
        public void setNoOwner(){
            this.owner = null;
        }
        
        public boolean hasOwner(){
            return (this.owner == null)? false: true;
        }
        
        public Drone getNearestFreeDrone(){
            Board board = GameHelper.getBoard();
            int selectedDistance = 9999;
            Drone selectedDrone = new Drone(-1);
            for (int droneId = 0; droneId < board.nbrDrone; droneId++){
                Drone drone = board.getJoueur(Board.ME_id).getDrone(droneId);
                int distance = Coordinate.computeDistance(this.XY, drone.getCoordinates());
                if (distance < selectedDistance){
                    if (!drone.isAllocated()){
                        selectedDistance = distance;
                        selectedDrone = drone;
                    }
                }
            }
            if (selectedDrone.getId() != -1){
                return selectedDrone;
            }
            return null;
        }
        
        public Drone getNearestAllocatedDrone(){
            Board board = GameHelper.getBoard();
            int selectedDistance = 9999;
            Drone selectedDrone = new Drone(-1);
            for (int droneId = 0; droneId < board.nbrDrone; droneId++){
                Drone drone = board.getJoueur(Board.ME_id).getDrone(droneId);
                int distance = Coordinate.computeDistance(this.XY, drone.getCoordinates());
                if (distance < selectedDistance){
                    if (drone.isAllocated() && drone.getAllocatedZone() == this){
                        selectedDistance = distance;
                        selectedDrone = drone;
                    }
                }
            }
            if (selectedDrone.getId() != -1){
                return selectedDrone;
            }
            return null;
        }
        
        public void allocateDrone(Drone drone){
            if(drone != null){
                this.allocatedDrones.add(drone);
                drone.setDestination(this.XY);
                drone.setAllocatedZone(this);
            }
        }
        
        public ArrayList<Drone> getAllocatedDrones(){
            return this.allocatedDrones;
        }
        
        public ArrayList<Drone> getAllocatedDronesInZone(){
            ArrayList<Drone> inZoneAllocatedDrones = new ArrayList<Drone>();
            for (Drone drone: this.allocatedDrones){
                int distance = Coordinate.computeDistance(drone.getCoordinates(), this.getCoordinates());
                if (distance < 100){
                    inZoneAllocatedDrones.add(drone);
                }
            }
            return inZoneAllocatedDrones;
        }
        
        public ArrayList<Drone> getDronesInZone(){
            ArrayList<Drone> inZoneDrones = new ArrayList<Drone>();
            for (int droneId = 0; droneId < Board.nbrDrone; droneId++){
                Drone drone = GameHelper.getBoard().getJoueur(Board.ME_id).getDrone(droneId);
                int distance = Coordinate.computeDistance(drone.getCoordinates(), this.getCoordinates());
                if (distance < 100){
                    inZoneDrones.add(drone);
                }
            }
            return inZoneDrones;
        }
        
        public String getAllocatedDronestoString(){
            String output = new String("Mes drones: ");
            output += (this.allocatedDrones.size() == 0)? "aucun": "";
            for (Drone drone : this.allocatedDrones){
                output += drone.toString();
            }
            return output;
        }
        
        public void freeDrone(Drone drone){
            this.allocatedDrones.remove(drone);
            drone.free();
        }
        
        public void clearAllocatedDrone(){
            for (Drone drone: this.allocatedDrones){
                drone.free();
            }
            this.allocatedDrones.clear();
        }
        
        public Coordinate getCoordinates(){
            return this.XY;
        }
        
        public void setRiskPoints(int joueurId, int riskPoints){
            this.riskPoints.set(joueurId, riskPoints);
        }
        
        public void addRiskPoint(int joueurId, int points){
            int riskPoint = this.riskPoints.get(joueurId);
            riskPoint += points;
            this.setRiskPoints(joueurId, riskPoint);
        }
        
        public void addRiskPointForAll(int points){
            for (int joueurId = 0; joueurId < GameHelper.getNbrJoueurs(); joueurId++){
                if (joueurId != GameHelper.getMyOwnId()){
                    this.addRiskPoint(joueurId, points);
                }
            }
        }
        
        public ArrayList<Integer> getRiskPoints(){
            return this.riskPoints;
        }
        public int getRiskPoints(int joueurId){
            return this.riskPoints.get(joueurId);
        }
        
        public void clearRiskPoints(){
            this.riskPoints.clear();
            for (int joueurId = 0; joueurId < GameHelper.getNbrJoueurs(); joueurId++){
                this.riskPoints.add(joueurId, 0);
            }
        }
        
        public int getRiskScore(){
            int safetyPoint = this.getRiskPoints(GameHelper.getMyOwnId());
            int maxRiskPoint = 0;
            for (int joueurId = 0; joueurId < Board.nbrJoueur; joueurId++){
                if (joueurId != Board.ME_id)
                    maxRiskPoint = Math.max(maxRiskPoint, this.getRiskPoints(joueurId));
            }
            //return (int) (maxRiskPoint-safetyPoint);
            return (int) maxRiskPoint;
        }
        
    }
    //end Zone
    
    public static class Drone{
        
        private int id;
        private Coordinate XY;
        private Coordinate previous_XY;
        private Coordinate destination;
        private boolean allocated;
        private Zone allocatedZone;
        
        public Drone(int id){
            this.id = id;
            this.allocated = false;
        }
        
        public Drone(int id, Coordinate XY){
            this(id);
            this.setCoordinates(XY);
        }
        
        public int getId(){
            return this.id;
        }
        
        public void setCoordinates(Coordinate XY){
            this.previous_XY = this.XY;
            this.XY = XY;
        }
        
        public Coordinate getCoordinates(){
            return this.XY;
        }
        
        public void setDestination(Coordinate coord){
            this.destination = coord;
        }
        
        public Coordinate getDestination(){
            return (this.destination == null)? new Coordinate(2000, 900): (Coordinate) this.destination;
        }
        
        public boolean isAllocated(){
            return this.allocated;
        }
        
        public void setAllocatedZone(Zone zone){
            this.allocatedZone = zone;
            this.allocated = true;
        }
        
        public Zone getAllocatedZone(){
            return this.allocatedZone;
        }
        
        public void free(){
            this.allocated = false;
            this.allocatedZone = null;
            this.destination = GameHelper.getDefaultDestination();
        }
        
    }
    //end Drone
    
    
    public static class Joueur {
        
        private int id;
        private int score;
        private ArrayList<Drone> drones;
        
        public Joueur(int id, int nbrDrone){
            this.setId(id);
            this.drones = new ArrayList<Drone>();
            //init drones
            initDrones(nbrDrone);
        }
        
        public void setId(int id){
            this.id = id;
        }
        
        public int getId(){
            return this.id;
        }
        
        public void addDrone(Drone drone){
            this.drones.add(drone);
        }
        
        public ArrayList getDronesList(){
            return this.drones;
        }
        
        public Drone getDrone(int id){
            return (Drone) this.drones.get(id);
        }
        
        public void initDrones(int nbrDrone){
            for (int i = 0; i < nbrDrone; i++){
                Drone drone = new Drone(i);
                this.drones.add(drone);
            }
        }
        
        public String toString(){
            return "id:" + this.id;
        }
        
    }
    
    
    public static class Coordinate{
        public int x;
        public int y;
        
        public Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }
        
        public void setX(int x){
            this.x = x;
        }
        
        public void setY(int y){
            this.y = y;
        }
        
        public int getX(){
            return this.x;
        }
        
        public int getY(){
            return this.y;
        }
        
        public static int computeDistance(Coordinate coordA, Coordinate coordB){
            double terme1 = Math.pow((double)coordB.x - (double)coordA.x, 2);
            double terme2 = Math.pow((double)coordB.y - (double)coordA.y, 2);
            return (int)(Math.round(Math.sqrt(terme1+terme2)));
        }
        
        public String toAnswerString(){
            return String.valueOf(this.x) + " " + String.valueOf(this.y);
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}