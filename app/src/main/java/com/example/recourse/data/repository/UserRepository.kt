package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getUserById(id: Long): User? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                mapResultSetToUser(resultSet)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.close()
        }
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")
            statement.setString(1, email)
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                mapResultSetToUser(resultSet)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.close()
        }
    }

    private fun mapResultSetToUser(rs: ResultSet): User {
        return User(
            id = rs.getLong("id"),
            companyId = rs.getLong("company_id"),
            fullName = rs.getString("full_name"),
            email = rs.getString("email"),
            role = UserRole.valueOf(rs.getString("role")),
            isActive = rs.getInt("is_active") == 1,
            createdAt = parseDateTime(rs.getString("created_at")) ?: LocalDateTime.now(),
            updatedAt = parseDateTime(rs.getString("updated_at")) ?: LocalDateTime.now()
        )
    }

    private fun parseDateTime(dateTimeStr: String?): LocalDateTime? {
        return try {
            dateTimeStr?.let { LocalDateTime.parse(it.substring(0, 19), formatter) }
        } catch (e: Exception) {
            null
        }
    }
}
