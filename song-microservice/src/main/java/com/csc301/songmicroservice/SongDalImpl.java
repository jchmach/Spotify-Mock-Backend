package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.FindIterable;

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
			if (!this.db.collectionExists("songs")) {
				this.db.createCollection("songs");
			}
			this.db.getCollection("songs").insertOne(doc);
			DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			song.put("id", doc.get("_id").toString());
			result.setData(song);
			return result;			
		}
		catch(Exception e) {
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
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
		try {
			Document query = new Document().append("_id", new ObjectId(songId));
			FindIterable<Document> results = this.db.getCollection("songs").find(query);
			if (!results.iterator().hasNext()) {
				return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			DbQueryStatus queryResult = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			queryResult.setData(results.first().get("songName"));
			return queryResult;
		}
		catch(Exception e) {
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		try {
			Document query = new Document().append("_id", new ObjectId(songId));
	        Document result = db.getCollection("songs").findOneAndDelete(query);
	        if (result != null) {
	        	return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
	        }
	        else {
	        	return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	        }

		}
		catch(Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		try {
			Document query = new Document().append("_id", new ObjectId(songId));
			FindIterable<Document> results = this.db.getCollection("songs").find(query);
			if (!results.iterator().hasNext()) {
				return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			String strFavourites = (String) results.first().get("songAmountFavourites");
			long favourites = Long.parseLong(strFavourites);
			if (shouldDecrement) {
				favourites -= 1;
			}
			else {
				favourites += 1;
			}
			this.db.getCollection("songs").updateOne(query, new Document("$set", new Document("songAmountFavourites", Long.toString(favourites))));
			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			
		}
		catch(Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}
}