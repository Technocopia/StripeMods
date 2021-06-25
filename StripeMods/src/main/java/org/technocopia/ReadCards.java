package org.technocopia;

import java.awt.event.ActionEvent;

/**
 * Sample Skeleton for 'ReadCards.fxml' Controller Class
 */

import java.net.URL;
import java.util.ArrayList;
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
	ArrayList<Long> nums = new ArrayList<Long>();

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert field != null : "fx:id=\"field\" was not injected: check your FXML file 'ReadCards.fxml'.";

	}

	@FXML
	void addallkeycards(ActionEvent event) {
		new Thread(() -> {

			for(Long num:nums) {
				DatabaseSheet.addKeyCard(num);
			}

		}).start();
		
	}

	public void setCardController(CardReaderCommand c) {
		this.command = c;
		command.setGotCard(new IOnCardRead() {
			@Override
			public void event(long newNumber) {
				nums.add(newNumber);
				Platform.runLater(() -> field.setText(field.getText() + newNumber + "\n"));

			}
		});
	}
}
