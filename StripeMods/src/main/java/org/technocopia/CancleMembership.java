/**
 * Sample Skeleton for 'CancelMembership.fxml' Controller Class
 */

package org.technocopia;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Plan;
import com.stripe.model.Price;
import com.stripe.model.Product;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CancleMembership {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="cardNumberLabel"
	private Label cardNumberLabel; // Value injected by FXMLLoader

	@FXML // fx:id="step2"
	private AnchorPane step2; // Value injected by FXMLLoader

	@FXML // fx:id="nameLabel"
	private Label nameLabel; // Value injected by FXMLLoader

	@FXML // fx:id="emailLabel"
	private Label emailLabel; // Value injected by FXMLLoader

	@FXML // fx:id="membershipTypeLabel"
	private Label membershipTypeLabel; // Value injected by FXMLLoader

	private Stage primaryStage;
	private long mumber = 0;

	private Alert a;

	@FXML
	void finishStep2(ActionEvent event) {
		Platform.runLater(() -> step2.setDisable(true));
		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.INFORMATION);
			a=alert;
			new Thread(() -> {
				try {
					DatabaseSheet.cancleMember(mumber,a);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Platform.runLater(() -> primaryStage.close());
				DatabaseSheet.runUpdate(a);
				Platform.runLater(() -> a.close());
			}).start();
	        alert.setTitle("This Opperation takes time");
	        alert.setHeaderText("");
	        alert.setContentText("Just chill out...");
	        alert.showAndWait();
		});
		new Thread(() -> {

		}).start();
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert cardNumberLabel != null
				: "fx:id=\"cardNumberLabel\" was not injected: check your FXML file 'CancelMembership.fxml'.";
		assert step2 != null : "fx:id=\"step2\" was not injected: check your FXML file 'CancelMembership.fxml'.";
		assert nameLabel != null
				: "fx:id=\"nameLabel\" was not injected: check your FXML file 'CancelMembership.fxml'.";
		assert emailLabel != null
				: "fx:id=\"emailLabel\" was not injected: check your FXML file 'CancelMembership.fxml'.";
		assert membershipTypeLabel != null
				: "fx:id=\"membershipTypeLabel\" was not injected: check your FXML file 'CancelMembership.fxml'.";

	}

	public void setCardController(CardReaderCommand command, Stage primaryStage) {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;
		command.setGotCard(newNumber -> {
			mumber = newNumber;
			try {
				final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
				String range2 = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
				String range = range2;
				Sheets service = new Sheets.Builder(HTTP_TRANSPORT, DatabaseSheet.JSON_FACTORY, DatabaseSheet.getCredentials(HTTP_TRANSPORT))
						.setApplicationName(DatabaseSheet.APPLICATION_NAME).build();
				ValueRange response;

				response = service.spreadsheets().values().get(spreadsheetId, range).execute();

				List<List<Object>> sourceData = response.getValues();

				for (List<Object> row : sourceData) {
					try {
						long idTest = Long.parseLong(row.get(4).toString());
						String email = row.get(0).toString();
						if (email.length() > 1 && idTest == newNumber) {
							Platform.runLater(() -> emailLabel.setText(row.get(1).toString()));
							Platform.runLater(() -> membershipTypeLabel.setText(row.get(3).toString()));
							Platform.runLater(() -> nameLabel.setText(row.get(0).toString()));
							Platform.runLater(() -> step2.setDisable(false));
							return;
						}

					} catch (Exception ex) {

					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}
}
