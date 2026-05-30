package com.example.bowlyApp.service

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.model.MealRecipe
import com.example.bowlyApp.model.RecipeIngredient
import com.example.bowlyApp.repository.MealRecipeRepository
import com.example.bowlyApp.repository.ProductRepository
import com.example.bowlyApp.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MealRecipeService(
    private val mealRecipeRepository: MealRecipeRepository,
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(MealRecipeService::class.java)

    @Transactional
    fun createLocalRecipe(username: String, request: CreateMealRecipeRequest): MealRecipeDto {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val recipe = MealRecipe(
            name = request.name,
            description = request.description,
            tags = request.tags,
            source = "LOCAL",
            user = user,
            isSingleMeal = request.isSingleMeal
        )

        addSectionsToRecipe(recipe, request.sections)

        val savedRecipe = mealRecipeRepository.save(recipe)
        logger.info("Utworzono przepis '${savedRecipe.name}' (ID=${savedRecipe.id}) dla użytkownika $username")
        return mapToDto(savedRecipe)
    }

    @Transactional
    fun updateLocalRecipe(username: String, id: Long, request: CreateMealRecipeRequest): MealRecipeDto {
        val recipe = mealRecipeRepository.findByIdWithIngredients(id)
            ?: throw IllegalArgumentException("Recipe not found")
        ensureCanModify(username, recipe)

        recipe.name = request.name
        recipe.description = request.description
        recipe.tags = request.tags
        recipe.isSingleMeal = request.isSingleMeal
        recipe.ingredients.clear()
        addSectionsToRecipe(recipe, request.sections)

        val saved = mealRecipeRepository.save(recipe)
        logger.info("Zaktualizowano przepis '${saved.name}' (ID=${saved.id})")
        return mapToDto(saved)
    }

    @Transactional
    fun deleteRecipe(username: String, id: Long) {
        val recipe = mealRecipeRepository.findById(id).orElseThrow {
            IllegalArgumentException("Recipe not found")
        }
        ensureCanModify(username, recipe)
        mealRecipeRepository.delete(recipe)
        logger.info("Usunięto przepis ID=$id")
    }

    @Transactional(readOnly = true)
    fun getAllRecipes(username: String, scope: String, singleMealOnly: Boolean?): List<MealRecipeDto> {
        return filterRecipes(
            recipes = mealRecipeRepository.findAllWithIngredients(),
            username = username,
            scope = scope,
            query = "",
            singleMealOnly = singleMealOnly
        )
    }

    @Transactional(readOnly = true)
    fun searchRecipes(
        username: String,
        query: String,
        scope: String,
        singleMealOnly: Boolean?
    ): List<MealRecipeDto> {
        return filterRecipes(
            recipes = mealRecipeRepository.findAllWithIngredients(),
            username = username,
            scope = scope,
            query = query,
            singleMealOnly = singleMealOnly
        )
    }

    private fun filterRecipes(
        recipes: List<MealRecipe>,
        username: String,
        scope: String,
        query: String,
        singleMealOnly: Boolean?
    ): List<MealRecipeDto> {
        return recipes
            .asSequence()
            .filter { recipe ->
                if (singleMealOnly == true) recipe.isSingleMeal
                else if (singleMealOnly == false) !recipe.isSingleMeal
                else true
            }
            .filter { recipe ->
                scope.equals("ALL", ignoreCase = true) || recipe.user?.username == username
            }
            .filter { recipe ->
                query.isBlank() || recipe.name.contains(query, ignoreCase = true)
            }
            .map { mapToDto(it) }
            .toList()
    }

    private fun addSectionsToRecipe(recipe: MealRecipe, sections: List<CreateRecipeSectionRequest>) {
        sections.forEach { section ->
            section.ingredients.forEach { ingredientReq ->
                if (ingredientReq.weightG <= 0) return@forEach

                val product = when {
                    ingredientReq.productId != null ->
                        productRepository.findById(ingredientReq.productId)
                            .orElseThrow { IllegalArgumentException("Product with ID ${ingredientReq.productId} not found") }
                    ingredientReq.product != null -> productService.findOrCreateProduct(ingredientReq.product)
                    else -> throw IllegalArgumentException("Ingredient must have productId or product payload")
                }

                recipe.ingredients.add(
                    RecipeIngredient(
                        recipe = recipe,
                        product = product,
                        weightG = ingredientReq.weightG,
                        sectionName = section.name.ifBlank { "Główna część" }
                    )
                )
            }
        }
    }

    private fun ensureCanModify(username: String, recipe: MealRecipe) {
        val owner = recipe.user?.username
        if (owner != null && owner != username) {
            throw IllegalArgumentException("Brak uprawnień do modyfikacji tego przepisu")
        }
    }

    private fun mapToDto(recipe: MealRecipe): MealRecipeDto {
        val grouped = recipe.ingredients.groupBy { it.sectionName }
        return MealRecipeDto(
            id = recipe.id,
            name = recipe.name,
            description = recipe.description,
            tags = recipe.tags,
            source = recipe.source,
            userId = recipe.user?.id,
            username = recipe.user?.username,
            isSingleMeal = recipe.isSingleMeal,
            sections = grouped.map { (sectionName, ingredients) ->
                RecipeSectionDto(
                    name = sectionName,
                    ingredients = ingredients.map { ingredient ->
                        val product = ingredient.product
                        RecipeIngredientDto(
                            productId = product.id,
                            productName = product.name,
                            weightG = ingredient.weightG,
                            calories = product.kcalPer100g,
                            protein = product.proteinPer100g,
                            fat = product.fatPer100g,
                            carbohydrates = product.carbsPer100g,
                            barcode = product.barcode,
                            source = product.source,
                            externalId = product.externalId
                        )
                    }
                )
            }
        )
    }
}
