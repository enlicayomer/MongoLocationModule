package com.omerenlicay;


import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class Connection {
	
	MongoClient mongoClient;
	DB database;
	
	public Connection(){
		
		try{
		mongoClient=new MongoClient("localhost", 27017);
		database=mongoClient.getDB("LBS");
		System.out.println("baglanti basarili.");
		}catch(Exception e)
		{
			System.out.println("veri tabanina baglanilamadi. "+e);
		}
	}

}