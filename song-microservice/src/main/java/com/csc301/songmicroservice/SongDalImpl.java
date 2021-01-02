package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
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
		DbQueryStatus status;
		if (songToAdd.getSongName() == null || songToAdd.getSongArtistFullName() == null || 
				songToAdd.getSongAlbum() == null) {
			status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
			return status;
		}
		Document doc = new Document()
				.append("songName", songToAdd.getSongName())
				.append("songArtistFullName", songToAdd.getSongArtistFullName())
				.append("songAlbum", songToAdd.getSongAlbum())
			    .append("songAmountFavourites", songToAdd.getSongAmountFavourites());
		db.getCollection("songs").insertOne(doc);
		songToAdd.setId(doc.getObjectId("_id"));
		status = new DbQueryStatus(songToAdd.getJsonRepresentation().toString(), DbQueryExecResult.QUERY_OK);
		status.setData(songToAdd.getJsonRepresentation());
		return status;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		DbQueryStatus status;
		if (ObjectId.isValid(songId)) {
			if (db.getCollection("songs").find(new Document().append("_id", new ObjectId(songId))).first() != null) {
				FindIterable<Document> documents = db.getCollection("songs")
			            .find(new Document()
			            .append("_id", new ObjectId(songId)));
				status = new DbQueryStatus(documents.first().toJson(), DbQueryExecResult.QUERY_OK);
				return status;
			}
		}
		status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return status;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		DbQueryStatus status;
		if (ObjectId.isValid(songId)) {
			if (db.getCollection("songs").find(new Document().append("_id", new ObjectId(songId))).first() != null) {
				FindIterable<Document> documents = db.getCollection("songs")
			            .find(new Document()
			            .append("_id", new ObjectId(songId)));
				status = new DbQueryStatus(documents.first().getString("songName"), DbQueryExecResult.QUERY_OK);
				return status;
			}
		}
		status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return status;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		DbQueryStatus status;
		if (ObjectId.isValid(songId)) {
			if (db.getCollection("songs").find(new Document().append("_id", new ObjectId(songId))).first() != null) {
				db.getCollection("songs").deleteOne(new Document("_id", new ObjectId(songId)));
				status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
				return status;
			}
		}
		status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return status;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		DbQueryStatus status;
		if (ObjectId.isValid(songId)) {
			if (db.getCollection("songs").find(new Document().append("_id", new ObjectId(songId))).first() != null) {
				FindIterable<Document> documents = db.getCollection("songs")
			            .find(new Document()
			            .append("_id", new ObjectId(songId)));
				Long likes = documents.first().getLong("songAmountFavourites");
				if (shouldDecrement && likes - 1 >= 0) {
					likes--;
				} else if (shouldDecrement && likes - 1 < 0) {
					status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_GENERIC);
					return status;
				} else {
					likes++;
				}
				db.getCollection("songs").updateOne(new BasicDBObject("_id", new ObjectId(songId)), new BasicDBObject("$set", new BasicDBObject("songAmountFavourites", likes)));
				status = new DbQueryStatus(null, DbQueryExecResult.QUERY_OK);
				return status;
			}
		}
		status = new DbQueryStatus(null, DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return status;
	}
}