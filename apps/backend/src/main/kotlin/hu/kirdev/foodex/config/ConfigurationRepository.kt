package hu.kirdev.foodex.config

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigurationRepository : JpaRepository<ConfigurationEntity, Int> {
    fun findTopByOrderByIdDesc(): ConfigurationEntity?
}