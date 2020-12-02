package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
		try{
			JSONObject song = new JSONObject().put("songName", songToAdd.getSongName()).put("songArtistFullName", songToAdd.getSongArtistFullName()).put("songAlbum", songToAdd.getSongAlbum())
					.put("songAmountFavourites", Long.toString(songToAdd.getSongAmountFavourites()));
			
			Document doc = Document.parse(song.toString());
			if (!db.collectionExists("songs")) {
				db.createCollection("songs");
			}
			db.getCollection("songs").insertOne(doc);
			DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			song.put("id", doc.get("_id").toString());
			result.setData(song);
			return result;			
		}
		catch(Exception e) {
			return new DbQueryStatus("NOT OK", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		return null;
	}
}