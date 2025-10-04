package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.ConfigurationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConfigurationRepository : JpaRepository<ConfigurationEntity, Long> {
    fun findTopByOrderByIdDesc(): ConfigurationEntity?
}