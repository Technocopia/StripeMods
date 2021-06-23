package org.technocopia;
/**
 * Sample Skeleton for 'MainUIWindow.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class MainUIWindow {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="controlpanel"
    private VBox controlpanel; // Value injected by FXMLLoader

	private CardReaderCommand command;

    @FXML
    void addKeycardToSubscription(ActionEvent event) {

    }

    @FXML
    void cancelMembership(ActionEvent event) {

    }

    @FXML
    void openScanCardsWidget(ActionEvent event) {

    }

    @FXML
    void runUpdateSpreadsheet(ActionEvent event) {

    }

    @FXML
    void signUpNewMember(ActionEvent event) {

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert controlpanel != null : "fx:id=\"controlpanel\" was not injected: check your FXML file 'MainUIWindow.fxml'.";

    }

	public void setCardController(CardReaderCommand c) {
		this.command = c;
		command.setGotCard(new IOnCardRead() {
			@Override
			public void event(long newNumber) {
				if(DatabaseSheet.checkAdministrator(newNumber)) {
					Platform.runLater(()->controlpanel.setDisable(false));
				}else
					Platform.runLater(()->controlpanel.setDisable(true));

			}
		});
	}
}
