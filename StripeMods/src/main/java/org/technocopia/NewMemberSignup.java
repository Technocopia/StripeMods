package org.technocopia;

/**
 * Sample Skeleton for 'NewMemberSignup.fxml' Controller Class
 */

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.client.protocol.RequestClientConnControl;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
public class NewMemberSignup {
	
	private static NewMemberSignup oneoff=null;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;


    @FXML // fx:id="cardNumberLabel"
    private Label cardNumberLabel; // Value injected by FXMLLoader

    @FXML // fx:id="step2"
    private AnchorPane step2; // Value injected by FXMLLoader

    @FXML // fx:id="selectApplication"
    private ChoiceBox<String> selectApplication; // Value injected by FXMLLoader

    @FXML // fx:id="nameLabel"
    private Label nameLabel; // Value injected by FXMLLoader

    @FXML // fx:id="emailLabel"
    private Label emailLabel; // Value injected by FXMLLoader

    @FXML // fx:id="membershipTypeLabel"
    private Label membershipTypeLabel; // Value injected by FXMLLoader

    @FXML // fx:id="step3"
    private AnchorPane step3; // Value injected by FXMLLoader

    @FXML // fx:id="monthfield"
    private TextField monthfield; // Value injected by FXMLLoader

    @FXML // fx:id="yearfield"
    private TextField yearfield; // Value injected by FXMLLoader

    @FXML // fx:id="cardnumberfield"
    private TextField cardnumberfield; // Value injected by FXMLLoader

    @FXML // fx:id="namefield"
    private TextField namefield; // Value injected by FXMLLoader

    @FXML // fx:id="cvcField"
    private TextField cvcField; // Value injected by FXMLLoader

	private Stage primaryStage;
	
	public NewMemberSignup(){
		if(oneoff==null)
			oneoff=this;
		else
			throw new RuntimeException("Onle one allowed WTF");
	}

    @FXML
    void confirmCardInfo(ActionEvent event) {
    	
    	primaryStage.close();

    }

    @FXML
    void finishStep2(ActionEvent event) {

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
//        if( x3 != null ) throw new RuntimeException("fx:id=\"x3\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
//        if( x4 != null ) throw new RuntimeException("fx:id=\"x4\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( cardNumberLabel != null ) throw new RuntimeException("fx:id=\"cardNumberLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( step2 != null ) throw new RuntimeException("fx:id=\"step2\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( selectApplication != null ) throw new RuntimeException("fx:id=\"selectApplication\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( nameLabel != null ) throw new RuntimeException("fx:id=\"nameLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( emailLabel != null ) throw new RuntimeException("fx:id=\"emailLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( membershipTypeLabel != null ) throw new RuntimeException("fx:id=\"membershipTypeLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( step3 != null ) throw new RuntimeException("fx:id=\"step3\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( monthfield != null ) throw new RuntimeException("fx:id=\"monthfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( yearfield != null ) throw new RuntimeException("fx:id=\"yearfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( cardnumberfield != null ) throw new RuntimeException("fx:id=\"cardnumberfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( namefield != null ) throw new RuntimeException("fx:id=\"namefield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
        if( cvcField != null) throw new RuntimeException( "fx:id=\"cvcField\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");

    }

	public void setCardController(CardReaderCommand command, List<Long> availible, Stage primaryStage) {
		this.primaryStage = primaryStage;
		command.setGotCard(newNumber -> {
				for(Long a:availible)
					if(a==newNumber) {
						setKeycardNumber(newNumber);
						return;
					}
				
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Card not in Availible List");
					alert.setHeaderText("Card unavailable");
					alert.setContentText("This card was not in the list of \n"
							+ "availible cards in the Membership sheet"
							+ "\nUse it anyway?");
					alert.showAndWait().ifPresent((btnType) -> {
					  if (btnType == ButtonType.OK) {
						  setKeycardNumber(newNumber);
					  }
					});
		});
	}

	private void setKeycardNumber(long newNumber) {
		Platform.runLater(()->cardNumberLabel.setText("card # "+newNumber));
		Platform.runLater(()->step2.setDisable(false));
	}
}
