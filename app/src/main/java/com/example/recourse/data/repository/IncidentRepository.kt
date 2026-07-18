package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IncidentRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getAllIncidents(): List<Incident> = withContext(Dispatchers.IO) {
        val incidents = mutableListOf<Incident>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM incidents")
            
            while (resultSet.next()) {
                incidents.add(mapResultSetToIncident(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext incidents
    }

    suspend fun getAverageResolutionTime(companyId: Long): Double = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext 0.0
        return@withContext try {
            // Join incidents with ai_agents to filter by company_id
            val query = """
                SELECT AVG(TIMESTAMPDIFF(HOUR, detected_at, resolved_at)) / 24.0 
                FROM incidents i
                JOIN ai_agents a ON i.agent_id = a.id
                WHERE a.company_id = ? AND i.status = 'RESOLVED' AND i.resolved_at IS NOT NULL
            """
            val statement = connection.prepareStatement(query)
            statement.setLong(1, companyId)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) resultSet.getDouble(1) else 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        } finally {
            connection.close()
        }
    }

    suspend fun updateIncidentStatus(incidentId: Long, newStatus: IncidentStatus): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext false
        
        return@withContext try {
            val statement = connection.prepareStatement("UPDATE incidents SET status = ? WHERE id = ?")
            statement.setString(1, newStatus.name)
            statement.setLong(2, incidentId)
            val rowsAffected = statement.executeUpdate()
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.close()
        }
    }

    suspend fun getIncidentById(id: Long): Incident? = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext null
        
        return@withContext try {
            val statement = connection.prepareStatement("SELECT * FROM incidents WHERE id = ?")
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                mapResultSetToIncident(resultSet)
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

    private fun mapResultSetToIncident(rs: ResultSet): Incident {
        return Incident(
            id = rs.getLong("id"),
            agentId = rs.getLong("agent_id"),
            triggeringEventId = rs.getLong("triggering_event_id").takeIf { !rs.wasNull() },
            severity = Severity.valueOf(rs.getString("severity")),
            status = IncidentStatus.valueOf(rs.getString("status")),
            description = rs.getString("description"),
            financialImpact = rs.getBigDecimal("financial_impact"),
            currency = rs.getString("currency"),
            detectedAt = parseDateTime(rs.getString("detected_at")) ?: LocalDateTime.now(),
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
