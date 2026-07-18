package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AiAgentRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getAllAgents(): List<AiAgent> = withContext(Dispatchers.IO) {
        val agents = mutableListOf<AiAgent>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM ai_agents")
            
            while (resultSet.next()) {
                agents.add(mapResultSetToAiAgent(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext agents
    }

    suspend fun getAgentCountByCompanyId(companyId: Long): Int = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext 0
        return@withContext try {
            val statement = connection.prepareStatement("SELECT COUNT(*) FROM ai_agents WHERE company_id = ?")
            statement.setLong(1, companyId)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) resultSet.getInt(1) else 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            connection.close()
        }
    }

    suspend fun getAverageGuardrailScore(companyId: Long): Double = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext 0.0
        return@withContext try {
            val statement = connection.prepareStatement("SELECT AVG(guardrail_score) FROM ai_agents WHERE company_id = ?")
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

    suspend fun updateAgentStatus(agentId: Long, newStatus: AgentStatus): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext false
        
        return@withContext try {
            val statement = connection.prepareStatement("UPDATE ai_agents SET status = ? WHERE id = ?")
            statement.setString(1, newStatus.name)
            statement.setLong(2, agentId)
            val rowsAffected = statement.executeUpdate()
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.close()
        }
    }

    private fun mapResultSetToAiAgent(rs: ResultSet): AiAgent {
        val agentType = AgentType.valueOf(rs.getString("agent_type"))
        return AiAgent(
            id = rs.getLong("id"),
            companyId = rs.getLong("company_id"),
            name = rs.getString("name"),
            agentType = agentType,
            autonomyLevel = AutonomyLevel.valueOf(rs.getString("autonomy_level")),
            status = AgentStatus.valueOf(rs.getString("status")),
            guardrailScore = rs.getDouble("guardrail_score").takeIf { !rs.wasNull() },
            connectedPlatform = when(agentType) {
                AgentType.REFUND -> "Stripe / Zendesk"
                AgentType.MESSAGING -> "Intercom / Twilio"
                AgentType.PRICING -> "Shopify / Magento"
                else -> "Custom API"
            },
            connectedAt = rs.getString("connected_at")?.let { parseDateTime(it) },
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
