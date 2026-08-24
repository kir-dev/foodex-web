package hu.kirdev.foodex.user

// Roles within FoodEx
enum class Role {
    GUEST, TRIAL, NEWBIE, MEMBER, ADMIN, SUPERUSER;

    fun isAdminOrAbove(): Boolean = this == ADMIN || this == SUPERUSER

    fun countsAsMemberForCapacity(): Boolean = this == MEMBER || this == ADMIN || this == SUPERUSER
}
