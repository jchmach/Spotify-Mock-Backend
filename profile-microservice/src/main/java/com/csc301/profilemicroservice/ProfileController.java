package com.csc301.profilemicroservice;

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
		if (params.get("userName") == null || params.get("fullName") == null || params.get("password") == null) {
			response.put("status", "ERROR");
			return response;
		}
		else {
			DbQueryStatus result = this.profileDriver.createUserProfile(params.get("userName"),params.get("fullName"), params.get("password"));
			if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
				response.put("status", "OK");
			}
			else {
				response.put("status", "ERROR");
			}
		}
		return response;
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus result = this.profileDriver.followFriend(userName, friendUserName);
		if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
			response.put("status", "OK");
		}
		else {
			response.put("status", "ERROR");
		}
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return null;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus result = this.profileDriver.unfollowFriend(userName, friendUserName);
		if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
			response.put("status", "OK");
		}
		else {
			response.put("status", "ERROR");
		}
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus result = this.playlistDriver.likeSong(userName, songId);
		if (result.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
			HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/updateSongFavouritesCount/" + songId).newBuilder();
			urlBuilder.addQueryParameter("shouldDecrement", "false");
			String url = urlBuilder.build().toString();
			
			System.out.println(url);
		    RequestBody body = RequestBody.create(null, new byte[0]);

			Request req = new Request.Builder()
					.url(url)
					.method("PUT", body)
					.build();

			Call call = client.newCall(req);
			Response responseFromIncrement = null;


			
			try {
				responseFromIncrement = call.execute();
				response.put("status", "OK");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.put("status", "ERROR");
			}

		}
		else {
			response.put("status", "ERROR");
		}
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		return null;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return null;
	}
	
	@RequestMapping(value = "/addSong/{songId}", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@PathVariable("songId") String songId,
			HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus result = this.playlistDriver.addSong(songId);
		response.put("status", result.getMessage());
		return response;
	}
	
}