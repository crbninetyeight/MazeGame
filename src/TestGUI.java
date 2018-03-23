// Maze Project
// by Alexandria Carlson and Christian Byrne
// 11/29/2017

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestGUI extends Application {
    public final double SCENE_SCALE = 30.0;
    @Override
    public void start(Stage primaryStage) {
        // initial maze data
        Maze maze = new Maze(new int[][] {
                {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0},
                {0,0,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,1,1,1,0},
                {0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0},
                {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}});

        // the central pane that stores all elements of the window
        VBox vBox = new VBox(8);
        vBox.setAlignment(Pos.TOP_CENTER);

        // the pane that stores the control buttons
        HBox controlBox = new HBox(8);
        controlBox.setAlignment(Pos.TOP_CENTER);

        // the button that takes a step
        Button stepButton = new Button("Take a Step");

        // the button that shows the path
        Button showButton = new Button("Show Path");

        // the checkbox that toggles whether the algorithm should autocomplete or not
        Label autopilotLabel = new Label("Autopilot: ");
        CheckBox autopilotButton = new CheckBox();
        autopilotButton.setSelected(false);

        // adding all the UI elements to the control panel
        controlBox.getChildren().addAll(stepButton, showButton, autopilotLabel, autopilotButton);


        // the pane that controls the operations of the underlying maze
        MazePane mazePane = new MazePane(maze);
        mazePane.setPrefSize(maze.getWidth()*SCENE_SCALE, maze.getHeight()*SCENE_SCALE);

        // adding the maze and control panes to the central pane
        vBox.getChildren().addAll(mazePane, controlBox);

        // take a step when the step button is pressed
        stepButton.setOnAction(e -> {
            mazePane.takeStep();
        });

        // show the path to the end of the maze
        showButton.setOnAction(e -> {
            // turn off autopilot
            if (autopilotButton.isSelected()) {
                autopilotButton.setSelected(false);
            }

            mazePane.showPath();
        });

        // when the checkbox is toggled, have the maze complete the maze by itself
        autopilotButton.setOnAction(e -> {
            if (autopilotButton.isSelected()) {
                mazePane.setAutopilot(true);
            } else {
                mazePane.setAutopilot(false);
            }
        });

        // the scene of the window that contains all of the elements
        Scene scene = new Scene(vBox, maze.getWidth()*SCENE_SCALE, maze.getHeight()*SCENE_SCALE + 40.0);

        // sets the scene of the window, as well as its title, and shows itself
        primaryStage.setScene(scene);
        primaryStage.setTitle("PokeMaze");
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
