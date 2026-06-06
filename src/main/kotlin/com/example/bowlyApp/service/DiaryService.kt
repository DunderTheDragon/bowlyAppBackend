package com.example.bowlyApp.service

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.model.ConsumedPortion
import com.example.bowlyApp.repository.ConsumedPortionRepository
import com.example.bowlyApp.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.round

@Service
class DiaryService(
    private val consumedPortionRepository: ConsumedPortionRepository,
    private val userRepository: UserRepository,
    private val productService: ProductService,
    private val workoutService: WorkoutService
) {
    private val logger = LoggerFactory.getLogger(DiaryService::class.java)

    @Transactional
    fun consumeProduct(username: String, request: ConsumeProductRequest) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val product = productService.findOrCreateProduct(request.product)

        // Krok 2: Stwórz wpis w dzienniku
        val portion = ConsumedPortion(
            user = user,
            product = product,
            consumedWeightG = request.weightG,
            mealDate = LocalDate.parse(request.mealDate),
            mealType = request.mealType
        )

        consumedPortionRepository.save(portion)
        logger.info("User '${user.username}' consumed ${request.weightG}g of '${product.name}'")
    }

    @Transactional
    fun deleteConsumedMeal(username: String, portionId: Long) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")
            
        val portion = consumedPortionRepository.findById(portionId)
            .orElseThrow { IllegalArgumentException("Portion not found") }
            
        if (portion.user.id != user.id) {
            throw IllegalArgumentException("You can only delete your own meals")
        }
        
        // Zwracamy na "Patelnię" zjedzoną ilość, jeśli to pochodziło z patelni
        portion.segment?.let { segment ->
            segment.currentWeightG += portion.consumedWeightG
            segment.batchMeal.isDepleted = false // Oznaczamy z powrotem jako aktywne, jeśli ktoś coś wyrzucił z dziennika
        }

        consumedPortionRepository.delete(portion)
    }

    @Transactional
    fun updateConsumedMeal(username: String, portionId: Long, request: ConsumeProductRequest) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val portion = consumedPortionRepository.findById(portionId)
            .orElseThrow { IllegalArgumentException("Portion not found") }

        if (portion.user.id != user.id) {
            throw IllegalArgumentException("You can only update your own meals")
        }

        // Zwracamy i korygujemy patelnię
        portion.segment?.let { segment ->
            val weightDifference = request.weightG - portion.consumedWeightG
            if (segment.currentWeightG - weightDifference < 0) {
                 throw IllegalArgumentException("Not enough weight left in the segment to increase portion")
            }
            segment.currentWeightG -= weightDifference
            
            if (segment.batchMeal.segments.all { it.currentWeightG <= 0.0 }) {
                segment.batchMeal.isDepleted = true
            } else {
                segment.batchMeal.isDepleted = false
            }
        }

        portion.consumedWeightG = request.weightG
        portion.mealType = request.mealType
        
        consumedPortionRepository.save(portion)
    }


    @Transactional(readOnly = true)
    fun getDailySummary(username: String, date: LocalDate): DailySummaryDto {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val portions = consumedPortionRepository.findByUserIdAndMealDate(user.id, date)

        val portionsDto = portions.map { portion ->
            val segment = portion.segment
            val product = portion.product
            
            var kcal = 0.0
            var protein = 0.0
            var fat = 0.0
            var carbs = 0.0

            // Przeliczanie dla patelni
            if (segment != null) {
                val basis = portion.weightBasisG ?: segment.initialWeightG
                if (basis > 0) {
                    val ratio = portion.consumedWeightG / basis
                    kcal = segment.totalKcal * ratio
                    protein = segment.totalProtein * ratio
                    fat = segment.totalFat * ratio
                    carbs = segment.totalCarbs * ratio
                }
            }
            // Przeliczanie dla "zwykłego" produktu w 100g
            else if (product != null) {
                val ratio = portion.consumedWeightG / 100.0
                kcal = product.kcalPer100g * ratio
                protein = product.proteinPer100g * ratio
                fat = product.fatPer100g * ratio
                carbs = product.carbsPer100g * ratio
            }

            ConsumedPortionDto(
                id = portion.id,
                segmentName = segment?.name,
                batchMealName = segment?.batchMeal?.name,
                productName = product?.name,
                consumedWeightG = portion.consumedWeightG,
                kcal = round(kcal * 100) / 100.0,
                protein = round(protein * 100) / 100.0,
                fat = round(fat * 100) / 100.0,
                carbs = round(carbs * 100) / 100.0
            )
        }

        val groupedByMealType = portionsDto.groupBy { portionDto ->
            val originalPortion = portions.find { it.id == portionDto.id }
            originalPortion?.mealType ?: "OTHER" 
        }

        var dailyKcal = 0.0
        var dailyProtein = 0.0
        var dailyFat = 0.0
        var dailyCarbs = 0.0

        val mealsMap = groupedByMealType.mapValues { (mealType, items) ->
            val totalKcal = items.sumOf { it.kcal }
            val totalProtein = items.sumOf { it.protein }
            val totalFat = items.sumOf { it.fat }
            val totalCarbs = items.sumOf { it.carbs }

            dailyKcal += totalKcal
            dailyProtein += totalProtein
            dailyFat += totalFat
            dailyCarbs += totalCarbs

            MealSummaryDto(
                mealType = mealType,
                totalKcal = round(totalKcal * 100) / 100.0,
                totalProtein = round(totalProtein * 100) / 100.0,
                totalFat = round(totalFat * 100) / 100.0,
                totalCarbs = round(totalCarbs * 100) / 100.0,
                portions = items
            )
        }

        val workouts = workoutService.getActivitiesForDate(username, date)
        val burnedKcal = workouts.sumOf { it.caloriesBurned }

        return DailySummaryDto(
            date = date,
            totalKcal = round(dailyKcal * 100) / 100.0,
            totalProtein = round(dailyProtein * 100) / 100.0,
            totalFat = round(dailyFat * 100) / 100.0,
            totalCarbs = round(dailyCarbs * 100) / 100.0,
            burnedKcal = round(burnedKcal * 100) / 100.0,
            workouts = workouts,
            meals = mealsMap
        )
    }
}