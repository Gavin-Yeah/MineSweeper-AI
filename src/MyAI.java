package src;

import java.util.ArrayDeque;
import java.util.ArrayList;

import java.util.List;

import java.util.Queue;

import src.Action.ACTION;

public class MyAI extends AI {
    
    private enum Status {SAFE, BOMB, UNKNOWN}
    
    private final static int[] DX = {-1, -1, -1, 0, 0, 1, 1, 1};
    private final static int[] DY = {-1, 0, 1, -1, 1, -1, 0, 1};
    
    private final int ROW_DIMENSION, COL_DIMENSION, TOT_MINES;
    private Tile[][] board;
    private Action prevAction;
    private Queue<Action> actions;
    
    @SuppressWarnings("unchecked")
    MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
        ROW_DIMENSION = rowDimension;
        COL_DIMENSION = colDimension;
        TOT_MINES = totalMines;
        board = new Tile[rowDimension + 1][colDimension + 1];
        initBoard();
        actions = new ArrayDeque<>();
        prevAction = new Action(ACTION.UNCOVER, startX, startY);
    }
    
    public Action getAction(int number) {
        updateBoard(prevAction);
        if (number != -1) {
            board[prevAction.y][prevAction.x].number = number;
            if (number == 0) {
                enqueueNeighbors(board[prevAction.y][prevAction.x], ACTION.UNCOVER);
            }
        }
        enqueueAvailableActions(ACTION.UNCOVER);
        enqueueAvailableActions(ACTION.FLAG);
        //enqueueAvailableActions();
        Action action = actions.poll();
        
        if(action== null) {
            List<Tile> list=formula();
            for (int i = 0; i < list.size(); i++) {
                enqueue(ACTION.UNCOVER,list.get(i));
                
            }
            formula2();
            formula3();
        
            action = actions.poll();
        }
        
      
         if (action == null) {
         finalturn();
         action = actions.poll();
         }
        if (action == null) {
            enqueue(ACTION.UNCOVER,randomTile());
            action = actions.poll();
        }
        return (prevAction = action);
    }
    
    
    
    
    private int count(Tile t, Status status) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int x = t.x + DX[i], y = t.y + DY[i];
            if (valid(x, y) && board[y][x].status == status) {
                count++;
            }
        }
        return count;
    }
    
    
    

    
    

    private boolean knownOnBorder(Tile t) {
        for (int i = 0; i < 8; i++) {
            int x = t.x + DX[i], y = t.y + DY[i];
            if (valid(x, y) && board[y][x].status == Status.UNKNOWN) {
            
                return true;
            }
        }
        
        return false;
    }
    private List<Tile> findBorderKnownTiles() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 1; i <= ROW_DIMENSION; i++) {
            for (int j = 1; j <= COL_DIMENSION; j++) {
                if (board[i][j].status == Status.SAFE && knownOnBorder(board[i][j]))
                    tiles.add(board[i][j]);
            }
        }
 
        return tiles;
    }
    
    private List<Tile> unknownNeighbor(Tile t) {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int x = t.x + DX[i], y = t.y + DY[i];
            if (valid(x, y) && board[y][x].status == Status.UNKNOWN) {
                tiles.add(board[y][x]);
            }
        }
       
        return tiles;
    }
    
    private List<Tile> knownNeighbor(Tile t) {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int x = t.x + DX[i], y = t.y + DY[i];
            if (valid(x, y) && board[y][x].status == Status.SAFE) {
                tiles.add(board[y][x]);
            }
        }

        return tiles;
    }
    
    
    
    private int unknownBomb(Tile t) {
        int number=t.number;
        if(number>0) {
            for (int i = 0; i < 8; i++) {
                int x = t.x + DX[i], y = t.y + DY[i];
                if (valid(x, y) && board[y][x].status == Status.BOMB) {
                    number--;
                }
            }
        }

        return number;
    }
    
    
    
    private List<Tile> formula(){
        List<Tile> list= new ArrayList<>();
        List<Tile> tiles= findBorderKnownTiles();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            List<Tile>knownNeighbors=knownNeighbor(tile);
            for (int j = 0; j < knownNeighbors.size(); j++) {
                Tile neighbor= knownNeighbors.get(j);
                int number1=unknownBomb(tile);
                int number2=unknownBomb(neighbor);
                if(number1==number2) {
                    List<Tile> unknownNeigbors1= unknownNeighbor(tile);
                    List<Tile> unknownNeigbors2= unknownNeighbor(neighbor);
                    if(unknownNeigbors1.containsAll(unknownNeigbors2)) {
                        unknownNeigbors1.removeAll(unknownNeigbors2);
                        list.addAll(unknownNeigbors1);
                        
                    }else if(unknownNeigbors2.containsAll(unknownNeigbors1)){
                        unknownNeigbors2.removeAll(unknownNeigbors1);
                        list.addAll(unknownNeigbors2);
                    }
                    
                }
                
            }
        }
        return list;
    }
    
    
    private void formula2(){
        
        List<Tile> tiles= findBorderKnownTiles();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            List<Tile>knownNeighbors=knownNeighbor(tile);
            for (int j = 0; j < knownNeighbors.size(); j++) {
                Tile neighbor= knownNeighbors.get(j);
                if(near(tile,neighbor)){
                
                int number1=unknownBomb(tile);
                int number2=unknownBomb(neighbor);
                List<Tile> unknownNeigbors1= unknownNeighbor(tile);
                List<Tile> unknownNeigbors2= unknownNeighbor(neighbor);
                if(number1==1&&number2==2) {
                    
                    List<Tile> list2=unknownNeigbors2;
                    list2.removeAll(unknownNeigbors1);
                    if(list2.size()==1) {
                        enqueue(ACTION.FLAG,list2.get(0));
                        
                    }
                    
                }
         
                
                
                
                
                    }
                
            }
        }
        
    }
    
    
    private void formula3(){
     
        List<Tile> tiles= findBorderKnownTiles();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            List<Tile>knownNeighbors=new ArrayList<>();
            if(tile.x+2<=COL_DIMENSION){
                knownNeighbors.add(board[tile.y][tile.x+2]);
            }
            if(tile.y+2<=ROW_DIMENSION){
                knownNeighbors.add(board[tile.y+2][tile.x]);
            }
            if(tile.x-2>=1){
                knownNeighbors.add(board[tile.y][tile.x-2]);
            }
            if(tile.y-2>=1){
                knownNeighbors.add(board[tile.y-2][tile.x]);
            }
            for (int j = 0; j < knownNeighbors.size(); j++) {
                Tile neighbor= knownNeighbors.get(j);
             
                
                int number1=unknownBomb(tile);
                int number2=unknownBomb(neighbor);
                List<Tile> unknownNeigbors1= unknownNeighbor(tile);
                List<Tile> unknownNeigbors2= unknownNeighbor(neighbor);
                if(number1==1&&number2==2) {
                    
                    List<Tile> list2=unknownNeigbors2;
                    list2.removeAll(unknownNeigbors1);
                    if(list2.size()==1) {
                        enqueue(ACTION.FLAG,list2.get(0));
                        
                    }
                    
                }
                if(number2==1&&number1==2) {
                    List<Tile> list1=unknownNeigbors1;
                    
                    list1.removeAll(unknownNeigbors2);
                    if(list1.size()==1) {
                        enqueue(ACTION.FLAG,list1.get(0));
                        
                        
                    }
                    
                }
            
                
            }
        }
        
    }
    
 
    
    
    private boolean near(Tile t1, Tile t2){
        boolean ret=false;
        int diffx,diffy;
        diffx=Math.abs(t1.x - t2.x);
        diffy=Math.abs(t1.y - t2.y);
        if(diffx==1&&diffy==0){
            ret=true;
        }else if(diffx==0&&diffy==1){
            ret =true;
        }
        return ret;
    }
    
    
    private List<Tile> findUnknownTiles() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 1; i <= ROW_DIMENSION; i++) {
            for (int j = 1; j <= COL_DIMENSION; j++) {
                if (board[i][j].status == Status.UNKNOWN)
                    tiles.add(board[i][j]);
            }
        }
        
        return tiles;
    }
    private int findBombTiles() {
        int count=0;
        for (int i = 1; i <= ROW_DIMENSION; i++) {
            for (int j = 1; j <= COL_DIMENSION; j++) {
                if (board[i][j].status == Status.BOMB)
                    count++;
            }
        }
        
        return count;
    }
    
    
    
    private void finalturn() {
        List<Tile>unknown=findUnknownTiles();
        int bombnum=findBombTiles();
        if(TOT_MINES-bombnum==unknown.size()) {
            for(int i=0;i<unknown.size();i++) {
                enqueue(ACTION.FLAG,unknown.get(i));
            }
        } else if(bombnum==TOT_MINES) {
            for(int i=0;i<unknown.size();i++) {
                enqueue(ACTION.UNCOVER,unknown.get(i));
            }
        }
    }
    
    
    
    
    private Tile randomTile() {
        Tile ret=null, rightTile=null;
        List<Tile> tiles= findBorderKnownTiles();
        if(tiles.size()==0){
            int x,y;
            do {
                x = (int) (Math.random() * COL_DIMENSION) + 1;
                y = (int) (Math.random() * ROW_DIMENSION) + 1;
            } while (board[y][x].status != Status.UNKNOWN);
            return board[y][x];
        }
        int minbomb=10;
        int maxnei=0;

        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            int  bomb=unknownBomb(tile);
   
            int  unknown=count(tile,Status.UNKNOWN) ;
       
            if(bomb<=minbomb) {
                if(unknown>=maxnei) {
                    minbomb=bomb;
                    maxnei=unknown;
                    rightTile=tiles.get(i);
                    
                }
            }
            
        }
 
        List<Tile> neighbors=unknownNeighbor(rightTile);
        int r = (int) (Math.random() * (neighbors.size()-1));
  
        ret=neighbors.get(r);
       
        
        return ret;
        
    }
    
    
    
    
    private void updateBoard(Action action) {
        Tile t = board[action.y][action.x];
        if (action.action == ACTION.UNCOVER) {
            t.status = Status.SAFE;
            for (Tile neighbor : getNeighbors(t))
                neighbor.unknownNeighbors--;
        } else if (action.action == ACTION.FLAG) {
            t.status = Status.BOMB;
            for (Tile neighbor : getNeighbors(t))
                neighbor.flaggedBombNeighbors++;
        }
    }
    

    
    private void enqueueNeighbors(Tile t, ACTION action) {
        for (Tile neighbor : getNeighbors(t)) {
            if (neighbor.status == Status.UNKNOWN)
                enqueue(action, neighbor);
        }
    }
    
    private void enqueueAvailableActions(ACTION action) { // simplify the function with arg void and process 2 actions
        for (int i = 1; i < board.length; i++) {
            for (int j = 1; j < board[0].length; j++) {
                Tile t = board[i][j];
                if ((t.number == t.flaggedBombNeighbors && action == ACTION.UNCOVER) ||
                    (t.number == t.unknownNeighbors && action == ACTION.FLAG)) {
                    enqueueNeighbors(t, action);
                }
            }
        }
    }
    
 
    
    
    private void enqueue(ACTION action, Tile t) {
        Action a = new Action(action, t.x, t.y);
        if (notInQueue(a)) actions.offer(a);
    }
    
    private boolean notInQueue(Action action) {
        for (Action a : actions) {
            if (a.action == action.action && a.x == action.x && a.y == action.y)
                return false;
        }
        return true;
    }
    
    private void initBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = board[i][j] == null ? new Tile(i, j) : board[i][j];
                board[i][j].unknownNeighbors = getNeighbors(board[i][j]).size();
            }
        }
    }
    
    private List<Tile> getNeighbors(Tile t) {
        List<Tile> neighbors = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int x = t.x + DX[i], y = t.y + DY[i];
            if (valid(x, y)) {
                board[y][x] = board[y][x] == null ? new Tile(x, y) : board[y][x];
                neighbors.add(board[y][x]);
            }
        }
        return neighbors;
    }
    
    private boolean valid(int x, int y) {
        return 1 <= x && x <= COL_DIMENSION && 1 <= y && y <= ROW_DIMENSION;
    }
    
    private static class Tile {
        int x, y, number = -1;
        int unknownNeighbors, flaggedBombNeighbors;
        Status status = Status.UNKNOWN;
        
        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
    
    
    
    
}




