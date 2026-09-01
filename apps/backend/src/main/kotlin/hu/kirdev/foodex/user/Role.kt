package hu.kirdev.foodex.user

// Roles within FoodEx
enum class Role {
    GUEST, ALUMNI, TRIAL, NEWBIE, MEMBER, ADMIN, SUPERUSER;

    fun isAdminOrAbove(): Boolean = this == ADMIN || this == SUPERUSER

    fun countsAsMemberForCapacity(): Boolean = this == MEMBER || this == ADMIN || this == SUPERUSER

    fun hasMemberPrivileges(): Boolean = this != GUEST && this != ALUMNI
}
