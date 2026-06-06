package com.example.bowlyApp.service

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.model.BatchMeal
import com.example.bowlyApp.model.BatchMealSegment
import com.example.bowlyApp.model.ConsumedPortion
import com.example.bowlyApp.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BatchMealService(
    private val batchMealRepository: BatchMealRepository,
    private val batchMealSegmentRepository: BatchMealSegmentRepository,
    private val mealRecipeRepository: MealRecipeRepository,
    private val mealRecipeService: MealRecipeService,
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val userRepository: UserRepository,
    private val consumedPortionRepository: ConsumedPortionRepository
) {
    private val logger = LoggerFactory.getLogger(BatchMealService::class.java)

    @Transactional
    fun createBatchMeal(username: String, request: CreateBatchMealRequest): BatchMealDto {
        require(request.segments.isNotEmpty()) {
            "Patelnia musi mieć co najmniej jedną sekcję ze składnikami"
        }

        logger.info(
            "Rozpoczynanie tworzenia nowej patelni: '${request.name}' z ${request.segments.size} sekcjami " +
                "(saveAsRecipe=${request.saveAsRecipe == true})."
        )

        request.segments.forEach { cacheSegmentProducts(it) }

        val linkedRecipeFromId = request.recipeId?.let { recipeId ->
            mealRecipeRepository.findById(recipeId)
                .orElseThrow { IllegalArgumentException("Nie znaleziono przepisu o ID $recipeId") }
        }

        val batchMeal = BatchMeal(
            name = request.name,
            recipe = linkedRecipeFromId
        )

        request.segments.forEach { segmentReq ->
            val product = resolveProduct(segmentReq)

            logger.info(
                "Dodawanie sekcji '${segmentReq.name}' z produktem ID=${product?.id}, " +
                    "waga=${segmentReq.initialWeightG}g, kcal=${segmentReq.totalKcal}"
            )

            val ratio = segmentReq.initialWeightG / 100.0
            val totalKcal = segmentReq.totalKcal ?: product?.let { it.kcalPer100g * ratio } ?: 0.0
            val totalProtein = segmentReq.totalProtein ?: product?.let { it.proteinPer100g * ratio } ?: 0.0
            val totalFat = segmentReq.totalFat ?: product?.let { it.fatPer100g * ratio } ?: 0.0
            val totalCarbs = segmentReq.totalCarbs ?: product?.let { it.carbsPer100g * ratio } ?: 0.0

            val segment = BatchMealSegment(
                batchMeal = batchMeal,
                name = segmentReq.name,
                product = product,
                initialWeightG = segmentReq.initialWeightG,
                currentWeightG = segmentReq.initialWeightG,
                rawWeightG = segmentReq.initialWeightG,
                totalKcal = totalKcal,
                totalProtein = totalProtein,
                totalFat = totalFat,
                totalCarbs = totalCarbs
            )
            batchMeal.segments.add(segment)
        }

        var savedBatchMeal = batchMealRepository.save(batchMeal)

        if (request.saveAsRecipe == true && !request.recipeSections.isNullOrEmpty()) {
            val recipeSections = request.recipeSections!!
            val created = mealRecipeService.createLocalRecipe(
                username,
                CreateMealRecipeRequest(
                    name = request.name,
                    sections = recipeSections
                )
            )
            logger.info("Utworzono przepis '${created.name}' (ID=${created.id}) wraz z patelnią")
            savedBatchMeal.recipe = mealRecipeRepository.findById(created.id).orElse(null)
            savedBatchMeal = batchMealRepository.save(savedBatchMeal)
        }

        logger.info("Pomyślnie utworzono patelnię o ID=${savedBatchMeal.id}")
        return mapToDto(savedBatchMeal)
    }

    private fun cacheSegmentProducts(segmentReq: CreateBatchMealSegmentRequest) {
        val toCache = buildList {
            segmentReq.products?.let { addAll(it) }
            segmentReq.product?.let { add(it) }
        }.distinctBy { ProductService.productDedupKey(it) }

        toCache.forEach { productDto ->
            val saved = productService.findOrCreateProduct(productDto)
            logger.info("Zapisano produkt w cache: ${saved.name} (ID=${saved.id})")
        }
    }

    private fun resolveProduct(segmentReq: CreateBatchMealSegmentRequest): com.example.bowlyApp.model.Product? {
        segmentReq.productId?.toLongOrNull()?.let { id ->
            productRepository.findById(id).orElse(null)?.let { return it }
        }
        segmentReq.product?.let { return productService.findOrCreateProduct(it) }
        return segmentReq.products
            ?.firstOrNull()
            ?.let { productService.findOrCreateProduct(it) }
    }

    @Transactional(readOnly = true)
    fun getActiveBatchMeals(): List<BatchMealDto> {
        return batchMealRepository.findAllActiveWithSegments()
            .distinctBy { it.id }
            .map { mapToDto(it) }
    }

    @Transactional
    fun consumePortion(username: String, request: ConsumePortionRequest) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val segment = batchMealSegmentRepository.findByIdWithPessimisticLock(request.segmentId)
            ?: throw IllegalArgumentException("Segment not found")

        if (request.weightG <= 0) {
            throw IllegalArgumentException("Consumed weight must be greater than zero")
        }

        if (segment.currentWeightG < request.weightG) {
            throw IllegalArgumentException("Not enough weight left in the segment")
        }

        segment.currentWeightG -= request.weightG

        val batchMeal = segment.batchMeal
        val allSegmentsDepleted = batchMeal.segments.all { it.currentWeightG <= 0.0 }
        if (allSegmentsDepleted) {
            batchMeal.isDepleted = true
        }

        val mealDate = request.mealDate?.let { LocalDate.parse(it) } ?: LocalDate.now()

        val consumedPortion = ConsumedPortion(
            user = user,
            segment = segment,
            consumedWeightG = request.weightG,
            mealDate = mealDate,
            mealType = request.mealType,
            weightBasisG = segment.initialWeightG
        )

        consumedPortionRepository.save(consumedPortion)
    }

    @Transactional
    fun updateSegmentCookedWeight(
        batchMealId: Long,
        segmentId: Long,
        request: UpdateSegmentCookedWeightRequest
    ): BatchMealDto {
        require(request.cookedWeightG > 0) {
            "Waga gotowa sekcji musi być większa od zera"
        }

        val segment = batchMealSegmentRepository.findById(segmentId)
            .orElseThrow { IllegalArgumentException("Sekcja o ID $segmentId nie istnieje") }

        if (segment.batchMeal.id != batchMealId) {
            throw IllegalArgumentException("Sekcja nie należy do podanej patelni")
        }

        val oldInitial = segment.initialWeightG
        if (oldInitial <= 0) {
            throw IllegalArgumentException("Nieprawidłowa aktualna waga sekcji")
        }

        val scale = request.cookedWeightG / oldInitial
        segment.initialWeightG = request.cookedWeightG
        segment.currentWeightG = (segment.currentWeightG * scale).coerceAtLeast(0.0)

        batchMealSegmentRepository.save(segment)
        logger.info(
            "Zaktualizowano wagę gotową sekcji '${segment.name}' (ID=$segmentId): " +
                "${oldInitial}g -> ${request.cookedWeightG}g"
        )

        return mapToDto(segment.batchMeal)
    }

    @Transactional
    fun deleteBatchMeal(id: Long) {
        val batchMeal = batchMealRepository.findById(id).orElseThrow {
            IllegalArgumentException("Patelnia o ID $id nie istnieje")
        }
        logger.info("Usuwanie patelni o ID=$id ('${batchMeal.name}')")
        batchMealRepository.delete(batchMeal)
    }

    private fun mapToDto(batchMeal: BatchMeal): BatchMealDto {
        return BatchMealDto(
            id = batchMeal.id,
            name = batchMeal.name,
            recipeId = batchMeal.recipe?.id,
            isDepleted = batchMeal.isDepleted,
            segments = batchMeal.segments.map {
                BatchMealSegmentDto(
                    id = it.id,
                    name = it.name,
                    product = it.product?.let { p -> productService.run { p.toSearchResult() } },
                    initialWeightG = it.initialWeightG,
                    currentWeightG = it.currentWeightG,
                    rawWeightG = it.rawWeightG,
                    totalKcal = it.totalKcal,
                    totalProtein = it.totalProtein,
                    totalFat = it.totalFat,
                    totalCarbs = it.totalCarbs
                )
            }
        )
    }
}
