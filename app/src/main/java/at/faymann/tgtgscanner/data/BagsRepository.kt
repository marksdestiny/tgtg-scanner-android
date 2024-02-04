package at.faymann.tgtgscanner.data

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

class BagsRepository {

    val items = MutableStateFlow<List<Bag>>(listOf())
    val lastUpdate = MutableStateFlow<Date?>(null)
}