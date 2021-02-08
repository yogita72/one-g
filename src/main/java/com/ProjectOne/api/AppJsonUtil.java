
package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.StringWriter;
import java.io.StringReader;
/*
import javax.json.JsonWriter;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
*/
import javax.json.*;


public class AppJsonUtil {

	public static String toString( JsonObject	json) {

		StringWriter stWriter = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(stWriter);
		jsonWriter.writeObject(json);
		jsonWriter.close();

		String jsonString = stWriter.toString();
		return jsonString;
	}

	public static JsonStructure fromString( String jsonString) {

		JsonReader reader = Json.createReader(new StringReader(jsonString));
		JsonStructure jsonst = reader.read();

		return jsonst;

	}

	public static JsonObject HashMapToJsonObject(HashMap<String, String> hashmap) {

		List<String>		fieldValues = null;
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder() ;
		for(String key: hashmap.keySet()){
			objectBuilder.add(key,hashmap.get(key));
		}
	
		JsonObject jsonObject = objectBuilder.build();
		return jsonObject;
	}
/*
	public static JsonObject HashMapToJsonObject(HashMap<String, List<String>> hashmap) {

		List<String>		fieldValues = null;
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder() ;

		for(String key: hashmap.keySet()){
			fieldValues = hashmap.get(key);
			objectBuilder.add(key, fieldValues.toString().replace( "]", "\"").replace("[", "\"").replace(", ", ","));

		}
		JsonObject jsonObject = objectBuilder.build();
		return jsonObject;
	}
*/
	public static String HashMapToJsonString( HashMap<String, List<String>> hashmap) {
		
			
		List<String>		fieldValues = null;
		String			jsonString= new String("");
		
		jsonString = jsonString.concat("{");	
		for(String key: hashmap.keySet()){
			fieldValues = hashmap.get(key);
			jsonString = jsonString.concat("\""); 
			jsonString = jsonString.concat(key); 
			jsonString = jsonString.concat("\""); 
			jsonString = jsonString.concat(":"); 
			jsonString = jsonString.concat( fieldValues.toString().replace( "]", "\"").replace("[", "\"").replace(", ", ","));
			jsonString = jsonString.concat(","); 
				
		}
		jsonString = jsonString.substring(0, jsonString.length() - 1) + "}";	
      		System.out.print("Json String:" + jsonString);
		return jsonString;
	}


	public static void navigateTree(JsonValue tree, String key) {
   		if (key != null)
      		System.out.print("Key " + key + ": ");
   		switch(tree.getValueType()) {
      		case OBJECT:
         		System.out.println("OBJECT");
         		JsonObject object = (JsonObject) tree;
         		for (String name : object.keySet())
            				navigateTree(object.get(name), name);
         		break;
      		case ARRAY:
         		System.out.println("ARRAY");
         		JsonArray array = (JsonArray) tree;
         		for (JsonValue val : array)
            			navigateTree(val, null);
         		break;
      		case STRING:
         		JsonString st = (JsonString) tree;
         		System.out.println("STRING " + st.getString());
         		break;
      		case NUMBER:
         		JsonNumber num = (JsonNumber) tree;
         		System.out.println("NUMBER " + num.toString());
         		break;
      		case TRUE:
      		case FALSE:
      		case NULL:
         		System.out.println(tree.getValueType().toString());
         		break;
   		}
	}

	public static void addJsonArrayToArrayBuilder(JsonArrayBuilder arrayBuilder,
							JsonArray	jsonArray) {


                Iterator<JsonValue> iterator = jsonArray.iterator();

                while( iterator.hasNext()) {
		
			arrayBuilder.add((JsonObject)iterator.next());
		}

	}

} 
