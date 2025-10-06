package hu.kirdev.foodex.model

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

    @ElementCollection
    @CollectionTable(
        name = "cooking_club_leaders",
        joinColumns = [JoinColumn(name = "cooking_club_id")]
    )
    @Column(name = "leader_id")
    var leaders: List<Int> = emptyList()
)