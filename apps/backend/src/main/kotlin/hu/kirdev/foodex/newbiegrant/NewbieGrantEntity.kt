package hu.kirdev.foodex.newbiegrant

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "newbie_grants")
data class NewbieGrantEntity(
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(unique = true, nullable = false, length = 36)
    var internalId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NewbieGrantEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id)"
    }
}
