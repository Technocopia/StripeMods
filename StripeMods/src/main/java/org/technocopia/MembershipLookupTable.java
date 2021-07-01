package org.technocopia;

import java.util.HashMap;
import java.util.Map;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;

public class MembershipLookupTable {
	private static HashMap<String, Product> lookup = null;

	public static String toHumanReadableString(String priceID) {

		return getLookup().get(priceID).getName();
	}

	public static HashMap<String, Product> getLookup() {
		if (lookup == null)
			setLookup(new HashMap<String, Product>());
		return lookup;
	}

	public static void clearLookup() {
		if (lookup != null)
			lookup.clear();
		lookup = null;
	}

	private static void setLookup(HashMap<String, Product> lookup) {
		MembershipLookupTable.lookup = lookup;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("active", true);
			ProductCollection products = Product.list(param);
			Iterable<Product> productsList = products.autoPagingIterable();
			for (Product prod : productsList) {
				// System.out.println(prod);
				Map<String, Object> params2 = new HashMap<>();
				params2.put("product", prod.getId());

				Iterable<Price> prices = Price.list(params2).autoPagingIterable();
				for (Price p : prices) {
					lookup.put(p.getId(), prod);
					// System.out.println("Price ID "+p.getId()+" for product "+prod.getName());
				}
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
	}
}
