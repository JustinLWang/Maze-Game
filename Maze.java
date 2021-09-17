import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Comparator;
import java.util.HashMap;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

import java.util.Random;


// represents the MazeGameWorld
class MazeGameWorld extends World {
  int width;
  int height;
  int cellSize;
  ArrayList<ArrayList<Node>> board;
  Random rand;
  HashMap<Posn, Posn> reps;
  ArrayList<Edge> worklist;
  ArrayList<Edge> mst;
  Node playersNode;
  Node endNode;
  ArrayList<Node> searchedNodes;

  // search booleans
  boolean bFirst;
  boolean dFirst;

  // game ended?
  boolean gameEnd;

  //constructor
  MazeGameWorld(int width, int height) {
    this.width = width;
    this.height = height;

    if (this.width > this.height) {
      this.cellSize = 800 / this.width;
    } else {
      this.cellSize = 800 / this.height;
    }

    this.worklist = new ArrayList<Edge>();
    rand = new Random();
    constructBoard();
    createInitWorkList();
    mapNodes(this.board);
    kruskalAlgo();
    updateCells();
    updateOutEdges();
    this.playersNode = this.board.get(0).get(0);
    this.endNode = this.board.get(this.width - 1).get(this.height - 1);
    bFirst = false;
    dFirst = false;
    gameEnd = false;

  }
  // constructor with random field

  MazeGameWorld(int width, int height, Random rand) {
    this.width = width;
    this.height = height;
    this.rand = rand;

    if (this.width > this.height) {
      this.cellSize = 800 / this.width;
    } else {
      this.cellSize = 800 / this.height;
    }

    this.worklist = new ArrayList<Edge>();
    constructBoard();
    createInitWorkList();
    mapNodes(this.board);
    kruskalAlgo();
    updateCells();
    updateOutEdges();
    this.playersNode = this.board.get(0).get(0);
    this.endNode = this.board.get(this.width - 1).get(this.height - 1);
    bFirst = false;
    dFirst = false;
    gameEnd = false;

  }

  // creates the initial list of edges
  // EFFECT: adds the edges to the worklist
  void createInitWorkList() {
    Node currentNode;
    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(i).size(); j++) {
        currentNode = this.board.get(i).get(j);

        if (i < this.board.size()) {
          worklist.add(new Edge(currentNode, currentNode.bottom, this.rand.nextInt(1000) + 1));
        }
        if (j < this.board.get(i).size()) {
          worklist.add(new Edge(currentNode, currentNode.right, this.rand.nextInt(1000) +  1));
        }
      }
    }
    worklist.get(0).weight = 0;
    worklist.get(worklist.size() - 1).weight = 0;
  }

  //unions two values in a hasmap
  //EFFECT: updates the representatives value at the first posn
  void union(Posn p1, Posn p2) {
    reps.put(this.find(p1), this.find(p2));
  }

  // finds a representative
  Posn find(Posn p1) {
    if (p1.equals(reps.get(p1))) {
      return p1;
    }
    else {
      return find(reps.get(p1)); 
    }
  }


  // map all nodes to themselves
  // EFFECT: makes every nodes key itself
  void mapNodes(ArrayList<ArrayList<Node>> allnodes) {
    this.reps = new HashMap<Posn, Posn>();
    // first index gets the ArrayList<Node>
    for (int i = 0; i < allnodes.size(); i = i + 1) {
      // second index gets the actual Node, and it has to be less than then  
      //the ArrayList its in (allnodes.get(indx1))
      for ( int j = 0; j < allnodes.get(i).size(); j = j + 1) {
        //each node reps itself
        reps.put(allnodes.get(i).get(j).posn, allnodes.get(i).get(j).posn);
      }
    }
  }


  // creates the spanning tree using Kruskals Algorithm
  // EFFECT: adds the edges to the mst and creates the tree
  void kruskalAlgo() {
    int allNodes = this.height * this.width;
    int i = 0;
    // built in sort function
    this.worklist.sort(new WeightComparator());
    Edge curredge;
    this.mst = new ArrayList<Edge>();
    while (this.mst.size() < allNodes && i < this.worklist.size()) {
      curredge = this.worklist.get(i);
      if (curredge.from != null && curredge.to != null) {
        if (this.find(find(curredge.from.posn)).equals(this.find(this.find(curredge.to.posn)))) {
          //DO NOTHING MOVE TO NEXT 
        } else {
          this.mst.add(curredge);
          this.union(this.find(curredge.from.posn), this.find(curredge.to.posn));
        }
      }
      i = i + 1;
    }
  }

  // updates the cells neighbors
  // EFFECT: mutates the fields (boolean values)
  void updateCells() {
    for (Edge e : this.mst) {
      if (e.from != null || e.to != null) { 
        if (e.from.top != null && e.from.top.equals(e.to)) {
          e.from.hasTop = true;
          e.to.hasBottom = true;
          //e.from.outEdges.add(e);
        }
        if (e.from.bottom != null && e.from.bottom.equals(e.to)) {
          e.from.hasBottom = true;
          e.to.hasTop = true;
          //e.from.outEdges.add(e);
        }
        if (e.from.left != null && e.from.left.equals(e.to)) {
          e.from.hasLeft = true;
          e.to.hasRight = true;
          //e.from.outEdges.add(e);
        }
        if (e.from.right != null && e.from.right.equals(e.to)) {
          e.from.hasRight = true;
          e.to.hasLeft = true;
          //e.from.outEdges.add(e);
        }
      }
    }
  }

  // updates the outEdges 
  void updateOutEdges() {
    for (Edge e : this.mst) {
      if (e.from != null && e.to != null) {
        e.from.outEdges.add(e);
        e.to.outEdges.add(e);
      }
    }
  }



  // constructs the maze board
  //EFFECT: adds Nodes and rows to the board
  void constructBoard() {
    ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();

    //creates an unlinked list of Nodes with a 2D array
    // 2D array board [width][height] or think [r][c]
    for (int i = 0; i < this.width; i++) {
      ArrayList<Node> row = new ArrayList<Node>();
      result.add(row);
      row = new ArrayList<Node>();
      for (int j = 0; j < this.height; j++) {
        result.get(i).add(new Node(i, j));
      }
    }

    this.board = result;

    // set top for all Nodes on the board
    for (int i = 0; i < this.width; i++) {
      for (int j = this.height - 1; j > 0; j--) {
        this.board.get(i).get(j).top = this.board.get(i).get(j - 1);
      }
    }

    // set bottom for all Nodes on the board
    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height - 1; j++) {
        this.board.get(i).get(j).bottom = this.board.get(i).get(j + 1);
      }
    }

    // sets right for all Nodes on the board
    for (int i = 0; i < this.width - 1; i++) {
      for (int j = 0; j < this.height; j++) {
        this.board.get(i).get(j).right = this.board.get(i + 1).get(j);
      }
    }

    // sets left for all Nodes on the board 
    for (int i = 1; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        this.board.get(i).get(j).left = this.board.get(i - 1).get(j);
      }
    }

    this.board.get(0).get(0).color = Color.green;
    this.board.get(this.width - 1).get(this.height - 1).color = new Color(128, 0, 128);

  }

  // makes the scene/world of the game
  public WorldScene makeScene() {
    WorldScene grid = new WorldScene(1000, 1000);

    for (int i = 0; i < this.width; i++) {
      for (int j = 0; j < this.height; j++) {
        grid.placeImageXY(this.board.get(i).get(j).drawCell(this.cellSize), 
            this.board.get(i).get(j).x * this.cellSize + (this.cellSize / 2), 
            this.board.get(i).get(j).y * this.cellSize + (this.cellSize / 2));
      }
    }
    if (this.gameEnd) {
      grid.placeImageXY(new TextImage("You Completed the Maze!", 30, Color.black), 250, 500);
    }
    return grid;
  }

  // if given a queue it will do bfs
  // if given a stack it will do dfs
  ArrayList<Node> search(Node from, Node to, ICollection<Node> list) {
    HashMap<Node, Edge> cameFromEdge = new HashMap<Node, Edge>();
    ArrayList<Node> result = new ArrayList<Node>();
    ArrayDeque<Node> alreadySeen = new ArrayDeque<Node>();
    list.add(from);
    while (!list.isEmpty()) {
      Node next = list.remove();
      if (next.equals(to)) {
        return result;
      } else if (alreadySeen.contains(next)) {
        // do nothing
      }
      else {
        for (Edge e: next.outEdges) {
          list.add(e.to);
          list.add(e.from);
          result.add(e.to);
          result.add(e.from);
          cameFromEdge.put(next, e);
        }

        alreadySeen.add(next);
      }
    }
    System.out.println(result.size());
    return result;
  }

  // key event methods
  public void onKeyEvent(String key) {
    if (key.equals("up") && !this.gameEnd) {
      if (this.playersNode.top != null && this.playersNode.hasTop) {
        this.playersNode.visited = true;
        this.playersNode.color = Color.cyan;
        this.playersNode = this.playersNode.top;
        this.playersNode.color = Color.blue;
      }
    }
    if (key.equals("down") && !this.gameEnd) {
      if (this.playersNode.bottom != null && this.playersNode.hasBottom) {
        this.playersNode.visited = true;
        this.playersNode.color = Color.cyan;
        this.playersNode = this.playersNode.bottom;
        this.playersNode.color = Color.blue;
      }
    }
    if (key.equals("left") && !this.gameEnd) {
      if (this.playersNode.left != null && this.playersNode.hasLeft) {
        this.playersNode.visited = true;
        this.playersNode.color = Color.cyan;
        this.playersNode = this.playersNode.left;
        this.playersNode.color = Color.blue;
      }

    }
    if (key.equals("right") && !this.gameEnd) {
      if (this.playersNode.right != null && this.playersNode.hasRight) {
        this.playersNode.visited = true;
        this.playersNode.color = Color.cyan;
        this.playersNode = this.playersNode.right;
        this.playersNode.color = Color.blue;
      }

    }
    if (key.equals("b") && !this.gameEnd) {
      this.searchedNodes = this.search(this.board.get(0).get(0), 
          this.board.get(this.width - 1).get(this.height - 1), new Queue<Node>());
      this.bFirst = true;
    }
    if (key.equals("d") && !this.gameEnd) {
      this.searchedNodes = this.search(this.board.get(0).get(0), 
          this.board.get(this.width - 1).get(this.height - 1), new Stack<Node>());
      this.dFirst = true;
    }
    if (key.equals("r")) {
      this.worklist = new ArrayList<Edge>();
      rand = new Random();
      constructBoard();
      createInitWorkList();
      mapNodes(this.board);
      kruskalAlgo();
      updateCells();
      updateOutEdges();
      this.playersNode = this.board.get(0).get(0);
      this.endNode = this.board.get(this.width - 1).get(this.height - 1);
      this.bFirst = false;
      this.dFirst = false;
      this.gameEnd = false;
    }
  }

  // onTick method
  public void onTick() {

    if (this.playersNode.equals(this.endNode)) {
      this.gameEnd = true;
    }

    if (this.bFirst) {
      if (this.searchedNodes.size() != 0) {
        this.playersNode.color = Color.cyan;
        this.playersNode = this.searchedNodes.get(0);
        this.searchedNodes.remove(0);
      }
    }

    if (this.dFirst) {
      if (this.searchedNodes.size() != 0) {
        this.playersNode.color = Color.cyan;
        this.playersNode = this.searchedNodes.get(0);
        this.searchedNodes.remove(0);
      }

    }

  }




}

// class that represents an edge
class Edge {
  Node from;
  Node to;
  int weight;
  Random rand = new Random();

  // constructor
  Edge(Node from, Node to) {
    this.from = from;
    this.to = to;
    this.weight = rand.nextInt();
  }

  // constructor that includes weight
  Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }


}

// class that compares the weight of edges
class WeightComparator implements Comparator<Edge> {

  // comapres weight of edges
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }

}

// Node class
class Node {
  int x;
  int y;
  Posn posn;
  Node left;
  Node bottom;
  Node top; 
  Node right;
  boolean visited;
  Color color;
  ArrayList<Edge> outEdges;

  // boolean for walls
  //Can you travel LRTB?
  boolean hasTop;
  boolean hasLeft;
  boolean hasBottom;
  boolean hasRight;

  Node(int x, int y) {
    this.posn = new Posn(x, y);
    this.x = x;
    this.y = y;
    this.hasBottom = false;
    this.hasTop = false;
    this.hasLeft = false;
    this.hasRight = false;
    this.color = Color.gray;
    this.outEdges = new ArrayList<Edge>();

  }

  Node(int x, int y, Color color, boolean hasTop, boolean hasLeft,
      boolean hasBottom, boolean hasRight) {
    this.posn = new Posn(x, y);
    this.x = x;
    this.y = y;
    this.hasTop = hasTop;
    this.hasLeft = hasLeft;
    this.hasBottom = hasBottom;
    this.hasRight = hasRight;
    this.color = Color.gray;
    this.outEdges = new ArrayList<Edge>();


  }

  Node(int x, int y, Node left, Node bottom, Node top, 
      Node right, Color color, boolean hasTop, boolean hasLeft, 
      boolean hasBottom, boolean hasRight) {
    this.x = x;
    this.y = y;
    this.posn = new Posn(x, y);
    this.left = left;
    this.bottom = bottom;
    this.top = top;
    this.right = right;
    this.hasTop = hasTop;
    this.hasLeft = hasLeft;
    this.hasBottom = hasBottom;
    this.hasRight = hasRight;
    this.color = Color.gray;
    this.outEdges = new ArrayList<Edge>();
  }


  // draws the cells of the board
  WorldImage drawCell(int cellSize) {
    WorldImage cell = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
    if (!this.hasTop) {
      cell = new AboveImage(new LineImage(new Posn(cellSize, 0), Color.black), cell);
    }
    if (!this.hasBottom) {
      cell = new AboveImage(cell, new LineImage(new Posn(cellSize, 0), Color.black));
    }
    if (!this.hasRight) {
      cell = new BesideImage(cell, new LineImage(new Posn(0, cellSize), Color.black));
    }
    if (!this.hasLeft) {
      cell = new BesideImage(new LineImage(new Posn(0, cellSize), Color.black), cell);
    }
    return cell;
  }


}

// represents ICollection interface
interface ICollection<T> {

  // checks if ICollection is empty
  boolean isEmpty();

  // adds given item
  void add(T item);

  // removes item
  T remove();
}

// represents a Stack
class Stack<T> implements ICollection<T> {
  ArrayDeque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  // determines if Stack is empty
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // adds item to front of Stack
  public void add(T item) {
    this.contents.addFirst(item);
  }

  // removes first item from Stack
  public T remove() {
    return this.contents.removeFirst();
  }

}

// represents a Queue
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  // determines if Queue is empty
  public boolean isEmpty() {
    return this.contents.size() == 0;
  }

  // adds item to back of Queue
  public void add(T item) {
    this.contents.addLast(item);
  }

  // removes first item from Queue
  public T remove() {
    return this.contents.removeFirst();
  }

}

//examples and tests for Maze
class MazeExamples {

  Random seeded = new Random(2);

  MazeGameWorld maze1 = new MazeGameWorld(20,10);
  MazeGameWorld maze2x2 = new MazeGameWorld(2,2);
  MazeGameWorld maze2 = new MazeGameWorld(2,1, seeded);
  MazeGameWorld maze3 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze4 = new MazeGameWorld(1,1, seeded);
  MazeGameWorld maze5 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze6 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze7 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze8 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze9 = new MazeGameWorld(3,3, seeded);
  MazeGameWorld maze10 = new MazeGameWorld(3,3, seeded);
  MazeGameWorld maze11 = new MazeGameWorld(3,3, seeded);
  MazeGameWorld maze12 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze13 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze14 = new MazeGameWorld(2,2, seeded);
  MazeGameWorld maze15 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze16 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze17 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze18 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze19 = new MazeGameWorld(5,5, seeded);
  MazeGameWorld maze20 = new MazeGameWorld(1,2, new Random(2));
  MazeGameWorld maze21 = new MazeGameWorld(2,3, new Random(2));


  ICollection<Node> coll1 = new Queue<Node>();
  ICollection<Node> coll2 = new Queue<Node>();
  ICollection<Node> coll6 = new Queue<Node>();
  ICollection<Node> coll3 = new Stack<Node>();
  ICollection<Node> coll4 = new Stack<Node>();
  ICollection<Node> coll5 = new Stack<Node>();


  Node node1 = new Node(2, 4, Color.black, false, true, true, true);
  Node node2 = new Node(2, 4, Color.green, true, false, true, true);
  Node node3 = new Node(2, 4, Color.green, true, true, false, true);
  Node node4 = new Node(2, 4, Color.green, true, true, true, false);

  Node node5 = new Node(1, 2, node1, node2, node3, node4, 
      Color.blue, true, true, true, true);

  void testSearch(Tester t) {

    ArrayList<Node> path = new ArrayList<Node>();
    path.add(this.maze20.board.get(0).get(1));
    path.add(this.maze20.board.get(0).get(0));
    t.checkExpect(this.maze20.search(this.maze20.board.get(0).get(0), 
        this.maze20.board.get(0).get(1), new Stack<Node>()), path);
    t.checkExpect(this.maze20.search(this.maze20.board.get(0).get(0), 
        this.maze20.board.get(0).get(1), new Queue<Node>()), path);
    ArrayList<Node> path2 = new ArrayList<Node>();
    path2.add(this.maze21.board.get(0).get(1)); //
    path2.add(this.maze21.board.get(0).get(0)); //
    path2.add(this.maze21.board.get(1).get(0)); //
    path2.add(this.maze21.board.get(0).get(0));  //
    path2.add(this.maze21.board.get(1).get(0)); //
    path2.add(this.maze21.board.get(0).get(0)); // 
    path2.add(this.maze21.board.get(0).get(1)); //
    path2.add(this.maze21.board.get(0).get(0)); // 
    path2.add(this.maze21.board.get(0).get(2)); //
    path2.add(this.maze21.board.get(0).get(1)); //
    path2.add(this.maze21.board.get(1).get(1)); //
    path2.add(this.maze21.board.get(0).get(1)); // 
    path2.add(this.maze21.board.get(1).get(1)); // 
    path2.add(this.maze21.board.get(0).get(1)); //
    path2.add(this.maze21.board.get(0).get(2)); //
    path2.add(this.maze21.board.get(0).get(1)); //
    path2.add(this.maze21.board.get(1).get(2));
    path2.add(this.maze21.board.get(0).get(2));


    t.checkExpect(this.maze21.search(this.maze21.board.get(0).get(0), 
        this.maze21.board.get(1).get(2), new Stack<Node>()), path2);
    ArrayList<Node> path3 = new ArrayList<Node>();
    path3.add(this.maze21.board.get(0).get(1)); //
    path3.add(this.maze21.board.get(0).get(0)); //
    path3.add(this.maze21.board.get(1).get(0)); //
    path3.add(this.maze21.board.get(0).get(0)); //
    path3.add(this.maze21.board.get(0).get(1)); // 
    path3.add(this.maze21.board.get(0).get(0)); //
    path3.add(this.maze21.board.get(0).get(2)); //
    path3.add(this.maze21.board.get(0).get(1)); //
    path3.add(this.maze21.board.get(1).get(1)); //
    path3.add(this.maze21.board.get(0).get(1)); //
    path3.add(this.maze21.board.get(1).get(0)); //
    path3.add(this.maze21.board.get(0).get(0)); //
    path3.add(this.maze21.board.get(0).get(2)); //
    path3.add(this.maze21.board.get(0).get(1)); //
    path3.add(this.maze21.board.get(1).get(2)); // 
    path3.add(this.maze21.board.get(0).get(2)); //
    path3.add(this.maze21.board.get(1).get(1)); //
    path3.add(this.maze21.board.get(0).get(1)); //
    t.checkExpect(this.maze21.search(this.maze21.board.get(0).get(0), 
        this.maze21.board.get(1).get(2), new Queue<Node>()), path3);


  }

  // test for onKeyEvent()
  void testOnKeyEvent(Tester t) {
    this.maze14.playersNode = this.maze14.board.get(0).get(0);
    this.maze14.playersNode.bottom = this.maze14.board.get(0).get(1);
    this.maze14.playersNode.hasBottom = true;

    t.checkExpect(maze14.playersNode.hasBottom, true);
    t.checkExpect(maze14.playersNode, this.maze14.board.get(0).get(0));
    t.checkExpect(maze14.playersNode.bottom, this.maze14.board.get(0).get(1));

    maze14.onKeyEvent("down");

    t.checkExpect(maze14.board.get(0).get(0).visited, true);
    t.checkExpect(maze14.board.get(0).get(0).color, Color.cyan);

    maze14.onKeyEvent("up");
    t.checkExpect(maze14.playersNode.visited, true);
    t.checkExpect(maze14.board.get(0).get(1).color, Color.cyan);

    maze14.onKeyEvent("down");
    maze14.onKeyEvent("right");
    t.checkExpect(maze14.board.get(0).get(1).visited, true);
    t.checkExpect(maze14.playersNode, maze14.board.get(1).get(1));

    maze14.onKeyEvent("left");
    t.checkExpect(maze14.board.get(1).get(1).visited, true);
    t.checkExpect(maze14.playersNode, maze14.board.get(0).get(1));

    maze15.onKeyEvent("b");
    t.checkExpect(maze15.bFirst, true);
    t.checkExpect(maze15.searchedNodes, maze15.search(maze15.board.get(0).get(0), 
        maze15.board.get(maze15.width - 1).get(maze15.height - 1), new Queue<Node>()));

    maze16.onKeyEvent("d");
    t.checkExpect(maze16.dFirst, true);
    t.checkExpect(maze16.searchedNodes, maze16.search(maze16.board.get(0).get(0), 
        maze16.board.get(maze16.width - 1).get(maze16.height - 1), new Stack<Node>()));


    this.maze16.gameEnd = true;
    t.checkExpect(maze16.gameEnd, true);

    maze16.onKeyEvent("r");
    t.checkExpect(maze16.dFirst, false);
    t.checkExpect(maze16.gameEnd, false);
    t.checkExpect(maze16.endNode, maze16.board.get(maze16.width - 1).get(maze16.height - 1));
    t.checkExpect(maze16.playersNode, maze16.board.get(0).get(0));








  }






  // test for onTick()
  void testOnTick(Tester t) {

    maze17.onKeyEvent("b");
    t.checkExpect(maze17.bFirst, true);
    t.checkExpect(maze17.searchedNodes.size(), 84);

    maze17.onTick();
    maze17.onKeyEvent("b");
    t.checkExpect(maze17.board.get(0).get(0).color, Color.cyan);
    t.checkExpect(maze17.playersNode, maze17.searchedNodes.get(0));



    maze18.onKeyEvent("d");
    t.checkExpect(maze18.dFirst, true);
    t.checkExpect(maze18.searchedNodes.size(), 50);

    maze18.onTick();
    maze18.onKeyEvent("b");
    t.checkExpect(maze18.board.get(0).get(0).color, Color.cyan);
    t.checkExpect(maze18.playersNode, maze18.searchedNodes.get(0));







  }

  // test for isEmpty()
  void testIsEmpty(Tester t) {
    t.checkExpect(this.coll1.isEmpty(), true);
    this.coll1.add(node1);
    t.checkExpect(this.coll1.isEmpty(), false);
    t.checkExpect(this.coll3.isEmpty(), true);
    this.coll3.add(node2);
    t.checkExpect(this.coll3.isEmpty(), false);

  }

  // test for add()
  void testAdd(Tester t) {
    t.checkExpect(this.coll2.isEmpty(), true);
    this.coll2.add(node1);
    this.coll2.add(node2);
    t.checkExpect(this.coll2.isEmpty(), false);
    t.checkExpect(this.coll2.remove(), this.node1);

    t.checkExpect(this.coll4.isEmpty(), true);
    this.coll4.add(node3);
    this.coll4.add(node4);
    t.checkExpect(this.coll4.isEmpty(), false);
    t.checkExpect(this.coll4.remove(), this.node4);
  }

  // test for remove()
  boolean testRemove(Tester t) {
    this.coll5.add(node1);
    this.coll5.add(node2);
    this.coll5.add(node3);

    this.coll6.add(node3);
    this.coll6.add(node2);
    this.coll6.add(node1);

    return t.checkExpect(this.coll5.remove(), node3)
        && t.checkExpect(this.coll6.remove(), node3);
  }

  // test for createInitWorkList()
  boolean testInitWorkList(Tester t) {

    this.maze8.worklist = new ArrayList<Edge>();
    this.maze8.createInitWorkList();
    this.maze9.worklist = new ArrayList<Edge>();
    this.maze9.createInitWorkList();

    return t.checkExpect(this.maze8.worklist.get(0).from, this.maze8.mst.get(0).from)
        && t.checkExpect(this.maze8.worklist.get(0).to, this.maze8.mst.get(1).from)
        && t.checkExpect(this.maze8.worklist.get(1).from, this.maze8.mst.get(0).from)
        && t.checkExpect(this.maze8.worklist.get(1).to, this.maze8.mst.get(2).from)
        && t.checkExpect(this.maze8.worklist.get(2).from, this.maze8.mst.get(1).from)
        && t.checkExpect(this.maze9.worklist.get(0).from, this.maze9.mst.get(0).from)
        && t.checkExpect(this.maze9.worklist.get(0).to, this.maze9.mst.get(4).from)
        && t.checkExpect(this.maze9.worklist.get(1).from, this.maze9.mst.get(0).from)
        && t.checkExpect(this.maze9.worklist.get(1).to, this.maze9.mst.get(1).from)
        && t.checkExpect(this.maze9.worklist.get(2).from, this.maze9.mst.get(4).from)
        && t.checkExpect(this.maze9.worklist.get(3).from, this.maze9.mst.get(4).from)
        && t.checkExpect(this.maze9.worklist.get(3).to, this.maze9.mst.get(2).from)
        && t.checkExpect(this.maze9.worklist.get(4).from, this.maze9.mst.get(4).to);




  }



  // test for union()
  void testUnion(Tester t) {

    t.checkExpect(maze5.reps.get(maze5.find(maze5.board.get(0).get(0).posn)), 
        new Posn(1, 0));
    this.maze5.union(maze5.board.get(0).get(0).posn, maze5.board.get(1).get(1).posn);
    t.checkExpect(maze5.reps.get(maze5.find(maze5.board.get(0).get(0).posn)), 
        new Posn(1, 0));
    t.checkExpect(maze5.find(maze5.board.get(0).get(0).posn), new Posn(1,0));
    this.maze5.union(maze5.board.get(0).get(0).posn, maze5.board.get(0).get(1).posn);
    t.checkExpect(maze5.find(maze5.board.get(0).get(0).posn), new Posn(1,0));



    t.checkExpect(maze6.reps.get(maze6.find(maze6.board.get(0).get(0).posn)), 
        new Posn(4, 3));
    this.maze6.union(maze6.board.get(0).get(0).posn, maze6.board.get(4).get(3).posn);
    t.checkExpect(maze6.reps.get(maze6.board.get(0).get(0).posn), new Posn(0, 1));

  }



  // test for find()
  boolean testFind(Tester t) {

    return t.checkExpect(this.maze7.reps.get(new Posn(0,1)), new Posn(0,2))
        && t.checkExpect(this.maze7.find(new Posn(0,2)), new Posn(2,0))
        && t.checkExpect(this.maze5.reps.get(new Posn(1,0)), new Posn(1,0))
        &&  t.checkExpect(this.maze5.find(new Posn(1,0)), new Posn(1,0))
        && t.checkExpect(this.maze7.reps.get(new Posn(4,4)), new Posn(2,4))
        &&  t.checkExpect(this.maze7.find(new Posn(4,4)), 
            this.maze7.find(maze7.reps.get(new Posn(4,4))));
  }

  // test for mapNodes()
  void testMapNodes(Tester t) {
    this.maze2x2.mapNodes(this.maze2x2.board);
    t.checkExpect(this.maze2x2.reps.get(new Posn(0,0)), new Posn(0,0));
    t.checkExpect(this.maze2x2.reps.get(new Posn(0,1)), new Posn(0,1));
    t.checkExpect(this.maze2x2.reps.get(new Posn(1,0)), new Posn(1,0));
    t.checkExpect(this.maze2x2.reps.get(new Posn(1,1)), new Posn(1,1));
  }

  // test for kruskalAlgo()
  boolean testKruskalAlgo(Tester t) {
    ArrayList<Edge> mst1 = new ArrayList<Edge>();
    this.maze2.kruskalAlgo();
    this.maze2.mst = mst1;
    Node node1 = new Node(0, 0);
    this.node1.hasBottom = true;
    Node node2 = new Node(0, 1);
    node2.hasTop = true;
    node1.bottom = node2;
    node2.top = node1;
    Edge edge1 = new Edge(node1, node2, -200);
    mst1.add(edge1);

    ArrayList<Edge> mst2 = new ArrayList<Edge>();
    this.maze10.kruskalAlgo();
    this.maze10.mst = mst2;
    Node node3 = new Node(30, 20);
    this.node1.hasBottom = true;
    Node node4 = new Node(20,30);
    node4.hasTop = true;
    node3.bottom = node4;
    node3.top = node3;
    Edge edge2 = new Edge(node3, node4, 1000);
    mst2.add(edge2);
    Node node5 = new Node(30, 20);
    this.node1.hasBottom = true;
    Node node6 = new Node(20,30);
    node6.hasTop = true;
    node5.bottom = node6;
    node5.top = node5;
    Edge edge3 = new Edge(node5, node6, 1000);
    mst2.add(edge3);


    ArrayList<Edge> mst0 = new ArrayList<Edge>();

    this.maze11.mst = mst0;
    this.maze11.kruskalAlgo();

    return t.checkExpect(this.maze2.mst, mst1)
        && t.checkExpect(this.maze10.mst, mst2)
        && t.checkExpect(this.maze11.mst, new ArrayList<Edge>());


  }





  // test for updateCells()
  void testUpdateCells(Tester t) {
    this.maze5.mst.get(0).from = new Node(1, 2);
    this.maze5.mst.get(0).to = new Node(5, 6);
    this.maze5.mst.get(0).from.top = this.maze5.mst.get(0).to;
    this.maze5.mst.get(0).from.hasTop = false;
    this.maze5.mst.get(0).to.hasBottom = false;



    t.checkExpect(this.maze5.mst.get(0).from.hasTop,false);
    t.checkExpect(this.maze5.mst.get(0).from.hasBottom, false);
    t.checkExpect(this.maze5.mst.get(0).from.hasLeft, false);
    t.checkExpect(this.maze5.mst.get(0).from.hasRight, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasTop, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasBottom, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasLeft, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasRight, false);

    this.maze5.updateCells();
    t.checkExpect(this.maze5.mst.get(0).from.hasTop,true);
    t.checkExpect(this.maze5.mst.get(0).from.hasBottom, false);
    t.checkExpect(this.maze5.mst.get(0).from.hasLeft, false);
    t.checkExpect(this.maze5.mst.get(0).from.hasRight, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasTop, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasBottom, true);
    t.checkExpect(this.maze5.mst.get(0).to.hasLeft, false);
    t.checkExpect(this.maze5.mst.get(0).to.hasRight, false);


    t.checkExpect(this.maze6.mst, this.maze6.mst);
    this.maze6.mst.get(0).from = null;
    t.checkExpect(this.maze6.mst, this.maze6.mst);
  }

  // test for updateOutEdges()
  void testUpdateOutEdges(Tester t) {
    this.maze19.mst = new ArrayList<Edge>(Arrays.asList(new Edge(node1, node2, 5)));
    t.checkExpect(node1.outEdges, new ArrayList<Edge>());
    t.checkExpect(node2.outEdges, new ArrayList<Edge>());
    maze19.updateOutEdges();
    t.checkExpect(node1.outEdges.get(0), new Edge(node1, node2, 5));
    t.checkExpect(node2.outEdges.get(0), new Edge(node1, node2, 5));
  }





  // test for constructBoard()
  void testConstructBoard(Tester t) {
    t.checkExpect(this.maze2x2.board.get(0).get(0).bottom, this.maze2x2.board.get(0).get(1));
    t.checkExpect(this.maze2x2.board.get(0).get(0).left, null);
    t.checkExpect(this.maze2x2.board.get(0).get(0).right, this.maze2x2.board.get(1).get(0));
    t.checkExpect(this.maze2x2.board.get(0).get(0).top, null);

    t.checkExpect(this.maze2x2.board.get(1).get(0).bottom, this.maze2x2.board.get(1).get(1));
    t.checkExpect(this.maze2x2.board.get(1).get(0).left, this.maze2x2.board.get(0).get(0));
    t.checkExpect(this.maze2x2.board.get(1).get(0).right, null);
    t.checkExpect(this.maze2x2.board.get(1).get(0).top, null);

    t.checkExpect(this.maze2x2.board.get(1).get(1).bottom, null);
    t.checkExpect(this.maze2x2.board.get(1).get(1).left, this.maze2x2.board.get(0).get(1));
    t.checkExpect(this.maze2x2.board.get(1).get(1).right, null);
    t.checkExpect(this.maze2x2.board.get(1).get(1).top, this.maze2x2.board.get(1).get(0));

    t.checkExpect(this.maze2x2.board.get(0).get(1).bottom, null);
    t.checkExpect(this.maze2x2.board.get(0).get(1).left, null);
    t.checkExpect(this.maze2x2.board.get(0).get(1).right, this.maze2x2.board.get(1).get(1));
    t.checkExpect(this.maze2x2.board.get(0).get(1).top, this.maze2x2.board.get(0).get(0));
  }

  // test for WeightComparator
  void testWeightComparator(Tester t) {
    t.checkExpect(new WeightComparator().compare(new Edge(this.maze2x2.board.get(0).get(0), 
        this.maze2x2.board.get(0).get(1), 10), 
        new Edge(this.maze2x2.board.get(0).get(0), this.maze2x2.board.get(1).get(0), 20)), -10);
    t.checkExpect(new WeightComparator().compare(new Edge(this.maze2x2.board.get(0).get(0), 
        this.maze2x2.board.get(1).get(0), 20), 
        new Edge(this.maze2x2.board.get(0).get(0), this.maze2x2.board.get(0).get(1), 10)), 10);
    t.checkExpect(new WeightComparator().compare(new Edge(this.maze2x2.board.get(1).get(1),
        this.maze2x2.board.get(0).get(1), 10), 
        new Edge(this.maze2x2.board.get(1).get(0), this.maze2x2.board.get(1).get(1), 2)), 8);
  }

  // test for makeScene()
  boolean testMakeScene(Tester t) {
    WorldScene world1 = new WorldScene(1000, 1000);
    world1.placeImageXY(this.maze4.board.get(0).get(0).drawCell(this.maze4.cellSize), 
        this.maze4.board.get(0).get(0).x * this.maze4.cellSize + (this.maze4.cellSize / 2), 
        this.maze4.board.get(0).get(0).y * this.maze4.cellSize
        + (this.maze4.cellSize / 2));

    return t.checkExpect(this.maze4.makeScene(), world1);


  }


  // test for drawCell()
  boolean testDrawCell(Tester t) {
    return t.checkExpect(this.node1.drawCell(0), 
        new AboveImage(new LineImage(new Posn(0, 0), Color.black), 
            new RectangleImage(0, 0, OutlineMode.SOLID, this.node1.color)))
        && t.checkExpect(this.node2.drawCell(10), 
            new BesideImage(new LineImage(new Posn(0, 10), Color.black), 
                new RectangleImage(10, 10, OutlineMode.SOLID, this.node2.color)))
        && t.checkExpect(this.node3.drawCell(5), 
            new AboveImage(new RectangleImage(5, 5, OutlineMode.SOLID, this.node3.color), 
                new LineImage(new Posn(5, 0), Color.black)))
        && t.checkExpect(this.node4.drawCell(2), 
            new BesideImage(new RectangleImage(2, 2, OutlineMode.SOLID, this.node4.color), 
                new LineImage(new Posn(0, 2), Color.black)));
  }




  //test for the game
  void testGame(Tester t) {
    maze1.bigBang(1000,1000, .05);
  }
}
