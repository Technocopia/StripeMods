package org.technocopia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import javafx.fxml.FXMLLoader;


public class Main {
	public static boolean live =false;
	public static CardReaderCommand command;
	
    private static final String APPLICATION_NAME = "Technocopia Sign-up Keiosk";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     * @throws URISyntaxException 
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException, URISyntaxException {
        // Load client secrets.
        InputStream in = Keys.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
        	java.awt.Desktop.getDesktop().browse(new URL("https://developers.google.com/workspace/guides/create-credentials").toURI());
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    
	public static void main(String[] args) throws Exception {

		Stripe.apiKey = Keys.Secret;
		command=new CardReaderCommand();
		URL in = Main.class.getClassLoader().getResource("MainUIWindow.fxml");
		if(in==null)
			throw new RuntimeException("No FXML found!");
		
		javafx.fxml.FXMLLoader loader =new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
		MainUIWindow ui = new MainUIWindow();
		loader.setController(ui);
		loader.setClassLoader(ui.getClass().getClassLoader());
		root = loader.load();
		
		javafx.application.Platform.runLater(() -> {
			javafx.stage.Stage primaryStage = new javafx.stage.Stage();

			javafx.scene.Scene scene = new javafx.scene.Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
		
		ui.setCardController(command);
		
		
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        final String spreadsheetId = "1uw4HVfUe_84FCFyCGBQeNqwv7I2fgep0OSJr1r80ZYM";
//        String range = "Form Responses 1!B2:T";
//        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//        } else {
//            for (int i=0;i<values.size();i++) {
//            	List row =values.get(i);
//                // Print columns A and E, which correspond to indices 0 and 4.
//            	try {
//            		System.out.println("Row # "+i+" value= "+ row.get(0)+" "+row.get(2)+" "+row.get(7));
//            	}catch(IndexOutOfBoundsException ex) {
//            		System.out.println("No data on line "+ i);
//            	}
//            }
//        }
//   
//		System.exit(0);
//		Map<String, Object> params = new HashMap<>();
//		// params.put("email", "monroe.lauren@gmail.com");
//		CustomerCollection customers = Customer.list(params);
//
//		for (Customer customer : customers.autoPagingIterable()) {
//			String custID = customer.getId();
//			Map<String, Object> params1 = new HashMap<>();
//			params1.put("customer", custID);
//			SubscriptionCollection subscriptions = Subscription.list(params1);
//			Iterable<Subscription> iterable = subscriptions.autoPagingIterable();
//			List<Subscription> result = new ArrayList<Subscription>();
//			iterable.forEach(result::add);
//			if (result.size() > 0) {
//
//				for (Subscription subs : result) {
//					//if (subs.getStatus().contentEquals("active")) {
//
//						for (String newPrice : updateCustomer(customer, subs))
//							if (newPrice != null)
//								setPrice(customer, newPrice);
////					} else {
////						System.out.println(
////								"No Active Subscriptions for " + customer.getEmail() + " " + customer.getDescription());
////					}
//				}
//			}
//		}
	}

	private static ArrayList<String> updateCustomer(Customer customer, Subscription subscription)
			throws StripeException {

		List<SubscriptionItem> subdata = subscription.getItems().getData();
		ArrayList<String> prices = new ArrayList<>();

		for (SubscriptionItem product : subdata) {
			String makePrice = makePrice(customer, product.getPrice());
			if(makePrice!=null)
				prices.add(makePrice);

		}
		if(prices.size()>0) {
			if(live)subscription.cancel();
			System.out.println("Canceling "+subscription.getId() +" for "+customer.getEmail() + " " + customer.getDescription());
		}
		return prices;

	}

	private static String makePrice(Customer customer, Price subscriptionItem) throws StripeException {
		int unitAmount = subscriptionItem.getUnitAmount().intValue();
		String from125 = "price_0J4zTMH0T8nvPnROxqHnL22E";
		String bayrental = "price_0J4o8rH0T8nvPnROT30iKMh9";
		String nights = "price_0J3n9RH0T8nvPnROsS2mSLhS";
		String week = "price_0J4nUtH0T8nvPnROhO8flTmM";
		String from90 = "price_0J4o0OH0T8nvPnROlkKFWany";
		String newself ="price_0J4zC6H0T8nvPnROnk6SYxvC";
		String O2cabnet="price_0J51E4H0T8nvPnROG82jxqCh";
		String newPrice = "";
		String id = subscriptionItem.getId().toLowerCase();
		switch (unitAmount) {
		case 4700:
			newPrice=O2cabnet;
			break;
		case 15000:
			newPrice = "price_0J4o1BH0T8nvPnROFyLG5l1m";
			break;
		case 2500:
			newPrice = newself;
			break;
		case 12500:
			newPrice = from125;
			break;
		case 9000:
			newPrice = from90;
			break;
		case 7500:
			if (id.contains("bay") || id.contains("cube"))
				newPrice = bayrental;
			else if (id.contains("night"))
				newPrice = nights;
			else if (id.contains("day"))
				newPrice = week;
			else 
				newPrice = week;
			break;
		case 100:
			// community member
			return null;
		case 8600:
		case 14400:
		case 10400:
		case 17300:
		case 2900:
		case 5400:
			// already updated
			System.out.println("Already Updated " + customer.getEmail() + " " + customer.getDescription());
			return null;
		default:
			System.out.println("UNKNOWN product!! "+customer.getEmail() + " " + customer.getDescription());
			System.out.println("\t\t" +
			// sub.getEmail()+" "+sub.getName()+" "+sub.getDescription()+"\t\t PLAN: "+
					id + " " + unitAmount);
			return null;
		}
		
		System.out.println("UPDATING ");
		System.out.println(customer.getEmail() + " " + customer.getDescription()+" new price: "+newPrice);
		Price newp = Price.retrieve(newPrice);
		System.out.println("\t\t" +
		// sub.getEmail()+" "+sub.getName()+" "+sub.getDescription()+"\t\t PLAN: "+
				id + " " + unitAmount + " to new price " + newp.getUnitAmount().intValue());
		// DANGER ZONE
		
		return newPrice;
	}

	private static void setPrice(Customer customert, String newPrice) throws StripeException {
		int timestampJuly1_2021 = 1625115600;

		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", newPrice);
		items.add(item1);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customert.getId());
		params.put("items", items);
		params.put("billing_cycle_anchor", timestampJuly1_2021);
		params.put("trial_end", timestampJuly1_2021);
		if(live)Subscription.create(params);
	}

}
