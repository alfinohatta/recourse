package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CompanyRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getCompanyById(id: Long): Company? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM companies WHERE id = ?")
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                mapResultSetToCompany(resultSet)
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

    private fun mapResultSetToCompany(rs: ResultSet): Company {
        return Company(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            industry = rs.getString("industry"),
            countryCode = rs.getString("country_code"),
            regulatoryRegion = RegulatoryRegion.valueOf(rs.getString("regulatory_region")),
            websiteUrl = rs.getString("website_url"),
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
