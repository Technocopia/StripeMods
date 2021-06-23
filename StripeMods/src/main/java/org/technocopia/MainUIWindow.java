package org.technocopia;
/**
 * Sample Skeleton for 'MainUIWindow.fxml' Controller Class
 */

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class MainUIWindow {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    
    @FXML // fx:id="loggedInAdministrator"
    private Label loggedInAdministrator; // Value injected by FXMLLoader
    
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="controlpanel"
    private VBox controlpanel; // Value injected by FXMLLoader

	private CardReaderCommand command;

    @FXML
    void addKeycardToSubscription(ActionEvent event) {
    	Platform.runLater(()->controlpanel.setDisable(true));
    }

    @FXML
    void cancelMembership(ActionEvent event) {
    	Platform.runLater(()->controlpanel.setDisable(true));
    }

    @FXML
    void openScanCardsWidget(ActionEvent event) {
    	Platform.runLater(()->controlpanel.setDisable(true));
    	
		URL in = ReadCards.class.getClassLoader().getResource("ReadCards.fxml");
		if(in==null)
			throw new RuntimeException("No FXML found!");
		
		javafx.fxml.FXMLLoader loader =new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
		ReadCards ui = new ReadCards();
		loader.setController(ui);
		loader.setClassLoader(ui.getClass().getClassLoader());
		try {
			root = loader.load();
			
			javafx.application.Platform.runLater(() -> {
				javafx.stage.Stage primaryStage = new javafx.stage.Stage();
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				    @Override
				    public void handle(WindowEvent t) {
				    	setCardController( command);
				    }
				});
				javafx.scene.Scene scene = new javafx.scene.Scene(root);
				primaryStage.setScene(scene);
				primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
				primaryStage.setResizable(true);
				primaryStage.show();
			});
			
			ui.setCardController(command);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    }

    @FXML
    void runUpdateSpreadsheet(ActionEvent event) {
    	Platform.runLater(()->controlpanel.setDisable(true));
    }

    @FXML
    void signUpNewMember(ActionEvent event) {
    	Platform.runLater(()->controlpanel.setDisable(true));
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert controlpanel != null : "fx:id=\"controlpanel\" was not injected: check your FXML file 'MainUIWindow.fxml'.";
        assert loggedInAdministrator != null : "fx:id=\"controlpanel\" was not injected: check your FXML file 'MainUIWindow.fxml'.";
    }

	public void setCardController(CardReaderCommand c) {
		this.command = c;
		command.setGotCard(new IOnCardRead() {
			@Override
			public void event(long newNumber) {
				if(DatabaseSheet.checkAdministrator(newNumber)) {
					Platform.runLater(()->controlpanel.setDisable(false));
					Platform.runLater(()->loggedInAdministrator.setText("Admin: "+DatabaseSheet.getCurrentAdmin()));
				}else
					Platform.runLater(()->controlpanel.setDisable(true));

			}
		});
	}
}
