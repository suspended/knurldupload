package com.knurld.dropbox.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.UploadErrorException;

public class UploadAPI {

	public static String uploadFile(InputStream inputStream, String fileName) {
		DBInstance instance = DBInstance.getInstance();
		String s = null;
		try {
			instance.getDbxClientV2().files().uploadBuilder("/" + fileName)
					.uploadAndFinish(inputStream);
			s = instance.getDbxClientV1().createShareableUrl("/" + fileName);

		} catch (UploadErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	// save uploaded file to new location
	@SuppressWarnings("unused")
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
