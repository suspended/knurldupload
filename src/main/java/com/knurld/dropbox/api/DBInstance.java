package com.knurld.dropbox.api;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v2.DbxClientV2;

public class DBInstance {
	private static DBInstance dBInstance = new DBInstance();
	
	
	
	private static final String ACCESS_TOKEN = "UmDUkC6dOAAAAAAAAAAAB300az5-WY4ps6P4plOZtoJ6ZEkHoenyqLEiZ-HjC2ph";
	DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");

	public DbxClientV2 getDbxClientV2() {
		return dbxClientV2;
	}
	public void setDbxClientV2(DbxClientV2 dbxClientV2) {
		this.dbxClientV2 = dbxClientV2;
	}
	public DbxClientV1 getDbxClientV1() {
		return dbxClientV1;
	}
	public void setDbxClientV1(DbxClientV1 dbxClientV1) {
		this.dbxClientV1 = dbxClientV1;
	}
	private DbxClientV2 dbxClientV2;
	private DbxClientV1 dbxClientV1;
	
	private DBInstance(){
		dbxClientV2 = new DbxClientV2(config, ACCESS_TOKEN);
		dbxClientV1 = new DbxClientV1(config, ACCESS_TOKEN);
	}
	public static DBInstance getInstance(){
		return dBInstance;
	}
}
