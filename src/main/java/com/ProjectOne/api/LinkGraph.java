package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public interface LinkGraph {

	public void populate();
	public JsonObject graph(); 

		
}
