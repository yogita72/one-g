/**
 * Copyright 2010 Newcastle University
 *
 * http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ProjectOne.endpoints;

import com.ProjectOne.Database;
import com.ProjectOne.api.Api;
import com.ProjectOne.api.Uploader;
import com.ProjectOne.api.LinkedinUploader;
import com.ProjectOne.api.GoogleContactsUploader;
import com.ProjectOne.api.OneUploader;
import com.ProjectOne.Common;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Entity;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.*;
import java.util.*;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

/**
 *
 *
 *
 */
@Path("/people")
public class ResourceEndpoint {
    @Inject
    private Database database;

	@Path("upload_data")	
    @POST
    public Response uploadData(@Context HttpServletRequest request) throws OAuthSystemException {

	String source = request.getParameter("source");	
		
		Response response = authorizeResource(request);		
		if( response == null) {
			Uploader	uploader = null ;
			Api.init();
			String body = getBodyFromRequest(request);
			if( source.equals("linkedin") ) {
			
				uploader = new LinkedinUploader();

			} else if( source.equals("google") ) {

				uploader = new GoogleContactsUploader();
			} else if( source.equals("one") ) {


				uploader = new OneUploader();
			}	
				
			if( uploader != null ) {
				uploader.upload(body);
			}
            return Response.status(Response.Status.OK).build();
		} else {
			return response;
		}
    }

	@Path("query")	
    @GET
    @Produces("text/html")
    @Consumes("text/html")
    public Response query(@Context HttpServletRequest request) throws OAuthSystemException {

	String query = request.getParameter("query");	
		

		Response response = authorizeResource(request);		

		if( response == null) {
			Api.init();
			System.out.printf("Server :query : : param %s-----\n", query);
			String result = Api.query(query);
				
            return Response.status(Response.Status.OK).entity(result).build();
		} else {

			return response;
		}

    }

	@Path("onegraph")	
    @GET
    @Produces("text/html")
    @Consumes("text/html")
    public Response onegraph(@Context HttpServletRequest request) throws OAuthSystemException {

	String names = request.getParameter("names");	
	String	skip = request.getParameter("skip");
	Map<String, String>	config= new HashMap<String, String>();
	
	if( skip != null ) {
		config.put("skip", skip);
		System.out.printf("Server :skip : : param %s-----\n", skip);
	}	

		Response response = authorizeResource(request);		

		if( response == null) {
			Api.init();
			System.out.printf("Server :query : : param %s-----\n", names);
			String result = Api.graph(names, true, config);
				
            return Response.status(Response.Status.OK).entity(result).build();
		} else {

			return response;
		}

    }
	@Path("connectgraph")	
    @GET
    @Produces("text/html")
    @Consumes("text/html")
    public Response connectgraph(@Context HttpServletRequest request) throws OAuthSystemException {

	String names = request.getParameter("names");	
	String	skip = request.getParameter("skip");
	Map<String, String>	config= new HashMap<String, String>();
	
	if( skip != null )
		config.put("skip", skip);
		

		Response response = authorizeResource(request);		

		if( response == null) {
			Api.init();
			System.out.printf("Server :query : : param %s-----\n", names);
			String result = Api.graph(names, false, config);
				
            return Response.status(Response.Status.OK).entity(result).build();
		} else {

			return response;
		}

    }
	private Response authorizeResource(HttpServletRequest request) throws OAuthSystemException {
        try {
            // Make the OAuth Request out of this request
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
            // Get the access token
            String accessToken = oauthRequest.getAccessToken();

            // Validate the access token
            if (!database.isValidToken(accessToken)) {
                // Return the OAuth error message
                OAuthResponse oauthResponse = OAuthRSResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setRealm(Common.RESOURCE_SERVER_NAME)
                        .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                        .buildHeaderMessage();

                //return Response.status(Response.Status.UNAUTHORIZED).build();
                return Response.status(Response.Status.UNAUTHORIZED)
                        .header(OAuth.HeaderType.WWW_AUTHENTICATE,
                        oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
                        .build();

            }
            // Return null 
            return null; 
        
        } catch (OAuthProblemException e) {
            // Check if the error code has been set
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {

                // Return the OAuth error message
                OAuthResponse oauthResponse = OAuthRSResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setRealm(Common.RESOURCE_SERVER_NAME)
                        .buildHeaderMessage();

                // If no error code then return a standard 401 Unauthorized response
                return Response.status(Response.Status.UNAUTHORIZED)
                        .header(OAuth.HeaderType.WWW_AUTHENTICATE,
                        oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
                        .build();
            }

            OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setRealm(Common.RESOURCE_SERVER_NAME)
                    .setError(e.getError())
                    .setErrorDescription(e.getDescription())
                    .setErrorUri(e.getUri())
                    .buildHeaderMessage();

            return Response.status(Response.Status.BAD_REQUEST)
                    .header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
                    .build();
        }
	}

	private String getBodyFromRequest(HttpServletRequest request) 
	{
  		StringBuffer jb = new StringBuffer();
  	//	byte[] line = new byte[] ; 
		String line = "";
		int	bytesRead = 0 ;
		int offset = 0 ;
		int	available;
  		try {
    		ServletInputStream reader = request.getInputStream();
		System.out.printf( "Available bytes : %d\n", reader.available());
		line = convertStreamToString(reader);
  		} catch (IOException e) {
			e.printStackTrace();
  		}
/*
		available = reader.available();

    		while ((bytesRead = reader.readLine(line, offset, available )) != 0) {
      			jb.append(line.toString());
			available = reader.available();
			offset = offset + bytesRead;
		}
		InputStreamReader isr = new InputStreamReader(reader);

    		BufferedReader br = new BufferedReader(isr);

		while ((line = br.readLine()) != null) {

      			jb.append(line);

		}
  		} catch (IOException e) {
			e.printStackTrace();
  		}

		return jb.toString();

*/
		return line;	
	}

	private String convertStreamToString(java.io.InputStream is) {
    		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    		return s.hasNext() ? s.next() : "";
	}	
}
