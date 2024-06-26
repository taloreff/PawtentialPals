package com.example.pawtentialpals.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PostDao.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${PostDao.TABLE_POSTS}")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "pawtentialpals.db"
        private const val DATABASE_VERSION = 1
    }
}
