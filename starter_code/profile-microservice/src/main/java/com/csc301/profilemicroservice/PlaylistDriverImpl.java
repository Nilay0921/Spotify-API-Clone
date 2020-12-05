package com.csc301.profilemicroservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.neo4j.driver.v1.Transaction;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;
	OkHttpClient client = new OkHttpClient();

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}
	
	public int checkSong (String userName, String songId) {
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		//Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		//insertF.put("frndUserName", frndUserName);
		//insert.put("userName", userName);
		insert.put("plName", userName + "-favourites");
		insert.put("songId", songId);
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:playlist {plName:$plName})" + 
	            		"-[r:includes]-(b:song {songId:$songId})\n"
	            		+ "RETURN r",
	                      insert);
	            if (node_boolean.hasNext()) {
	            	return 1;
	            }
	        }
			return 0;
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		DbQueryStatus dbQueryStatus;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> response = new HashMap<String, Object>();
		String songName = "";
		
		//int s = playlistDriver.checkSong(userName, songId);
		/*if (s == 1) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}*/
		
		//String path = String.format("GET http://localhost:3001/getSongById/songId", songId);

		if (String.valueOf(songId) != null) {
			HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001/getSongTitleById/" + songId).newBuilder();
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			String url = urlBuilder.build().toString();
			
			//HttpUrl.Builder urlBuilder1 = HttpUrl.parse("http://localhost:3001/getSongTitleById/" + songId).newBuilder();
			//urlBuilder.addQueryParameter("shouldDecrement", "false");
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			//String url1 = urlBuilder.build().toString();
				
			//System.out.println(url);
		    //RequestBody body = RequestBody.create(null, new byte[0]);

			Request r = new Request.Builder()
					.url(url)
					.method("GET", null)
					.build();

			Call call = client.newCall(r);
			Response responseFromAddMs = null;
			

			String addServiceBody = "{}";

			try {
				responseFromAddMs = call.execute();
				addServiceBody = responseFromAddMs.body().string();
				//responseFromSong = call1.execute();
				//addServiceBody1 = responseFromSong.body().string();
				//System.out.println(addServiceBody);
				//response.put("data", mapper.readValue(addServiceBody, Map.class));
				songName = mapper.readValue(addServiceBody, Map.class).get("data").toString();
				//String status1 = mapper.readValue(addServiceBody1, Map.class).get("data").toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		//Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		Map<String, Object> insertSong = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		//insertF.put("frndUserName", frndUserName);
		//insert.put("userName", userName);
		insert.put("plName", userName + "-favourites");
		insert.put("songId", songId);
		insertSong.put("plName", userName + "-favourites");
		insertSong.put("songId", songId);
		insertSong.put("song", songName);
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:profile {userName:$userName}) RETURN a", 
	                insertUserName);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            /*node_boolean = tx.run("MATCH (a:profile {userName:$frndUserName}) RETURN a", 
		                insertF);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }*/
	            node_boolean = tx.run("MATCH (a:playlist {plName:$plName})" + 
	            		"-[r:includes]-(b:song {songId:$songId})\n"
	            		+ "RETURN r",
	                      insert);
	            if (node_boolean.hasNext()) {
	            	System.out.println("Hello");
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
	            	return status;
	              }
	            tx.success();
	        }
			session.writeTransaction(tx -> tx.run("MATCH (a:playlist {plName:$plName})\n"
		              + "MERGE (a)-[r:includes]-(c:song {songId:$songId, song:$song})", 
					insertSong));
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		//Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		//insertF.put("frndUserName", frndUserName);
		//insert.put("userName", userName);
		insert.put("plName", userName + "-favourites");
		insert.put("songId", songId);
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:profile {userName:$userName}) RETURN a", 
	                insertUserName);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            /*node_boolean = tx.run("MATCH (a:profile {userName:$frndUserName}) RETURN a", 
		                insertF);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }*/
	            node_boolean = tx.run("MATCH (a:playlist {plName:$plName})" + 
	            		"-[r:includes]-(b:song {songId:$songId})\n"
	            		+ "RETURN r",
	                      insert);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	              }
	            tx.success();
	        }
			session.writeTransaction(tx -> tx.run("MATCH (a:playlist {plName:$plName})" + 
            		"-[r:includes]-(b:song {songId:$songId})\n"
            		+ "DELETE r, b", 
					insert));
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		//Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("songId", songId);
		//insertF.put("frndUserName", frndUserName);
		//insert.put("userName", userName);
		//insert.put("plName", userName + "-favourites");
		//insert.put("songId", songId);
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:song {songId:$songId}) RETURN a", 
	                insertUserName);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            /*node_boolean = tx.run("MATCH (a:profile {userName:$frndUserName}) RETURN a", 
		                insertF);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }*/
	            /*node_boolean = tx.run("MATCH (a:playlist {plName:$plName})" + 
	            		"-[r:includes]-(b:song {songId:$songId})\n"
	            		+ "RETURN r",
	                      insert);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	              }
	            tx.success();*/
	        }
			session.writeTransaction(tx -> tx.run("MATCH (s:song {songId: \"\" + $songId + \"\"}) DETACH DELETE s", 
					insertUserName));
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}
}
