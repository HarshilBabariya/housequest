package com.homequest.app.notifications

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import androidx.annotation.RawRes
import com.homequest.app.R
import com.homequest.app.domain.FestivalPack

enum class Sound(@RawRes val res: Int) {
    COOKER_WHISTLE(R.raw.sfx_cooker_whistle),
    DOORBELL(R.raw.sfx_doorbell),
    DEBIT_TING(R.raw.sfx_debit_ting),
    CREDIT_TING(R.raw.sfx_credit_ting),
    MICROWAVE(R.raw.sfx_microwave),
    CARD_DECLINED(R.raw.sfx_card_declined),
    PIGGY_BANK(R.raw.sfx_piggy_bank),
    CASH_REGISTER(R.raw.sfx_cash_register),
    CLOCK(R.raw.sfx_clock),
    STOVE(R.raw.sfx_stove),
    SPARKLER(R.raw.sfx_sparkler),
    UTTARAYAN(R.raw.sfx_uttarayan),
    TIRANGA(R.raw.sfx_tiranga),
    HOLI(R.raw.sfx_holi),
    RAKHI(R.raw.sfx_rakhi),
    GANESH(R.raw.sfx_ganesh),
    NAVRATRI(R.raw.sfx_navratri),
    DIWALI(R.raw.sfx_diwali),
    CHRISTMAS(R.raw.sfx_christmas),
    NEW_YEAR(R.raw.sfx_new_year);

    fun uri(context: Context): Uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/${context.resources.getResourceEntryName(res)}")

    companion object {
        fun of(pack: FestivalPack): Sound = when (pack) {
            FestivalPack.UTTARAYAN -> UTTARAYAN
            FestivalPack.TIRANGA -> TIRANGA
            FestivalPack.HOLI -> HOLI
            FestivalPack.RAKHI -> RAKHI
            FestivalPack.GANESH -> GANESH
            FestivalPack.NAVRATRI -> NAVRATRI
            FestivalPack.DIWALI -> DIWALI
            FestivalPack.CHRISTMAS -> CHRISTMAS
            FestivalPack.NEW_YEAR -> NEW_YEAR
        }
    }
}

class SoundPlayer(private val context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build()
    private val samples = mutableMapOf<Sound, Int>()
    private val ready = mutableSetOf<Int>()
    private val waiting = mutableSetOf<Int>()

    init {
        pool.setOnLoadCompleteListener { p, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            ready += sampleId
            if (waiting.remove(sampleId)) p.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun play(sound: Sound) {
        if (context.getSystemService(AudioManager::class.java).ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        val sampleId = samples.getOrPut(sound) { pool.load(context, sound.res, 1) }
        if (sampleId in ready) pool.play(sampleId, 1f, 1f, 1, 0, 1f) else waiting += sampleId
    }
}
