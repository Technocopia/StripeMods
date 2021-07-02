package org.technocopia;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;


public class Main {
	public static boolean live =true;
	public static CardReaderCommand command=null;
	
	public static BankCardReader reader= null;
    
	public static void main(String[] args) throws Exception {
		URL in = Main.class.getClassLoader().getResource("MainUIWindow.fxml");
		if(in==null)
			throw new RuntimeException("No FXML found!");
		Stripe.apiKey = Keys.getSecret();
		reader= new BankCardReader();
		command=new CardReaderCommand();
		
		com.sun.javafx.application.PlatformImpl.startup(()->{});

		
		javafx.fxml.FXMLLoader loader =new javafx.fxml.FXMLLoader(in);
		javafx.scene.Parent root;
		MainUIWindow ui = new MainUIWindow();
		loader.setController(ui);
		loader.setClassLoader(ui.getClass().getClassLoader());
		root = loader.load();
		
		javafx.application.Platform.runLater(() -> {
			javafx.stage.Stage primaryStage = new javafx.stage.Stage();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			    @Override
			    public void handle(WindowEvent t) {
			    	command.disconnect();
			    	try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        Platform.exit();
			        System.exit(0);
			    }
			});
			javafx.scene.Scene scene = new javafx.scene.Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
		
		ui.setCardController(command);	
		DatabaseSheet.checkAdministrator(7142231);
	}



	public static void setUpNewSubscription(Customer customert, String newPrice) throws StripeException {
		Map<String, Object> params1 = new HashMap<>();
		params1.put("customer", customert.getId());
		SubscriptionCollection subscriptions = Subscription.list(params1);
		Iterable<Subscription> iterable = subscriptions.autoPagingIterable();
		for (Subscription subs : iterable) {
			//System.out.println(subs);
			List<SubscriptionItem> subdata = subs.getItems().getData();
			for (SubscriptionItem product : subdata) {
				String id2= MembershipLookupTable.toHumanReadableString(product.getPrice().getId());
				String id = id2.toLowerCase();
				if (id.contains("day") || id.contains("24") || id.contains("week")
						|| id.contains("nights")||id.contains("community")) {
					subs.cancel();
				} 
			}

		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		long timestampOfNextMonthOnTheFirst=cal.getTimeInMillis()/1000;
		
		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", newPrice);
		items.add(item1);
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customert.getId());
		params.put("items", items);
		params.put("billing_cycle_anchor", timestampOfNextMonthOnTheFirst);
		params.put("trial_end", timestampOfNextMonthOnTheFirst);
		if(live)Subscription.create(params);
	}

}
