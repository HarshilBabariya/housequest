package com.homequest.app.data.repository

import com.homequest.app.data.local.MemberDao
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Outfits
import kotlinx.coroutines.flow.Flow

class MemberRepository(
    private val dao: MemberDao,
    private val gamification: GamificationRepository
) {
    val members: Flow<List<MemberEntity>> = dao.observeAll()
    val owner: Flow<MemberEntity?> = dao.observeOwner()

    suspend fun ensureOwner() {
        if (dao.count() == 0) {
            dao.insert(newMember("You", "Me", isOwner = true))
        }
    }

    suspend fun ownerId(): Long = dao.owner()?.id ?: error("Owner member missing")

    suspend fun add(name: String, relation: String) {
        dao.insert(newMember(name, relation))
        gamification.recordMemberAdded(dao.count() - 1)
    }

    suspend fun rename(member: MemberEntity, name: String, relation: String) =
        dao.update(member.copy(name = name, relation = relation))

    suspend fun updateAvatar(memberId: Long, avatar: AvatarConfig) {
        val member = dao.byId(memberId) ?: return
        dao.update(
            member.copy(
                skinTone = avatar.skinTone,
                hairId = avatar.hairId,
                hairColor = avatar.hairColor,
                outfitId = avatar.outfitId,
                accessoryId = avatar.accessoryId
            )
        )
    }

    suspend fun delete(member: MemberEntity) {
        if (!member.isOwner) dao.delete(member)
    }

    private fun newMember(name: String, relation: String, isOwner: Boolean = false) = AvatarConfig().let { avatar ->
        MemberEntity(
            name = name,
            relation = relation,
            isOwner = isOwner,
            skinTone = avatar.skinTone,
            hairId = avatar.hairId,
            hairColor = avatar.hairColor,
            outfitId = avatar.outfitId,
            accessoryId = avatar.accessoryId
        )
    }
}

fun MemberEntity.avatar(): AvatarConfig = AvatarConfig(
    skinTone = skinTone,
    hairId = hairId.ifBlank { HairStyles.SHORT },
    hairColor = hairColor,
    outfitId = outfitId.ifBlank { Outfits.TEE },
    accessoryId = accessoryId.ifBlank { Accessories.NONE }
)
