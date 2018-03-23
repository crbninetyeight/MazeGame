// Maze Project
// by Alexandria Carlson and Christian Byrne
// 11/29/2017

import javafx.animation.PathTransition;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.Random;

public class MazePane extends Pane {
    // bike music that is to be played
    private final Media bike = new Media(Paths.get("res/bikesong.m4a").toUri().toString());

    // victory music that is to be played
    private final Media victory = new Media(Paths.get("res/victorysong.m4a").toUri().toString());

    // the bike music player
    private final MediaPlayer bikePlayer = new MediaPlayer(bike);

    // the victory music player
    private final MediaPlayer victoryPlayer = new MediaPlayer(victory);

    // actor images depending on direction faced
    private final Image PRO_NORTH = new Image("pro_north.png");
    private final Image PRO_SOUTH = new Image("pro_south.png");
    private final Image PRO_EAST  = new Image("pro_east.png");
    private final Image PRO_WEST  = new Image("pro_west.png");

    // the image that is shown at the end of the maze
    private final Image POKEBALL = new Image("pokeball.png");

    // grass tiles that are chosen at random for the paths
    private final Image TILE_GRASS_A = new Image("grass_patch_a.png");
    private final Image TILE_GRASS_B = new Image("grass_patch_b.png");
    private final Image TILE_GRASS_C = new Image("grass_patch_c.png");

    // wall tiles
    private final Image TILE_WALL_TL = new Image("wall_topleft.png");
    private final Image TILE_WALL_TC = new Image("wall_topcenter.png");
    private final Image TILE_WALL_TR = new Image("wall_topright.png");
    private final Image TILE_WALL_ML = new Image("wall_middleleft.png");
    private final Image TILE_WALL_MC = new Image("wall_middlecenter.png");
    private final Image TILE_WALL_MR = new Image("wall_middleright.png");
    private final Image TILE_WALL_BL = new Image("wall_bottomleft.png");
    private final Image TILE_WALL_BC = new Image("wall_bottomcenter.png");
    private final Image TILE_WALL_BR = new Image("wall_bottomright.png");

    // the path used for animation
    private Line path;

    // the maze data
    private Maze maze;

    // flags that are used to describe whether the maze algorithm is on autopilot, is running, or is finished
    private boolean autopilot;
    private boolean isRunning;
    private boolean isComplete;

    // map of rectangles squares
    private Rectangle[][] visitedMap;

    // array of images that illustrate the maze data
    private ImageView[][] rectangles;
    private ImageView[][] walls;

    // the images used for both the actor and the end tile
    private ImageView actor;
    private ImageView pokeball;

    // a black rectangle that obscures the scene when the actor reaches the end point, and text that shows
    // up on top of it that congratulates the user.
    private Rectangle victoryScreen;
    private Text victoryText;

    // the start and end points
    private int startX, startY;
    private int endX, endY;

    // the position of the actor on the maze plane
    private SimpleFloatProperty actorX;
    private SimpleFloatProperty actorY;

    // the next position of the actor on the maze plane, used for calculating animation paths
    private SimpleFloatProperty actorNextX;
    private SimpleFloatProperty actorNextY;

    public MazePane(Maze maze) {
        super();

        // the maze is neither running or on autopilot
        autopilot = false;
        isRunning = false;

        bikePlayer.setAutoPlay(true);

        this.maze = maze;

        // construct the scene based off of maze data
        flush();
    }

    public void flush() {
        if (rectangles == null) {
            // set up the tiles so that they are all initialized
            rectangles = new ImageView[maze.getWidth()][maze.getHeight()];
            for (int x = 0; x < maze.getWidth(); x++) {
                for (int y = 0; y < maze.getHeight(); y++) {
                    rectangles[x][y] = new ImageView();
                    rectangles[x][y].fitWidthProperty().bind(widthProperty().divide(maze.getWidth()));
                    rectangles[x][y].fitHeightProperty().bind(heightProperty().divide(maze.getHeight()));
                    rectangles[x][y].xProperty().bind(rectangles[x][y].fitWidthProperty().multiply(x));
                    rectangles[x][y].yProperty().bind(rectangles[x][y].fitHeightProperty().multiply(y));

                    getChildren().add(rectangles[x][y]);
                }
            }
        }

        if (visitedMap == null) {
            // set up visited tiles so that they are initialized
            visitedMap = new Rectangle[maze.getWidth()][maze.getHeight()];
            for (int x = 0; x < maze.getWidth(); x++) {
                for (int y = 0; y < maze.getHeight(); y++) {
                    visitedMap[x][y] = new Rectangle();
                    visitedMap[x][y].widthProperty().bind(rectangles[0][0].fitWidthProperty().multiply(.25));
                    visitedMap[x][y].heightProperty().bind(rectangles[0][0].fitHeightProperty().multiply(.25));

                    // adjust the positions so that the squares are at the center of the tile
                    visitedMap[x][y].xProperty().bind(
                            rectangles[x][y].fitWidthProperty().multiply(x).add(rectangles[x][y].fitWidthProperty()
                                    .multiply(.40))
                    );

                    visitedMap[x][y].yProperty().bind(
                            rectangles[x][y].fitHeightProperty().multiply(y).add(rectangles[x][y].fitHeightProperty()
                                    .multiply(.40))
                    );

                    // the visited squares are going to be invisible in the beginning
                    visitedMap[x][y].setFill(Color.TRANSPARENT);
                    visitedMap[x][y].setStroke(Color.TRANSPARENT);

                    getChildren().add(visitedMap[x][y]);
                }
            }
        }

        // give each tile an image depending on what maze cell data it corresponds to
        Random rand = new Random(0);
        for (int x = 0; x < maze.getWidth(); x++) {
            for (int y = 0; y < maze.getHeight(); y++) {
                switch (maze.getCell(x, y)) {
                    case Maze.CELL_WALL:
                        rectangles[x][y].imageProperty().setValue(TILE_WALL_MC);
                        break;

                    case Maze.CELL_PATH:
                        // each grass tile is chosen in a pseudo-random manner (it's the same result for each run)
                        switch (rand.nextInt(3)) {
                            case 0:
                                rectangles[x][y].imageProperty().setValue(TILE_GRASS_A);
                                break;

                            case 1:
                                rectangles[x][y].imageProperty().setValue(TILE_GRASS_B);
                                break;

                            case 2:
                                rectangles[x][y].imageProperty().setValue(TILE_GRASS_C);
                                break;

                            default:
                                rectangles[x][y].imageProperty().setValue(TILE_WALL_BC);
                        }
                        break;
                }
            }
        }

        // find the start and end points

        startX = -1;
        startY = -1;
        endX = -1;
        endY = -1;

        // starting top-left, going right
        for (int x = 0; x < maze.getWidth() && startX < 0; x++) {
            if (maze.getCell(x, 0) == Maze.CELL_PATH) {
                startX = x;
                startY = 0;
            }
        }

        // starting top-left, going down
        for (int y = 1; y < maze.getHeight() && (startY >= 0) ? (endY < 0) : (startY < 0); y++) {
            if (startY < 0 && maze.getCell(0, y) == Maze.CELL_PATH) {
                startX = 0;
                startY = y;
            } else if (maze.getCell(0, y) == Maze.CELL_PATH) {
                endX = 0;
                endY = y;
            }
        }

        // starting bottom-left, going right
        for (int x = 1; x < maze.getWidth() && (startX >= 0) ? (endX < 0) : (startX < 0); x++) {
            if (startY < 0 && maze.getCell(x, maze.getHeight()-1) == Maze.CELL_PATH) {
                startX = x;
                startY = maze.getHeight() - 1;
            } else if (maze.getCell(x, maze.getHeight()-1) == Maze.CELL_PATH) {
                endX = x;
                endY = maze.getHeight()-1;
            }
        }

        // starting top-right, going down
        for (int y = 1; y < maze.getHeight() && (startY >= 0) ? (endY < 0) : (startY < 0); y++) {
            if (startY < 0 && maze.getCell(maze.getWidth()-1, y) == Maze.CELL_PATH) {
                startX = maze.getWidth()-1;
                startY = y;
            } else if (maze.getCell(maze.getWidth()-1, y) == Maze.CELL_PATH) {
                endX = maze.getWidth()-1;
                endY = y;
            }
        }

        // set the actor position to the starting point
        maze.setActorX(startX);
        maze.setActorY(startY);

        // make the actor adopt a tile property and image
        actor = new ImageView();
        actor.fitWidthProperty().bind(widthProperty().divide(maze.getWidth()));
        actor.fitHeightProperty().bind(heightProperty().divide(maze.getHeight()));
        actorX = new SimpleFloatProperty(maze.getActorX());
        actorY = new SimpleFloatProperty(maze.getActorY());

        actor.xProperty().bind(actor.fitWidthProperty().multiply(actorX));
        actor.yProperty().bind(actor.fitHeightProperty().multiply(actorY));

        actorNextX = new SimpleFloatProperty(maze.getActorX() + 1);
        actorNextY = new SimpleFloatProperty(maze.getActorY() + 1);

        actor.setImage(PRO_EAST);

        // do the same for the end tile
        pokeball = new ImageView();
        pokeball.fitWidthProperty().bind(widthProperty().divide(maze.getWidth()));
        pokeball.fitHeightProperty().bind(heightProperty().divide(maze.getHeight()));
        pokeball.xProperty().bind(pokeball.fitWidthProperty().multiply(endX));
        pokeball.yProperty().bind(pokeball.fitHeightProperty().multiply(endY));
        pokeball.setImage(POKEBALL);

        // set up the path so that it is ready to animate the actor
        path = new Line();
        path.setStroke(Color.TRANSPARENT);
        path.startXProperty().bind(actorX.multiply(actor.fitWidthProperty()).add(actor.fitWidthProperty().divide(2.0)));
        path.startYProperty().bind(actorY.multiply(actor.fitHeightProperty()).add(actor.fitHeightProperty().divide(2.0)));
        path.endXProperty().bind(actorNextX.multiply(actor.fitWidthProperty()).add(actor.fitWidthProperty().divide(2.0)));
        path.endYProperty().bind(actorNextY.multiply(actor.fitHeightProperty()).add(actor.fitHeightProperty().divide(2.0)));

        // prepare the victory rectangle, and make it transparent so that it is not visible
        victoryScreen = new Rectangle();
        victoryScreen.widthProperty().bind(widthProperty());
        victoryScreen.heightProperty().bind(heightProperty());
        victoryScreen.setFill(Color.TRANSPARENT);

        // do the same for the victory text
        victoryText = new Text();
        victoryText.xProperty().bind(widthProperty().divide(2.0).subtract(60.0));
        victoryText.yProperty().bind(heightProperty().divide(2.0));
        victoryText.setText("Congratulations, you got the pokeball!");
        victoryText.setFill(Color.TRANSPARENT);
        victoryText.setTextAlignment(TextAlignment.CENTER);

        getChildren().add(path);
        getChildren().add(pokeball);
        getChildren().add(actor);
        getChildren().add(victoryScreen);
        getChildren().add(victoryText);
    }

    // makes the maze class take a step and animate so that it looks smooth to the viewer
    public void takeStep() {
        // checks to see if the actor is at the end of the maze, if so notify the rest of the class that this is
        // the case.
        if (maze.getActorX() == endX && maze.getActorY() == endY) {
            isComplete = true;
        }

        // prevent further steps from occurring as the animation is in progress, as this causes visual oddities
        if (!isRunning && !isComplete) {
            int lastX = maze.getActorX();
            int lastY = maze.getActorY();

            // color the visitedMap so that it makes a trail
            visitedMap[lastX][lastY].setFill(Color.WHITE);
            visitedMap[lastX][lastY].setStroke(Color.WHITE);

            // take a step and display the result in the console
            maze.takeStep(true);

            // prepare this data so that the line has an endpoint for animation
            actorNextX.set(maze.getActorX());
            actorNextY.set(maze.getActorY());

            // creates a pathTransition that adds the line path and the actor so that it is ready for animating
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.millis(200));
            pathTransition.setPath(path);
            pathTransition.setNode(actor);
            pathTransition.setAutoReverse(true);
            pathTransition.setOrientation(PathTransition.OrientationType.NONE);
            pathTransition.setCycleCount(1);

            // set the image of the actor so that it appears to be facing the direction they are moving in.
            switch (maze.getFace()) {
                case NORTH:
                    actor.imageProperty().setValue(PRO_NORTH);
                    break;

                case SOUTH:
                    actor.imageProperty().setValue(PRO_SOUTH);
                    break;

                case EAST:
                    actor.imageProperty().setValue(PRO_EAST);
                    break;

                case WEST:
                    actor.imageProperty().setValue(PRO_WEST);
                    break;
            }

            // were the actor's x position property be bound to anything, unbind it and its y as to prevent visual oddities
            if (actor.xProperty().isBound()) {
                actor.xProperty().unbind();
                actor.yProperty().unbind();
            }

            // run the animation
            isRunning = true;
            pathTransition.play();

            // these statements control what the mazePane should do next once the animation is complete.
            // if the mazePane is on autopilot, have the procedure go on an indefinite loop until the autopilot
            // flag is unchecked.

            // otherwise, just run it once and wait for the procedure to get called again.
            if (autopilot) {
                pathTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (autopilot) {
                            actorX.setValue(maze.getActorX());
                            actorY.setValue(maze.getActorY());

                            isRunning = false;
                            takeStep();
                        } else {
                            actorX.setValue(maze.getActorX());
                            actorY.setValue(maze.getActorY());

                            isRunning = false;
                        }
                    }
                });
            } else {
                pathTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        actorX.setValue(maze.getActorX());
                        actorY.setValue(maze.getActorY());

                        isRunning = false;
                    }
                });
            }
        } else if (isComplete) {
            if (bikePlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                bikePlayer.stop();
            }

            if (victoryPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                victoryPlayer.play();
            }

            victoryScreen.setFill(Color.BLACK);
            victoryText.setFill(Color.WHITE);
        }
    }

    // shows the maze path without any animation
    public void showPath() {
        if (isRunning || autopilot) {
            isRunning = false;
            autopilot = false;
        }

        maze.findExit();

        actorX.setValue(maze.getActorX());
        actorY.setValue(maze.getActorY());

        // if the actor square is not bound to anything, bind it to the final actor coordinates
        if (!actor.xProperty().isBound()) {
            actor.xProperty().bind(actorX.subtract(actorNextX.subtract(2.0)).multiply(actor.fitWidthProperty()));
            actor.yProperty().bind(actorY.subtract(actorNextY).multiply(actor.fitHeightProperty()));
        }

        for (int i = 0; i < maze.getWidth(); i++) {
            for (int j = 0; j < maze.getHeight(); j++) {
                int a = maze.getCell(i, j);
                if (a == Maze.CELL_VISITED || a == Maze.CELL_BAD) {
                    visitedMap[i][j].setFill(Color.WHITE);
                    visitedMap[i][j].setStroke(Color.WHITE);
                }
            }
        }
    }

    // set maze data to work with
    public void setMaze(Maze maze) { this.maze = maze; }

    // set whether the mazePlane should run the takeStep procedure on its own.
    public void setAutopilot(boolean autopilot) { this.autopilot = autopilot; }
}
