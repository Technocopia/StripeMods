package org.technocopia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

public class Main {

	public static void main(String[] args) throws StripeException {

		Stripe.apiKey = Keys.Secret;
		Map<String, Object> params = new HashMap<>();
		// params.put("email", "monroe.lauren@gmail.com");
		CustomerCollection customers = Customer.list(params);

		for (Customer customer : customers.autoPagingIterable()) {
			String custID = customer.getId();
			Map<String, Object> params1 = new HashMap<>();
			params1.put("customer", custID);
			SubscriptionCollection subscriptions = Subscription.list(params1);
			Iterable<Subscription> iterable = subscriptions.autoPagingIterable();
			List<Subscription> result = new ArrayList<Subscription>();
			iterable.forEach(result::add);
			if (result.size() > 0) {

				for (Subscription subs : result) {
					if (subs.getStatus().contentEquals("active")) {

						for (String newPrice : updateCustomer(customer, subs))
							if (newPrice != null)
								setPrice(customer, newPrice);
					} else {
						System.out.println(
								"No Active Subscriptions for " + customer.getEmail() + " " + customer.getDescription());
					}
				}
			}
		}
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
		if(prices.size()>0)
			subscription.cancel();
		return prices;

	}

	private static String makePrice(Customer customer, Price subscriptionItem) throws StripeException {
		int unitAmount = subscriptionItem.getUnitAmount().intValue();
		String from125 = "price_0J4zTMH0T8nvPnROxqHnL22E";
		String bayrental = "price_0J4o8rH0T8nvPnROT30iKMh9";
		String nights = "price_0J3n9RH0T8nvPnROsS2mSLhS";
		String week = "price_0J4nUtH0T8nvPnROhO8flTmM";
		String from90 = "price_0J4o0OH0T8nvPnROlkKFWany";
		String newPrice = "";
		String id = subscriptionItem.getId().toLowerCase();
		switch (unitAmount) {
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
		case 17300:
		case 10400:
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
		Subscription subscription2 = Subscription.create(params);
	}

}
