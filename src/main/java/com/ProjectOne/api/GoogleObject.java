package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import javax.json.*;
import org.neo4j.graphdb.*;
import com.ProjectOne.api.Neo4jWithCache;

public class GoogleObject 
{

	public Node commitToNeo4j(JsonObject json, Node rootNode) {

		Node personNode = null;

		if( rootNode == null) {

			String 	name="";

			if( json.containsKey("author")) {

				JsonObject authorJson = (JsonObject)((JsonArray)json.get("author")).get(0);
				name = ((JsonObject)authorJson.get("name")).get("$t").toString();


				String email="";
				JsonObject emailJson = (JsonObject)authorJson.get("email");	
				if( emailJson != null) {
					email = emailJson.get("$t").toString();	
				}

				if( !name.equals("") ) {
					personNode = Neo4jWithCache.lookupOrCreate( "people", "name", name.replace("\"", "") ); 
					System.out.printf("NodeName1 : %s \n", name);


	        		List<String>    index = new ArrayList<String>();
                		Map<String, String>     properties = new HashMap<String, String>();
                		index.add("root");
				if( !email.equals("")) {
                			index.add("people-email");
                			properties.put("email", email);
				}
                		Neo4jWithCache.updatePropertyAndLabel(personNode, index, properties);
				} else if ( !email.equals("")) {

					personNode = Neo4jWithCache.lookupOrCreate( "people-email", "email", email.replace("\"", "") ); 
					System.out.printf("NodeName1 : %s \n", email);

	        			List<String>    index = new ArrayList<String>();
                			Map<String, String>     properties = new HashMap<String, String>();
                			index.add("root");
                			Neo4jWithCache.updatePropertyAndLabel(personNode, index, properties);
				}
			}

		} else {

			if(!json.containsKey("gd$name") 
				|| !json.containsKey("title") 
				|| !json.containsKey("gd$email") ) 			{
				System.out.printf("No Name or title\n");
				return null;
			}	

			String name="";
			String email="";	

			email= ((JsonObject) ((JsonArray)json.get("gd$email")).get(0)).get("address").toString();	

			if( json.containsKey("gd$name")) {

				JsonObject fullNameJson = (JsonObject) ( (JsonObject) json.get("gd$name")).get("gd$fullName");
				if( fullNameJson != null) {

					name = fullNameJson.get("$t").toString();
				}	
				if( !name.equals("") ) {
					personNode = Neo4jWithCache.lookupOrCreate( "people", "name", name.replace("\"", "") ); 
					System.out.printf("NodeName1 : %s \n", name);

					if( ! email.equals("") ) {
	        				List<String>    index = new ArrayList<String>();
                				Map<String, String>     properties = new HashMap<String, String>();
                				index.add("people-email");
                				properties.put("email", email.replace("\"", ""));
                				Neo4jWithCache.updatePropertyAndLabel(personNode, index, properties);
					}
				}
			} 
			if( (personNode == null) && (json.containsKey("title"))) {

				JsonObject titleJson = (JsonObject) json.get("title");
				if( titleJson != null) {

					name = titleJson.get("$t").toString();
				}	

				if( !name.equals("") ) {
					personNode = Neo4jWithCache.lookupOrCreate( "people", "name", name.replace("\"", "") ); 
					System.out.printf("NodeName1 : %s \n", name);

					if( ! email.equals("") ) {
	        			List<String>    index = new ArrayList<String>();
                			Map<String, String>     properties = new HashMap<String, String>();
                			index.add("people-email");
                			properties.put("email", email.replace("\"", ""));
                			Neo4jWithCache.updatePropertyAndLabel(personNode, index, properties);
					}
				}	
			} 
			if((personNode == null ) && ( !email.equals(""))) {

				personNode = Neo4jWithCache.lookupOrCreate( "people-email", "email", email.replace("\"", "") ); 
				System.out.printf("NodeName1 : %s \n", email);

			}
			
			if( personNode != null) {
				Map<String, String>	relProps = new HashMap<String, String>();
				relProps.put("google", "true");
				Neo4jWithCache.lookupOrCreateRelationship(rootNode, personNode, "knows", relProps); 
			}
		}
		
		return personNode;
	}
}
