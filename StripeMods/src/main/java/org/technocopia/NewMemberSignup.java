package org.technocopia;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Sample Skeleton for 'NewMemberSignup.fxml' Controller Class
 */

import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.StreamSupport;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.input.KeyEvent;

public class NewMemberSignup {

	@FXML
	private RadioButton selectCash;

	@FXML
	private TextField fieldOfCardNum;

	private String phonenumber;

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
	@FXML // fx:id="confirmbutton"
	private Button confirmbutton; // Value injected by FXMLLoader

	@FXML
	private RadioButton typeButton;

	@FXML
	private ToggleGroup swipeType;

	@FXML
	private RadioButton swipeButton;

	private Stage primaryStage;
	private List<List<Object>> responses;

	private long newNumberID;
	private Alert a;
	private boolean updateCardInfoMode = false;

	private Scene scene;

	private EventHandler<KeyEvent> eventHandler;

	private String swipeString = "";
	private boolean StartedParse = false;
	private boolean FinishedParse = false;

	private List<Long> availible;

	private boolean cashMode = false;

	@FXML
	void confirmCardInfo(ActionEvent event) throws StripeException {
		Platform.runLater(() -> step3.setDisable(true));
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			a = alert;
			new Thread(() -> {
				runMemberAdd();
			}).start();
			alert.setTitle("This Opperation takes time");
			alert.setHeaderText("");
			alert.setContentText("Just chill out...");
			alert.showAndWait();
		});

	}

	private void runMemberAdd() {
		try {
			String email = emailLabel.getText();
			String name = nameLabel.getText();
			String membership = membershipTypeLabel.getText();
			Customer customer;
			HashMap<String, Object> params2 = new HashMap<String, Object>();
			params2.put("email", email);
			Platform.runLater(() -> a.setContentText("Read current customers"));
			Iterable<Customer> customers = Customer.list(params2).autoPagingIterable();
			long size = StreamSupport.stream(customers.spliterator(), false).count();
			PaymentMethod paymentMethodObject = null;
			String paymentMethod = "";
			if (!cashMode) {
				String cardnumber = cardnumberfield.getText();
				int month = Integer.parseInt(monthfield.getText());
				int year = Integer.parseInt("20" + yearfield.getText());
				String cvc = cvcField.getText();

				
				
				if (size == 0 || updateCardInfoMode) {
					Platform.runLater(() -> a.setContentText("Make new Customer/Bank card"));
					paymentMethodObject = makePaymentMethod(cardnumber, month, year, cvc);
					paymentMethod = paymentMethodObject.getId();
				}
			}
			if (size != 0) {
				customer = StreamSupport.stream(customers.spliterator(), false).findFirst().get();
				if (updateCardInfoMode) {
					Map<String, Object> params = new HashMap<>();
					params.put("customer", customer.getId());
					if (!cashMode) 
						paymentMethodObject = paymentMethodObject.attach(params);

					Map<String, Object> params1 = new HashMap<>();
					Map<String, Object> invoicesettings = new HashMap<>();
					if (!cashMode) 
						invoicesettings.put("default_payment_method", paymentMethod);
					params1.put("invoice_settings", invoicesettings);
					customer = customer.update(params1);

				}
			} else {
				CustomerCreateParams params=null;
				if (!cashMode) {
					params = CustomerCreateParams.builder().setEmail(email)
						.setPaymentMethod(paymentMethod).setName(name)
						.setInvoiceSettings(CustomerCreateParams.InvoiceSettings.builder()
								.setDefaultPaymentMethod(paymentMethod).build())
						.build();
				}else {
					params = CustomerCreateParams.builder().setEmail(email)
							.setName(name)
							.setInvoiceSettings(CustomerCreateParams.InvoiceSettings.builder()
									.build())
							.build();
				}
				customer = Customer.create(params);
			}
			if (!updateCardInfoMode) {
				String price = "price_0J4zTMH0T8nvPnROxqHnL22E";

				if (membership.toLowerCase().contains("weekday")) {
					if (membership.toLowerCase().contains("family"))
						price = "price_0J4o06H0T8nvPnROqFWbFIGk";
					else
						price = "price_0J4nUtH0T8nvPnROhO8flTmM";
				}
				if (membership.toLowerCase().contains("nights")) {
					if (membership.toLowerCase().contains("family"))
						price = "price_0J4o0OH0T8nvPnROlkKFWany";
					else
						price = "price_0J3n9RH0T8nvPnROsS2mSLhS";
				}
				if (membership.toLowerCase().contains("all access")) {
					if (membership.toLowerCase().contains("family"))
						price = "price_0J4o1BH0T8nvPnROFyLG5l1m";
					else
						price = "price_0J4zTMH0T8nvPnROxqHnL22E";
				}
				if (membership.toLowerCase().contains("comunity")) {
					price = "price_0IF5K1H0T8nvPnROZnJi7JmB";
				}
				Platform.runLater(() -> a.setContentText("Set up subscription"));

				Main.setUpNewSubscription(customer, price);
				Platform.runLater(() -> a.setContentText("Add new member to Membership sheet"));
				DatabaseSheet.setNewMember(name, email, phonenumber, MembershipLookupTable.toHumanReadableString(price),
						newNumberID);
				Platform.runLater(() -> a.setContentText("Update old sheet"));
				// GroupsManager.getGroup("").addMember(email);
				DatabaseSheet.sendNewMemberNotifications(name, email, newNumberID);

				DatabaseSheet.runUpdate(a);
			}
			Platform.runLater(() -> a.close());
		} catch (StripeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Platform.runLater(() -> primaryStage.close());
	}

	// https://stripe.com/docs/api/payment_methods/create
	private PaymentMethod makePaymentMethod(String cardnumber, int month, int year, String cvc) throws StripeException {
		// https://stripe.com/docs/api/payment_methods/create
		Map<String, Object> card = new HashMap<>();
		card.put("number", cardnumber);

		card.put("exp_month", month);
		card.put("exp_year", year);
		card.put("cvc", cvc);

		Map<String, Object> params1 = new HashMap<>();
		params1.put("type", "card");
		params1.put("card", card);

		PaymentMethod paymentMethodObject = PaymentMethod.create(params1);
		return paymentMethodObject;
	}

	@FXML
	void finishStep2(ActionEvent event) {
		Platform.runLater(() -> step2.setDisable(true));
		Platform.runLater(() -> step3.setDisable(false));
		Platform.runLater(() -> cardnumberfield.requestFocus());
		cardnumberfield.setOnAction(ev -> {
			Platform.runLater(() -> monthfield.requestFocus());
		});
		monthfield.setOnAction(ev -> {
			Platform.runLater(() -> yearfield.requestFocus());
		});
		yearfield.setOnAction(ev -> {
			Platform.runLater(() -> cvcField.requestFocus());
		});
		cvcField.setOnAction(ev -> {
			Platform.runLater(() -> confirmbutton.requestFocus());
		});
		confirmbutton.setOnAction(ev -> {
			try {
				confirmCardInfo(ev);
			} catch (StripeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		selectSwipe(null);
	}

	@FXML
	void selectCashAction(ActionEvent event) {
		Platform.runLater(() -> cardnumberfield.setDisable(true));
		Platform.runLater(() -> monthfield.setDisable(true));
		Platform.runLater(() -> yearfield.setDisable(true));
		Platform.runLater(() -> namefield.setDisable(true));
		Platform.runLater(() -> cvcField.setDisable(true));
		cashMode = true;
	}

	@FXML
	void selectSwipe(ActionEvent event) {
		cashMode = false;
		Platform.runLater(() -> cardnumberfield.setDisable(true));
		Platform.runLater(() -> monthfield.setDisable(true));
		Platform.runLater(() -> yearfield.setDisable(true));
		Platform.runLater(() -> namefield.setDisable(true));
		Platform.runLater(() -> cvcField.setDisable(false));

		Platform.runLater(() -> swipeButton.requestFocus());
		swipeString = "";
		StartedParse = false;
		FinishedParse = false;
		eventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {

				// new Thread(() -> {
				String character = keyEvent.getCharacter();
				// System.out.print(character);
				if (character.contains("?") && FinishedParse) {
					System.out.println("Card read done");
					Platform.runLater(() -> cvcField.requestFocus());
					FinishedParse = false;
				}
				if (character.contains("?") && StartedParse) {
					System.out.println("Parse swipe ");
					String[] parts = swipeString.split("\\^");
					for (int i = 0; i < parts.length; i++) {
						System.out.println(parts[i]);
					}
					String num = parts[0];
					String[] nambits = parts[1].split("/");
					String fn = nambits[1];
					String ln = nambits[0];
					String Name = fn + " " + ln;
					String year = parts[2].substring(0, 2);
					String month = parts[2].substring(2, 4);
					Platform.runLater(() -> monthfield.setText(month));
					Platform.runLater(() -> yearfield.setText(year));
					Platform.runLater(() -> namefield.setText(Name));
					Platform.runLater(() -> cardnumberfield.setText(num));

					swipeString = "";
					StartedParse = false;
					FinishedParse = true;
				} else
					swipeString += character;
				if (swipeString.contentEquals("%B")) {
					System.out.println("Start");
					swipeString = "";
					StartedParse = true;
				}

				// }).start();

			}
		};
		scene.setOnKeyTyped(eventHandler);
	}

	@FXML
	void selectType(ActionEvent event) {
		cashMode = false;
		Platform.runLater(() -> cardnumberfield.requestFocus());
		Platform.runLater(() -> cardnumberfield.setDisable(false));
		Platform.runLater(() -> monthfield.setDisable(false));
		Platform.runLater(() -> yearfield.setDisable(false));
		Platform.runLater(() -> namefield.setDisable(false));
		Platform.runLater(() -> cvcField.setDisable(false));

		/// scene.removeEventHandler(null, eventHandler);;
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
//        if( x3 != null ) throw new RuntimeException("fx:id=\"x3\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
//        if( x4 != null ) throw new RuntimeException("fx:id=\"x4\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (cardNumberLabel == null)
			throw new RuntimeException(
					"fx:id=\"cardNumberLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (step2 == null)
			throw new RuntimeException(
					"fx:id=\"step2\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (selectApplication == null)
			throw new RuntimeException(
					"fx:id=\"selectApplication\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (nameLabel == null)
			throw new RuntimeException(
					"fx:id=\"nameLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (emailLabel == null)
			throw new RuntimeException(
					"fx:id=\"emailLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (membershipTypeLabel == null)
			throw new RuntimeException(
					"fx:id=\"membershipTypeLabel\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (step3 == null)
			throw new RuntimeException(
					"fx:id=\"step3\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (monthfield == null)
			throw new RuntimeException(
					"fx:id=\"monthfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (yearfield == null)
			throw new RuntimeException(
					"fx:id=\"yearfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (cardnumberfield == null)
			throw new RuntimeException(
					"fx:id=\"cardnumberfield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (namefield == null)
			throw new RuntimeException(
					"fx:id=\"namefield\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		if (cvcField == null)
			throw new RuntimeException(
					"fx:id=\"cvcField\" was not injected: check your FXML file 'NewMemberSignup.fxml'.");
		assert swipeType != null : "fx:id=\"swipeType\" was not injected: check your FXML file 'NewMemberSignup.fxml'.";
		assert swipeButton != null
				: "fx:id=\"swipeButton\" was not injected: check your FXML file 'NewMemberSignup.fxml'.";

	}

	public void setCardController(CardReaderCommand command, List<Long> availible, Stage primaryStage, Scene scene) {
		this.availible = availible;
		this.primaryStage = primaryStage;
		this.scene = scene;
		command.setGotCard(newNumber -> {
			OnNewCardEvent(newNumber);
		});
		if (!updateCardInfoMode) {
			try {
				responses = DatabaseSheet.memberSignupResponses();
				String first = null;
				for (int i = responses.size() - 1; i > responses.size() - 20; i--) {
					List<Object> row = responses.get(i);
					String string = row.get(0).toString();
					if (first == null)
						first = string;
					selectApplication.getItems().add(string);

				}
				// selectApplication.getSelectionModel().select(first);
				selectApplication.getSelectionModel().selectedIndexProperty()
						.addListener((observableValue, oldval, newval) -> {
							String selectedItem = selectApplication.getItems().get(newval.intValue());
							System.out.println("Selected " + selectedItem);
							for (List<Object> row : responses) {
								String string = row.get(0).toString();
								if (string.contentEquals(selectedItem)) {
									Platform.runLater(() -> emailLabel.setText(row.get(2).toString()));
									phonenumber = row.get(1).toString();
									Platform.runLater(() -> membershipTypeLabel.setText(row.get(7).toString()));
									Platform.runLater(() -> nameLabel.setText(string));
									Platform.runLater(() -> namefield.setText(string));
								}
							}
						});
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Platform.runLater(() -> selectApplication.setDisable(true));

		}
	}

	private void OnNewCardEvent(long newNumber) {
		if (updateCardInfoMode) {
			setKeycardNumber(newNumber, null);
			return;
		}
		for (Long a : availible)
			if (a == newNumber) {
				setKeycardNumber(newNumber, null);
				return;
			}
		Platform.runLater(() -> {
			Alert alert1 = new Alert(AlertType.CONFIRMATION);
			alert1.setTitle("Card not in Availible List");
			alert1.setHeaderText("Card unavailable");
			alert1.setContentText("This card was not in the list of \n" + "availible cards in the Membership sheet"
					+ "\nUse it anyway?");
			alert1.showAndWait().ifPresent((btnType1) -> {
				if (btnType1 == ButtonType.OK) {
					new Thread(() -> {
						setKeycardNumber(newNumber, DatabaseSheet.addKeyCard(newNumber));
					}).start();

				}
			});
		});
	}

	@FXML
	void onCardNum(ActionEvent event) {
		long id = Long.parseLong(fieldOfCardNum.getText());
		OnNewCardEvent(id);
	}

	private void setKeycardNumber(long newNumber, String string) {
		if (string == null) {
			this.newNumberID = newNumber;
			Platform.runLater(() -> cardNumberLabel.setText("card # " + newNumber));
			if (updateCardInfoMode) {

				String custID;
				try {
					custID = DatabaseSheet.getCustomerStringFromCardID(newNumber);
					if (custID == null) {
						Platform.runLater(() -> {
							Alert alert1 = new Alert(AlertType.CONFIRMATION);
							alert1.setTitle("UNKNOWN member");
							alert1.setHeaderText("Card unknown");
							alert1.setContentText("This card is not in use at this time");
							alert1.showAndWait();
						});
					}
					Customer cust = Customer.retrieve(custID);
					Platform.runLater(() -> emailLabel.setText(cust.getEmail()));
					Platform.runLater(() -> nameLabel.setText(cust.getName()));
					Platform.runLater(() -> namefield.setText(cust.getName()));
					Platform.runLater(() -> finishStep2(null));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else
				Platform.runLater(() -> step2.setDisable(false));

		} else {
			Platform.runLater(() -> {
				Alert alert1 = new Alert(AlertType.CONFIRMATION);
				alert1.setTitle("Card ALREADY USED");
				alert1.setHeaderText("Card unavailable");
				alert1.setContentText("This card belongs to " + string + "\nPick a different one");
				alert1.showAndWait();
			});
		}
	}

	public void setUpdateCardInfoMode(boolean updateCardInfoMode) {
		this.updateCardInfoMode = updateCardInfoMode;
	}
}
