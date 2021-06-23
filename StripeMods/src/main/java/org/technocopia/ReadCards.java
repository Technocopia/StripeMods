package org.technocopia;

/**
 * Sample Skeleton for 'ReadCards.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ReadCards {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="field"
    private TextArea field; // Value injected by FXMLLoader

	private CardReaderCommand command;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert field != null : "fx:id=\"field\" was not injected: check your FXML file 'ReadCards.fxml'.";

    }
    
	public void setCardController(CardReaderCommand c) {
		this.command = c;
		command.setGotCard(new IOnCardRead() {
			@Override
			public void event(long newNumber) {
		
					Platform.runLater(()->field.setText(field.getText()+"\n"+newNumber));

			}
		});
	}
}
