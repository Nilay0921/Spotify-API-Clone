package com.csc301.profilemicroservice;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus;
		
		String userName = params.get("userName");
		String fullName = params.get("fullName");
		String password = params.get("password");
		
		if (userName.equals("") || fullName.equals("") || password.equals("")) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		
		dbQueryStatus = profileDriver.createUserProfile(userName, fullName, password);

		//response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus = profileDriver.followFriend(userName, friendUserName);

		//response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus = profileDriver.getAllSongFriendsLike(userName);
		
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.unfollowFriend(userName, friendUserName);

		//response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {
		DbQueryStatus dbQueryStatus;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		int s = playlistDriver.checkSong(userName, songId);
		if (s == 1) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		
		//String path = String.format("GET http://localhost:3001/getSongById/songId", songId);

		if (String.valueOf(songId) != null) {
			HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001/updateSongFavouritesCount/" + songId).newBuilder();
			urlBuilder.addQueryParameter("shouldDecrement", "false");
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			String url = urlBuilder.build().toString();
			
			//HttpUrl.Builder urlBuilder1 = HttpUrl.parse("http://localhost:3001/getSongTitleById/" + songId).newBuilder();
			//urlBuilder.addQueryParameter("shouldDecrement", "false");
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			//String url1 = urlBuilder.build().toString();
				
			//System.out.println(url);
		    RequestBody body = RequestBody.create(null, new byte[0]);

			Request r = new Request.Builder()
					.url(url)
					.method("PUT", body)
					.build();

			Call call = client.newCall(r);
			Response responseFromAddMs = null;
			
			//Call call1 = client.newCall(r1);
			//Response responseFromSong = null;

			String addServiceBody = "{}";
			//String addServiceBody1 = "{}";

			try {
				responseFromAddMs = call.execute();
				addServiceBody = responseFromAddMs.body().string();
				//responseFromSong = call1.execute();
				//addServiceBody1 = responseFromSong.body().string();
				//System.out.println(addServiceBody);
				//response.put("data", mapper.readValue(addServiceBody, Map.class));
				String status = mapper.readValue(addServiceBody, Map.class).get("status").toString();
				//String status1 = mapper.readValue(addServiceBody1, Map.class).get("data").toString();
				if (status.equals("OK")) {
					dbQueryStatus = playlistDriver.likeSong(userName, songId);
					response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				} else {
					dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		DbQueryStatus dbQueryStatus;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		//int s = playlistDriver.checkSong(userName, songId);
		/*if (s == 1) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}*/
		
		//String path = String.format("GET http://localhost:3001/getSongById/songId", songId);

		if (String.valueOf(songId) != null) {
			HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001/updateSongFavouritesCount/" + songId).newBuilder();
			urlBuilder.addQueryParameter("shouldDecrement", "true");
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			String url = urlBuilder.build().toString();
			
			//HttpUrl.Builder urlBuilder1 = HttpUrl.parse("http://localhost:3001/getSongTitleById/" + songId).newBuilder();
			//urlBuilder.addQueryParameter("shouldDecrement", "false");
			//urlBuilder.addQueryParameter("secondNumber", secondNumber);
			//String url1 = urlBuilder.build().toString();
				
			//System.out.println(url);
		    RequestBody body = RequestBody.create(null, new byte[0]);

			Request r = new Request.Builder()
					.url(url)
					.method("PUT", body)
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
				String status = mapper.readValue(addServiceBody, Map.class).get("status").toString();
				//String status1 = mapper.readValue(addServiceBody1, Map.class).get("data").toString();
				if (status.equals("OK")) {
					dbQueryStatus = playlistDriver.unlikeSong(userName, songId);
					response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				} else {
					dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus = playlistDriver.deleteSongFromDb(songId);

		//response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}
}