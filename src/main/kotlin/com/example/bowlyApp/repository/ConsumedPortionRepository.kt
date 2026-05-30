package com.example.bowlyApp.repository

import com.example.bowlyApp.model.ConsumedPortion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ConsumedPortionRepository : JpaRepository<ConsumedPortion, Long> {
    fun findByUserIdAndMealDate(userId: Long, mealDate: LocalDate): List<ConsumedPortion>
}
