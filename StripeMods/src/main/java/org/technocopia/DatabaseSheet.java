package org.technocopia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

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
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentIntentCollection;
import com.stripe.model.Plan;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.SubscriptionItem;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseSheet {
	private static String currentAdmin = "None";
	static final String APPLICATION_NAME = "Technocopia Sign-up Keiosk";
	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokensheets";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final String CREDENTIALS_FILE_PATH = "credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException        If the credentials.json file cannot be found.
	 * @throws URISyntaxException
	 */
	static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException, URISyntaxException {
		// Load client secrets.
		InputStream in = getCredentialsInputStream();
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		java.io.File dataDirectory = new java.io.File(TOKENS_DIRECTORY_PATH);
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(dataDirectory))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static InputStream getCredentialsInputStream()
			throws IOException, URISyntaxException, MalformedURLException, FileNotFoundException {
		InputStream in = Keys.class.getResourceAsStream("/"+CREDENTIALS_FILE_PATH);
		if (in == null) {
			File credsFile = new File(System.getProperty("user.home")+System.getProperty("file.separator")+CREDENTIALS_FILE_PATH);
			if(credsFile.exists()) {
				return new FileInputStream(credsFile); 
			}
			
			java.awt.Desktop.getDesktop()
					.browse(new URL("https://developers.google.com/workspace/guides/create-credentials").toURI());
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		return in;
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
						long num = Long.parseLong(row.get(0).toString());
						String access = row.get(2).toString();
						String name = row.get(1).toString();

						if (newNumber == num && access.toLowerCase().contentEquals("yes")) {
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

		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("User Not Authorized");
			alert.setHeaderText("Check permissions");
			alert.setContentText("Look at permissions in sheet...");
			alert.showAndWait();
		});
		try {
			java.awt.Desktop.getDesktop().browse(
					new URL("https://docs.google.com/spreadsheets/d/1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U/")
							.toURI());
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentAdmin = "None";
		return false;
	}

	public static String getCurrentAdmin() {
		return currentAdmin;
	}

	private static void setCurrentAdmin(String currentAdmin) {
		if (currentAdmin == null)
			throw new RuntimeException();
		DatabaseSheet.currentAdmin = currentAdmin;
	}

	public static List<List<Object>> memberSignupResponses()
			throws GeneralSecurityException, IOException, URISyntaxException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String spreadsheetId = "1uw4HVfUe_84FCFyCGBQeNqwv7I2fgep0OSJr1r80ZYM";
		String range = "Form Responses 1!B2:O";
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ValueRange response;

		response = service.spreadsheets().values().get(spreadsheetId, range).execute();

		return response.getValues();
	}

	public static List<Long> availibleKeyCards() {
		ArrayList<Long> list = new ArrayList<>();
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();
			for (List<Object> rows : sourceData) {
				try {
					long id = Long.parseLong(rows.get(4).toString());
					String email = rows.get(0).toString();
					if (email.length() < 1)
						list.add(id);
				} catch (Exception ex) {
//					System.out.print("\n");
//					for(Object o:rows) {
//						System.out.print(" "+o);
//					}
				} // no carn number on this row
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return list;
	}

	public static void runUpdate(Alert a) {
		List<List<Object>> values = new ArrayList<>();

		try {
			// Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:F";
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();

			Map<String, Object> params = new HashMap<>();
			CustomerCollection customers = Customer.list(params);
			List<List<Object>> inactives = new ArrayList<>();
			List<List<Object>> communityMembers = new ArrayList<>();
			List<List<Object>> nosubhascard = new ArrayList<>();
			List<List<Object>> familyMembers = new ArrayList<>();
			List<List<Object>> fulldayMembers = new ArrayList<>();
			List<List<Object>> nightsMembers = new ArrayList<>();
			HashMap<String,List<Object>> thisYearsFinances=new HashMap<>();
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			String sheetNameForFinances = "" + year;

			for (Customer customer : customers.autoPagingIterable()) {
				ArrayList<Object> line = new ArrayList<>();
				while (line.size() < 8)
					line.add("");
				if (!customer.getDelinquent()) {
					line.set(0, "      Paid");
				} else {
					line.set(0, "DELINQUENT this month");
				}

				String custID = customer.getId();
				line.set(1, custID);
				String email = customer.getEmail();
				line.set(2, email);
				if(thisYearsFinances.get(custID)==null) {
					ArrayList<Object> finances = new ArrayList<>();
					finances.add(custID);
					finances.add(email);
					for (int i = 0; i < 12*2; i++) {
						finances.add("=0");
					}
					thisYearsFinances.put(custID, finances);
				}

			
				Map<String, Object> paramspaymentintents = new HashMap<>();
				paramspaymentintents.put("customer", custID);
				paramspaymentintents.put("limit", 24*3);
				Iterable<PaymentIntent> paymentIntents = PaymentIntent.list(paramspaymentintents).autoPagingIterable();
				boolean hasPaymentsThisyear=false;
				for (PaymentIntent payment : paymentIntents) {
					Iterable<Charge> chargeList = payment.getCharges().autoPagingIterable();
					for (Charge singleCharge : chargeList) {
						if (singleCharge.getPaid()&& !singleCharge.getRefunded()) {
							long timestampOfPayment = singleCharge.getCreated();
							double amount = ((double) singleCharge.getAmount()) / 100.0;
							Calendar calOfCharge = Calendar.getInstance();
							calOfCharge.setTimeInMillis(timestampOfPayment * 1000);
							int yearOfCharge = calOfCharge.get(Calendar.YEAR);
							int monthOfCharge = calOfCharge.get(Calendar.MONTH);
							if (yearOfCharge == year ) {
								int index = (monthOfCharge) + 2;
								//System.out.println(singleCharge);
								//System.out.println(payment);
								if (yearOfCharge == year) {
									hasPaymentsThisyear=true;
									String string = thisYearsFinances.get(custID).get(index).toString();
									if(string.contentEquals("=0")) {
										string="=";
									}else {
										string=string+ "+";
									}
									thisYearsFinances.get(custID).set(index, string+ amount);
								} 
								System.out.println( email+" charged "+amount+" on "+(monthOfCharge+1)+"/"+yearOfCharge);
							}
						}
					}
				}

				for (List<Object> rows : sourceData) {
					try {
						String emailToTest = rows.get(1).toString();
						if (emailToTest.toLowerCase().contains(email.toLowerCase())) {
							line.set(3, rows.get(0).toString());
							line.set(4, rows.get(4).toString());
							break;
						}
					} catch (Exception ex) {
						// ex.printStackTrace();
					}
				}

				Map<String, Object> params1 = new HashMap<>();
				params1.put("customer", custID);
				SubscriptionCollection subscriptions = Subscription.list(params1);
				Iterable<Subscription> iterable = subscriptions.autoPagingIterable();
				List<Subscription> result = new ArrayList<Subscription>();
				for (Subscription sub : iterable)
					result.add(sub);
				if (result.size() > 0) {
					String membershipType = "";
					String space = "";
					for (Subscription subs : result) {
						// System.out.println(subs);
						List<SubscriptionItem> subdata = subs.getItems().getData();
						for (SubscriptionItem product : subdata) {
							// System.out.println(product);
//							Product p =Product.;
//							String id2 = p.getName();
							String id2 = MembershipLookupTable.toHumanReadableString(product.getPrice().getId());
							//String id = id2.toLowerCase();
							if (isMembership(id2)) {
								membershipType = id2;
							} else {
								if (space.length() > 0)
									space += "," + id2;
								else
									space = id2;
							}
						}

					}
					line.set(5, membershipType);
					line.set(6, space);
					if (membershipType.contains("Community"))
						communityMembers.add(line);
					else if (membershipType.contains("Family"))
						familyMembers.add(line);
					else if (membershipType.contains("24"))
						fulldayMembers.add(line);
					else if (membershipType.contains("Nights"))
						nightsMembers.add(line);
					else
						values.add(0, line);
					Platform.runLater(() -> a.setContentText("Updating " + email));
					
				} else {

					line.set(7, "No Subscriptions");
					line.set(0, "");
					if (line.get(4).toString().length() > 1) {
						nosubhascard.add(line);
					} else
						inactives.add(line);
				}
				System.out.println("Updateing sheets for "+ email);
				if(!hasPaymentsThisyear) {
					thisYearsFinances.remove(custID);
				}
			}
			for (List<Object> rows : nightsMembers)
				values.add(rows);
			for (List<Object> rows : fulldayMembers)
				values.add(rows);
			for (List<Object> rows : familyMembers)
				values.add(rows);
			for (List<Object> rows : communityMembers)
				values.add(rows);
			for (List<Object> rows : nosubhascard)
				values.add(rows);
			for (List<Object> rows : inactives)
				values.add(rows);
			clearAutogen("AUTOGEN");
			clearAutogen(sheetNameForFinances);
			writeDataToAutogen(values, HTTP_TRANSPORT);
			
			
			List<List<Object>> thisYearsFinancesList = new ArrayList<>();
			for(List<Object> row:thisYearsFinances.values()) {
				for(int i=0;i<row.size();i++) {
					if(row.get(i).toString().contentEquals("=0")) {
						row.set(i, "");
					}
				}
				thisYearsFinancesList.add(row);
			}

			writeDataToFInances(thisYearsFinancesList, HTTP_TRANSPORT, sheetNameForFinances);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static boolean isMembership(String subscriptionName) {
		subscriptionName=subscriptionName.toLowerCase();
		return (subscriptionName.contains("day") || subscriptionName.contains("24") || subscriptionName.contains("week") || subscriptionName.contains("nights")
				|| subscriptionName.contains("community") || subscriptionName.contains("member"));
	}

	private static void writeDataToFInances(List<List<Object>> values, final NetHttpTransport HTTP_TRANSPORT,
			String page) throws IOException, URISyntaxException {
		List<ValueRange> data = new ArrayList<>();

		data.add(new ValueRange().setRange(page + "!A3:Z").setValues(values));
		// Additional ranges to update ...
		Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
				.setData(data);
		serviceWrite.spreadsheets().values().batchUpdate("1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U", body)
				.execute();
	}

	private static void writeDataToAutogen(List<List<Object>> values, final NetHttpTransport HTTP_TRANSPORT)
			throws IOException, URISyntaxException {
		List<ValueRange> data = new ArrayList<>();

		data.add(new ValueRange().setRange("AUTOGEN!A3:H").setValues(values));
		// Additional ranges to update ...
		Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
				.setData(data);
		serviceWrite.spreadsheets().values().batchUpdate("1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U", body)
				.execute();
	}

	private static void clearAutogen(String sheet) throws GeneralSecurityException, IOException, URISyntaxException {
		// TODO Auto-generated method stub
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String spreadsheetId = "1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U";
		String range2 = sheet+"!A3:Z";
		String range = range2;
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ValueRange response;

		response = service.spreadsheets().values().get(spreadsheetId, range).execute();

		List<List<Object>> sourceData = response.getValues();
		if (sourceData == null)
			return;
		for (List<Object> row : sourceData) {
			for (int i = 0; i < row.size(); i++) {
				row.set(i, "");
			}
		}
		List<ValueRange> data = new ArrayList<>();

		data.add(new ValueRange().setRange(range2).setValues(sourceData));
		// Additional ranges to update ...
		Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
				.setData(data);
		serviceWrite.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();

	}

	public static void setNewMember(String name, String emailtoSet, String phonenumber, String humanReadableString,
			long newNumber) {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range2 = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
			String range = range2;
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();

			for (List<Object> row : sourceData) {
				ArrayList<Object> mrow = (ArrayList<Object>) row;
				try {
					long id = Long.parseLong(mrow.get(4).toString());
					String email = mrow.get(0).toString();
					if (email.length() < 1 && id == newNumber) {
						mrow.set(0, name);
						mrow.set(1, emailtoSet);
						mrow.set(2, phonenumber);
						mrow.set(3, humanReadableString);
					}
				} catch (Exception ex) {

				}
			}
			List<ValueRange> data = new ArrayList<>();

			data.add(new ValueRange().setRange(range2).setValues(sourceData));
			// Additional ranges to update ...
			Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
					.setData(data);
			serviceWrite.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static String getCustomerStringFromCardID(long ID) throws Exception {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String spreadsheetId = "1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U";
		String range2 = "AUTOGEN!A2:G";
		String range = range2;
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ValueRange response;

		response = service.spreadsheets().values().get(spreadsheetId, range).execute();

		List<List<Object>> sourceData = response.getValues();
		for (List<Object> row : sourceData) {
			try {
				long id = Long.parseLong(row.get(4).toString());
				if (id == ID)
					return row.get(1).toString();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static void cancleMember(long newNumber, Alert a) throws Exception {
		Platform.runLater(() -> a.setContentText("Get customer ID from card"));
		String customerID = getCustomerStringFromCardID(newNumber);
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range2 = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
			String range = range2;
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;
			Platform.runLater(() -> a.setContentText("Get membership data"));
			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();

			for (List<Object> row : sourceData) {
				try {
					long idTest = Long.parseLong(row.get(4).toString());
					if (idTest == newNumber) {
						String name = row.get(0).toString();
						String emailtoSet = row.get(1).toString();
						DatabaseSheet.sendCancledMemberNotifications(name, emailtoSet, idTest);

						row.set(0, "");
						row.set(1, "");
						row.set(2, "");
						row.set(3, "");
						Platform.runLater(() -> a.setContentText("Lookup subscriptions for customer"));
						Map<String, Object> params1 = new HashMap<>();
						params1.put("customer", customerID);
						SubscriptionCollection subscriptions = Subscription.list(params1);
						Iterable<Subscription> iterable = subscriptions.autoPagingIterable();
						for (Subscription subs : iterable) {
							// System.out.println(subs);
							List<SubscriptionItem> subdata = subs.getItems().getData();
							for (SubscriptionItem product : subdata) {
								String id2 = MembershipLookupTable.toHumanReadableString(product.getPrice().getId());
								String id = id2.toLowerCase();
								if (id.contains("day") || id.contains("24") || id.contains("week")
										|| id.contains("nights")) {
									System.out.println("CANCELING " + "");
									subs.cancel();
								}
							}

						}
					}

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			}
			Platform.runLater(() -> a.setContentText("Write updated spreadsheet"));
			List<ValueRange> data = new ArrayList<>();

			data.add(new ValueRange().setRange(range2).setValues(sourceData));
			// Additional ranges to update ...
			Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
					.setData(data);
			serviceWrite.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static String addKeyCard(long newNumber) {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range2 = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
			String range = range2;
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;
			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();

			for (List<Object> row : sourceData) {
				try {

					if (row.size() == 0) {
						row.add("");
						row.add("");
						row.add("");
						row.add("");
						row.add(newNumber);
						System.out.println("Adding new card " + newNumber);
						break;
					}
					long idTest = Long.parseLong(row.get(4).toString());
					if (idTest == newNumber) {
						// this card is in use already
						return row.get(0).toString();
					}

				} catch (Exception ex) {

				}
			}
			List<ValueRange> data = new ArrayList<>();

			data.add(new ValueRange().setRange(range2).setValues(sourceData));
			// Additional ranges to update ...
			Sheets serviceWrite = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED")
					.setData(data);
			serviceWrite.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void sendDelinquantPaymentNotifications() {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "10xDNHk0P70jmwuzEaLpYdMhQzwlEth2ruLZK7vpx7P4";
			String range = "Membership " + Calendar.getInstance().get(Calendar.YEAR) + "!A5:E";
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();

			CustomerCollection customers;

			customers = Customer.list(new HashMap<>());
			for (Customer customer : customers.autoPagingIterable()) {
				Map<String, Object> params1 = new HashMap<>();
				params1.put("customer", customer.getId());
				SubscriptionCollection subscriptions = Subscription.list(params1);
				Iterable<Subscription> iterable = subscriptions.autoPagingIterable();

				if (customer.getDelinquent() && StreamSupport.stream(iterable.spliterator(), false).count() > 0) {
					String email = customer.getEmail();
					long idnum = 0;
					String name = "";
					for (List<Object> rows : sourceData) {
						try {
							String emailToTest = rows.get(1).toString();
							if (emailToTest.toLowerCase().contains(email.toLowerCase())) {
								name = rows.get(0).toString();
								idnum = Long.parseLong(rows.get(4).toString());
								break;
							}
						} catch (Exception ex) {
							// ex.printStackTrace();
						}
					}
					sendMemberNotifications("DelinquantPaymentNotifications", name, email, idnum);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sendMemberNotifications("NewMemberNotifications",name,email,newNumber);
	}

	public static void sendNewMemberNotifications(String name, String email, long newNumber) {
		sendMemberNotifications("NewMemberNotifications", name, email, newNumber);
	}

	public static void sendCancledMemberNotifications(String name, String email, long newNumber) {
		sendMemberNotifications("CancleMembershipNotifications", name, email, newNumber);
	}

	public static void sendMemberNotifications(String tab, String name, String email, long newNumber) {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			final String spreadsheetId = "1j4QNlpi6piCcE8o0M7nwvmxUH1FtRjEW3OwE1rVob4U";
			String range2 = tab + "!A2:D";
			String range = range2;
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange response;

			response = service.spreadsheets().values().get(spreadsheetId, range).execute();

			List<List<Object>> sourceData = response.getValues();
			for (List<Object> row : sourceData) {
				String mail = row.get(0).toString();
				if (mail.contentEquals("member"))
					mail = email;
				String cc = row.get(1).toString();

				String subject = row.get(2).toString();
				String body = row.get(3).toString() + "\n" + name + "\t" + email + " KeyCard Number = " + newNumber;

				MailManager.sendEmail(mail, cc, subject, body);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
