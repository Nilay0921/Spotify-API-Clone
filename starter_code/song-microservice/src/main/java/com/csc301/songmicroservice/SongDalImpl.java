package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
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
		Document doc = new Document()
				.append("songName", songToAdd.getSongName())
				.append("songArtistFullName", songToAdd.getSongArtistFullName())
				.append("songAlbum", songToAdd.getSongAlbum())
			    .append("songAmountFavourites", songToAdd.getSongAmountFavourites());
		//System.out.println(songToAdd.getId());
		//doc.append("id", songToAdd.getId());
		db.getCollection("songs").insertOne(doc);
		songToAdd.setId(doc.getObjectId("_id"));
		DbQueryStatus status = new DbQueryStatus(songToAdd.getJsonRepresentation().toString(), DbQueryExecResult.QUERY_OK);	
		return status;
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