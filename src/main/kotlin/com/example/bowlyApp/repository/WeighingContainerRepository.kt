package com.example.bowlyApp.repository

import com.example.bowlyApp.model.WeighingContainer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WeighingContainerRepository : JpaRepository<WeighingContainer, Long>
