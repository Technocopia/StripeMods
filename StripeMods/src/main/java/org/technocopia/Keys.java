package org.technocopia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Keys {

	
	private static final String Secret =	"sk_live_somethingsomething";

	public static String getSecret() {
		try {
			java.awt.Desktop.getDesktop().browse(new URL("https://dashboard.stripe.com/apikeys").toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Secret;
	}

}
