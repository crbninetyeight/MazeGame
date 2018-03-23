// Maze Project
// by Alexandria Carlson and Christian Byrne
// 11/29/2017

import java.util.ArrayList;

/**
 * Created by chrx on 11/13/17.
 */
public class Maze {
    // cell types
    //  - CELL_WALL = an impassable wall
    //  - CELL_PATH = a path that has not yet been visited
    //  - CELL_VISITED = a path that has been visited once
    //  - CELL_BAD = a path that has been visited more than once
    public static final int CELL_WALL = 0;
    public static final int CELL_PATH = 1;
    public static final int CELL_VISITED = 2;
    public static final int CELL_BAD = 3;

    // a chronology of the actor's past positions, used when backtracing
    private ArrayList<ActorPosition> actorHistory;

    // the two-dimensional array of cells that makes up the maze
    private int[][] maze;

    // the width and height of the maze
    private int width;
    private int height;

    // the actor's position
    private int x = 2, y = 0;

    // the start point of the maze
    private int startX, startY;

    // the end point of the maze
    private int endX, endY;

    // whether the maze is completely traversed
    private boolean complete;

    // the direction that the actor is facing
    private Direction face = Direction.SOUTH;

    // the current algorithm that is being used to navigate the maze
    //  - is either DEFAULT or BACKTRACE
    //  -- DEFAULT is the normal navigation behaviour
    //  -- BACKTRACE goes backwards in the order that preceded the current position
    private AIMode aiMode = AIMode.DEFAULT;

    // constructs the maze
    public Maze(int[][] maze) {
        this.maze = maze;

        actorHistory = new ArrayList<>();

        width = maze[0].length;
        height = maze.length;

        findEndpoints();

        complete = false;
    }

    public void findEndpoints() {
        startX = -1;
        startY = -1;
        endX = -1;
        endY = -1;

        // starting top-left, going right
        for (int x = 0; x < getWidth() && startX < 0; x++) {
            if (getCell(x, 0) == Maze.CELL_PATH) {
                startX = x;
                startY = 0;
            }
        }

        // starting top-left, going down
        for (int y = 1; y < getHeight() && (startY >= 0) ? (endY < 0) : (startY < 0); y++) {
            if (startY < 0 && getCell(0, y) == Maze.CELL_PATH) {
                startX = 0;
                startY = y;
            } else if (getCell(0, y) == Maze.CELL_PATH) {
                endX = 0;
                endY = y;
            }
        }

        // starting bottom-left, going right
        for (int x = 1; x < getWidth() && (startX >= 0) ? (endX < 0) : (startX < 0); x++) {
            if (startY < 0 && getCell(x, getHeight()-1) == Maze.CELL_PATH) {
                startX = x;
                startY = getHeight() - 1;
            } else if (getCell(x, getHeight()-1) == Maze.CELL_PATH) {
                endX = x;
                endY = getHeight()-1;
            }
        }

        // starting top-right, going down
        for (int y = 1; y < getHeight() && (startY >= 0) ? (endY < 0) : (startY < 0); y++) {
            if (startY < 0 && getCell(getWidth()-1, y) == Maze.CELL_PATH) {
                startX = getWidth()-1;
                startY = y;
            } else if (getCell(getWidth()-1, y) == Maze.CELL_PATH) {
                endX = getWidth()-1;
                endY = y;
            }
        }
    }

    // displays the current state of the maze in the console output
    public void displayMaze() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (j == x && i == y) {
                    System.out.print("@");
                } else {
                    switch (maze[i][j]) {
                        case CELL_WALL:
                            System.out.print("#");
                            break;
                        case CELL_PATH:
                            System.out.print(" ");
                            break;
                        case CELL_VISITED:
                            System.out.print("~");
                            break;
                        case CELL_BAD:
                            System.out.print(" ");
                            break;
                    }
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    // makes the actor take a step in the direction that it is facing
    public void takeStep(boolean display) {
        if (!complete) {
            switch (aiMode) {
                // the actor is not in a dead-end, and is navigating normally
                case DEFAULT:
                    if (isDeadEnd()) {
                        flip();
                        aiMode = AIMode.RETRACE;
                        takeStep(false);
                        break;
                    } else {
                        if (checkRight() != CELL_PATH) {
                            while (checkFront() != CELL_PATH) {
                                turn();
                            }
                        } else turn();

                        move();
                    }

                    break;

                // the actor was caught in a dead-end, and is tracing its steps back until it finds an opening
                case RETRACE:
                    ActorPosition lastPosition = actorHistory.get(actorHistory.size() - 1);

                    if (checkRight() != CELL_PATH && checkFront() != CELL_PATH) {
                        if (lastPosition.getX() != x) {
                            if (lastPosition.getX() > x) face = Direction.EAST;
                            else face = Direction.WEST;
                        } else {
                            if (lastPosition.getY() > y) face = Direction.SOUTH;
                            else face = Direction.NORTH;
                        }

                        move();
                    } else {
                        aiMode = AIMode.DEFAULT;
                        takeStep(false);
                    }
            }

            if (x == endX && y == endY) complete = true;

            // displays maze ONLY if the display flag is checked
            if (display) displayMaze();
        }
    }

    // takes steps until the maze is finished, then displays the results
    public void findExit() {
        while (!complete) {
            takeStep(false);
        }

        displayMaze();
    }


    // checks if actor is in a dead end
    // returns true if it is
    public boolean isDeadEnd() {
        boolean end = false;
        int openings = 0;
        for (int i = 0; i < 4; i++) {
            if (checkFront() == 1) {
                openings++;
            }
            turn();
        }

        if (openings == 0)
            end = true;

        return end;
    }

    // checks the front side that the actor is facing
    public int checkFront() {
        int front = 0;
        try {
            switch (face) {
                case NORTH:
                    front = maze[y - 1][x];
                    break;
                case SOUTH:
                    front = maze[y + 1][x];
                    break;
                case EAST:
                    front = maze[y][x + 1];
                    break;
                case WEST:
                    front = maze[y][x - 1];
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException Ex) {
            return 0;
        }

        return front;
    }

    // checks the left side that the actor is facing
    public int checkLeft() {
        int left = 0;
        try {
            switch (face) {
                case NORTH:
                    left = maze[y][x - 1];
                    break;
                case SOUTH:
                    left = maze[y][x + 1];
                    break;
                case EAST:
                    left = maze[y - 1][x];
                    break;
                case WEST:
                    left = maze[y + 1][x];
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException Ex) {
            return 0;
        }

        return left;
    }

    // checks the right side that the actor is facing
    public int checkRight() {
        int right = 0;
        try {
            switch (face) {
                case NORTH:
                    right = maze[y][x + 1];
                    break;
                case SOUTH:
                    right = maze[y][x - 1];
                    break;
                case EAST:
                    right = maze[y + 1][x];
                    break;
                case WEST:
                    right = maze[y - 1][x];
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException Ex) {
            return 0;
        }

        return right;
    }

    // turns the actor in a clockwise fashion
    public void turn() {
        switch (face) {
            case NORTH:
                face = Direction.EAST;
                break;
            case SOUTH:
                face = Direction.WEST;
                break;
            case EAST:
                face = Direction.SOUTH;
                break;
            case WEST:
                face = Direction.NORTH;
                break;
        }
    }

    // turns the actor counter-clockwise
    public void turnCCW() {
        switch (face) {
            case NORTH:
                face = Direction.WEST;
                break;
            case SOUTH:
                face = Direction.EAST;
                break;
            case EAST:
                face = Direction.NORTH;
                break;
            case WEST:
                face = Direction.SOUTH;
                break;
        }
    }

    public void move() {
        // places trail for path and counts steps since last turn
        switch (aiMode) {
            case DEFAULT:
                maze[y][x] = 2;
                actorHistory.add(new ActorPosition(x, y));
                break;
            case RETRACE:
                maze[y][x] = 3;
                actorHistory.remove(actorHistory.size()-1);
                break;
        }

        // sets player position forward
        switch (face) {
            case NORTH:
                y--;
                break;
            case SOUTH:
                y++;
                break;
            case EAST:
                x++;
                break;
            case WEST:
                x--;
                break;
        }
    }

    // flips the actor position, or rotates by 180 degrees
    public void flip() {
        switch (face) {
            case NORTH:
                face = Direction.SOUTH;
                break;
            case SOUTH:
                face = Direction.NORTH;
                break;
            case EAST:
                face = Direction.WEST;
                break;
            case WEST:
                face = Direction.EAST;
                break;
        }
    }

    // returns if the maze is complete
    public boolean isComplete() { return complete; }

    // returns the direction that the actor is facing
    public Direction getFace() { return face; }

    // returns the width and height of the maze
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // returns a cell at a given point in the maze
    public int getCell(int x, int y) { return maze[y][x]; }

    // returns the actor's x and y coordinates
    public int getActorX() { return x; }
    public int getActorY() { return y; }

    // sets the actor's x and y coordinates
    public void setActorX(int x) { this.x = x; }
    public void setActorY(int y) { this.y = y; }

    // a class that contains the actor's position, used for storing position history
    public class ActorPosition {
        private int x, y;

        public ActorPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; };
        public int getY() { return y; };
    }
}
enum Direction {NORTH, SOUTH, EAST, WEST}

/*
    Two AI modes: default and retrace
 */
enum AIMode {DEFAULT, RETRACE}