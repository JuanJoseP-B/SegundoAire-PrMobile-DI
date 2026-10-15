package com.segundoaire.domain.model

/**
 * Paquetes comunes de redes sociales, video y juegos que suelen quitar tiempo.
 * Solo se sugieren los que están instalados; el resto de la lista no importa.
 * Son nombres de paquete, no marcas en pantalla: el ícono sale del dispositivo.
 */
object SuggestedApps {

    val packageNames: Set<String> = setOf(
        // Redes sociales
        "com.instagram.android",
        "com.instagram.barcelona",
        "com.facebook.katana",
        "com.facebook.lite",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.twitter.android",
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.pinterest",
        // Video
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "tv.twitch.android.app",
        "com.amazon.avod.thirdpartyclient",
        "com.disney.disneyplus",
        // Juegos
        "com.king.candycrushsaga",
        "com.supercell.clashofclans",
        "com.supercell.clashroyale",
        "com.supercell.brawlstars",
        "com.roblox.client",
        "com.kiloo.subwaysurf",
        "com.innersloth.spacemafia",
        "com.nianticlabs.pokemongo",
        "com.dts.freefireth",
        "com.tencent.ig",
        "com.activision.callofduty.shooter",
        "com.mojang.minecraftpe"
    )
}
