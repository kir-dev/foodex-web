package hu.kirdev.foodex.oidcuser

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.user.OidcUser

data class CircleMembership(val id: Long, val name: String, val title: List<String>)
data class ExecutiveAt(val id: Long, val name: String)

class FoodExOidcUser(private val oidcUser: OidcUser) : OidcUser by oidcUser {
    val internalId get() = subject
    var extraAuthorities: List<GrantedAuthority> = listOf()
    val memberships = parseCircleMemberships()
    val executiveAtCircles = parseExecutiveAt()

    override fun getAuthorities(): Collection<GrantedAuthority?> = oidcUser.authorities union extraAuthorities

    private fun parseExecutiveAt(): List<ExecutiveAt> {
        val executiveAt = oidcUser.getClaim<List<Map<String, Any>>>("pek.sch.bme.hu:executiveAt/v1") ?: listOf()
        return executiveAt.mapNotNull {
            runCatching {
                ExecutiveAt(
                    (it["id"] as Number).toLong(),
                    it["name"].toString(),
                )
            }.getOrNull()
        }
    }

    private fun parseCircleMemberships(): List<CircleMembership> {
        val memberships = oidcUser.getClaim<List<Map<String, Any>>>("pek.sch.bme.hu:activeMemberships/v1") ?: listOf()
        return memberships.mapNotNull {
            runCatching {
                CircleMembership(
                    (it["id"] as Number).toLong(),
                    it["name"].toString(),
                    (it["title"] as List<*>).map { it.toString() },
                )
            }.getOrNull()
        }
    }
}
