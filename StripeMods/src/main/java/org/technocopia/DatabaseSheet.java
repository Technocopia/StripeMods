package org.technocopia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseSheet {
	private static String currentAdmin="None";
	private static final String APPLICATION_NAME = "Technocopia Sign-up Keiosk";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException        If the credentials.json file cannot be found.
	 * @throws URISyntaxException
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
			throws IOException, URISyntaxException {
		// Load client secrets.
		InputStream in = Keys.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			java.awt.Desktop.getDesktop()
					.browse(new URL("https://developers.google.com/workspace/guides/create-credentials").toURI());
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static boolean checkAdministrator(long newNumber) {
		try {
			// Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U";
			String range = "Administrators!A2:C";
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				System.out.println("No data found.");
			} else {
				for (int i = 0; i < values.size(); i++) {
					List row = values.get(i);
					// Print columns A and E, which correspond to indices 0 and 4.
					try {
						long num = Long.parseLong( row.get(0).toString());
						String access = row.get(2).toString();
						String name = row.get(1).toString();
						
						if(newNumber==num && access.toLowerCase().contentEquals("yes")) {
							setCurrentAdmin(name);
							return true;
						}
					} catch (IndexOutOfBoundsException ex) {
						System.out.println("No data on line " + i);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle("User Not Authorized");
	        alert.setHeaderText("Check permissions");
	        alert.setContentText("Look at permissions in sheet...");
	        alert.showAndWait();
		});
		try {
			java.awt.Desktop.getDesktop()
			.browse(new URL("https://docs.google.com/spreadsheets/d/1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U/").toURI());
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentAdmin="None";
		return false;
	}

	public static String getCurrentAdmin() {
		return currentAdmin;
	}

	private static void setCurrentAdmin(String currentAdmin) {
		if(currentAdmin==null)
			throw new RuntimeException();
		DatabaseSheet.currentAdmin = currentAdmin;
	}

}
