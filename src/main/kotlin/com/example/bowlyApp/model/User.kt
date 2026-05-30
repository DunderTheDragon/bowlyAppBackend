package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val role: String = "USER",

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    // Profile
    @Column(nullable = true)
    var gender: String? = "MALE",
    @Column(nullable = true)
    var age: java.lang.Integer? = java.lang.Integer(25),
    @Column(name = "height_cm", nullable = true)
    var heightCm: java.lang.Double? = java.lang.Double(180.0),
    @Column(name = "weight_kg", nullable = true)
    var weightKg: java.lang.Double? = java.lang.Double(80.0),
    @Column(name = "target_weight_kg", nullable = true)
    var targetWeightKg: java.lang.Double? = java.lang.Double(75.0),
    @Column(name = "weekly_change_rate_kg", nullable = true)
    var weeklyChangeRateKg: java.lang.Double? = java.lang.Double(0.5),
    @Column(name = "activity_level", nullable = true)
    var activityLevel: java.lang.Double? = java.lang.Double(1.375),
    
    // Proporcje makro (precyzyjne wartości procentowe, suma = 100.0)
    @Column(name = "protein_ratio", nullable = true)
    var proteinRatio: java.lang.Double? = java.lang.Double(30.0),
    @Column(name = "fat_ratio", nullable = true)
    var fatRatio: java.lang.Double? = java.lang.Double(30.0),
    @Column(name = "carbs_ratio", nullable = true)
    var carbsRatio: java.lang.Double? = java.lang.Double(40.0),

    // Ustawienia widoku
    @Column(name = "is_dark_theme", nullable = true)
    var isDarkTheme: java.lang.Boolean? = null,
    @Column(name = "show_batch_onboarding", nullable = true)
    var showBatchOnboarding: java.lang.Boolean? = java.lang.Boolean(true)
)