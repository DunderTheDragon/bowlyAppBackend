package com.example.bowlyApp.service

import com.example.bowlyApp.dto.CreateWeighingContainerRequest
import com.example.bowlyApp.dto.UpdateWeighingContainerRequest
import com.example.bowlyApp.dto.WeighingContainerDto
import com.example.bowlyApp.model.WeighingContainer
import com.example.bowlyApp.repository.WeighingContainerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WeighingContainerService(
    private val weighingContainerRepository: WeighingContainerRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<WeighingContainerDto> =
        weighingContainerRepository.findAll().map { it.toDto() }

    @Transactional
    fun create(request: CreateWeighingContainerRequest): WeighingContainerDto {
        validateRequest(request.name, request.type, request.weightG, request.imageBase64)
        val saved = weighingContainerRepository.save(
            WeighingContainer(
                name = request.name.trim(),
                type = request.type.uppercase(),
                weightG = request.weightG,
                imageBase64 = request.imageBase64?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun update(id: Long, request: UpdateWeighingContainerRequest): WeighingContainerDto {
        validateRequest(request.name, request.type, request.weightG, request.imageBase64)
        val container = weighingContainerRepository.findById(id).orElseThrow {
            IllegalArgumentException("Naczynie o ID $id nie istnieje")
        }
        val updated = weighingContainerRepository.save(
            container.copy(
                name = request.name.trim(),
                type = request.type.uppercase(),
                weightG = request.weightG,
                imageBase64 = request.imageBase64?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        return updated.toDto()
    }

    @Transactional
    fun delete(id: Long) {
        if (!weighingContainerRepository.existsById(id)) {
            throw IllegalArgumentException("Naczynie o ID $id nie istnieje")
        }
        weighingContainerRepository.deleteById(id)
    }

    private fun validateRequest(name: String, type: String, weightG: Double, imageBase64: String?) {
        require(name.trim().isNotEmpty()) { "Nazwa naczynia jest wymagana" }
        require(type.trim().isNotEmpty()) { "Typ naczynia jest wymagany" }
        require(weightG > 0) { "Waga naczynia musi być większa od zera" }
        if (imageBase64 != null && imageBase64.length > MAX_IMAGE_BASE64_LENGTH) {
            throw IllegalArgumentException("Obraz jest zbyt duży (max ${MAX_IMAGE_BASE64_LENGTH} znaków base64)")
        }
    }

    private fun WeighingContainer.toDto() = WeighingContainerDto(
        id = id,
        name = name,
        type = type,
        weightG = weightG,
        imageBase64 = imageBase64
    )

    companion object {
        const val MAX_IMAGE_BASE64_LENGTH = 200_000
        val ALLOWED_TYPES = setOf("PAN", "PLATE", "POT", "OTHER")
    }
}
