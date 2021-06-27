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
		Stripe.apiKey = Keys.Secret;
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
		
		MailManager.sendEmail("mad.hephaestus@gmail.com", "Ho Boy it worked!", "yep, all the things worked.");
		
		

//   
//		System.exit(0);

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
						|| id.contains("nights")) {
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
