package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PolicyRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getAllPolicies(): List<Policy> = withContext(Dispatchers.IO) {
        val policies = mutableListOf<Policy>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM policies")
            
            while (resultSet.next()) {
                policies.add(mapResultSetToPolicy(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext policies
    }

    private fun mapResultSetToPolicy(rs: ResultSet): Policy {
        return Policy(
            id = rs.getLong("id"),
            companyId = rs.getLong("company_id"),
            policyNumber = rs.getString("policy_number"),
            coverageLimit = rs.getBigDecimal("coverage_limit"),
            premiumAnnual = rs.getBigDecimal("premium_annual"),
            currency = rs.getString("currency"),
            status = PolicyStatus.valueOf(rs.getString("status")),
            effectiveDate = rs.getString("effective_date"),
            expirationDate = rs.getString("expiration_date"),
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
