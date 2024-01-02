package com.example.shopease

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "shopease.db"

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable =
            "CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_EMAIL TEXT, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT)"

        db?.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Upgrade policy if needed in the future
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT 1 FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?", arrayOf(username))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT 1 FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun saveUserData(email: String, username: String, password: String) {
        // Save user data in the database
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }

        db.insert(TABLE_USERS, null, values)
        db.close()
    }

    fun isValidLogin(username: String, password: String): Boolean {
        // Check if the username and password match a user in the database
        val db = readableDatabase
        val query =
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(username, password))
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    fun getEmailByUsername(username: String): String? {
        val db = readableDatabase
        val columns = arrayOf(COLUMN_EMAIL)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        val cursor: Cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)

        var email: String? = null

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_EMAIL)
            email = cursor.getString(columnIndex)
        }

        cursor.close()
        db.close()

        return email
    }

    fun updatePassword(username: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PASSWORD, newPassword)

        // Update the password based on the username
        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USERNAME = ?",
            arrayOf(username)
        )

        db.close()

        // Return true if the password was successfully updated
        return rowsAffected > 0
    }
}
