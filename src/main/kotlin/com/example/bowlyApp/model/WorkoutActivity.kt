package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "workout_activities")
data class WorkoutActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "activity_date", nullable = false)
    val activityDate: LocalDate,

    @Column(nullable = false)
    var name: String,

    @Column(name = "calories_burned", nullable = false)
    var caloriesBurned: Double,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
