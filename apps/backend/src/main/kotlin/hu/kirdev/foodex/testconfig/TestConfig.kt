package hu.kirdev.foodex.testconfig

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.shift.ShiftEntity
import hu.kirdev.foodex.shift.ShiftRepository
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import java.util.UUID

@Configuration
@Profile("test")
class TestConfig {

    @Bean
    fun initTestConfig(
        userRepository: UserRepository,
        cookingClubRepository: CookingClubRepository,
        openingRequestRepository: OpeningRequestRepository,
        shiftRepository: ShiftRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            /***** Users *****/
            val user1 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.ADMIN,
                    name = "Sali Nora",
                    nickname = "Nori",
                    email = "nori@gmail.com",
                    favouriteQuote = "I <3 FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user2 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.MEMBER,
                    name = "Kis Pista",
                    nickname = "Pistike",
                    email = "kis.pista@gmail.com",
                    favouriteQuote = "I <3 FoodEx too!",
                    isActive = true,
                    profilePicture = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/1200px-Cat_August_2010-4.jpg"
                )
            )
            val user3 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.GUEST,
                    name = "Langosch es Paschta korvez",
                    nickname = "langosch paschta korvez",
                    email = "langosch.paschta.korvez@gmail.com",
                    favouriteQuote = "I <3 langos es paschta",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user4 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.MEMBER,
                    name = "Americano korvez es member",
                    nickname = "americano korvez es member",
                    email = "americano.korvez.member@gmail.com",
                    favouriteQuote = "I <3 Hamgurger es FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user5 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.NEWBIE,
                    name = "Langosch korvez es newbie",
                    nickname = "langosch korvez es newbie",
                    email = "langosch.korvez.newbie@gmail.com",
                    favouriteQuote = "I <3 Langosch es FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user6 = userRepository.save(
                UserEntity(
                    internalId = UUID.randomUUID().toString(),
                    role = Role.NEWBIE,
                    name = "Ujonc Peter",
                    nickname = "Peti",
                    email = "peti.ujonc@gmail.com",
                    favouriteQuote = "I <3 FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )

            /***** Cooking clubs *****/
            // Pizzásch
            val club223 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 223,
                    name = "Pizzasch-223",
                    leaders = mutableListOf(user1)
                )
            )

            // Americano
            val club403 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 403,
                    name = "Americano-403",
                    leaders = mutableListOf(user1, user4)
                )
            )

            // Vödör
            val club179 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 179,
                    name = "Vödör-179",
                    leaders = mutableListOf(user1)
                )
            )

            // LángoSCH
            val club473 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 473,
                    name = "Langosch-473",
                    leaders = mutableListOf(user1, user3, user5)
                )
            )

            // Kakas
            val club31 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 31,
                    name = "Kakas-31",
                    leaders = mutableListOf(user1)
                )
            )

            // Paschta;
            val club528 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 528,
                    name = "Paschta;-528",
                    leaders = mutableListOf(user1, user3)
                )
            )

            // Palacsintázó
            val club395 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 395,
                    name = "Palacsintázó-395",
                    leaders = mutableListOf(user1)
                )
            )

            // ReggeliSCH
            val club490 = cookingClubRepository.save(
                CookingClubEntity(
                    id = 490,
                    name = "ReggliSCH-490",
                    leaders = mutableListOf(user1)
                )
            )

            /***** Opening requests *****/
            val request1 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = user4,
                    cookingClub = club403,
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(4),
                    place = "10. konyha",
                    description = "ez egy leiras americano"
                )
            )
            val request2 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = user5,
                    cookingClub = club403,
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras langosch"
                )
            )
            val request3 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = user1,
                    cookingClub = club473,
                    opening = LocalDateTime.now().minusYears(1),
                    closing = LocalDateTime.now().minusYears(1).plusHours(2),
                    place = "18. konyha",
                    description = "1 evel ezelottrol request, pizzasch"
                )
            )
            val request4 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = user5,
                    cookingClub = club403,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(2),
                    place = "11. konyha",
                    description = "mar elkezdodott langosch"
                )
            )
            val request5 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = user5,
                    cookingClub = club403,
                    opening = LocalDateTime.now().minusMonths(6),
                    closing = LocalDateTime.now().minusMonths(6).plusHours(2),
                    place = "4. konyha",
                    description = "ELMULT FELEVBOL VAN!!!"
                )
            )
            val request6 = openingRequestRepository.save(
                OpeningRequestEntity(
                    isAccepted = true,
                    user = user5,
                    cookingClub = club403,
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(2),
                    place = "4. konyha",
                    description = "MAR ELFOGADOTT KERES!!!"
                )
            )
            val request7 = openingRequestRepository.save(
                OpeningRequestEntity(
                    isAccepted = true,
                    user = user5,
                    cookingClub = club403,
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras langosch"
                )
            )

            /***** Shifts *****/
            val shift1 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club403,
                    maxMembers = 20,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusDays(1),
                    place = "-1 konyha",
                    comment = "meg nem kezdodott el",
                    workers = mutableListOf<UserEntity>(user1, user2),
                )
            )
            val shift2 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club473,
                    maxMembers = 10,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "13. konyha",
                    comment = "ez a muszak eppen tart",
                    workers = mutableListOf<UserEntity>(user1, user2, user5),
                )
            )
            val shift3 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club223,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "10. konyha",
                    comment = "ez a muszak eppen tart",
                    workers = mutableListOf<UserEntity>(user1, user4, user5, user6),
                )
            )
            val shift4 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club403,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "8. konyha",
                    comment = "ez a muszak eppen tart es BETELT",
                    workers = mutableListOf(user1, user2, user4, user5, user6),
                )
            )
            val shift5 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club528,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(20),
                    closing = LocalDateTime.now().minusHours(15),
                    place = "10. konyha",
                    comment = "LEJART",
                    workers = mutableListOf<UserEntity>(user1, user2, user5, user6),
                )
            )
            val shift6 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = club528,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusMonths(6).minusHours(4),
                    closing = LocalDateTime.now().minusMonths(6),
                    place = "10. konyha",
                    comment = "ELOZO FELEVBOL VAN!!!",
                    workers = mutableListOf<UserEntity>(user1, user2, user5, user6),
                )
            )
        }
    }
}