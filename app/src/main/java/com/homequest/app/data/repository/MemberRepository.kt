package com.homequest.app.data.repository

import com.homequest.app.data.local.MemberDao
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Gender
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Mood
import com.homequest.app.domain.Outfits
import com.homequest.app.domain.Relations
import com.homequest.app.domain.SkinTones
import com.homequest.app.util.Pin
import com.homequest.app.util.titleCased
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MemberRepository(
    private val dao: MemberDao,
    private val settings: SettingsRepository,
    private val gamification: GamificationRepository
) {
    val members: Flow<List<MemberEntity>> = dao.observeAll()

    val activeMember: Flow<MemberEntity?> = combine(members, settings.settings) { list, s ->
        list.firstOrNull { it.id == s.activeMemberId }
    }

    suspend fun ensureOwner() {
        if (dao.count() == 0) dao.insert(newMember("You", Relations.ME, isOwner = true, avatar = AvatarConfig()))
    }

    suspend fun activeMemberId(): Long {
        val active = settings.current().activeMemberId
        return if (active != 0L && dao.byId(active) != null) active else dao.owner()?.id ?: error("Owner member missing")
    }

    suspend fun add(name: String, relation: String, phone: String, gender: Gender): Long {
        val id = dao.insert(newMember(name, relation, phone = phone, avatar = randomAvatar(gender)))
        gamification.recordMemberAdded(dao.count() - 1)
        return id
    }

    suspend fun updateDetails(member: MemberEntity, name: String, relation: String, phone: String, gender: Gender) =
        dao.update(member.copy(name = name.titleCased(), relation = relation, phone = phone.trim(), gender = gender))

    suspend fun updateAvatar(memberId: Long, avatar: AvatarConfig) {
        val member = dao.byId(memberId) ?: return
        dao.update(
            member.copy(
                skinTone = avatar.skinTone,
                hairId = avatar.hairId,
                hairColor = avatar.hairColor,
                outfitId = avatar.outfitId,
                accessoryId = avatar.accessoryId,
                gender = avatar.gender,
                mood = avatar.mood?.name.orEmpty()
            )
        )
    }

    suspend fun setPin(memberId: Long, pin: String?) {
        val member = dao.byId(memberId) ?: return
        dao.update(member.copy(pinHash = pin?.let { Pin.hash(it) } ?: ""))
    }

    suspend fun verifyPin(memberId: Long, pin: String): Boolean {
        val member = dao.byId(memberId) ?: return false
        return member.pinHash.isEmpty() || Pin.matches(pin, member.pinHash)
    }

    suspend fun signIn(memberId: Long) = settings.setActiveMember(memberId)

    suspend fun signOut() = settings.setActiveMember(0)

    suspend fun delete(member: MemberEntity) {
        if (member.isOwner) return
        dao.delete(member)
        if (settings.current().activeMemberId == member.id) signOut()
    }

    private fun randomAvatar(gender: Gender) = AvatarConfig(
        skinTone = Random.nextInt(SkinTones.size),
        hairId = if (gender == Gender.FEMALE) listOf(HairStyles.LONG, HairStyles.CURLY).random() else listOf(HairStyles.SHORT, HairStyles.BUZZ).random(),
        hairColor = Random.nextInt(HairColors.size),
        outfitId = listOf(Outfits.TEE, Outfits.SHIRT).random(),
        accessoryId = Accessories.NONE,
        gender = gender
    )

    private fun newMember(name: String, relation: String, isOwner: Boolean = false, phone: String = "", avatar: AvatarConfig) =
        MemberEntity(
            name = name.titleCased(),
            relation = relation,
            isOwner = isOwner,
            phone = phone.trim(),
            skinTone = avatar.skinTone,
            hairId = avatar.hairId,
            hairColor = avatar.hairColor,
            outfitId = avatar.outfitId,
            accessoryId = avatar.accessoryId,
            gender = avatar.gender
        )
}

fun MemberEntity.avatar(): AvatarConfig = AvatarConfig(
    skinTone = skinTone,
    hairId = hairId.ifBlank { HairStyles.SHORT },
    hairColor = hairColor,
    outfitId = outfitId.ifBlank { Outfits.TEE },
    accessoryId = accessoryId.ifBlank { Accessories.NONE },
    gender = gender,
    mood = Mood.entries.firstOrNull { it.name == mood }
)

val MemberEntity.hasPin: Boolean get() = pinHash.isNotEmpty()
