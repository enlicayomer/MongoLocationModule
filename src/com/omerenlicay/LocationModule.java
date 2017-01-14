package com.omerenlicay;

import java.util.ArrayList;
import java.util.Date;


import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class LocationModule {
	Connection con = new Connection();

	public LocationModule() {
		// try{
		// DBCollection collusr = con.database.getCollection("user");
		//

		// BasicDBObject doc = new BasicDBObject("name", "MongoDB")
		// .append("type", "database")
		// .append("count", 1)
		// .append("info", new BasicDBObject("x", 203).append("y", 102));
		// coll.insert(doc);
		// System.out.println("kayit basarili.");
		// }catch(Exception e)
		// {
		// System.out.println("hata: "+e);
		// }

		// BasicDBObject userCol = new BasicDBObject("userName",
		// "Omer").append("userType", "Active").append("userDep",
		// new ObjectId(obj.get("_id").toString()));
		// collusr.insert(userCol);

		// DBCursor cursor=coll.find();
		//
		// cursor=coll.find(findCorp);
		// try {
		// while(cursor.hasNext()) {
		// System.out.println(cursor.next());
		// }
		// } finally {
		// cursor.close();
		// }
		
		
//		setCorparation("C", "DetaySoft", "X");
//		setLocation("DetaySoft", "Teknokent", 12.025, 15.06);
//		setWPSInformations("Teknokent", "Detay_ArGe", "18:28:61:61:a7:b7", "87%");
		
		DBCollection createIndex=con.database.getCollection("BL10");
		createIndex.createIndex(new BasicDBObject("coordinates","2dsphere"));
		System.out.println(getCorpList());
		
	}

	public void setLocation(String corparation, String tag, double latitude, double longitude) {

		
		Date nowDate = new Date();

		DBCollection collusr = con.database.getCollection("BL10");

		DBCollection coll = con.database.getCollection("BL00");

		BasicDBObject findCorp = new BasicDBObject("name", corparation);

		DBCursor cur = coll.find(findCorp);
		
		DBObject obj = cur.one();
		
		if (obj == null)
			System.out.println("firma yok");
		else {
			BasicDBObject userCol = new BasicDBObject();
			userCol.put("corp", new ObjectId(obj.get("_id").toString()));
			userCol.put("tag", tag);
			userCol.put("create_at", nowDate);
			userCol.put("loc", new BasicDBObject("lat", latitude).append("lon", longitude));
			
			
			collusr.insert(userCol);
			
		}
		
	}
	
	
	
	public void setCorparation(String corparationType, String corparationName, String corparationActive){
		
		DBCollection coll = con.database.getCollection("BL00");
		
		BasicDBObject corpObj=new BasicDBObject();
		corpObj.put("type", corparationType);
		corpObj.put("name", corparationName);
		corpObj.put("Active", corparationActive);
		
		coll.insert(corpObj);
		
	}
	
	public void setWPSInformations(String corpTag, String ssid, String bssid, String signal){
		
		DBCollection wpscol=con.database.getCollection("wpscol");
		
		DBCollection coll = con.database.getCollection("BL10");

		BasicDBObject findCorp = new BasicDBObject("tag", corpTag);

		DBCursor cur = coll.find(findCorp);
		
		DBObject obj = cur.one();
		
		BasicDBObject wpsobj=new BasicDBObject();
		wpsobj.put("GPSID", new ObjectId(obj.get("_id").toString()));
		wpsobj.put("CPID", new ObjectId(obj.get("corp").toString()));
		wpsobj.put("SSID", ssid);
		wpsobj.put("BSSID", bssid);
		wpsobj.put("signal", signal);
		
		wpscol.insert(wpsobj);
	}
	
	public ArrayList getCorpList()
	{
		ArrayList corpList=new ArrayList<>();
		DBCollection getCorp=con.database.getCollection("BL00");
		
		DBCursor cursor=getCorp.find();
		
		while(cursor.hasNext())
		{
			//System.out.println(cursor.next());
			corpList.add(cursor.next());
		}
		cursor.close();
		
		return corpList;
	
	}
	
	public void getLocation(double latitude, double longitude)
	{
		
	}
	

	
}
