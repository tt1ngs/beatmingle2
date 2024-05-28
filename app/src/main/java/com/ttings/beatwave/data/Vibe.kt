package com.ttings.beatwave.data

import com.ttings.beatwave.R

data class Vibe(
    val name: String,
    val location: Int
)

val listOfVibesItems = listOf(
    Vibe(
        name = "CHILL",
        location = R.drawable.vibes_chill
    ),
    Vibe(
        name = "COUNTRY",
        location = R.drawable.vibes_country
    ),
    Vibe(
        name = "ELECTRONIC",
        location = R.drawable.vibes_electronic
    ),
    Vibe(
        name = "HIP HOP",
        location = R.drawable.vibes_hh
    ),
    Vibe(
        name = "PARTY",
        location = R.drawable.vibes_party
    ),
    Vibe(
        name = "POP",
        location = R.drawable.vibes_pop
    ),
    Vibe(
        name = "R&B",
        location = R.drawable.vibes_rnb
    ),
    Vibe(
        name = "SOUL",
        location = R.drawable.vibes_soul
    ),
    Vibe(
        name = "STUDY",
        location = R.drawable.vibes_study
    ),
    Vibe(
        name = "WORKOUT",
        location = R.drawable.vibes_workout
    )
)
