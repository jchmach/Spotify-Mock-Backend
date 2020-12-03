package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

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

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		try (Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("plName", userName + "-favorites");
        	params.put("songId", songId);
			session.writeTransaction(tx -> tx.run("MATCH (p:playlist{plName: $plName})," + "(s:song{songId: $songId}) \n" + "MERGE (p)-[:includes]->(s) \n", 
					params)); 
			session.close();
			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);

		}
		catch(Exception e) {
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		
		return null;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		return null;
	}
	
	public DbQueryStatus addSong(String songId) {
		try (Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("songId", songId);
        	session.writeTransaction(tx -> tx.run("MERGE (n:song {songId: $songId})", params));
        	session.close();
        	return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		}
		catch (Exception e) {
			e.printStackTrace();
        	return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}
}
