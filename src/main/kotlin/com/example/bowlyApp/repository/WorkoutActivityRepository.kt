package com.example.bowlyApp.repository

import com.example.bowlyApp.model.WorkoutActivity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface WorkoutActivityRepository : JpaRepository<WorkoutActivity, Long> {
    fun findByUserIdAndActivityDate(userId: Long, activityDate: LocalDate): List<WorkoutActivity>
}
