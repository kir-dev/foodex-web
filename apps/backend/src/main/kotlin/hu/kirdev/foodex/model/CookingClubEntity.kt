package hu.kirdev.foodex.model

import hu.kirdev.foodex.converter.LongListConverter
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(name = "cooking_clubs")
data class CookingClubEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(length = 30, nullable = false)
    @field:Size(max = 30)
    val name: String,

    @Column(columnDefinition = "json")
    @Convert(converter = LongListConverter::class)
    var leaders: List<Long> = emptyList()
)