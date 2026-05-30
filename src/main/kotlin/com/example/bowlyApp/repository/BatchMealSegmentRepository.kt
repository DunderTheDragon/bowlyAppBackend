package com.example.bowlyApp.repository

import com.example.bowlyApp.model.BatchMealSegment
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BatchMealSegmentRepository : JpaRepository<BatchMealSegment, Long> {
    
    // Optymistyczne lub pesymistyczne blokowanie rekordu, aby zapobiec Race Condition
    // kiedy dwie osoby dodają zjedzoną porcję z tej samej patelni w tym samym ułamku sekundy
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BatchMealSegment s WHERE s.id = :id")
    fun findByIdWithPessimisticLock(id: Long): BatchMealSegment?
}
