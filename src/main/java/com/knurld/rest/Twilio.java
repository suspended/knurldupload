package com.knurld.rest;

import java.util.HashMap;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Play;
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
	public Response incomingCall() {
		// Create a TwiML response and add our friendly message.
		TwiMLResponse twiml = new TwiMLResponse();
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
	public Response handlekey(@FormParam("Digits") String digits, @FormParam("CallSid") String callId) {
		TwiMLResponse twiml = new TwiMLResponse();
		System.out.println("digits:" + digits);
		System.out.println("Keys for urls:" + urls.keySet());
		digits = digits.toString();

		if (urls.containsKey(digits)) {
			Say pleaseLeaveMessage = new Say("Record your monkey howl after the tone.");
			// Record the caller's voice.
			callSidMap.put(callId, digits);
			Record record = new Record();
			record.setMaxLength(30);
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
				twiml.append(new Say("Thanks for using Knurld... take a listen to what you howled."));
				twiml.append(new Play(recordingUrl));
				twiml.append(new Say("Goodbye"));
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		}
		return Response.status(200).entity(twiml.toXML()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/getKey/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKey(@PathParam("key") String key) {
		JSONObject success = new JSONObject();
		success.put("url", urls.get(key));
		return Response.status(200).entity(success.toJSONString()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@POST
	@Path("/recordForKey")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recordForKey(@FormParam("key") String key) {
		System.out.println("Key:"+key);
		urls.put(key, "");
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

}
