package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClaimRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getAllClaims(): List<Claim> = withContext(Dispatchers.IO) {
        val claims = mutableListOf<Claim>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM claims")
            
            while (resultSet.next()) {
                claims.add(mapResultSetToClaim(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext claims
    }

    suspend fun getClaimByIncidentId(incidentId: Long): Claim? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM claims WHERE incident_id = ?")
            statement.setLong(1, incidentId)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) mapResultSetToClaim(resultSet) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.close()
        }
    }

    suspend fun createClaim(policyId: Long, incidentId: Long, amount: java.math.BigDecimal, currency: String): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext false
        val claimNumber = "CLM-${System.currentTimeMillis() % 1000000}"
        
        return@withContext try {
            val statement = connection.prepareStatement(
                "INSERT INTO claims (policy_id, incident_id, claim_number, status, amount_claimed, currency, opened_at) " +
                "VALUES (?, ?, ?, 'OPENED', ?, ?, NOW())"
            )
            statement.setLong(1, policyId)
            statement.setLong(2, incidentId)
            statement.setString(3, claimNumber)
            statement.setBigDecimal(4, amount)
            statement.setString(5, currency)
            val rowsAffected = statement.executeUpdate()
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.close()
        }
    }

    private fun mapResultSetToClaim(rs: ResultSet): Claim {
        return Claim(
            id = rs.getLong("id"),
            policyId = rs.getLong("policy_id"),
            incidentId = rs.getLong("incident_id"),
            claimNumber = rs.getString("claim_number"),
            status = ClaimStatus.valueOf(rs.getString("status")),
            amountClaimed = rs.getBigDecimal("amount_claimed"),
            amountPaid = rs.getBigDecimal("amount_paid"),
            currency = rs.getString("currency"),
            openedAt = parseDateTime(rs.getString("opened_at")) ?: LocalDateTime.now(),
            resolvedAt = rs.getString("resolved_at")?.let { parseDateTime(it) },
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
