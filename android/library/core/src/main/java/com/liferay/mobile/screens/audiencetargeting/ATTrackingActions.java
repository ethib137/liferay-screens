package com.liferay.mobile.screens.audiencetargeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.liferay.mobile.screens.audiencetargeting.interactor.AudienceTargetingResult;
import com.liferay.mobile.screens.context.LiferayScreensContext;
import com.liferay.mobile.screens.context.LiferayServerContext;
import com.liferay.mobile.screens.context.SessionContext;
import com.liferay.mobile.screens.util.LiferayLocale;
import com.liferay.mobile.screens.util.LiferayLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Javier Gamarra
 */
public class ATTrackingActions {

	public static final String USER_AGENT = "Android AT";
	public static final String ANALYTICS_SERVLET = "/o/analytics-processor/track";

	public static final String INSTALLATION = "installation";
	public static final String SESSION = "session";
	public static final String VIEW = "view";
	public static final String FORM_VIEW = "formView";
	public static final String FORM_SUBMIT = "formSubmit";
	public static final String CLICK = "click";
	public static final String BUTTON_CLICK = "buttonClick";
	public static final String SESSION_ON_SCREEN = "sessionOnScreen";
	public static final String AT_ON_SCREEN = "atOnScreen";

//	public static final String FORM_INTERACTION = "formInteraction";

	public static void postInstallation(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("AT_INSTALLATION", Context.MODE_PRIVATE);
		String installed = "installed";
		if (!preferences.contains(installed)) {
			LiferayScreensContext.init(context);
			long consumerId = LiferayServerContext.getConsumerId();

			ATReferrer referrer = new ATReferrer(ATReferrer.CONSUMER, consumerId);
			ATReferrer[] referrers = new ATReferrer[]{referrer};

			post(INSTALLATION, "", 0, "New installation", referrers);

			preferences.edit().putBoolean(installed, true).apply();
		}
	}

	public static void postSession(Context context) {
		//FIXME to server context
		LiferayScreensContext.init(context);
		long consumerId = LiferayServerContext.getConsumerId();

		ATReferrer referrer = new ATReferrer(ATReferrer.CONSUMER, consumerId);
		ATReferrer[] referrers = new ATReferrer[]{referrer};

		post(SESSION, "", 0, "New Session", referrers);
	}

	public static void postTime(Context context, String event, long time) {
		//FIXME to server context
		LiferayScreensContext.init(context);
		long consumerId = LiferayServerContext.getConsumerId();

		ATReferrer referrer = new ATReferrer(ATReferrer.CONSUMER, consumerId);
		ATReferrer[] referrers = new ATReferrer[]{referrer};

		post(event, "", 0, String.valueOf(time), referrers);
	}

	public static void postWithATResult(Context context, String type, AudienceTargetingResult result, String elementId) {
		LiferayScreensContext.init(context);
		long consumerId = LiferayServerContext.getConsumerId();

		ATReferrer consumer = new ATReferrer(ATReferrer.CONSUMER, consumerId);
		ATReferrer segment = new ATReferrer(ATReferrer.USER_SEGMENT, result.getUserSegmentIds());
		ATReferrer placeholder = new ATReferrer(ATReferrer.PLACEHOLDER, Long.valueOf(result.getPlaceholderId()));
		ATReferrer campaign = new ATReferrer(ATReferrer.CAMPAIGN, result.getCampaignId());
		ATReferrer[] referrers = new ATReferrer[]{consumer, segment, placeholder, campaign,};

		post(type, result.getClassName(), result.getClassPK(), elementId, referrers);
	}

	public static void ddl(Context context, String formView, long recordId, String elementId) {
		LiferayScreensContext.init(context);
		long consumerId = LiferayServerContext.getConsumerId();

		ATReferrer consumer = new ATReferrer(ATReferrer.CONSUMER, consumerId);
//		ATReferrer segment = new ATReferrer(ATReferrer.USER_SEGMENT, 0);
//		ATReferrer placeholder = new ATReferrer(ATReferrer.PLACEHOLDER, 0);
//		ATReferrer campaign = new ATReferrer(ATReferrer.CAMPAIGN, 0);
		ATReferrer[] referrers = new ATReferrer[]{consumer,
//			segment, placeholder, campaign,
		};

		post(formView, "com.liferay.portlet.dynamicdatalists.model.DDLRecordSet", recordId, elementId, referrers);
	}

	public static void post(final String eventType, final String className, final long classPK, final String elementId, final ATReferrer[] referrers) {

		final Locale currentLocale = LiferayLocale.getDefaultLocale();
		final String localeLanguage = LiferayLocale.getSupportedLocale(currentLocale.getDisplayLanguage());
		final long companyId = LiferayServerContext.getCompanyId();
		final long groupId = LiferayServerContext.getGroupId();
		final long userId = SessionContext.getLoggedUser() == null ? 0 : SessionContext.getLoggedUser().getId();
		final String visitedWeb = "android";


		final String date = new SimpleDateFormat().format(Calendar.getInstance().getTime());

		Executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					launchRequest(className, classPK, elementId, referrers, groupId, eventType, date, companyId, localeLanguage, userId, visitedWeb);
				}
				catch (Exception e) {
					LiferayLogger.e("Error sending tracking action", e);
				}
			}
		});
	}

	private static void launchRequest(String className, long classPK, String elementId, ATReferrer[] referrers, long groupId, String eventType, String date, long companyId, String localeLanguage, long userId, String visitedWeb) throws JSONException, IOException {
		JSONArray events = new JSONArray();
		events.put(createATEvent(className, classPK, elementId, referrers, groupId, eventType, date));

		JSONObject themeDisplayData = createThemeDisplay(companyId, localeLanguage, userId, visitedWeb);

		String content = "events=" + events.toString() + "&context=" + themeDisplayData.toString();

		String url = LiferayServerContext.getServer() + ANALYTICS_SERVLET;
		int responseCode = sendContent(url, "POST", content);

		if (responseCode == HttpURLConnection.HTTP_OK) {
			//INFO sent through bus?
		}
		else {
			LiferayLogger.e("Error sending tracking action");
		}
	}

	private static int sendContent(String server, String method, String content) throws IOException {
		URL url = new URL(server);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestMethod(method);
		connection.setDoInput(true);

		OutputStreamWriter writer = new OutputStreamWriter(
			connection.getOutputStream());
		writer.write(content);
		writer.close();

		return connection.getResponseCode();
	}

	private static JSONObject createATEvent(
		String className, long classPK, String elementId, ATReferrer[] referrers,
		long groupId, String eventType, String date)
		throws JSONException {
		JSONObject event = new JSONObject();
		event.put("properties", createProperty(className, classPK, elementId, referrers, groupId));
		event.put("event", eventType);
		event.put("timestamp", date);
		return event;
	}

	@NonNull
	private static JSONObject createProperty(
		String className, long classPK, String elementId, ATReferrer[] referrers, long groupId)
		throws JSONException {
		JSONObject properties = new JSONObject();
		properties.put("className", className);
		properties.put("classPK", classPK);
		properties.put("elementId", elementId);
		properties.put("groupId", groupId);
		properties.put("referrers", createReferrers(referrers));
		return properties;
	}

	@NonNull
	private static JSONArray createReferrers(ATReferrer[] referrers) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (ATReferrer referrer : referrers) {
			jsonArray.put(createReferrer(referrer));
		}
		return jsonArray;
	}

	private static JSONObject createReferrer(ATReferrer referrer) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("referrerClassName", referrer.getClassName());
		jsonObject.put("referrerClassPKs", referrer.getFormattedClassPKs());
		return jsonObject;
	}

	@NonNull
	private static JSONObject createThemeDisplay(
		long companyId, String languageId, long userId, String layoutURL)
		throws JSONException {
		JSONObject themeDisplayData = new JSONObject();
		themeDisplayData.put("companyId", companyId);
		themeDisplayData.put("languageId", languageId);
		themeDisplayData.put("userId", userId);
		themeDisplayData.put("layoutURL", layoutURL);
		return themeDisplayData;
	}
}