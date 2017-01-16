package com.omerenlicay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;


import com.mongodb.BasicDBObject;
import com.mongodb.Block;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;


public class LocationModule {
	Connection con = new Connection();

	public LocationModule() {
		
//
//		 setCorparation("C", "DetaySoft", "X");
		 setLocation("DetaySoftt", "Teknokent", 12.025, 15.06);
//		setWPSInformations("Teknokent", "Detay_ArGe", "18:28:61:61:a7:b7", "87%");
//

//
//     	getGPSLocation(15.025, 15.06);
//		 System.out.println(getCorpList());;

		//getWPSLocation("18:28:61:61:a7:b5");
	}

	
	/**
	 * setLocation metod.
	 * 
	 * @param corparation
	 * @param tag
	 * @param latitude
	 * @param longitude
	 */
	public void setLocation(String corparation, String tag, double latitude, double longitude) {

		Date nowDate = new Date();

		try{
		MongoCollection<Document> collectionBL10 = con.database.getCollection("BL10");
		MongoCollection<Document> collectionBL00 = con.database.getCollection("BL00");
		
		Document BL00Find = new Document("name", corparation);
		FindIterable<Document> findCorp = collectionBL00.find(BL00Find);
		
		Document BL00Document=new Document();
		BL00Document.put("GPSID", new ObjectId(findCorp.first().get("_id").toString()));
		BL00Document.put("tag", tag);
		BL00Document.put("create_at", nowDate);
		BL00Document.put("loc", new BasicDBObject("lat", latitude).append("lon", longitude));
		
		collectionBL10.insertOne(BL00Document);
		}catch(NullPointerException e)
		{
			System.out.println("bilinmeyen konum");
		}catch(Exception e)
		{

			System.out.println("Lokasyon set error: "+e);
		}

	}

	
	
	/**
	 * Firma SET metodu.
	 * MongoDB 3.4
	 * 
	 * @param corparationType
	 * @param corparationName
	 * @param corparationActive
	 */
	public void setCorparation(String corparationType, String corparationName, String corparationActive) {

		try{
		MongoCollection<Document> collectionBL00 = con.database.getCollection("BL00");
		
		Document BL00Document=new Document();
		BL00Document.put("type", corparationType);
		BL00Document.put("name", corparationName);
		BL00Document.put("Active", corparationActive);
		
		collectionBL00.insertOne(BL00Document);
		}catch(Exception e)
		{
			System.out.println("Corparation set error: "+e);
		}

	}

	
	
	
	/**
	 * WPS bilgilerini; SSID, BSSID, signal parametreleri ile
	 * GPSID ve CPID corpTag parametresine göre alýnýyor. 
	 * MongoDB 3.4
	 * 
	 * @param corpTag
	 * @param ssid
	 * @param bssid
	 * @param signal
	 */
	public void setWPSInformations(String corpTag, String ssid, String bssid, String signal) {

		try{
		MongoCollection<Document> collectionWpscol = con.database.getCollection("wpscol");
		MongoCollection<Document> collectionBL10 = con.database.getCollection("BL10");

		Document wpsCollection = new Document("tag", corpTag);
		FindIterable<Document> cursor = collectionBL10.find(wpsCollection);

		
		Document wpsObj = new Document();
		wpsObj.put("GPSID", new ObjectId(cursor.first().get("_id").toString()));
		wpsObj.put("CPID", new ObjectId(cursor.first().get("corp").toString()));
		wpsObj.put("SSID", ssid);
		wpsObj.put("BSSID", bssid);
		wpsObj.put("signal", signal);
		collectionWpscol.insertOne(wpsObj);
		}catch(Exception e)
		{
			System.out.println("WPS Info set error: "+e);
		}
	}

	
	
	/**
	 * BL00 koleksiyonundaki tüm dokumanlarý döndürür. MongoDB version 3.4
	 * 
	 * @return List<Document>
	 */
	public List<Document> getCorpList() {
		
		List<Document> foundDocument = null;
		try{
		
		List<Document> corpList = new ArrayList<>();
		MongoCollection<Document> collection = con.database.getCollection("BL00");

		MongoCursor<Document> cursor = collection.find().iterator();

		while (cursor.hasNext()) {
			corpList.add(cursor.next());
		}
		cursor.close();
		 foundDocument = collection.find().into(new ArrayList<Document>());
		cursor.close();
		
		}catch(Exception e)
		{
			System.out.println("Corparation get list error: "+e);
		}
		return foundDocument;
	}

	/**
	 * getLocation metod.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public void getGPSLocation(double latitude, double longitude) {
		try {
			
			MongoCollection<Document> collection = con.database.getCollection("BL10");

			collection.createIndex(Indexes.geo2dsphere("loc"));

			Point refPoint = new Point(new Position(latitude, longitude));

			collection.find(Filters.near("loc", refPoint, 100.0, 0.0)).forEach(printBlock);
			
		} catch (Exception e) {
			System.out.println("hata: " + e);
		}

	}

	
	Block<Document> printBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			System.out.println(document.toJson());
		}
	};
		
	
	
	/**
	 * 
	 * @param bssid
	 */
	public void getWPSLocation(String bssid)
	{
		try{
			MongoCollection<Document> collectionWpsCol = con.database.getCollection("wpscol");
			Document wpsCollection = new Document("BSSID", bssid);
			FindIterable<Document> Wpscursor = collectionWpsCol.find(wpsCollection);
			
			System.out.println(Wpscursor.first().get("BSSID").toString());
			
				
			
			MongoCollection<Document> collectionCorp = con.database.getCollection("BL10");
			Document getCorpTag=new Document("_id",Wpscursor.first().get("GPSID"));
			FindIterable<Document> CorpTagCursor=collectionCorp.find(getCorpTag);
			System.out.println(CorpTagCursor.first().get("tag"));
			
			
		}catch(Exception e)
		{
			System.out.println("Hata: "+e);
			System.out.println("Gecerli BSSID sisteme kayýtlý degil.");
			setWPSInformations("Teknokent", "havelsanWiFi", bssid, "%75");
		}
		
	}
	
	
	

}
