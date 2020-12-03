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
		
		return null;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		
		return null;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
