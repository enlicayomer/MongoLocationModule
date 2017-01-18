package com.omerenlicay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.omerenlicay.model.DeviceInfoModel;
import com.omerenlicay.model.GeoLocationModel;
import com.omerenlicay.model.LocationInfoModel;
import com.omerenlicay.model.ManuelSetModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

public class LocationModule extends LocationModuleAbstract {
	ManuelSetModel manuelSetModel = new ManuelSetModel();
	LocationInfoModel locationInfoModel = new LocationInfoModel();

	public LocationModule() {
		new Connection();
		//
		// setCorparation("C", "DetaySoft", "X");
		// setLocation("DetaySoftt", "Teknokent", 12.025, 15.06);
		// setWPSInformations("Teknokent", "Detay_ArGe", "18:28:61:61:a7:b7",
		// "87%");
		//
		MongoCollection<Document> collectionUser = Connection.database.getCollection("user");
		Document document = new Document();
		FindIterable<Document> find = collectionUser.find(document);
		// System.out.println(find.first().toString());
		//
		// getGPSLocation(15.025, 15.06);
		// System.out.println(getCorpList());

		// getWPSLocation("18:28:61:61:a7:b7");

		initializingMethod(find.first().toJson());

	}

	public void initializingMethod(String jsonObject) {

		JsonObject infoObject = getProperty(jsonObject);

		// cihaz bilgileri
		JsonArray locationInfoObject = infoObject.getAsJsonArray("deviceinfolist");
		List<DeviceInfoModel> deviceInfoList = new ArrayList<DeviceInfoModel>();

		// bagli cihaz
		String isConnectedDeviceId = "";
		for (JsonElement deviceInfoObject : locationInfoObject) {

			DeviceInfoModel deviceInfoModel = new DeviceInfoModel();

			String deviceBssid = getString(deviceInfoObject.getAsJsonObject().get("bssid"));

			deviceInfoModel.setDeviceBssid(deviceBssid);

			String deviceSsid = getString(deviceInfoObject.getAsJsonObject().get("ssid"));
			deviceInfoModel.setDeviceSsid(deviceSsid);

			String deviceIsConnected = getString(deviceInfoObject.getAsJsonObject().get("isconnected"));
			deviceInfoModel.setIsConnected(deviceIsConnected);
			if (deviceIsConnected.equals("X"))
				isConnectedDeviceId = deviceBssid;

			String deviceSignalRate = getString(deviceInfoObject.getAsJsonObject().get("signalrate"));
			deviceInfoModel.setSignalRate(deviceSignalRate);

			deviceInfoList.add(deviceInfoModel);

			if (deviceBssid.isEmpty())
				deviceInfoList.remove(deviceInfoList.size() - 1);

		}

		// Geolokasyon bilgileri
		GeoLocationModel geoLocationModel = new GeoLocationModel();
		JsonObject geolocation = infoObject.getAsJsonObject("geolocation");

		String latitude = geolocation.getAsJsonObject().get("latitude").getAsString();
		if (latitude.isEmpty())
			latitude = null;

		String longitude = geolocation.getAsJsonObject().get("longitude").getAsString();
		if (longitude.isEmpty())
			longitude = null;
		geoLocationModel.setLatitude(latitude);
		geoLocationModel.setLongitude(longitude);

		/**
		 * GPS verisi ile konum tespiti yapmak için lat ve lon degerlerinin null
		 * olmamasný kontrol edip getGPSLocation metoduna gönderiyorum. Daha
		 * sonra ayný pakette gelen WPS bilgilerini bu GPS bilgisi ile
		 * iliþkilendiriyorum.
		 */
		if (latitude != null && longitude != null) {

			if (getGPSLocation(Double.parseDouble(latitude), Double.parseDouble(longitude))) {
				if (!deviceInfoList.isEmpty())
					for (int i = 0; i < deviceInfoList.size(); i++)
						updateWPSInformations(locationInfoModel.getTag(), deviceInfoList.get(i).getDeviceSsid(),
								deviceInfoList.get(i).getDeviceBssid());
			}
			else
			{
				locationInfoModel.setBilinmeyenKonum("manuel GPS set edilecek");
			}
		}

		// WPS Konum Tespit Kontrolleri
		// lat ve lon alýnamamýþ ise wps kontrolü yap. BSSID ile.
		if (latitude == null || longitude == null) {
			boolean setWps = false;

			// device listesi boþsa (gelen veride device bilgisi hiç yoksa)
			if (deviceInfoList.isEmpty())
				locationInfoModel.setBilinmeyenKonum("Bilinmeyen Konum");

			for (int i = 0; i < deviceInfoList.size(); i++) {

				if (getWPSLocation(deviceInfoList.get(i).getDeviceBssid())) {
					System.out.println("Konum bilgisi basari ile alindi.");
					setWps = true;
				}
			}

			//Bu noktada yeni bir veri paket gelecek.
			if (!setWps) {
				
				locationInfoModel.setBilinmeyenKonum("manuel WPS konumu set edilecek");
//				for (int i = 0; i < deviceInfoList.size(); i++) {
//					setWPSisNotGpsData(manuelSetModel.getTag(), deviceInfoList.get(i).getDeviceSsid(),
//							deviceInfoList.get(i).getDeviceBssid());
//				}
			}
		}

		System.out.println(locationInfoModel.getCorporation() + " " + locationInfoModel.getTag());

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
		
		try {
			MongoCollection<Document> collectionBL10 = Connection.database.getCollection("BL10");
			MongoCollection<Document> collectionBL00 = Connection.database.getCollection("BL00");

			Document BL00Find = new Document("name", corparation);
			FindIterable<Document> findCorp = collectionBL00.find(BL00Find);

			Document BL00Document = new Document();
			BL00Document.put("GPSID", new ObjectId(findCorp.first().get("_id").toString()));
			BL00Document.put("tag", tag);
			BL00Document.put("create_at", nowDate);
			BL00Document.put("loc", new BasicDBObject("lat", latitude).append("lon", longitude));

			collectionBL10.insertOne(BL00Document);
		} catch (NullPointerException e) {
			/*
			 * NullPointerException dönüyorsa corporation parametresi yanlýþ
			 * girilmiþtir veya yoktur
			 */
			System.err.println("bilinmeyen konum");
		} catch (Exception e) {

			System.err.println("Lokasyon set error: " + e);
		}

	}

	/**
	 * Firma SET metodu. MongoDB 3.4
	 * 
	 * @param corparationType
	 * @param corparationName
	 * @param corparationActive
	 */
	public void setCorparation(String corparationType, String corparationName, String corparationActive) {

		try {
			MongoCollection<Document> collectionBL00 = Connection.database.getCollection("BL00");

			Document BL00Document = new Document();
			BL00Document.put("type", corparationType);
			BL00Document.put("name", corparationName);
			BL00Document.put("Active", corparationActive);

			collectionBL00.insertOne(BL00Document);
		} catch (Exception e) {
			System.err.println("Corparation set error: " + e);
		}

	}

	/**
	 * 
	 * @param tag
	 * @param ssid
	 * @param bssid
	 * @param signalRate
	 * @param isConnected
	 */
	public static void setWPSisNotGpsData(String tag, String ssid, String bssid) {
		try {
			MongoCollection<Document> collectionWps = Connection.database.getCollection("wpscol");

			Document setWpsInfo = new Document();
			setWpsInfo.put("tag", tag);
			setWpsInfo.put("ssid", ssid);
			setWpsInfo.put("bssid", bssid);

			collectionWps.insertOne(setWpsInfo);

		} catch (Exception e) {
			System.err.println("hata " + e);
		}
	}

	/**
	 * WPS bilgilerini; SSID, BSSID, signal parametreleri ile GPSID ve CPID
	 * corpTag parametresine göre alýnýyor. MongoDB 3.4
	 * 
	 * @param corpTag
	 * @param ssid
	 * @param bssid
	 * @param signal
	 */
	public void updateWPSInformations(String corpTag, String ssid, String bssid) {
		System.out.println(corpTag);
		System.out.println(bssid);
		try {
			MongoCollection<Document> collectionWpscol = Connection.database.getCollection("wpscol");
			MongoCollection<Document> collectionBL10 = Connection.database.getCollection("BL10");

			Document wpsCollectionDel = new Document("BSSID", bssid);
			collectionWpscol.deleteMany(wpsCollectionDel);

			Document wpsCollection = new Document("tag", corpTag);
			FindIterable<Document> cursor = collectionBL10.find(wpsCollection);

			Document wpsObj = new Document();
			wpsObj.put("GPSID", new ObjectId(cursor.first().get("_id").toString()));
			wpsObj.put("CPID", new ObjectId(cursor.first().get("corp").toString()));
			wpsObj.put("SSID", ssid);
			wpsObj.put("BSSID", bssid);
			collectionWpscol.insertOne(wpsObj);

		} catch (NullPointerException e) {
			/*
			 * NullPointerException dönüyorsa corpTag parametresi yanlýþ
			 * girilmiþtir veya sistemde kayýtlý deðildir.
			 */
			System.out.println("Girilen þirket etiketi sistemde mevcut deðildir.");

		} catch (Exception e) {
			System.err.println("WPS Info set error: " + e);
		}
	}

	/**
	 * BL00 koleksiyonundaki tüm dokumanlarý döndürür. MongoDB version 3.4
	 * 
	 * @return List<Document>
	 */
	public List<Document> getCorpList() {

		List<Document> foundDocument = null;
		try {

			List<Document> corpList = new ArrayList<>();
			MongoCollection<Document> collection = Connection.database.getCollection("BL00");

			MongoCursor<Document> cursor = collection.find().iterator();

			while (cursor.hasNext()) {
				corpList.add(cursor.next());
			}
			cursor.close();
			foundDocument = collection.find().into(new ArrayList<Document>());
			cursor.close();

		} catch (Exception e) {
			System.err.println("Corparation get list error: " + e);
		}
		return foundDocument;
	}

	/**
	 * getLocation metod.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public boolean getGPSLocation(double latitude, double longitude) {
		boolean returnValue = false;

		String konumTag = "";
		String konumCorp = "";

		try {

			MongoCollection<Document> collectionBL10 = Connection.database.getCollection("BL10");
			MongoCollection<Document> collectionBL00 = Connection.database.getCollection("BL00");
			collectionBL10.createIndex(Indexes.geo2dsphere("loc"));

			Point refPoint = new Point(new Position(latitude, longitude));

			konumTag = collectionBL10.find(Filters.nearSphere("loc", refPoint, 100.0, 0.0)).first().get("tag")
					.toString();

			Document findCorpId = new Document("tag", konumTag);
			FindIterable<Document> findCo = collectionBL10.find(findCorpId);
			Document findCorp = new Document("_id", findCo.first().get("corp"));
			FindIterable<Document> findCorpCursor = collectionBL00.find(findCorp);
			konumCorp = findCorpCursor.first().get("name").toString();

			// System.out.println(konumCorp+" "+konumTag);
			locationInfoModel.setTag(konumTag);
			locationInfoModel.setCorporation(konumCorp);
			returnValue = true;
		} catch (Exception e) {
			System.err.println("GPS hata: " + e);
			returnValue = false;
		}
		return returnValue;

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
	public boolean getWPSLocation(String bssid) {

		boolean returnValue = false;
		try {
			MongoCollection<Document> collectionWpsCol = Connection.database.getCollection("wpscol");
			Document wpsCollection = new Document("BSSID", bssid);

			FindIterable<Document> Wpscursor = collectionWpsCol.find(wpsCollection);

			MongoCollection<Document> collectionTag = Connection.database.getCollection("BL10");
			MongoCollection<Document> collectionCorp = Connection.database.getCollection("BL00");

			// get tag operasyonu
			Document getCorpTag = new Document("_id", Wpscursor.first().get("GPSID"));
			FindIterable<Document> CorpTagCursor = collectionTag.find(getCorpTag);

			// get corp operasyon
			Document getCorp = new Document("_id", CorpTagCursor.first().get("corp"));
			FindIterable<Document> CorpCursor = collectionCorp.find(getCorp);

			locationInfoModel.setCorporation(CorpCursor.first().get("name").toString());
			locationInfoModel.setTag(CorpTagCursor.first().get("tag").toString());

			System.out.println(locationInfoModel.getTag() + " " + locationInfoModel.getCorporation());

			returnValue = true;
		} catch (NullPointerException e) {

			System.err.println("Gecerli BSSID sisteme kayýtlý degil. Tag Eklenecek.");
			returnValue = false;
		}
		return returnValue;

	}

}
