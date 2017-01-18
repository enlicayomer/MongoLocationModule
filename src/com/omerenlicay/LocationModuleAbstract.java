package com.omerenlicay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class LocationModuleAbstract {

	/**
	 * 
	 * @param jsonObject
	 * @return
	 */
	  protected static JsonObject getProperty(String jsonObject) {
	        //Gelen parametler JsonObject'e parse ediliyor
	        JsonParser jsonParser = new JsonParser();
	        JsonObject object = jsonParser.parse(jsonObject).getAsJsonObject();

	        return object;
	    }
	  
	    /**
	     * Gelen parametlerin filtreden gectigi yer
	     *
	     * @param element
	     * @return
	     */
	    protected static String getString(JsonElement element) {
	        String returnString = "";
	        if (element != null) {
	            returnString = element.getAsString();
	            returnString = returnString.replace("'", "\'");
	        }
	        return returnString;
	    }
}
