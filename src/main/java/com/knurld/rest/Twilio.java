package com.knurld.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataParam;
import com.twilio.sdk.verbs.Dial;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Play;
import com.twilio.sdk.verbs.Record;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

@Path("/twilio")
public class Twilio {
	// incomingCall

	@POST
	@Path("/incomingCall")
	@Produces(MediaType.APPLICATION_XML)
	public Response incomingCall() {
		// Create a TwiML response and add our friendly message.
		TwiMLResponse twiml = new TwiMLResponse();
		Say say = new Say("Hi There, you calling Knurld!");

		// Play an MP3 for incoming callers.
		Play play = new Play("http://demo.twilio.com/hellomonkey/monkey.mp3");

		Gather gather = new Gather();
		gather.setAction("https://knurldupload.herokuapp.com/rest/twilio/handlekey");
		gather.setNumDigits(1);
		gather.setMethod("POST");
		Say sayInGather = new Say("To speak to a real monkey, press 1. " + "Press 2 to record your own monkey howl. "
				+ "Press any other key to start over.");
		try {
			gather.append(sayInGather);
			twiml.append(say);
			twiml.append(play);
			twiml.append(gather);
		} catch (TwiMLException e) {
			e.printStackTrace();
		}

		return Response.status(200).entity(twiml).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@POST
	@Path("/handlekey")
	@Produces(MediaType.APPLICATION_XML)
	public Response handlekey(@FormParam("Digits") String digits) {
		TwiMLResponse twiml = new TwiMLResponse();
		
		System.out.println("digits:" + digits);

		if (digits != null && digits.equals("1")) {
			// Connect 310 555 1212 to the incoming caller.
			Dial dial = new Dial("+14085148375");

			// If the above dial failed, say an error message.
			Say say = new Say("The call failed, or the remote party hung up. Goodbye.");
			try {
				twiml.append(dial);
				twiml.append(say);
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		} else if (digits != null && digits.equals("2")) {
			Say pleaseLeaveMessage = new Say("Record your monkey howl after the tone.");
			// Record the caller's voice.
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
		}

		return Response.status(200).entity(twiml).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}

	@POST
	@Path("/handleRecording")
	@Produces(MediaType.APPLICATION_XML)
	public Response handleRecording(@FormParam("RecordingUrl") String recordingUrl) {
		System.out.println("RecordingUrl:" + recordingUrl);
		TwiMLResponse twiml = new TwiMLResponse();
		if (recordingUrl != null) {
			try {
				twiml.append(new Say("Thanks for howling... take a listen to what you howled."));
				twiml.append(new Play(recordingUrl));
				twiml.append(new Say("Goodbye"));
			} catch (TwiMLException e) {
				e.printStackTrace();
			}
		}
		return Response.status(200).entity(twiml).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

	}
}
