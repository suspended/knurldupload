package com.knurld.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataParam;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Play;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

@Path("/twilio")
public class Twilio {
	// incomingCall

	@POST
	@Path("/incomingCall")
	@Produces(MediaType.APPLICATION_XML)
	public Response incomingCall(@FormDataParam("Digits") String digits) {
		// Create a TwiML response and add our friendly message.
		TwiMLResponse twiml = new TwiMLResponse();
		Say say = new Say("Hi There, you calling Knurld!");

		// Play an MP3 for incoming callers.
		Play play = new Play("http://demo.twilio.com/hellomonkey/monkey.mp3");

		Gather gather = new Gather();
		gather.setAction("/handle-key");
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
}
