package com.knurld.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.knurld.dropbox.api.UploadAPI;
import com.knurld.vad.WordDetection;
import com.knurld.vad.WordInterval;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Record;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

@Path("/twilio")
public class Twilio {
	// incomingCall

	public static HashMap<String, String> urls = new HashMap<String, String>();
	public static HashMap<String, String> callSidMap = new HashMap<String, String>();

	@POST
	@Path("/incomingCall")
	@Produces(MediaType.APPLICATION_XML)
	public Response incomingCall(@FormParam("Caller") String caller) {
		// Create a TwiML response and add our friendly message.
		TwiMLResponse twiml = new TwiMLResponse();

		System.out.println("Getting incoming call from :" + caller);

		Say say = new Say("Hi There, you calling Knurld!");

		// Play an MP3 for incoming callers.
		// Play play = new
		// Play("http://demo.twilio.com/hellomonkey/monkey.mp3");

		Gather gather = new Gather();
		gather.setAction("https://knurldupload.herokuapp.com/rest/twilio/handlekey");
		gather.setNumDigits(4);
		gather.setMethod("POST");
		Say sayInGather = new Say("To use api please enter key fron the screen");
		try {
			gather.append(sayInGather);
			twiml.append(say);
			// twiml.append(play);
			twiml.append(gather);
		} catch (TwiMLException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(twiml.toXML()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@POST
	@Path("/handlekey")
	@Produces(MediaType.APPLICATION_XML)
	public Response handlekey(@FormParam("Digits") String digits, @FormParam("CallSid") String callId,
			@FormParam("Caller") String caller) {
		TwiMLResponse twiml = new TwiMLResponse();
		System.out.println("digits:" + digits);
		System.out.println("Keys for urls:" + urls.keySet());

		digits = digits.toString();

		if (urls.containsKey(digits)) {
			Say pleaseLeaveMessage = new Say(
					"Record your voice as words on screen after the tone. After finish please hangup or presss hash or pound key");
			// Record the caller's voice.
			callSidMap.put(callId, digits);
			Record record = new Record();
			record.set("trim", "do-not-trim");
			record.setMaxLength(30);
			record.setFinishOnKey("#");
			// You may need to change this to point to the location of your
			// servlet
			record.setAction("https://knurldupload.herokuapp.com/rest/twilio/handleRecording");
			try {
				twiml.append(pleaseLeaveMessage);
				twiml.append(record);
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Rejecting incoming call from :" + caller);
			Say say = new Say("Please register as Knurld developer");
			try {
				// twiml.append(dial);
				twiml.append(say);
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		}

		return Response.status(200).entity(twiml.toXML()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@POST
	@Path("/handleRecording")
	@Produces(MediaType.APPLICATION_XML)
	public Response handleRecording(@FormParam("RecordingUrl") String recordingUrl,
			@FormParam("CallSid") String callId) {
		System.out.println("RecordingUrl:" + recordingUrl);
		urls.put(callSidMap.get(callId), recordingUrl);
		TwiMLResponse twiml = new TwiMLResponse();
		if (recordingUrl != null) {
			try {
				twiml.append(new Say("Thanks for using Knurld.."));
				// twiml.append(new Play(recordingUrl));
				twiml.append(new Say("Have a nice day!"));
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		}
		return Response.status(200).entity(twiml.toXML()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	public static File downloadFile(String url) {
		try {
			File file = new File(UUID.randomUUID().toString() + ".wav");
			FileUtils.copyURLToFile(new URL(url), file);
			return file;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/getKey/{key}/{repeats}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKey(@PathParam("key") String key, @PathParam("repeats") String repeats) {
		JSONObject success = new JSONObject();
		String url = urls.get(key);
		if (urls.get(key) != "") {
			File file = downloadFile(urls.get(key));
			String fileName = file.getName();
			if (fileName == null || fileName.isEmpty()) {
				fileName = UUID.randomUUID().toString() + ".wav";

			}
			try {
				url = makeDownladableLink(UploadAPI.uploadFile(new FileInputStream(file), fileName));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("File path is :" + file.getPath());
			List<WordInterval> words = WordDetection.detectWordsAutoSensitivity(file.getPath(),
					Integer.parseInt(repeats));
			JSONArray array = new JSONArray();
			for (WordInterval wordInterval : words) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("start", wordInterval.getStartTime());
				jsonObject.put("stop", wordInterval.getStopTime());
				array.add(jsonObject);
			}
			success.put("intervals", array);
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		success.put("url", url);

		return Response.status(200).entity(success.toJSONString()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	public static String makeDownladableLink(String str) {
		if (str.charAt(str.length() - 1) == '0') {
			str = str.substring(0, str.length() - 1) + "1";
			return str;
		} else {
			return str;
		}
	}

	@POST
	@Path("/recordForKey/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recordForKey(@PathParam("key") String key) {
		System.out.println("Key:" + key);
		if (urls.containsKey(key)) {
			return Response.status(500).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
		}
		urls.put(key, "");
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

}
