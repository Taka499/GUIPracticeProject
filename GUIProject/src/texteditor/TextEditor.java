package texteditor;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Window;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.nio.file.Paths;

public class TextEditor extends Application {
	Stage stage;
	BorderPane root = new BorderPane();
	TextArea fileTextArea = new TextArea();
	Label statusLabel = new Label();
	StringBuilder fileContent;
	FileUtilities fileUtilities = new FileUtilities();
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage)
	{
		this.stage = stage;
		setScreen();
		Scene scene = new Scene(root, 960, 540);
		stage.setTitle("Text Reader"); //has only reading functions
		stage.setScene(scene);
		stage.show();
	}
	
	public void setScreen()
	{
			//create menus
		Menu fileMenu = new Menu("File");
		Menu toolsMenu = new Menu("Tools");
		Menu helpMenu = new Menu("Help");
		
			// create menu items
		MenuItem openFileMenuItem = new MenuItem("Open File");
		MenuItem saveFileMenuItem = new MenuItem("Save File");
		MenuItem exitFileMenuItem = new MenuItem("Exit");
		MenuItem closeFileMenuItem = new MenuItem("Close File");
		MenuItem searchToolsMenuItem = new MenuItem("Search");
		MenuItem replaceToolsMenuItem = new MenuItem("Replace");
		MenuItem wordCountToolsMenuItem = new MenuItem("Word Count");
		MenuItem aboutHelpMenuItem = new MenuItem("About");
		
			// attach event handlers
		openFileMenuItem.setOnAction(new OpenFileHandler());
		saveFileMenuItem.setOnAction(new SaveFileHandler());
		searchToolsMenuItem.setOnAction(new SearchToolHandler());
		replaceToolsMenuItem.setOnAction(new ReplaceToolHandler());
		wordCountToolsMenuItem.setOnAction(new WordCountToolHandler());
		aboutHelpMenuItem.setOnAction(new AboutHandler());
		
		
		closeFileMenuItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				fileTextArea.clear();
				statusLabel.setText("");
				root.setCenter(null);
			}
		});
		
		exitFileMenuItem.setOnAction(actionEvent->Platform.exit());// lambda expression
		
			// set status bar width and color
		statusLabel.setPrefWidth(this.stage.getMaxWidth());
		statusLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
		
			// add menu items to menus and menus to menubar
		MenuBar menuBar = new MenuBar();
		fileMenu.getItems().addAll(openFileMenuItem, saveFileMenuItem, closeFileMenuItem, new SeparatorMenuItem(), exitFileMenuItem);
		toolsMenu.getItems().addAll(searchToolsMenuItem, replaceToolsMenuItem, new SeparatorMenuItem(), wordCountToolsMenuItem);
		helpMenu.getItems().addAll(aboutHelpMenuItem);
		menuBar.getMenus().addAll(fileMenu, toolsMenu, helpMenu);
		
			// set root children
		root.setTop(menuBar);
		root.setBottom(statusLabel);
	}// end setScreen()
	
	private class OpenFileHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle (ActionEvent event)
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file");
			String initialPath = Paths.get(".\\data").toAbsolutePath().normalize().toString();
			fileChooser.setInitialDirectory(new File(initialPath)); //relative path of data folder
			// if no setInitialDirectory(), will set to root folder of OS
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Text Files", "*.txt"),
					new ExtensionFilter("All Files", "*.*"));
			File file = null;
			if ((file = fileChooser.showOpenDialog(stage)) != null)
			{
				fileContent = fileUtilities.readFile(file.getAbsolutePath());
				fileTextArea.clear();
				fileTextArea.appendText(fileContent.toString());
				fileTextArea.setWrapText(true);
				fileTextArea.positionCaret(0);
				statusLabel.setText(file.getAbsolutePath());	
				root.setCenter(fileTextArea);
			}// end if
		}// end handle()
	}// end OpenFileHandler
	
	private class SaveFileHandler implements EventHandler<ActionEvent>
	{

		@Override
		public void handle(ActionEvent event)
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save file");
			String initialPath = Paths.get(".\\data").toAbsolutePath().normalize().toString();
			fileChooser.setInitialDirectory(new File(initialPath));
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Text Files", "*.txt"),
					new ExtensionFilter("All Files", "*.*"));
			File file = null;
			if ((file = fileChooser.showSaveDialog(stage)) != null)
			{
				if (fileUtilities.writeFile(file.getAbsolutePath(), new StringBuilder(fileTextArea.getText())))
				{
					statusLabel.setText(String.format("Saved as %s", file.getAbsoluteFile()));
				}
				else
				{
					statusLabel.setText(String.format("Failed to save as %s", file.getAbsoluteFile()));
				}
			}
		}
		
	}
	
	// then other Handler...
	private class SearchToolHandler implements EventHandler<ActionEvent>
	{
		TextField userTextField = new TextField();
		Text numSearched = new Text();
		String searchString;
		int[] indexList;
		int currentIndex;
		
		@Override
		public void handle(ActionEvent event)
		{
				// create a new stage(window pop-up)
			Stage searchStage = new Stage();
			searchStage.initOwner(stage);
				// you can continue to use the editor while a search window is opening.
			searchStage.initModality(Modality.NONE);
			
				// set GridPane and add it to Scene
			GridPane searchRoot = new GridPane();
			searchRoot.setAlignment(Pos.CENTER);
			searchRoot.setHgap(10);
			searchRoot.setVgap(10);
			Scene searchScene = new Scene(searchRoot, 350, 120);
				// create child nodes
			Button nextBtn = new Button("Search Next");
			Button prevBtn = new Button("Previous");
			Text searchText = new Text("Search for:");
			
				// disable the buttons when text field is empty
			//BooleanBinding nextBtnBB = user
			nextBtn.disableProperty().bind(new BooleanBinding()
			{
				{
					super.bind(userTextField.textProperty());
				}
				
				@Override
				protected boolean computeValue()
				{
					return userTextField.getText().isEmpty();
				}
			});
				// ToDo: disable the previous button when text field is empty or the next button wasn't used
			/*prevBtn.disableProperty().bind(new BooleanBinding()
			{
				{
					super.bind(userTextField.textProperty());
				}
				
				@Override
				protected boolean computeValue()
				{
					return currentIndex < 0 ? true:userTextField.getText().isEmpty();
				}
			});*/
				// set EventHandler
			nextBtn.setOnAction(new nextButtonHandler());
			prevBtn.setOnAction(new previousButtonHandler());
				// allow ENTER key to search
			userTextField.setOnKeyPressed(new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(KeyEvent e)
				{
					if (e.getCode().equals(KeyCode.ENTER))
					{
						nextBtn.fire();
					}
				}
			});
			
				// add nodes to GridPane
			searchRoot.add(searchText, 0, 0);
			searchRoot.add(userTextField, 1, 0, 2, 1);
			searchRoot.add(prevBtn, 3, 1);
			searchRoot.add(nextBtn, 3, 0);
			searchRoot.add(numSearched, 2, 1);
			searchRoot.setGridLinesVisible(true); // ToDo: set to fault after all the coding
			
				// button need to be able to select the searching string, and by clicking the next button
				// move to next searched string. 
			
			searchStage.setTitle("Search...");
			searchStage.setScene(searchScene);
			searchStage.show();
		}
		
		// handler to handle Next Search button
		private class nextButtonHandler implements EventHandler<ActionEvent>
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (!userTextField.getText().equals(searchString) || searchString.isEmpty())
				{
					currentIndex = -1;
					numSearched.setText("");
					searchString = userTextField.getText();
					indexList = fileUtilities.searchAll(fileContent, searchString);
					numSearched.setVisible(true);
				}
				currentIndex = (currentIndex+1)%indexList.length;
				fileTextArea.selectRange(indexList[currentIndex], 
						indexList[currentIndex]+searchString.length());
				userTextField.selectAll();
				numSearched.setText(String.format("%d found, showing %d/%d", 
						indexList.length, currentIndex+1, indexList.length));
			}
		}
		
		// handler to handle Previous button
		private class previousButtonHandler implements EventHandler<ActionEvent>
		{
			@Override
			public void handle(ActionEvent event)
			{
				currentIndex = currentIndex-1 < 0 ? 
						indexList.length+currentIndex-1:(currentIndex-1)%indexList.length;
				fileTextArea.selectRange(indexList[currentIndex], 
						indexList[currentIndex]+searchString.length());
				userTextField.selectAll();
				numSearched.setText(String.format("%d found, showing %d/%d", 
						indexList.length, currentIndex+1, indexList.length));
			}
		}
	}
	
	private class ReplaceToolHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent event)
		{
			
		}
	}
	
	private class WordCountToolHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent event)
		{
			
		}
	}
	
	private class AboutHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent event)
		{
			
		}
	}
}// end TextReader
