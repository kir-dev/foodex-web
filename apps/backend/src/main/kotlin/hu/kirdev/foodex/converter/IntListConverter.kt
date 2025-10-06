package hu.kirdev.foodex.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class IntListConverter : AttributeConverter<List<Int>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Int>): String {
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error converting List<Int> to JSON string", e)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Int> {
        return try {
            if (dbData.isNullOrBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(dbData)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error converting JSON string to List<Int>", e)
        }
    }
}