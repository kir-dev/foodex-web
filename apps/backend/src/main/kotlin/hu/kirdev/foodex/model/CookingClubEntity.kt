package hu.kirdev.foodex.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import com.fasterxml.jackson.databind.ObjectMapper

@Entity
@Table(name = "cooking_clubs")
data class CookingClubEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(length = 30, nullable = false)
    @Size(max = 30)
    val name: String,

    @Column(columnDefinition = "json")
    @Convert(converter = LongListConverter::class)
    var leaders: List<Long> = emptyList()
)

// Converter to store List<Long> as JSON in the database
@Converter
class LongListConverter : AttributeConverter<List<Long>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Long>?): String {
        return try {
            objectMapper.writeValueAsString(attribute ?: emptyList<Long>())
        } catch (e: Exception) {
            throw IllegalArgumentException("Error converting List<Long> to JSON", e)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Long> {
        return try {
            if (dbData.isNullOrBlank()) emptyList()
            else objectMapper.readValue(dbData, objectMapper.typeFactory.constructCollectionType(List::class.java, Long::class.java))
        } catch (e: Exception) {
            throw IllegalArgumentException("Error converting JSON to List<Long>", e)
        }
    }
}