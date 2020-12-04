package com.csc301.profilemicroservice;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		try (Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("userName", userName);
        	params.put("fullName", fullName);
        	params.put("password", password);
        	StatementResult result = session.readTransaction(tx -> tx.run("MATCH (n:profile {userName: $userName, fullName: $fullName, password: $password}) RETURN n", params));
        	if(result.hasNext()) {
        		return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
        	}
        	session.writeTransaction(tx -> tx.run("MERGE (n:profile {userName: $userName, fullName: $fullName, password: $password})", params));
        	Map<String, Object> playlistParams = new HashMap<>();
        	String temp = userName + "-favorites";
        	params.put("plName", temp);
        	playlistParams.put("plName", temp);
        	session.writeTransaction(tx -> tx.run("MERGE (n:playlist {plName: $plName})", params));
			session.writeTransaction(tx -> tx.run("MATCH (p:playlist{plName: $plName})," + "(n:profile{userName: $userName, fullName: $fullName, password: $password}) \n" + "MERGE (n)-[:created]->(p) \n", 
					params));
			session.close();
			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        
		}
		catch(Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);

		}
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		try (Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("userName", userName);
        	params.put("friendName", frndUserName);
        	StatementResult user = session.readTransaction(tx -> tx.run("MATCH (n:profile {userName: $userName}) RETURN n", params));
        	StatementResult friend = session.readTransaction(tx -> tx.run("MATCH (n:profile {userName: $friendName}) RETURN n", params));
        	StatementResult relation = session.readTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName})-[r:follows]->(f:profile{userName: $friendName}) RETURN r", params));
        	
        	if (!user.hasNext() || !friend.hasNext() || relation.hasNext() || userName.equals(frndUserName)) {
        		return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        	}
			session.writeTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName})," + "(f:profile{userName: $friendName}) \n" + "MERGE (p)-[:follows]->(f) \n", 
					params));       
			session.close();
			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		try (Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("userName", userName);
        	params.put("friendName", frndUserName);
        	StatementResult user = session.readTransaction(tx -> tx.run("MATCH (n:profile {userName: $userName}) RETURN n", params));
        	StatementResult friend = session.readTransaction(tx -> tx.run("MATCH (n:profile {userName: $friendName}) RETURN n", params));
        	StatementResult relation = session.readTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName})-[r:follows]->(f:profile{userName: $friendName}) RETURN r", params));
        	if (!user.hasNext() || !friend.hasNext() || !relation.hasNext()) {
        		return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        	}
			session.writeTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName})-[r:follows]->(f:profile{userName: $friendName}) \n" + "DELETE r", 
					params));       
			session.close();
			System.out.println("wrong here");
			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		try(Session session = driver.session()){
        	Map<String, Object> params = new HashMap<>();
        	params.put("userName", userName);
        	params.put("plName", "");
        	ArrayList<String> ids = null;
        	StatementResult exists = session.readTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName}) RETURN p", params));
        	if (!exists.hasNext()) {
        		return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        	}
        	StatementResult result = session.readTransaction(tx -> tx.run("MATCH (p:profile{userName: $userName})-[:follows]->(f) RETURN f", params));
        	Map<String, Object> following = new HashMap<>();
        	while(result.hasNext()) {
        		 ids = new ArrayList<String>();
        		 Record profile = result.next();
        		 params.replace("plName", profile.get("f").get("userName").asString() + "-favorites");
        		 
        		 StatementResult songs = session.readTransaction(tx -> tx.run("MATCH (p:playlist{plName: $plName})-[:includes]->(s) RETURN s", params));
        		 while (songs.hasNext()) {
        			 System.out.println("ran here");
        			 ids.add(songs.next().get("s").get("songId").asString());
        		 }
        		 following.put(profile.get("f").get("userName").asString(), ids);
        	}
        	DbQueryStatus response = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        	response.setData(following);
        	return response;
		}
		catch(Exception e) {
			e.printStackTrace();
			return new DbQueryStatus("ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);			
		}
	}
}
