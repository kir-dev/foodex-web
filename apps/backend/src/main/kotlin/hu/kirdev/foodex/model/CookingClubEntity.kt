package hu.kirdev.foodex.model

import hu.kirdev.foodex.converter.IntListConverter
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(name = "cooking_clubs")
data class CookingClubEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(length = 30, nullable = false)
    @field:Size(max = 30)
    val name: String,

    @Convert(converter = IntListConverter::class)
    @Column(columnDefinition = "TEXT")
    var leaders: List<Int> = emptyList()
)