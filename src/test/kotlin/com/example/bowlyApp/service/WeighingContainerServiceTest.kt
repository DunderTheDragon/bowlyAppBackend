package com.example.bowlyApp.service

import com.example.bowlyApp.model.WeighingContainer
import com.example.bowlyApp.repository.WeighingContainerRepository
import com.example.bowlyApp.support.TestFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class WeighingContainerServiceTest {

    private val repository = mockk<WeighingContainerRepository>()
    private lateinit var service: WeighingContainerService

    @BeforeEach
    fun setUp() {
        service = WeighingContainerService(repository)
    }

    @Test
    fun `create zapisuje naczynie`() {
        every { repository.save(any()) } answers {
            firstArg<WeighingContainer>().copy(id = 1L)
        }

        val dto = service.create(TestFixtures.createContainerRequest())

        assertEquals("Talerz", dto.name)
        assertEquals("PLATE", dto.type)
        assertEquals(250.0, dto.weightG)
    }

    @Test
    fun `create odrzuca wagę mniejszą lub równą zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.create(TestFixtures.createContainerRequest(weightG = 0.0))
        }
    }

    @Test
    fun `create odrzuca pustą nazwę`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.create(TestFixtures.createContainerRequest(name = " "))
        }
    }

    @Test
    fun `delete usuwa istniejące naczynie`() {
        every { repository.existsById(1L) } returns true
        every { repository.deleteById(1L) } returns Unit

        service.delete(1L)

        verify { repository.deleteById(1L) }
    }

    @Test
    fun `delete rzuca wyjątek gdy naczynie nie istnieje`() {
        every { repository.existsById(99L) } returns false

        assertThrows(IllegalArgumentException::class.java) {
            service.delete(99L)
        }
    }

    @Test
    fun `update modyfikuje naczynie`() {
        val existing = WeighingContainer(id = 1L, name = "Stary", type = "PAN", weightG = 300.0)
        every { repository.findById(1L) } returns Optional.of(existing)
        every { repository.save(any()) } answers { firstArg() }

        val dto = service.update(1L, com.example.bowlyApp.dto.UpdateWeighingContainerRequest(
            name = "Nowy",
            type = "pot",
            weightG = 400.0
        ))

        assertEquals("Nowy", dto.name)
        assertEquals("POT", dto.type)
    }
}
