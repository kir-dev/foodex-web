package hu.kirdev.foodex.converter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

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