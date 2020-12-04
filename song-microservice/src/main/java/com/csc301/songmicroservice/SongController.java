package com.csc301.songmicroservice;

import org.json.JSONObject;
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
import okhttp3.RequestBody;
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
		DbQueryStatus result = this.songDal.getSongTitleById(songId);
		if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
			response.put("data", result.getData());
		}
		response.put("status", result.getMessage());
		return response;
	}

	
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		DbQueryStatus result = this.songDal.deleteSongById(songId);
		if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {

			String url = "http://localhost:3002/deleteAllSongsFromDb/" + songId;
			
		    RequestBody body = RequestBody.create(null, new byte[0]);

			Request req = new Request.Builder()
					.url(url)
					.method("PUT", body)
					.build();
		
			Call call = client.newCall(req);
			Response responseFromDeleteSong = null;

			String addSongBody = "{}";

			try {
				responseFromDeleteSong = call.execute();
				response.put("status", "OK");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		else {
			response.put("status", "ERROR");
		}
		return response;
	}

	
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		if (params.get("songName") == null || params.get("songArtistFullName") == null || params.get("songAlbum") == null) {
			response.put("status", "ERROR");
			return response;
		}
		else {
			Song temp = new Song(params.get("songName"), params.get("songArtistFullName"), params.get("songAlbum"));
			temp.setSongAmountFavourites(0);
			
			
			DbQueryStatus result = this.songDal.addSong(temp);
			if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
				response.put("data", ((JSONObject)result.getData()).toMap());				
			}
			String id = (String) ((JSONObject)result.getData()).get("id");
			String url = "http://localhost:3002/addSong/" + id;
			
			System.out.println(url);
		    RequestBody body = RequestBody.create(null, new byte[0]);

			Request req = new Request.Builder()
					.url(url)
					.method("POST", body)
					.build();
		
			Call call = client.newCall(req);
			Response responseFromAddSong = null;

			String addSongBody = "{}";

			try {
				responseFromAddSong = call.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}

				
			response.put("status", result.getMessage());
			return response;
		}
	}

	
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("data", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus result = this.songDal.updateSongFavouritesCount(songId, shouldDecrement.equals("true"));
		response.put("status", result.getMessage());
		return response;
	}
}