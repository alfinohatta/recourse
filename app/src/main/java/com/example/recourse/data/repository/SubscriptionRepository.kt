package com.example.recourse.data.repository

import com.example.recourse.data.db.DatabaseHelper
import com.example.recourse.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SubscriptionRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun getSubscriptionsByCompanyId(companyId: Long): List<Subscription> = withContext(Dispatchers.IO) {
        val subscriptions = mutableListOf<Subscription>()
        val connection = DatabaseHelper.getConnection() ?: return@withContext emptyList()
        
        try {
            val statement = connection.prepareStatement("SELECT * FROM subscriptions WHERE company_id = ?")
            statement.setLong(1, companyId)
            val resultSet = statement.executeQuery()
            
            while (resultSet.next()) {
                subscriptions.add(mapResultSetToSubscription(resultSet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.close()
        }
        
        return@withContext subscriptions
    }

    suspend fun purchaseSubscription(companyId: Long, tier: SubscriptionTier, monthlyFee: java.math.BigDecimal): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseHelper.getConnection() ?: return@withContext false
        
        return@withContext try {
            val statement = connection.prepareStatement(
                "INSERT INTO subscriptions (company_id, tier, status, monthly_fee, start_date) " +
                "VALUES (?, ?, 'ACTIVE', ?, CURDATE())"
            )
            statement.setLong(1, companyId)
            statement.setString(2, tier.name)
            statement.setBigDecimal(3, monthlyFee)
            val rowsAffected = statement.executeUpdate()
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.close()
        }
    }

    private fun mapResultSetToSubscription(rs: ResultSet): Subscription {
        return Subscription(
            id = rs.getLong("id"),
            companyId = rs.getLong("company_id"),
            tier = SubscriptionTier.valueOf(rs.getString("tier")),
            status = SubscriptionStatus.valueOf(rs.getString("status")),
            monthlyFee = rs.getBigDecimal("monthly_fee"),
            oneTimeFee = rs.getBigDecimal("one_time_fee"),
            startDate = rs.getString("start_date"),
            endDate = rs.getString("end_date"),
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
