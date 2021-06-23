package org.technocopia;

public class MembershipLookupTable {

	public static String toHumanReadableString(String id) {
		switch (id) {
		case "price_0J4zTMH0T8nvPnROxqHnL22E":
			return "Full Day 24/7 - New";
		case "price_0J51E4H0T8nvPnROG82jxqCh":
			return "O2 Cabinets New";
		case "price_0J4zC6H0T8nvPnROnk6SYxvC":
			return "shelf-new";
		case "price_0J4oPGH0T8nvPnROWScwvuKk":
			return "Flex Bay/ 100 sq ft - New";
		case "price_0J4oIDH0T8nvPnRO4rRRA8q3":
			return "Bay with Pillar - New";
		case "price_0J4o8rH0T8nvPnROT30iKMh9":
			return "Bay Rental - New";
		case "price_0J4o1BH0T8nvPnROFyLG5l1m":
			return "24/7 - Family";
		case "price_0J4o0OH0T8nvPnROlkKFWany":
			return "Nights - Family";
		case "price_0J4o06H0T8nvPnROqFWbFIGk":
			return "Weekday Family";
		case "price_0J4nUtH0T8nvPnROhO8flTmM":
			return "Membership - Weekdays - New";
		case "price_0J3n9RH0T8nvPnROsS2mSLhS":
			return "Membership Nights - New";
		}
		return "UNKNOWN_" + id;
	}
}
