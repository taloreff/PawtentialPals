package com.example.pawtentialpals.services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.Comment

class PostDao(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "posts.db"
        const val DATABASE_VERSION = 1
        const val TABLE_POSTS = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "user_name"
        const val COLUMN_USER_IMAGE = "user_image"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_POST_IMAGE = "post_image"
        const val COLUMN_MAP_IMAGE = "map_image"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_COMMENTS = "comments"

        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_POSTS + "("
                + COLUMN_ID + " TEXT PRIMARY KEY, "
                + COLUMN_USER_ID + " TEXT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_IMAGE + " TEXT, "
                + COLUMN_TIMESTAMP + " INTEGER, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_POST_IMAGE + " TEXT, "
                + COLUMN_MAP_IMAGE + " TEXT, "
                + COLUMN_LIKES + " INTEGER, "
                + COLUMN_COMMENTS + " TEXT"
                + ")")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        onCreate(db)
    }

    fun insertPost(post: PostModel) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, post.id)
        values.put(COLUMN_USER_ID, post.userId)
        values.put(COLUMN_USER_NAME, post.userName)
        values.put(COLUMN_USER_IMAGE, post.userImage)
        values.put(COLUMN_TIMESTAMP, post.timestamp)
        values.put(COLUMN_DESCRIPTION, post.description)
        values.put(COLUMN_LOCATION, post.location)
        values.put(COLUMN_POST_IMAGE, post.postImage)
        values.put(COLUMN_MAP_IMAGE, post.mapImage)
        values.put(COLUMN_LIKES, post.likes)
        values.put(COLUMN_COMMENTS, post.comments.joinToString(separator = ",") { Comment.toString(it) })

        db.insertWithOnConflict(TABLE_POSTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getAllPosts(): List<PostModel> {
        val posts = mutableListOf<PostModel>()
        val selectQuery = "SELECT * FROM $TABLE_POSTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val commentsList = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    .split(",")
                    .map { Comment.fromString(it) }

                val post = PostModel(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                    userImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_IMAGE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    postImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_IMAGE)),
                    mapImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAP_IMAGE)),
                    likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                    comments = commentsList
                )
                posts.add(post)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return posts
    }

    fun deletePost(post: PostModel) {
        val db = this.writableDatabase
        db.delete(TABLE_POSTS, "$COLUMN_ID = ?", arrayOf(post.id))
        db.close()
    }
}
