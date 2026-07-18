package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PolicyThresholdRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getThresholdsByPolicyId(policyId: Long): List<PolicyThreshold> = withContext(Dispatchers.IO) {
        val thresholds = mutableListOf<PolicyThreshold>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.prepareStatement("SELECT * FROM policy_thresholds WHERE policy_id = ?")
            statement.setLong(1, policyId)
            val resultSet = statement.executeQuery()
            
            while (resultSet.next()) {
                thresholds.add(mapResultSetToThreshold(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext thresholds
    }

    suspend fun getThresholdForEvent(policyId: Long, eventType: EventType): PolicyThreshold? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM policy_thresholds WHERE policy_id = ? AND trigger_type = ?")
            statement.setLong(1, policyId)
            statement.setString(2, eventType.name)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) mapResultSetToThreshold(resultSet) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.close()
        }
    }

    private fun mapResultSetToThreshold(rs: ResultSet): PolicyThreshold {
        return PolicyThreshold(
            id = rs.getLong("id"),
            policyId = rs.getLong("policy_id"),
            triggerType = EventType.valueOf(rs.getString("trigger_type")),
            thresholdValue = rs.getBigDecimal("threshold_value"),
            thresholdUnit = ThresholdUnit.valueOf(rs.getString("threshold_unit")),
            description = rs.getString("description"),
            createdAt = parseDateTime(rs.getString("created_at")) ?: LocalDateTime.now()
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
