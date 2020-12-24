package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	
	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);

		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus;
		
		dbQueryStatus = songDal.getSongTitleById(songId);
		
		if (dbQueryStatus.getMessage() != null) {
			response.put("data", dbQueryStatus.getMessage());
		}
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		
		ObjectMapper mapper = new ObjectMapper();
		DbQueryStatus dbQueryStatus = songDal.deleteSongById(songId);
		
		//int s = playlistDriver.checkSong(userName, songId);
		/*if (s == 1) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}*/
		
		//String path = String.format("GET http://localhost:3001/getSongById/songId", songId);

		if (String.valueOf(songId) != null) {
			HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3002/deleteAllSongsFromDb/" + songId).newBuilder();
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
					.method("DELETE", null)
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
				//songName = mapper.readValue(addServiceBody, Map.class).get("data").toString();
				//String status1 = mapper.readValue(addServiceBody1, Map.class).get("data").toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//response.put("data", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus;
		
		if (params.get("songName") == null || params.get("songArtistFullName") == null || params.get("songAlbum") == null) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		
		Song songToAdd = new Song(params.get("songName"), params.get("songArtistFullName"), 
				params.get("songAlbum"));
		dbQueryStatus = songDal.addSong(songToAdd);

		response.put("data", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("data", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus;
		//System.out.println(shouldDecrement);
		if (shouldDecrement.equals("")) {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		
		boolean sd;
		if (shouldDecrement.equals("true")) {
			sd = true;
		} else if (shouldDecrement.equals("false")) {
			sd = false;
		} else {
			dbQueryStatus = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		
		dbQueryStatus = songDal.updateSongFavouritesCount(songId, sd);

		//response.put("data", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;

	}
}