package com.knurld.rest;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.knurld.dropbox.api.UploadAPI;
import com.sun.jersey.multipart.FormDataParam;

@Path("/file")
public class Twilio {

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("fileName") String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			fileName = UUID.randomUUID().toString() + ".wav";

		}
		String output = UploadAPI.uploadFile(uploadedInputStream, fileName);
		if (output != null) {
			return Response.status(200).entity(output).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();

		} else {
			return Response.status(500).entity("Not able to upload file").build();
		}
	}
}
