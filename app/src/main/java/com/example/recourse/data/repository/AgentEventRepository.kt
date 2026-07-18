package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AgentEventRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getEventsByAgentId(agentId: Long): List<AgentEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<AgentEvent>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.prepareStatement("SELECT * FROM agent_events WHERE agent_id = ? ORDER BY occurred_at DESC")
            statement.setLong(1, agentId)
            val resultSet = statement.executeQuery()
            
            while (resultSet.next()) {
                events.add(mapResultSetToEvent(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext events
    }

    suspend fun getMonthlyTotalForAgent(agentId: Long, eventType: EventType): java.math.BigDecimal = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext java.math.BigDecimal.ZERO
        return@withContext try {
            val statement = connection.prepareStatement(
                "SELECT SUM(amount) FROM agent_events WHERE agent_id = ? AND event_type = ? AND occurred_at >= DATE_FORMAT(NOW() ,'%Y-%m-01')"
            )
            statement.setLong(1, agentId)
            statement.setString(2, eventType.name)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) resultSet.getBigDecimal(1) ?: java.math.BigDecimal.ZERO else java.math.BigDecimal.ZERO
        } catch (e: Exception) {
            e.printStackTrace()
            java.math.BigDecimal.ZERO
        } finally {
            connection.close()
        }
    }

    suspend fun insertSimulatedAnomaly(agentId: Long, amount: java.math.BigDecimal, type: EventType): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext false
        return@withContext try {
            val statement = connection.prepareStatement(
                "INSERT INTO agent_events (agent_id, event_type, amount, currency, is_anomalous, metadata, occurred_at) " +
                "VALUES (?, ?, ?, 'USD', 1, ?, NOW())"
            )
            statement.setLong(1, agentId)
            statement.setString(2, type.name)
            statement.setBigDecimal(3, amount)
            statement.setString(4, "{\"simulated\": true, \"note\": \"Edge case scenario trigger\"}")
            val rows = statement.executeUpdate()
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.close()
        }
    }

    suspend fun getEventById(id: Long): AgentEvent? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM agent_events WHERE id = ?")
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                mapResultSetToEvent(resultSet)
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

    private fun mapResultSetToEvent(rs: ResultSet): AgentEvent {
        return AgentEvent(
            id = rs.getLong("id"),
            agentId = rs.getLong("agent_id"),
            eventType = EventType.valueOf(rs.getString("event_type")),
            amount = rs.getBigDecimal("amount"),
            currency = rs.getString("currency"),
            isAnomalous = rs.getInt("is_anomalous") == 1,
            metadata = rs.getString("metadata"),
            occurredAt = parseDateTime(rs.getString("occurred_at")) ?: LocalDateTime.now(),
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
