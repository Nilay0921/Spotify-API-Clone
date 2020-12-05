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
	
	public void createPlaylist(String userName) {
		Map<String, Object> insert = new HashMap<String, Object>();
		Map<String, Object> insert1 = new HashMap<String, Object>();
		insert.put("plName", userName + "-favourites");
		insert1.put("userName", userName);
		insert1.put("plName", userName + "-favourites");
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				
			}
			session.writeTransaction(tx -> tx.run("MERGE (b:playlist {plName:$plName})", 
					insert));
			session.writeTransaction(tx -> tx.run("MATCH (a:profile {userName:$userName}),"
		              + "(b:playlist {plName:$plName})\n" + 
		              "MERGE (a)-[r:created]->(b)\n" + 
		              "RETURN r", 
					insert1));
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		insert.put("userName", userName);
		insert.put("fullName", fullName);
		insert.put("password", password);
		insert.put("plName", userName + "-favourites");
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:profile {userName:$userName}) RETURN a", 
	                insertUserName);
	            if (node_boolean.hasNext()) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
	            	return status;
	            }
	            tx.success();
	        }
			session.writeTransaction(tx -> tx.run("MERGE (a:profile {userName: $userName, fullName: $fullName, password: $password})", 
					insert));
			createPlaylist(userName);
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		insertF.put("frndUserName", frndUserName);
		insert.put("userName", userName);
		insert.put("frndUserName", frndUserName);
		if (userName.equals(frndUserName)) {
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
        	return status;
		}
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:profile {userName:$userName}) RETURN a", 
	                insertUserName);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            node_boolean = tx.run("MATCH (a:profile {userName:$frndUserName}) RETURN a", 
		                insertF);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            node_boolean = tx.run("MATCH (a:profile {userName:$userName})" + 
	            		"-[r:follows]->(b:profile {userName:$frndUserName})\n"
	            		+ "RETURN r",
	                      insert);
	            if (node_boolean.hasNext()) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
	            	return status;
	              }
	            tx.success();
	        }
			session.writeTransaction(tx -> tx.run("MATCH (a:profile {userName:$userName}),"
		              + "(b:profile {userName:$frndUserName})\n" + 
		              "MERGE (a)-[r:follows]->(b)\n" + 
		              "RETURN r", 
					insert));
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		DbQueryStatus status;
		Map<String, Object> insertUserName = new HashMap<String, Object>(); 
		Map<String, Object> insertF = new HashMap<String, Object>();
		Map<String, Object> insert = new HashMap<String, Object>();
		insertUserName.put("userName", userName);
		insertF.put("frndUserName", frndUserName);
		insert.put("userName", userName);
		insert.put("frndUserName", frndUserName);
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			try (Transaction tx = session.beginTransaction()){
				
	            StatementResult node_boolean = tx.run("MATCH (a:profile {userName:$userName}) RETURN a", 
	                insertUserName);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            node_boolean = tx.run("MATCH (a:profile {userName:$frndUserName}) RETURN a", 
		                insertF);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	            }
	            node_boolean = tx.run("MATCH (a:profile {userName:$userName})" + 
	            		"-[r:follows]->(b:profile {userName:$frndUserName})\n"
	            		+ "RETURN r",
	                      insert);
	            if (!(node_boolean.hasNext())) {
	            	status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	            	return status;
	              }
	            tx.success();
	        }
			session.writeTransaction(tx -> tx.run("MATCH (a:profile {userName:$userName})"
		              + "-[r:follows]->(b:profile {userName: $frndUserName})\n" + 
		              "DELETE r", 
					insert));
			session.close();
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
			return status;
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
