package org.technocopia;

/**
 * Sample Skeleton for 'MainUIWindow.fxml' Controller Class
 */

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
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

	private CardReaderCommand command = null;
	Alert a;

	@FXML
	void addKeycardToSubscription(ActionEvent event) {
		Platform.runLater(() -> controlpanel.setDisable(true));
		new Thread(() -> {

			launchNewMemberSignup(true);

		}).start();
	}

	@FXML
	void cancelMembership(ActionEvent event) {
		Platform.runLater(() -> controlpanel.setDisable(true));
		new Thread(() -> {

			launchCancleMembership();

		}).start();
	}

	@FXML
	void openScanCardsWidget(ActionEvent event) {
		Platform.runLater(() -> controlpanel.setDisable(true));

		launchCardScanWidget();

	}
    @FXML
    void sendDelinquentNotes(ActionEvent event) {
    	runWithPopup(() -> {

			DatabaseSheet.sendDelinquantPaymentNotifications();

			Platform.runLater(() -> a.close());
		});
    	
    	
    }
    
    private void runWithPopup(Runnable r) {
    	Platform.runLater(() -> controlpanel.setDisable(true));
    	Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			a = alert;
			new Thread(r).start();
			alert.setTitle("This Opperation takes time");
			alert.setHeaderText("");
			alert.setContentText("Just chill out...");
			alert.showAndWait();
		});
    }
    
	private void launchCardScanWidget() {
		URL in = ReadCards.class.getClassLoader().getResource("ReadCards.fxml");
		if (in == null)
			throw new RuntimeException("No FXML found!");

		javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
		ReadCards ui = new ReadCards();
		loader.setController(ui);
		loader.setClassLoader(ui.getClass().getClassLoader());
		try {
			root = loader.load();

			javafx.application.Platform.runLater(() -> {
				javafx.stage.Stage primaryStage = new javafx.stage.Stage();
				ui.setCardController(command,primaryStage);
				primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent t) {
						System.out.println("Closing Window of card scans");
						setCardController(command);
					}
				});
				javafx.scene.Scene scene = new javafx.scene.Scene(root);
				primaryStage.setScene(scene);
				primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
				primaryStage.setResizable(true);
				primaryStage.show();
			});

			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@FXML
	void runUpdateSpreadsheet(ActionEvent event) {
		
		runWithPopup(() -> {
			DatabaseSheet.runUpdate(a);
			Platform.runLater(() -> a.close());
		});
	

	}

	@FXML
	void signUpNewMember(ActionEvent event) {
		Platform.runLater(() -> controlpanel.setDisable(true));
		new Thread(() -> {

			launchNewMemberSignup(false);

		}).start();
	}

	private void launchCancleMembership() {
		URL in = ReadCards.class.getClassLoader().getResource("CancelMembership.fxml");
		if (in == null)
			throw new RuntimeException("No FXML found!");
		System.out.println("Loading XML " + in.toExternalForm());
		javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
//		loader.setController(new  NewMemberSignup());
//		loader.setClassLoader(NewMemberSignup.class.getClassLoader());
		try {
			root = loader.load();

			javafx.application.Platform.runLater(() -> {
				javafx.stage.Stage primaryStage = new javafx.stage.Stage();
				CancleMembership controller = loader.getController();
				controller.setCardController(command, primaryStage);
				primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent t) {
						System.out.println("Closing Window of  cancel membership");
						setCardController(command);
					}
				});
				javafx.scene.Scene scene = new javafx.scene.Scene(root);
				primaryStage.setScene(scene);
				primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
				primaryStage.setResizable(true);
				primaryStage.show();
			});

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void launchNewMemberSignup(boolean b) {
		List<Long> availible = DatabaseSheet.availibleKeyCards();
		URL in = ReadCards.class.getClassLoader().getResource("NewMemberSignup.fxml");
		if (in == null)
			throw new RuntimeException("No FXML found!");
		System.out.println("Loading XML " + in.toExternalForm());
		javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
//		loader.setController(new  NewMemberSignup());
//		loader.setClassLoader(NewMemberSignup.class.getClassLoader());
		try {
			root = loader.load();

			javafx.application.Platform.runLater(() -> {
				javafx.stage.Stage primaryStage = new javafx.stage.Stage();
				javafx.scene.Scene scene = new javafx.scene.Scene(root);
				NewMemberSignup controller = loader.getController();
				controller.setCardController(command, availible, primaryStage,scene);
				controller.setUpdateCardInfoMode(b);
				primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent t) {
						System.out.println("Closing Window of new member signup");
						setCardController(command);
					}
				});
				
				primaryStage.setScene(scene);
				primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
				primaryStage.setResizable(true);
				primaryStage.show();
			});

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert controlpanel != null
				: "fx:id=\"controlpanel\" was not injected: check your FXML file 'MainUIWindow.fxml'.";
		assert loggedInAdministrator != null
				: "fx:id=\"controlpanel\" was not injected: check your FXML file 'MainUIWindow.fxml'.";
	}

	public void setCardController(CardReaderCommand c) {

		this.command = c;
		command.setGotCard(new IOnCardRead() {
			@Override
			public void event(long newNumber) {
				if (DatabaseSheet.checkAdministrator(newNumber)) {
					Platform.runLater(() -> controlpanel.setDisable(false));
					Platform.runLater(() -> loggedInAdministrator.setText("Admin: " + DatabaseSheet.getCurrentAdmin()));
				} else
					Platform.runLater(() -> controlpanel.setDisable(true));

			}
		});
		//runUpdateSpreadsheet(null);
	}
}
