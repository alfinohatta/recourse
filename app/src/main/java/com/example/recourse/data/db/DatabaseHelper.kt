package com.example.recourse.data.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseHelper {
    // Standard config for local MySQL in emulator
    // SANITIZATION: Replace with your actual database credentials
    private const val DB_URL = "jdbc:mysql://10.0.2.2:3306/recourse_ai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    private const val USER = "YOUR_DB_USER"
    private const val PASS = "YOUR_DB_PASSWORD"

    init {
        try {
            // Load driver explicitly for older Android versions
            Class.forName("com.mysql.cj.jdbc.Driver")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Set login timeout to prevent long hangs
            DriverManager.setLoginTimeout(5)
            DriverManager.getConnection(DB_URL, USER, PASS)
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }
}
