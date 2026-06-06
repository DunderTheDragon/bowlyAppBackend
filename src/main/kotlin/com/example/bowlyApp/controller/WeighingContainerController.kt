package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.CreateWeighingContainerRequest
import com.example.bowlyApp.dto.UpdateWeighingContainerRequest
import com.example.bowlyApp.dto.WeighingContainerDto
import com.example.bowlyApp.service.WeighingContainerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/containers")
class WeighingContainerController(
    private val weighingContainerService: WeighingContainerService
) {
    @GetMapping
    fun getAll(): ResponseEntity<List<WeighingContainerDto>> =
        ResponseEntity.ok(weighingContainerService.getAll())

    @PostMapping
    fun create(@RequestBody request: CreateWeighingContainerRequest): ResponseEntity<WeighingContainerDto> =
        ResponseEntity.ok(weighingContainerService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateWeighingContainerRequest
    ): ResponseEntity<WeighingContainerDto> =
        ResponseEntity.ok(weighingContainerService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        weighingContainerService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
